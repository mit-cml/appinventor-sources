// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Minimal direct HTTP helper for the LTI tool. Unlike the Google LMS client,
 * this opens connections directly with no proxy, because the LTI platform
 * (Moodle) is reached on localhost while the dev server may be launched with a
 * SOCKS proxy for the Google calls.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiHttp {

  private static final int CONNECT_TIMEOUT_MILLIS = 15000;
  private static final int READ_TIMEOUT_MILLIS = 30000;

  private LtiHttp() {}

  /** GETs a URL and returns the body, throwing on a 4xx or 5xx response. */
  static String get(String urlString) throws IOException {
    // Force a direct connection. Moodle is on localhost, and the dev server may
    // be launched with a system or SOCKS proxy meant only for the Google calls.
    HttpURLConnection conn =
        (HttpURLConnection) new URL(urlString).openConnection(Proxy.NO_PROXY);
    try {
      conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
      conn.setReadTimeout(READ_TIMEOUT_MILLIS);
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");
      return readBody(conn);
    } finally {
      conn.disconnect();
    }
  }

  /**
   * POSTs an application/x-www-form-urlencoded body and returns the response,
   * throwing on a 4xx or 5xx response.
   */
  static String postForm(String urlString, String body) throws IOException {
    // Force a direct connection. Moodle is on localhost, and the dev server may
    // be launched with a system or SOCKS proxy meant only for the Google calls.
    HttpURLConnection conn =
        (HttpURLConnection) new URL(urlString).openConnection(Proxy.NO_PROXY);
    try {
      conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
      conn.setReadTimeout(READ_TIMEOUT_MILLIS);
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Connection", "close");
      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes(StandardCharsets.UTF_8));
      }
      return readBody(conn);
    } finally {
      conn.disconnect();
    }
  }

  /**
   * POSTs a JSON body with a bearer token and returns the response, throwing on
   * a 4xx or 5xx response.
   */
  static String postJsonWithBearer(String urlString, String json, String bearer,
      String contentType) throws IOException {
    // Force a direct connection. Moodle is on localhost, and the dev server may
    // be launched with a system or SOCKS proxy meant only for the Google calls.
    HttpURLConnection conn =
        (HttpURLConnection) new URL(urlString).openConnection(Proxy.NO_PROXY);
    try {
      conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
      conn.setReadTimeout(READ_TIMEOUT_MILLIS);
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", contentType);
      conn.setRequestProperty("Authorization", "Bearer " + bearer);
      conn.setRequestProperty("Connection", "close");
      try (OutputStream os = conn.getOutputStream()) {
        os.write(json.getBytes(StandardCharsets.UTF_8));
      }
      return readBody(conn);
    } finally {
      conn.disconnect();
    }
  }

  private static String readBody(HttpURLConnection conn) throws IOException {
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
      throw new IOException("HTTP " + status + " from " + conn.getURL() + ": " + sb);
    }
    return sb.toString();
  }
}
