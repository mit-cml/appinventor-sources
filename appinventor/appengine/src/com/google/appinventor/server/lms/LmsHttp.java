// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Small HTTP and JSON helpers shared by the LMS Google API clients. Uses
 * {@link HttpURLConnection} for requests and the {@code org.json} parser already
 * on the server classpath for responses.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LmsHttp {

  /** Connect timeout for the Google API calls, in milliseconds. */
  private static final int CONNECT_TIMEOUT_MILLIS = 15000;

  /** Read timeout for the Google API calls, in milliseconds. */
  private static final int READ_TIMEOUT_MILLIS = 30000;

  private LmsHttp() {}

  /**
   * Applies connect and read timeouts so a slow or hung Google response cannot
   * pin a server request thread indefinitely.
   *
   * @param conn the connection to configure
   */
  static void applyTimeouts(HttpURLConnection conn) {
    conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
    conn.setReadTimeout(READ_TIMEOUT_MILLIS);
  }

  /**
   * URL-encodes a value for an application/x-www-form-urlencoded body.
   *
   * @param value the value to encode
   * @return the URL-encoded value
   */
  static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  /**
   * Reads the response body as UTF-8, or throws on a 4xx/5xx response. On an
   * error the thrown message includes the response body for diagnosis, so this
   * must not be used for an endpoint whose error body could carry a secret
   * (Google's token and Drive error bodies do not).
   *
   * @param conn an already-sent connection
   * @param label a short label for error messages
   * @return the response body as a string
   * @throws IOException on a 4xx or 5xx response, or on a read error
   */
  static String readBody(HttpURLConnection conn, String label) throws IOException {
    try {
      int status = conn.getResponseCode();
      InputStream stream =
          (status >= 200 && status < 400) ? conn.getInputStream() : conn.getErrorStream();
      StringBuilder sb = new StringBuilder();
      if (stream != null) {
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
          String line;
          while ((line = reader.readLine()) != null) {
            sb.append(line);
          }
        }
      }
      if (status >= 400) {
        throw new IOException(label + " returned HTTP " + status + ": " + sb);
      }
      return sb.toString();
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Returns the value of a top-level field as a non-empty string, or
   * {@code null} if the field is absent or empty.
   *
   * @param json a JSON object body
   * @param key the field name
   * @return the field value, or {@code null} if the field is absent or empty
   * @throws IOException if {@code json} is not valid JSON
   */
  static String jsonField(String json, String key) throws IOException {
    try {
      String value = new JSONObject(json).optString(key, null);
      return (value == null || value.isEmpty()) ? null : value;
    } catch (JSONException e) {
      throw new IOException("Could not parse the JSON response", e);
    }
  }
}
