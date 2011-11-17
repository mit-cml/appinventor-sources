// Copyright 2010 Google Inc. All Rights Reserved.

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
