// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.blockseditor.jsonp;

import java.util.Map;

/**
 * An interface for handling HTTP requests.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface HttpRequestHandler {
  /**
   * Returns the response to the request.
   *
   * @param parameters the request parametere
   */
  String handleRequest(Map<String, String> parameters);
}
