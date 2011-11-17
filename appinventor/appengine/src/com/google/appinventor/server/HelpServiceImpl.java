// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.appinventor.server.util.BuildData;
import com.google.appinventor.shared.rpc.help.HelpService;

import java.util.logging.Logger;

/**
 * Implementation of the help service.
 *
 * <p>The help service also provides build information.
 *
 */
public class HelpServiceImpl extends OdeRemoteServiceServlet implements HelpService {

  private static final long serialVersionUID = -73163124332949366L;

  // Logging support
  private static final Logger LOG = Logger.getLogger(HelpServiceImpl.class.getName());

  /*
   * Provide the server deploy timestamp
   */
  public String getBuildTimeStamp() {
   return BuildData.getTimestampAsString();
  }
}
