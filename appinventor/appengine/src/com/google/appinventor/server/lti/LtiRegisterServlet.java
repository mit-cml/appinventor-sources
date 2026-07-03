// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * LTI 1.3 Dynamic Registration, served at /lti/register. A platform administrator
 * starts registration in the LMS, which opens this endpoint with the platform
 * openid_configuration URL and a one time registration_token. The tool fetches
 * the platform configuration, registers itself with the platform, and stores the
 * platform in the registry, so an administrator can register App Inventor by
 * pasting one URL rather than copying several values by hand.
 *
 * <p>The endpoint is closed by default and opened by a flag, because registering
 * a platform lets it launch users, so it must not be open to an untrusted party.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiRegisterServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiRegisterServlet.class.getName());
  private static final String TOOL_CONFIG =
      "https://purl.imsglobal.org/spec/lti-tool-configuration";
  private static final String SCORE_SCOPE =
      "https://purl.imsglobal.org/spec/lti-ags/scope/score";
  private static final String CLIENT_NAME = "App Inventor";

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (!LtiConfig.registrationEnabled()) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN,
          "Dynamic registration is not open on this server");
      return;
    }
    String configUrl = req.getParameter("openid_configuration");
    if (configUrl == null || configUrl.isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing openid_configuration");
      return;
    }
    try {
      JSONObject config = new JSONObject(LtiHttp.get(configUrl));
      JSONObject request = toolRegistration(LtiConfig.loginUrl(), LtiConfig.launchUrl(),
          LtiConfig.jwksUrl(), domainOf(LtiConfig.toolBaseUrl()), CLIENT_NAME);
      String registrationEndpoint = config.getString("registration_endpoint");
      String regToken = req.getParameter("registration_token");
      String body = (regToken == null || regToken.isEmpty())
          ? LtiHttp.postJson(registrationEndpoint, request.toString())
          : LtiHttp.postJsonWithBearer(registrationEndpoint, request.toString(), regToken,
              "application/json");
      JSONObject registered = new JSONObject(body);
      JSONObject registeredTool = registered.optJSONObject(TOOL_CONFIG);
      String deploymentId =
          (registeredTool == null) ? "" : registeredTool.optString("deployment_id", "");
      storageIo.storeLtiPlatform(config.getString("issuer"),
          registered.getString("client_id"),
          config.getString("authorization_endpoint"),
          config.getString("token_endpoint"),
          config.getString("jwks_uri"),
          deploymentId, true);
      resp.setContentType("text/html; charset=utf-8");
      resp.getWriter().write("<!DOCTYPE html><html><head><meta charset='utf-8'>"
          + "<title>App Inventor registered</title></head>"
          + "<body style='font-family:sans-serif;max-width:640px;margin:2rem auto'>"
          + "<h2>App Inventor is registered with this platform.</h2>"
          + "<p>You can close this window and add App Inventor activities to your courses.</p>"
          + "<script>(window.opener || window.parent).postMessage("
          + "{subject: 'org.imsglobal.lti.close'}, '*');</script></body></html>");
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI dynamic registration failed", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Dynamic registration failed");
    }
  }

  /** Builds the tool registration request the tool sends to the platform. */
  @VisibleForTesting
  static JSONObject toolRegistration(String loginUrl, String launchUrl, String jwksUrl,
      String domain, String clientName) {
    JSONObject toolConfig = new JSONObject()
        .put("domain", domain)
        .put("target_link_uri", launchUrl)
        .put("claims", new JSONArray().put("iss").put("sub").put("name").put("email"))
        .put("messages", new JSONArray()
            .put(new JSONObject().put("type", "LtiResourceLinkRequest"))
            .put(new JSONObject().put("type", "LtiDeepLinkingRequest")
                .put("target_link_uri", launchUrl)));
    return new JSONObject()
        .put("application_type", "web")
        .put("response_types", new JSONArray().put("id_token"))
        .put("grant_types", new JSONArray().put("client_credentials").put("implicit"))
        .put("initiate_login_uri", loginUrl)
        .put("redirect_uris", new JSONArray().put(launchUrl))
        .put("client_name", clientName)
        .put("jwks_uri", jwksUrl)
        .put("token_endpoint_auth_method", "private_key_jwt")
        .put("scope", SCORE_SCOPE)
        .put(TOOL_CONFIG, toolConfig);
  }

  private static String domainOf(String baseUrl) throws Exception {
    URI uri = new URI(baseUrl);
    return (uri.getPort() == -1) ? uri.getHost() : uri.getHost() + ":" + uri.getPort();
  }
}
