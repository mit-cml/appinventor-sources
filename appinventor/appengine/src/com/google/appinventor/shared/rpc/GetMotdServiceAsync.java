// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interface for the service providing the MOTD.  All declarations in this
 * interface are mirrored in {@link GetMotdService}.  For further
 * information see {@link GetMotdService}.
 *
 *
 * @author kerr@google.com (Debby Wallach)
 */
public interface GetMotdServiceAsync {

  /**
   * @see GetMotdService#getMotd()
   */
  void getMotd(AsyncCallback<Motd> callback);

  /**
   * @see GetMotdService#getCheckInterval()
   */
  void getCheckInterval(AsyncCallback<Integer> callback);
}
