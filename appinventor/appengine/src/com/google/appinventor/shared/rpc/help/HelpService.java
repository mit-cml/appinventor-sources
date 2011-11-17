// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.help;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Interface for the service providing help information.
 *
 */
@RemoteServiceRelativePath(ServerLayout.HELP_SERVICE)
public interface HelpService extends RemoteService {

  /**
   * Returns the Build TimeStamp, or a message indicating that it could not be provided.
   * @return build time stamp
   */
  String getBuildTimeStamp();
}
