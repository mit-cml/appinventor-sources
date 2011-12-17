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
   * Returns true if this server instance is running on app engine production
   * @return is production server running
   */
  boolean isProductionServer();
}
