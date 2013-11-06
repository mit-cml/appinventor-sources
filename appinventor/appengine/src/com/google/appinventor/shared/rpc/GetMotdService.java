// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Interface for the service providing the MOTD.
 *
 * @author kerr@google.com (Debby Wallach)
 */
@RemoteServiceRelativePath(ServerLayout.GET_MOTD_SERVICE)
public interface GetMotdService extends RemoteService {

  /**
   * Get MOTD.
   */
  Motd getMotd();

  /**
   * @return the interval to use, in seconds, between motd update checks
   */
  int getCheckInterval();
}
