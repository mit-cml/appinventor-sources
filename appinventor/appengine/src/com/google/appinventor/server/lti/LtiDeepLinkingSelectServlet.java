// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData;

import java.io.IOException;
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
 * time token minted when the picker was rendered, so the form carries nothing
 * that could be tampered with. The token also records the teacher, and the
 * chosen project is verified to belong to that teacher before it is signed into
 * the response, so a tampered form cannot hand another user's project to
 * students.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiDeepLinkingSelectServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiDeepLinkingSelectServlet.class.getName());
  private static final String LTI = "https://purl.imsglobal.org/spec/lti/claim/";
  private static final String LTI_DL = "https://purl.imsglobal.org/spec/lti-dl/claim/";
  private static final long RESPONSE_TTL_SECONDS = 300;

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String templateId = req.getParameter("template_project_id");
      LtiState.DeepLink dl = LtiState.consumeDeepLink(req.getParameter("dl"));
      if (templateId == null || templateId.isEmpty() || dl == null || dl.returnUrl.isEmpty()) {
        invalidSelection(resp);
        return;
      }
      StoredData.LtiPlatformData platform = LtiConfig.platform(dl.issuer);
      if (platform == null) {
        invalidSelection(resp);
        return;
      }

      // The chosen project must belong to the teacher who opened the picker. The
      // picker only lists that teacher's own projects, but the posted id is
      // editable, so the owner is verified here before it is signed into a
      // response that would copy the project to students.
      String title;
      try {
        long pid = Long.parseLong(templateId);
        String owner = storageIo.getProjectUserId(pid);
        if (owner == null || !owner.equals(dl.teacherUserId)) {
          invalidSelection(resp);
          return;
        }
        title = "App Inventor " + storageIo.getProjectName(owner, pid);
      } catch (NumberFormatException e) {
        invalidSelection(resp);
        return;
      }

      long now = System.currentTimeMillis() / 1000L;
      LtiKeys.SigningKey signing = LtiKeys.signingKey();

      // A template reference the tool signs for itself, so that only a template
      // the teacher was verified to own can reach a student, even if a custom
      // parameter were set outside this flow.
      String templateRef = LtiJwt.sign(
          new JSONObject().put("alg", "RS256").put("typ", "JWT").put("kid", signing.kid),
          new JSONObject().put("template_project_id", templateId).put("iat", now),
          signing.privateKey);

      JSONObject contentItem = new JSONObject()
          .put("type", "ltiResourceLink")
          .put("title", title)
          .put("url", LtiConfig.launchUrl())
          .put("custom", new JSONObject().put("template", templateRef));
      JSONArray contentItems = new JSONArray().put(contentItem);

      JSONObject header = new JSONObject()
          .put("alg", "RS256").put("typ", "JWT").put("kid", signing.kid);
      JSONObject payload = new JSONObject()
          .put("iss", platform.clientId)
          .put("aud", platform.issuer)
          .put("iat", now)
          .put("exp", now + RESPONSE_TTL_SECONDS)
          .put("nonce", LtiState.random())
          .put(LTI + "deployment_id", dl.deploymentId)
          .put(LTI + "message_type", "LtiDeepLinkingResponse")
          .put(LTI + "version", "1.3.0")
          .put(LTI_DL + "content_items", contentItems);
      if (!dl.data.isEmpty()) {
        payload.put(LTI_DL + "data", dl.data);
      }

      String jwt = LtiJwt.sign(header, payload, signing.privateKey);

      // Auto POST the signed response back to the platform return url.
      resp.setContentType("text/html; charset=utf-8");
      StringBuilder html = new StringBuilder()
          .append("<!DOCTYPE html><html><body onload='document.forms[0].submit()'>")
          .append("<form method='post' action='")
          .append(LtiHtml.escape(dl.returnUrl)).append("'>")
          .append("<input type='hidden' name='JWT' value='")
          .append(LtiHtml.escape(jwt)).append("'>")
          .append("<noscript><button type='submit'>Continue</button></noscript>")
          .append("</form></body></html>");
      resp.getWriter().write(html.toString());
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI deep linking selection failed", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Deep linking failed");
    }
  }

  private static void invalidSelection(HttpServletResponse resp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    resp.setContentType("text/plain; charset=utf-8");
    resp.getWriter().println("This selection is no longer valid. Close this window, then use "
        + "Select content again from your course.");
  }
}
