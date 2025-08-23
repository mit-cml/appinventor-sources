// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.flags.Flag;
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

  private static final Flag<String> appengineHost = Flag.createFlag("appengine.host", "");

  public static String getStartPage() {
    return System.getProperty(START_PAGE_KEY);
  }

  /**
   * Indicates whether this server instance is running on app engine production
   * @return  true if this server instance is running on app engine production
   */
  public static boolean isProductionServer() {
    // TODO: Consume this from environment variables when not running in App Engine
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
  }

  /**
   * Returns the current runtime version.
   * @return string representing the current runtime version
   */
  public static String getRuntimeVersion() {
    // TODO: Consume this from environment variables when not running in App Engine
    return SystemProperty.version.get();
  }

  /**
   * Returns the current deployment version.
   * @return string representing the current deployment version
   */
  public static String getDeploymentVersion() {
    // TODO: Consume this from environment variables when not running in App Engine
    return SystemProperty.applicationVersion.get();
  }

  /**
   * Returns the public-facing hostname running the "app engine" side of App Inventor.
   * @return the public-facing hostname, without protocol
   */
  public static String getAppEngineHost() {
    if (Server.isProductionServer()) {
      if (StringUtils.isNullOrEmpty(appengineHost.get())) {
        String applicationVersionId = SystemProperty.applicationVersion.get();
        String applicationId = SystemProperty.applicationId.get();
        return applicationVersionId + "." + applicationId + ".appspot.com";
      } else {
        return appengineHost.get();
      }
    } else {
      // TODO(user): Figure out how to make this more generic
      return "localhost:8888";
    }
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
