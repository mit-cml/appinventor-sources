// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.launch;

import com.google.appinventor.shared.jsonp.JsonpConnectionInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Asynchronous version of {@link LaunchService}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface LaunchServiceAsync {
  /**
   * See {@link LaunchService#clearJsonpConnectionInfo(String)}.
   */
  void clearJsonpConnectionInfo(String filePrefix, AsyncCallback<Void> callback);

  /**
   * See {@link LaunchService#retrieveJsonpConnectionInfo(String)}.
   */
  void retrieveJsonpConnectionInfo(String filePrefix,
      AsyncCallback<JsonpConnectionInfo> callback);

  /**
   * See {@link LaunchService#getWebStartProjectPath(long)}.
   */
  void getWebStartProjectPath(long projectId, AsyncCallback<String> callback);
}
