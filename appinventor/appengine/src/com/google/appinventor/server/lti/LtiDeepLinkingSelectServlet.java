// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Receives the teacher's template choice from the Deep Linking picker
 * (LtiLaunchServlet renders it) and returns a signed LTI 1.3 Deep Linking
 * Response to the platform. The response contains one ltiResourceLink content
 * item whose custom parameter carries the chosen template project id, so that
 * when a student later launches the assignment the launch delivers that id and
 * the student is given a copy of the teacher's template.
 *
 * <p>Exploration spike: the return url and data ride in hidden form fields rather
 * than a server side store, so they are not tamper protected.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiDeepLinkingSelectServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiDeepLinkingSelectServlet.class.getName());
  private static final String LTI = "https://purl.imsglobal.org/spec/lti/claim/";
  private static final String LTI_DL = "https://purl.imsglobal.org/spec/lti-dl/claim/";

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String templateId = req.getParameter("template_project_id");
      String returnUrl = req.getParameter("return_url");
      String data = req.getParameter("data");
      String deploymentId = req.getParameter("deployment_id");
      if (templateId == null || templateId.isEmpty() || returnUrl == null || returnUrl.isEmpty()) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing template or return url");
        return;
      }

      String title = "App Inventor assignment";
      try {
        long pid = Long.parseLong(templateId);
        String owner = storageIo.getProjectUserId(pid);
        title = "App Inventor: " + storageIo.getProjectName(owner, pid);
      } catch (Exception ignore) {
        // Keep the default title if the name cannot be read.
      }

      JSONObject contentItem = new JSONObject()
          .put("type", "ltiResourceLink")
          .put("title", title)
          .put("url", LtiConfig.launchUrl())
          .put("custom", new JSONObject().put("template_project_id", templateId));
      JSONArray contentItems = new JSONArray().put(contentItem);

      long now = System.currentTimeMillis() / 1000L;
      JSONObject header = new JSONObject()
          .put("alg", "RS256").put("typ", "JWT").put("kid", LtiConfig.KID);
      JSONObject payload = new JSONObject()
          .put("iss", LtiConfig.clientId())
          .put("aud", LtiConfig.issuer())
          .put("iat", now)
          .put("exp", now + 300)
          .put("nonce", LtiState.random())
          .put(LTI + "deployment_id", deploymentId)
          .put(LTI + "message_type", "LtiDeepLinkingResponse")
          .put(LTI + "version", "1.3.0")
          .put(LTI_DL + "content_items", contentItems);
      if (data != null && !data.isEmpty()) {
        payload.put(LTI_DL + "data", data);
      }

      PrivateKey key = LtiJwt.loadPrivateKey(LtiConfig.privateKeyFile());
      String jwt = LtiJwt.sign(header, payload, key);

      // Auto POST the signed response back to the platform return url.
      resp.setContentType("text/html; charset=utf-8");
      StringBuilder html = new StringBuilder();
      html.append("<!DOCTYPE html><html><body onload='document.forms[0].submit()'>");
      html.append("<form method='post' action='").append(escapeAttr(returnUrl)).append("'>");
      html.append("<input type='hidden' name='JWT' value='").append(escapeAttr(jwt)).append("'>");
      html.append("<noscript><button type='submit'>Continue</button></noscript>");
      html.append("</form></body></html>");
      resp.getWriter().write(html.toString());
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI deep linking selection failed", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Deep linking failed: " + e.getMessage());
    }
  }

  private static String escapeAttr(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;");
  }
}
