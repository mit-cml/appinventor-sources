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
 * <p>The platform return url and opaque data are held server side under a one
 * time token minted when the picker was rendered, so the form itself carries
 * nothing that could be tampered with.
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
      LtiState.DeepLink dl = LtiState.consumeDeepLink(req.getParameter("dl"));
      if (templateId == null || templateId.isEmpty() || dl == null || dl.returnUrl.isEmpty()) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("text/plain; charset=utf-8");
        resp.getWriter().println("This selection has expired. Close this window, then use "
            + "Select content again from your course.");
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
          .put(LTI + "deployment_id", dl.deploymentId)
          .put(LTI + "message_type", "LtiDeepLinkingResponse")
          .put(LTI + "version", "1.3.0")
          .put(LTI_DL + "content_items", contentItems);
      if (!dl.data.isEmpty()) {
        payload.put(LTI_DL + "data", dl.data);
      }

      PrivateKey key = LtiJwt.loadPrivateKey(LtiConfig.privateKeyFile());
      String jwt = LtiJwt.sign(header, payload, key);

      // Auto POST the signed response back to the platform return url.
      resp.setContentType("text/html; charset=utf-8");
      StringBuilder html = new StringBuilder()
          .append("<!DOCTYPE html><html><body onload='document.forms[0].submit()'>")
          .append("<form method='post' action='").append(escapeAttr(dl.returnUrl)).append("'>")
          .append("<input type='hidden' name='JWT' value='").append(escapeAttr(jwt)).append("'>")
          .append("<noscript><button type='submit'>Continue</button></noscript>")
          .append("</form></body></html>");
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
