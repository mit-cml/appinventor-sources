// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
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
   * Opens a connection to {@code urlString} through the optional outbound proxy
   * named by the {@code lms.proxy.host}, {@code lms.proxy.port}, and
   * {@code lms.proxy.type} system properties. Production on App Engine reaches
   * Google directly, so these are unset there and the connection is direct. They
   * exist for restricted networks and local development behind a proxy, and they
   * bypass any host-level proxy the platform might otherwise impose.
   *
   * @param urlString the absolute URL to open
   * @return an unconnected {@link HttpURLConnection} using the configured proxy
   * @throws IOException if the URL is malformed or the connection cannot open
   */
  static HttpURLConnection open(String urlString) throws IOException {
    return (HttpURLConnection) new URL(urlString).openConnection(proxy());
  }

  @VisibleForTesting
  static Proxy proxy() {
    String host = System.getProperty("lms.proxy.host", "");
    if (host.isEmpty()) {
      return Proxy.NO_PROXY;
    }
    int port = Integer.parseInt(System.getProperty("lms.proxy.port", "0"));
    Proxy.Type type = "socks".equalsIgnoreCase(System.getProperty("lms.proxy.type", "http"))
        ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
    return new Proxy(type, new InetSocketAddress(host, port));
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
   * Reads a failed connection's error stream as UTF-8 for diagnostics, returning
   * an empty string when there is none. Used to surface a Google API error body
   * (for example a disabled-API or insufficient-scope message) in the thrown
   * exception so the cause is visible in the logs.
   *
   * @param conn an already-sent connection whose status is 4xx or 5xx
   * @return the error body, or an empty string
   */
  static String errorBody(HttpURLConnection conn) {
    InputStream stream = conn.getErrorStream();
    if (stream == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    } catch (IOException e) {
      // Best effort: return whatever was read before the failure.
    }
    return sb.toString();
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
