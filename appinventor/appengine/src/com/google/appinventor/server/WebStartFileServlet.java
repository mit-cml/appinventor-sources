// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.appinventor.server.project.utils.Security;
import com.google.appinventor.server.project.utils.WebStartSupportDispatcher;
import com.google.appinventor.server.storage.UnauthorizedAccessException;
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
    //   /<baseurl>/webstartfile/encryptedUserAndProjectId/fileName
    String uriComponents[] = req.getRequestURI().split("/", 5);
    // TODO(lizlooney,user) If the URI doesn't contain enough components, the following lines
    // will throw an ArrayIndexOutOfBoundsException. We could deal with that outcome more cleanly
    // by returning an HTTP error code. This applies to all of our servlets.

    String userId = Security.decryptUserId(uriComponents[3]);
    // projectId can be 0 when we are retrieving files that are not associated
    // with a specific project, e.g., the codeblocks jar
    long projectId = Security.decryptProjectId(uriComponents[3]);

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
    //   /<baseurl>/webstartfile/encryptedUserAndProjectId/fileName
    String uriComponents[] = req.getRequestURI().split("/", 5);
    // TODO(lizlooney,user) If the URI doesn't contain enough components, the following lines
    // will throw an ArrayIndexOutOfBoundsException. We could deal with that outcome more cleanly
    // by returning an HTTP error code. This applies to all of our servlets.

    String userId = Security.decryptUserId(uriComponents[3]);
    long projectId = Security.decryptProjectId(uriComponents[3]);

    // Set the user in the OdeFilter, which is used everywhere as the UserInfoProvider.
    odeFilter.setUserFromUserId(userId);
    try {
      String fileName = uriComponents[4];
      dispatcher.getWebStartSupport().doPost(req, resp, userId, projectId, fileName);
    } finally {
      odeFilter.removeUser();
    }
  }
}
