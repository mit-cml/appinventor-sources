// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

/**
 * A listener to be notified of RPC events of remote services.
 *
 */
public interface RpcListener {
  /**
   * Method that is called right before a RPC call is sent to the server.
   *
   * @param method name of the service method
   * @param params parameters of the service method
   */
  void onStart(String method, Object... params);

  /**
   * Method that is called after a RPC call successfully completed.
   *
   * @param method name of the service method
   * @param result return value of the service method
   */
  void onSuccess(String method, Object result);

  /**
   * Method that is called after a RPC call failed with an exception.
   *
   * @param method name of the service method
   * @param caught the caught exception
   */
  void onFailure(String method, Throwable caught);
}
