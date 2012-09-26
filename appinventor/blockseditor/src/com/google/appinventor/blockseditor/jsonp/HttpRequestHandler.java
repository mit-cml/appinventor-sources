// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
