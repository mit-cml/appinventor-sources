// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.appinventor.server.project.utils.WebStartSupportDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for downloading jnlp files, which are used to launch WebStart
 * enabled applications.
 *
 */
public class WebStartJnlpServlet extends OdeServlet {

  // Constants for accessing split URL
  private static final int PURPOSE_INDEX = 3;
  private static final int MAX_INDEX = PURPOSE_INDEX + 1;  // Must be highest of preceding constants

  // Dispatcher for retrieving the appropriate WebStartSupport
  private final WebStartSupportDispatcher dispatcher = new WebStartSupportDispatcher();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    // URIs for jnlp files (which are used to launch JavaWebStart enabled applications) are
    // structured as follows:
    //   /<baseurl>/webstartjnlp/purpose/id
    String uriComponents[] = req.getRequestURI().split("/", MAX_INDEX + 1);
    String purpose = uriComponents[PURPOSE_INDEX];
    dispatcher.getWebStartSupport().sendJnlpFile(req, resp, purpose);
  }
}
