// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appinventor.shared.rpc.ServerLayout;
/*
import com.google.gwt.gserver.SerializationPolicyProvider;
import com.google.gwt.gserver.SystemResourceSerializationPolicyProvider;
*/

import javax.servlet.http.HttpServletRequest;

/**
 * TODO(user);
 * Leftover server-related methods. This will probably go away at some point.
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


  /* TODO(user): What does this correspond to in App Engine world? */
// Construct a serialization policy provider which knows where the resources are.
/*
  SerializationPolicyProvider serializationPolicyProvider =
        new SystemResourceSerializationPolicyProvider(new String[][] {{
            GoogleServletEngine.getContextpathFlag() + ServerLayout.ODE_BASEURL,
            GWT_UI_MODULE + '/'}});

    // Tell the services to use our serialization policy provider.
    projectService.setSerializationPolicyProvider(serializationPolicyProvider);
    helpService.setSerializationPolicyProvider(serializationPolicyProvider);
    userInfoService.setSerializationPolicyProvider(serializationPolicyProvider);
    launchService.setSerializationPolicyProvider(serializationPolicyProvider);
*/

  /**
   * Indicates whether this server instance is a production server.
   *
   * @return  true if this server instance is running in prod
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
