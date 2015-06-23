// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.project.utils.JavaWebStart;
import com.google.appinventor.server.project.utils.WebStartSupportDispatcher;
import com.google.appinventor.server.storage.UnauthorizedAccessException;
import com.google.appinventor.server.util.ResourceUtil;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for downloading files for a WebStart enabled application.
 *
 * <p>This needs to be done from a servlet that does not require login because
 * requests for the libraries of the WebStart application do not contain login
 * information. To ensure safety they contain an encrypted user ID as part of
 * their URL.
 *
 */
public class WebStartFileServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(WebStartFileServlet.class.getName());

  private final OdeAuthFilter odeFilter = new OdeAuthFilter();

  // Dispatcher for retrieving the appropriate WebStartSupport
  private final WebStartSupportDispatcher dispatcher = new WebStartSupportDispatcher();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    // URIs for file requests are structured as follows:
    //   /<baseurl>/webstartfile/<projectpath>/<fileName>
    String uriComponents[] = req.getRequestURI().split("/", 5);
    // TODO(lizlooney,user) If the URI doesn't contain enough components, the following lines
    // will throw an ArrayIndexOutOfBoundsException. We could deal with that outcome more cleanly
    // by returning an HTTP error code. This applies to all of our servlets.

    // Special Case the Blocks Editor download. We now request it without the encrypted
    // userid and projectid information. We don't really need it and having it present
    // causes some versions of Java Web Start to fail in its caching code because the
    // codebase attribute was too long. The encrypted id data is *long*.
    if (uriComponents.length < 5) {
      if (uriComponents[3].equals(ResourceUtil.CODEBLOCKS_JAR)) {
        String fileName = uriComponents[3]; // The Blocks Editor
        try {
          dispatcher.getWebStartSupport().doGet(req, resp, "", 0, fileName);
          return;
        } catch (Exception e) {
          if (e instanceof UnauthorizedAccessException ||
            e.getCause() instanceof UnauthorizedAccessException) {
            try {
              resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e1) {
              throw CrashReport.createAndLogError(LOG, req, null, e1);
            }
          }
        }
      }
    }

    JavaWebStart.DecryptedIds decryptedIds;
    try {
      decryptedIds = JavaWebStart.decryptWebStartProjectPath(uriComponents[3]);
    } catch (EncryptionException e) {
      // The user and project ids in the URL are not decryptable. They may have been encrypted with
      // a different version of App Inventor. Respond with GONE (response code 410), which means
      // (according to rfc2616, section 10) "The requested resource is no longer available at the
      // server and no forwarding address is known."
      try {
        resp.sendError(HttpServletResponse.SC_GONE);
        return;
      } catch (IOException e1) {
        throw CrashReport.createAndLogError(LOG, req, null, e1);
      }
    }

    String userId = decryptedIds.userId;
    // projectId can be 0 when we are retrieving files that are not associated
    // with a specific project, e.g., the codeblocks jar
    long projectId = decryptedIds.projectId;

    // Set the user in the OdeFilter, which is used everywhere as the UserInfoProvider.
    odeFilter.setUserFromUserId(userId);
    try {
      String fileName = uriComponents[4];
      dispatcher.getWebStartSupport().doGet(req, resp, userId, projectId, fileName);
    } catch (Exception e) {
      if (e instanceof UnauthorizedAccessException ||
          e.getCause() instanceof UnauthorizedAccessException) {
        try {
          resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (IOException e1) {
          throw CrashReport.createAndLogError(LOG, req, null, e1);
        }
      }
    } finally {
      odeFilter.removeUser();
    }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    // URIs for file requests are structured as follows:
    //   /<baseurl>/webstartfile/<projectpath>/<fileName>
    String uriComponents[] = req.getRequestURI().split("/", 5);
    // TODO(lizlooney,user) If the URI doesn't contain enough components, the following lines
    // will throw an ArrayIndexOutOfBoundsException. We could deal with that outcome more cleanly
    // by returning an HTTP error code. This applies to all of our servlets.

    JavaWebStart.DecryptedIds decryptedIds;
    try {
      decryptedIds = JavaWebStart.decryptWebStartProjectPath(uriComponents[3]);
    } catch (EncryptionException e) {
      // The user and project ids in the URL are not decryptable. They may have been encrypted with
      // a different version of App Inventor. Respond with GONE (response code 410), which means
      // (according to rfc2616, section 10) "The requested resource is no longer available at the
      // server and no forwarding address is known."
      try {
        resp.sendError(HttpServletResponse.SC_GONE);
        return;
      } catch (IOException e1) {
        throw CrashReport.createAndLogError(LOG, req, null, e1);
      }
    }

    String userId = decryptedIds.userId;
    long projectId = decryptedIds.projectId;

    // Set the user in the OdeFilter, which is used everywhere as the UserInfoProvider.
    odeFilter.setUserFromUserId(userId);
    try {
      String fileName = uriComponents[4];
      dispatcher.getWebStartSupport().doPost(req, resp, userId, projectId, fileName);
    } finally {
      odeFilter.removeUser();
    }

    if (!decryptedIds.versionOk) {
      // The blocks editor is running code that no longer matches the code running here in the app
      // engine server. Respond with CONFLICT (response code 409), which means (according to
      // rfc2616, section 10) "The request could not be completed due to a conflict with the
      // current state of the resource."

      // In this case, this is a bit of a lie, because the request was actually completed. We were
      // able to save the .blk file, but we want the user to know that they need to refresh the
      // browser and restart the blocks editor.
      try {
        resp.sendError(HttpServletResponse.SC_CONFLICT);
        return;
      } catch (IOException e1) {
        throw CrashReport.createAndLogError(LOG, req, null, e1);
      }
    }
  }
}
