// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.llm.BYOKConfig;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.aiagent.BYOKCatalog;
import com.google.appinventor.shared.settings.SettingsConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Resolves a {@link BYOKConfig} from a user's stored settings.
 *
 * <p>{@link #resolveForUser(String)} loads settings via
 * {@link StorageIo#loadSettings(String)}; {@link #resolveFromJson(String)}
 * is the parsing core, exposed package-private for unit tests.
 *
 * <p>All-or-nothing semantics: if any of provider, model, or API key is
 * missing (or base URL when the provider requires it), the resolver
 * returns {@code null} and the caller must fall back to flag-based
 * configuration.
 */
public final class BYOKResolver {

  private static final Logger LOG = Logger.getLogger(BYOKResolver.class.getName());

  private BYOKResolver() {}

  /** Returns the BYOK config for {@code userId} or {@code null} if BYOK is
   *  not fully configured. Never throws. */
  public static BYOKConfig resolveForUser(String userId) {
    if (userId == null || userId.isEmpty()) {
      return null;
    }
    try {
      StorageIo io = StorageIoInstanceHolder.getInstance();
      String json = io.loadSettings(userId);
      return resolveFromJson(json);
    } catch (RuntimeException e) {
      LOG.log(Level.WARNING, "BYOKResolver: failed to load user settings", e);
      return null;
    }
  }

  /** Visible for testing. Parses a settings JSON blob. */
  static BYOKConfig resolveFromJson(String json) {
    if (json == null || json.isEmpty()) {
      return null;
    }
    JSONObject root;
    try {
      root = new JSONObject(json);
    } catch (JSONException e) {
      return null;
    }
    JSONObject general = root.optJSONObject(SettingsConstants.USER_GENERAL_SETTINGS);
    if (general == null) {
      return null;
    }

    String providerWire = general.optString(
        SettingsConstants.AI_AGENT_BYOK_PROVIDER, "");
    String model = general.optString(
        SettingsConstants.AI_AGENT_BYOK_MODEL, "");
    String apiKey = general.optString(
        SettingsConstants.AI_AGENT_BYOK_API_KEY, "");
    String baseUrl = general.optString(
        SettingsConstants.AI_AGENT_BYOK_BASE_URL, "");
    String reasoningWire = general.optString(
        SettingsConstants.AI_AGENT_BYOK_REASONING, "");

    BYOKCatalog.Provider provider = BYOKCatalog.Provider.fromWireName(providerWire);
    if (provider == null || model.isEmpty() || apiKey.isEmpty()) {
      return null;
    }
    if (BYOKCatalog.requiresBaseUrl(provider) && baseUrl.isEmpty()) {
      return null;
    }
    if (!BYOKCatalog.requiresBaseUrl(provider)) {
      // Ignore stale base URL stored when user previously selected a
      // compatible provider.
      baseUrl = "";
    }

    String reasoning = sanitizeReasoning(provider, model, reasoningWire);
    return new BYOKConfig(provider.wireName(), model, apiKey, baseUrl, reasoning);
  }

  /** Returns the stored reasoning value when the catalog confirms the
   *  selected model accepts it (provider-native values are stored verbatim).
   *  Returns "" otherwise — including when the model does not support
   *  reasoning or the stored value is stale. */
  static String sanitizeReasoning(BYOKCatalog.Provider provider, String model,
                                  String stored) {
    if (stored == null || stored.isEmpty()) {
      return "";
    }
    for (String allowed : BYOKCatalog.reasoningOptions(provider, model)) {
      if (allowed.equals(stored)) {
        return stored;
      }
    }
    return "";
  }
}
