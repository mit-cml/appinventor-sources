// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appinventor.shared.rpc.ServerLayout;

import javax.servlet.http.HttpServletRequest;

/**
 * TODO(user);
 * Useful server-related methods. Probably these should be moved elsewhere.
 *
 */
public class Server {

  // Start page for ODE/YA. This is where other servlets redirect to to get the main page
  public static final String START_PAGE = getStartPage();

  // System property name to obtain GWT UI module
  private static final String START_PAGE_KEY = "StartPage";


  public static String getStartPage() {
    return System.getProperty(START_PAGE_KEY);
  }

  /**
   * Indicates whether this server instance is running on app engine production
   *
   * @return  true if this server instance is running on app engine production
   */
  public static boolean isProductionServer() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
  }

  /**
   * Returns URL built by appending path to the current server and the server
   * port and base URL.
   *
   * @param req HTTP request
   * @param path requested path
   * @return build URL
   */
  public static String urlFromPath(HttpServletRequest req, String path) {
    // TODO(user): omit the port if it is the default port for a schema
    return req.getScheme() + "://" + req.getServerName() + ':' + req.getServerPort()
        + ServerLayout.ODE_BASEURL + path;
  }
}
