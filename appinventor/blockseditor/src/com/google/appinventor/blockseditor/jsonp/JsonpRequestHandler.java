// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.blockseditor.jsonp;

import com.google.appinventor.common.jsonp.JsonpConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * A request handler base class for JSONP requests that can be handled
 * synchronously.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class JsonpRequestHandler implements HttpRequestHandler {
  /**
   * The server that this handler is for, used for logging messages.
   */
  protected final HttpServer server;

  /**
   * Creates a JsonpRequestHandler.
   *
   * @param server the server for this handler, used for logging messages
   */
  public JsonpRequestHandler(HttpServer server) {
    this.server = server;
  }

  @Override
  public final String handleRequest(Map<String, String> parameters) {
    // Get the response value from the subclass.
    boolean success;
    String responseValue;
    try {
      responseValue = getResponseValue(parameters);
      success = true;
    } catch (Throwable e) {
      if (HttpServer.LOG_TO_SYSTEM_OUT) {
        e.printStackTrace(System.out);
      }
      responseValue = e.toString();
      success = false;
    }

    // Prepare the JSONP response, which is javascript code.
    // We call the callback and pass parameters for id, success, and the response value.
    String callback = parameters.get(JsonpConstants.CALLBACK);
    String id = parameters.get(JsonpConstants.ID);
    StringBuilder response = new StringBuilder();
    response.append(callback).append("(\"").append(id).append("\",").append(success).append(",");
    if (responseValue != null) {
      // We need to encode the response value.
      String encodedResponseValue = responseValue;
      try {
        encodedResponseValue = URLEncoder.encode(responseValue, "UTF-8");
        // URLEncoder.encode converted the space characters into plus signs.
        // However, the ODE client is going to use javascript's decodeURIComponent to decode the
        // response and it won't convert the plus signs back to spaces.
        // So, here we convert the plus signs to %20, which is what the ODE client is expecting.
        encodedResponseValue = encodedResponseValue.replace("+", "%20");
      } catch (UnsupportedEncodingException wontHappen) {
      }
      response.append("\"").append(encodedResponseValue).append("\"");
    } else {
      // The response value is just null.
      response.append("null");
    }
    response.append(")\n");

    server.log("    handleRequest returning: " + response + "\n");
    return response.toString();
  }

  /**
   * Returns the response value that will be passed to the javascript callback.
   *
   * @param parameters the request parametere
   */
  public abstract String getResponseValue(Map<String, String> parameters) throws Throwable;
}
