// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.properties.json;

import com.google.appinventor.common.utils.StringUtils;

/**
 * Helper methods used for JSON encoding and decoding.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class JSONUtil {
  private JSONUtil() {
  }
  
  /**
   * Converts a String to a JSON String.
   * Returns null if the String is null.
   */
  public static String toJson(String s) {
    return StringUtils.toJson(s);
  }

  /**
   * Converts an int to a JSON String.
   */
  public static String toJson(int i) {
    return Integer.toString(i);
  }

  /**
   * Converts a long to a JSON String.
   */
  public static String toJson(Long l) {
    return Long.toString(l);
  }

  /**
   * Converts a boolean to a JSON String.
   */
  public static String toJson(boolean b) {
    return Boolean.toString(b);
  }

  /**
   * Convert an array of String to a JSON String.
   * Returns null if the String is null.
   */
  public static String toJson(String[] array) {
    if (array != null) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      String separator = "";
      for (String s : array) {
        sb.append(separator).append(StringUtils.toJson(s));
        separator = ",";
      }
      sb.append("]");
      return sb.toString();
    } else {
      return null;
    }
  }

  /**
   * Convert a {@link JSONEncodable} to a JSON String.
   * Returns null if the JSONEncodable is null.
   */
  public static String toJson(JSONEncodable jsonEncodable) {
    return jsonEncodable != null
        ? jsonEncodable.toJson()
        : null;
  }

  /**
   * Convert an array of {@link JSONEncodable} to a JSON String.
   * Returns null if the JSONEncodable is null.
   */
  public static String toJson(JSONEncodable[] array) {
    if (array != null) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      String separator = "";
      for (JSONEncodable jsonEncodable : array) {
        sb.append(separator).append(JSONUtil.toJson(jsonEncodable));
        separator = ",";
      }
      sb.append("]");
      return sb.toString();
    } else {
      return null;
    }
  }

  /**
   * Converts a {@link JSONValue} to a String.
   * Returns null if the JSONValue is null.
   */
  public static String stringFromJsonValue(JSONValue jsonValue) {
    return jsonValue != null ? jsonValue.asString().getString() : null;
  }

  /**
   * Converts a {@link JSONValue} to an int.
   */
  public static int intFromJsonValue(JSONValue jsonValue) {
    return jsonValue.asNumber().getInt();
  }

  /**
   * Converts a {@link JSONValue} to a long.
   */
  public static long longFromJsonValue(JSONValue jsonValue) {
    return jsonValue.asNumber().getLong();
  }

  /**
   * Converts a {@link JSONValue} to a boolean.
   */
  public static boolean booleanFromJsonValue(JSONValue jsonValue) {
    return jsonValue.asBoolean().getBoolean();
  }
}
