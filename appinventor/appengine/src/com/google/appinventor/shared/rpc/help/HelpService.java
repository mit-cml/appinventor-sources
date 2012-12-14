// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
