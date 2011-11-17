// Copyright 2009 Google Inc. All Rights Reserved.

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
