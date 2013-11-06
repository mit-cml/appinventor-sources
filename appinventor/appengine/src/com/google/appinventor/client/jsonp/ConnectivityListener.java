// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
