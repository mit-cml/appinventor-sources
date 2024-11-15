// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

public class Server {

  private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
  
  // Start page for ODE/YA; main page redirected by other servlets
  public static final String START_PAGE;
  
  // System property name for GWT UI module start page
  private static final String START_PAGE_KEY = "StartPage";

  static {
    START_PAGE = getStartPage();
    if (START_PAGE == null) {
      LOGGER.warning("Start page not found. Ensure 'StartPage' property is set.");
    }
  }

  /**
   * Retrieves the start page URL from system properties.
   *
   * @return the configured start page URL, or null if not set
   */
  private static String getStartPage() {
    return System.getProperty(START_PAGE_KEY);
  }

  /**
   * Checks if this server instance is running on App Engine production.
   *
   * @return true if running on App Engine production, false otherwise
   */
  public static boolean isProductionServer() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
  }

  /**
   * Builds a full URL by appending the provided path to the current server's base URL.
   *
   * @param req the HTTP request to retrieve scheme, server name, and port
   * @param path the requested path to append
   * @return a complete URL as a string
   */
  public static String urlFromPath(HttpServletRequest req, String path) {
    int port = req.getServerPort();
    boolean isDefaultPort = (req.getScheme().equals("http") && port == 80) ||
                            (req.getScheme().equals("https") && port == 443);
    String portPart = isDefaultPort ? "" : ":" + port;

    return req.getScheme() + "://" + req.getServerName() + portPart + ServerLayout.ODE_BASEURL + path;
  }
}

