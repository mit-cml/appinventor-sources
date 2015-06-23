// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.launch;

import com.google.appinventor.shared.jsonp.JsonpConnectionInfo;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Interface for the service providing launch related services.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@RemoteServiceRelativePath(ServerLayout.LAUNCH_SERVICE)
public interface LaunchService extends RemoteService {
  /**
   * Clears the JSONP connection info by saving an empty string in the info file.
   *
   * @param filePrefix  prefix of the file name containing the info
   */
  void clearJsonpConnectionInfo(String filePrefix);

  /**
   * Retrieves the JSONP connection info and then clears it.
   *
   * @param filePrefix  prefix of the file name containing the info
   * @return the JSONP connection info or null if it has not been saved
   */
  JsonpConnectionInfo retrieveJsonpConnectionInfo(String filePrefix);

  /**
   * Returns the project path that can be used in JSONP requests sent to a
   * JavaWebStart HTTP server.
   *
   * @param projectId
   * @return the project path as a String
   */
  String getWebStartProjectPath(long projectId);
}
