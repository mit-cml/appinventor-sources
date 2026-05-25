// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import com.google.appinventor.server.aiagent.AIDebug;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * LLM provider for OpenRouter, a unified gateway that routes requests
 * to the best available backend for any model.
 *
 * <p>Thin subclass of {@link OpenAIChatCompletionsProvider} that sets
 * the OpenRouter endpoint, adds required routing headers, and forwards
 * {@code reasoning.effort} so OpenRouter can translate it to the right
 * underlying parameter per model family (OpenAI {@code reasoning.effort},
 * Anthropic {@code thinking}, Gemini {@code thinkingConfig}, etc.).
 */
public class OpenRouterProvider extends OpenAIChatCompletionsProvider {

  private static final Logger LOG = Logger.getLogger(OpenRouterProvider.class.getName());

  private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api";

  private final String reasoningEffort;

  OpenRouterProvider(String apiKey, String model, String reasoningEffort) {
    super(apiKey, model, OPENROUTER_BASE_URL);
    this.reasoningEffort = reasoningEffort == null ? "" : reasoningEffort;
  }

  @Override
  protected String getProviderName() {
    return "OpenRouter";
  }

  @Override
  protected Map<String, String> getHeaders() {
    Map<String, String> headers = super.getHeaders();
    headers.put("HTTP-Referer", "https://appinventor.mit.edu");
    headers.put("X-Title", "MIT App Inventor");
    return headers;
  }

  @Override
  protected void decorateRequestBody(JSONObject requestBody) {
    if (!reasoningEffort.isEmpty()) {
      requestBody.put("reasoning",
          new JSONObject().put("effort", reasoningEffort));
    }
    // No manual provider routing — OpenRouter's own sticky routing keeps
    // cache warm per-conversation (hashed on first system + first user msg)
    // and is disabled when provider.order is set.
    // OpenRouter omits usage on streaming unless explicitly requested.
    if (AIDebug.enabled()) {
      requestBody.put("usage", new JSONObject().put("include", true));
    }
  }

  /**
   * After each call, when AIDebug is enabled, query OpenRouter's
   * generation-metadata endpoint to surface which upstream provider
   * actually served the request and whether prefix cache fired.
   */
  @Override
  protected void onResponseLogged(JSONObject responseJson) {
    final String genId = responseJson.optString("id", "");
    if (genId.isEmpty()) {
      AIDebug.log(LOG, "OpenRouter generation meta: skipped (no id in response)");
      return;
    }
    // Capture the request scope ref now; fire fetch in background so the
    // OR /generation propagation lag (often 5-30s) doesn't block the
    // response. Background thread appends to this same request's log
    // file when the row is available.
    final String requestRef = AIDebug.currentRequestRef();
    if (requestRef == null) {
      return;
    }
    Thread t = new Thread(() -> fetchAndLogGenerationMeta(requestRef, genId),
        "or-gen-meta-" + genId);
    t.setDaemon(true);
    t.start();
  }

  private void fetchAndLogGenerationMeta(String requestRef, String genId) {
    int[] backoffs = {1000, 2000, 4000, 8000, 15000};
    String lastErr = "no attempts";
    for (int attempt = 0; attempt < backoffs.length; attempt++) {
      try {
        Thread.sleep(backoffs[attempt]);
        URL url = new URL(baseUrl + "/v1/generation?id="
            + java.net.URLEncoder.encode(genId, "UTF-8"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        int code = conn.getResponseCode();
        if (code == 200) {
          StringBuilder sb = new StringBuilder();
          try (BufferedReader reader = new BufferedReader(new InputStreamReader(
              conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
              sb.append(line);
            }
          }
          JSONObject body = new JSONObject(sb.toString());
          JSONObject data = body.optJSONObject("data");
          if (data != null) {
            AIDebug.appendToRequestLog(requestRef,
                "OpenRouter generation meta [" + genId + "] attempt="
                + (attempt + 1) + ":\n" + data.toString(2));
            return;
          }
          lastErr = "200 OK but no data field";
        } else {
          lastErr = "HTTP " + code;
        }
      } catch (Exception e) {
        lastErr = e.getClass().getSimpleName() + ": " + e.getMessage();
      }
    }
    AIDebug.appendToRequestLog(requestRef,
        "OpenRouter generation meta [" + genId + "] gave up after "
        + backoffs.length + " attempts; last=" + lastErr);
  }
}
