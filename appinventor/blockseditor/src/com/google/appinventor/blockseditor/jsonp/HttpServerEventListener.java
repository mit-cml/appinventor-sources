// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.blockseditor.jsonp;

/**
 * Listener interface for receiving events from {@link HttpServer}.
 *
 * <p>Classes interested in processing events from an HttpServer must
 * implement this interface, and register with the HttpServer instance using
 * its {@link HttpServer#addHttpServerEventListener(HttpServerEventListener)}
 * method.
 *
 * <p>When the HttpServer selects HTTP the port, the listeners'
 * {@link #onPortSelected(HttpServer, int)} methods will be invoked.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface HttpServerEventListener {

  /**
   * Invoked when the {@link HttpServer} has selected the HTTP port
   *
   * @param server  the HttpServer
   * @param port  the port that the server will listen on for HTTP requests
   */
  void onPortSelected(HttpServer server, int port);

  /**
   * Invoked when there is a change in connectivity: either the server has
   * received no requests in a while or the server has received a request
   * after a period of no requests.
   *
   * @param server  the HttpSever
   * @param status  false if there have been no requests in a while, true
   *                otherwise.
   */
  void onConnectivityStatusChange(HttpServer server, boolean status);
}
