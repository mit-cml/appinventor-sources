// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.common.jsonp;

/**
 * Constants related to JSONP.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class JsonpConstants {

  private JsonpConstants() {
  }

  // Constants used in HTTP requests:
  public static final String CONTACT = "contact";
  public static final String QUIT = "quit";

  // Constants used in HTTP request parameters:
  public static final String OUTPUT = "output";
  public static final String REQUIRED_OUTPUT_VALUE = "json";
  public static final String CALLBACK = "callback";
  public static final String REQUIRED_CALLBACK_VALUE = "jsonpcb";
  public static final String ID = "id";
  public static final String SECRET = "s";
  public static final String POLLING = "polling";

  // Constants used in HTTP responses:
  public static final String NOT_FINISHED_YET = "not finished yet";
}
