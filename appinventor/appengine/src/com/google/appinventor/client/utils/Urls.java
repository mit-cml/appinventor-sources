// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.utils;

import com.google.common.base.Function;

/**
 * Utility class for working with URLs.
 *
 */
public final class Urls {

  private Urls() {
  }

  /**
   * Escapes a string for use within the query parameter section of a URI.
   *
   * @param param  string to escape
   */
  public static native String escapeQueryParameter(String param) /*-{
    return encodeURIComponent(param);
  }-*/;

  /**
   * Returns a {@code Function} that encodes a URI query parameter by
   * calling {@link #escapeQueryParameter}.
   */
  public static Function<String, String> getEscapeQueryParameterFunction() {
    return new Function<String, String>() {
      public String apply(String s) {
        return escapeQueryParameter(s);
      }
    };
  }
}
