// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server;


import com.google.appinventor.shared.rpc.help.HelpService;

/**
 * Implementation of the help service.
 *
 * <p>The help service also provides information.
 *
 */
public class HelpServiceImpl extends OdeRemoteServiceServlet implements HelpService {

  private static final long serialVersionUID = -73163124332949366L;

  @Override
  public boolean isProductionServer() {
    return Server.isProductionServer();
  }
}
