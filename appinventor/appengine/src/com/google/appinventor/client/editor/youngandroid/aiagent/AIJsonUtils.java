// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 * Shared JSON field extraction and escaping utilities for the AI agent module.
 *
 * <p>All methods are static. This is a leaf utility with no dependencies on
 * other AI agent classes.</p>
 */
public final class AIJsonUtils {

  private AIJsonUtils() {}

  /**
   * Extracts a string value from a GWT {@link JSONObject} field. Returns null
   * if the field is absent or not a string.
   */
  public static String getStringField(JSONObject json, String field) {
    if (!json.containsKey(field)) {
      return null;
    }
    JSONValue val = json.get(field);
    if (val.isString() != null) {
      return val.isString().stringValue();
    }
    // Tolerate non-string values by converting to string.
    return val.toString();
  }

  /**
   * Escapes a string for safe embedding inside a JSON string literal
   * (without surrounding quotes).
   */
  public static String escapeJsonString(String raw) {
    if (raw == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(raw.length());
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      switch (c) {
        case '"':  sb.append("\\\""); break;
        case '\\': sb.append("\\\\"); break;
        case '\n': sb.append("\\n");  break;
        case '\r': sb.append("\\r");  break;
        case '\t': sb.append("\\t");  break;
        default:   sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Extracts a simple JSON field value from a JSON payload string.
   * This is a lightweight extraction -- not a full JSON parser.
   *
   * @param json      the JSON payload
   * @param fieldName the field name to extract
   * @return the extracted value, or the field name if not found
   */
  public static String extractField(String json, String fieldName) {
    if (json == null || json.isEmpty()) {
      return fieldName;
    }
    String key = "\"" + fieldName + "\"";
    int idx = json.indexOf(key);
    if (idx < 0) {
      return fieldName;
    }
    int colonIdx = json.indexOf(':', idx + key.length());
    if (colonIdx < 0) {
      return fieldName;
    }
    int start = colonIdx + 1;
    // Skip whitespace
    while (start < json.length() && json.charAt(start) == ' ') {
      start++;
    }
    if (start >= json.length()) {
      return fieldName;
    }
    if (json.charAt(start) == '"') {
      // String value — find closing quote, skipping escaped characters
      int end = start + 1;
      while (end < json.length()) {
        char c = json.charAt(end);
        if (c == '\\') {
          end += 2; // skip escape sequence (e.g., \", \\, \n)
          continue;
        }
        if (c == '"') {
          break;
        }
        end++;
      }
      if (end >= json.length()) {
        return unescapeJsonString(json.substring(start + 1));
      }
      return unescapeJsonString(json.substring(start + 1, end));
    } else {
      // Non-string value (number, boolean, etc.)
      int end = start;
      while (end < json.length()
          && json.charAt(end) != ',' && json.charAt(end) != '}') {
        end++;
      }
      return json.substring(start, end).trim();
    }
  }

  /**
   * Unescapes a JSON string value (the content between the outer quotes).
   * Handles standard JSON escape sequences: \", \\, \/, \n, \r, \t.
   */
  public static String unescapeJsonString(String s) {
    if (s.indexOf('\\') < 0) {
      return s; // fast path: no escapes
    }
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' && i + 1 < s.length()) {
        char next = s.charAt(i + 1);
        switch (next) {
          case '"':  sb.append('"');  i++; break;
          case '\\': sb.append('\\'); i++; break;
          case '/':  sb.append('/');  i++; break;
          case 'n':  sb.append('\n'); i++; break;
          case 'r':  sb.append('\r'); i++; break;
          case 't':  sb.append('\t'); i++; break;
          default:   sb.append(c);         break;
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Produces a JSON-escaped string literal (with surrounding quotes).
   */
  public static String jsonString(String value) {
    if (value == null) {
      return "\"\"";
    }
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"':  sb.append("\\\""); break;
        case '\\': sb.append("\\\\"); break;
        case '\n': sb.append("\\n");  break;
        case '\r': sb.append("\\r");  break;
        case '\t': sb.append("\\t");  break;
        default:
          if (c < 0x20) {
            sb.append("\\u");
            String hex = Integer.toHexString(c);
            for (int pad = hex.length(); pad < 4; pad++) {
              sb.append('0');
            }
            sb.append(hex);
          } else {
            sb.append(c);
          }
      }
    }
    sb.append("\"");
    return sb.toString();
  }

  /**
   * Escapes HTML special characters and converts newlines to {@code <br>} tags.
   */
  public static String escapeAndFormat(String text) {
    if (text == null) {
      return "";
    }
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("\n", "<br>");
  }
}
