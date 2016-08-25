// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

/**
 * UriBuilder a class which facilitates building a URI with a colleciton
 * of parameters in a query string.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

package com.google.appinventor.server.util;

public class UriBuilder {
  private StringBuilder sb = new StringBuilder();
  private boolean first = true;

  public UriBuilder(String baseUri) {
    sb.append(baseUri);
  }

  public UriBuilder add(String key, String value) {
    // If value is null, we don't add it
    if (value == null) {
      return this;
    }
    if (first) {
      sb.append("?");
      first = false;
    } else {
      sb.append("&");
    }
    sb.append(key + "=" + value);
    return this;
  }

  public String build() {
    return new String(sb);
  }
}
