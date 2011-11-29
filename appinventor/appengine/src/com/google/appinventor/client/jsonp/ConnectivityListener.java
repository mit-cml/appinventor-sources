// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.jsonp;

/**
 * Listener interface for receiving notification from a {@link JsonpConnection}
 * concerning the connectivity with the server.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface ConnectivityListener {

  /**
   * Invoked when the connectivity status changes.
   *
   * @param connection  the JsonpConnection
   * @param status false if the {@link JsonpConnection} has not received a
   *               response from the server, indicating that the server might
   *               be dead.
   */
  void onConnectivityStatusChange(JsonpConnection connection, boolean status);
}
