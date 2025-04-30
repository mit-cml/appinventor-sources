// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.common.base.Function;
import com.google.gwt.user.client.Window;

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

  private static boolean isSet(String str) {
    return str != null && !str.isEmpty();
  }

  /**
   * Constructs a relative URI from the current window's location, carrying over the locale,
   * repo, and galleryId parameters from the current session.
   *
   * @param base the base URI
   * @param hasParam true if the base URL already has a parameter, so ? should not be used
   * @return the new URI to redirect to
   */
  public static String makeUri(String base, boolean hasParam) {
    String[] params = new String[] { "locale", "repo", "galleryId" };
    String separator = "?";
    if (hasParam) {
      separator = "&";
    }
    StringBuilder sb = new StringBuilder(base);
    for (String param : params) {
      String value = Window.Location.getParameter(param);
      if (isSet(value)) {
        sb.append(separator);
        sb.append(param);
        sb.append("=");
        sb.append(value);
        separator = "&";
      }
    }
    return sb.toString();
  }

  public static String makeUri(String base) {
    return makeUri(base, false);
  }

}
