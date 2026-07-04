// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Minimal direct HTTP helper for the LTI tool. Opens connections with no proxy,
 * because the LTI platform (Moodle) is reached on localhost while the dev server
 * may be launched with a SOCKS proxy meant only for the Google calls. Every call
 * throws on a 4xx or 5xx response, which HttpURLConnection does not do by default.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiHttp {

  private static final int CONNECT_TIMEOUT_MILLIS = 15000;
  private static final int READ_TIMEOUT_MILLIS = 30000;
  private static final int MAX_BODY_BYTES = 1 << 20;

  private LtiHttp() {}

  static String get(String urlString) throws IOException {
    HttpURLConnection conn = open(urlString, "GET");
    try {
      conn.setRequestProperty("Accept", "application/json");
      return readBody(conn);
    } finally {
      conn.disconnect();
    }
  }

  static String postForm(String urlString, String body) throws IOException {
    return post(urlString, body, "application/x-www-form-urlencoded", null);
  }

  static String postJson(String urlString, String json) throws IOException {
    return post(urlString, json, "application/json", null);
  }

  static String postJsonWithBearer(String urlString, String json, String bearer,
      String contentType) throws IOException {
    return post(urlString, json, contentType, bearer);
  }

  private static String post(String urlString, String body, String contentType, String bearer)
      throws IOException {
    HttpURLConnection conn = open(urlString, "POST");
    try {
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", contentType);
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Connection", "close");
      if (bearer != null) {
        conn.setRequestProperty("Authorization", "Bearer " + bearer);
      }
      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes(StandardCharsets.UTF_8));
      }
      return readBody(conn);
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Opens a connection to the URL with the given method. Forces a direct
   * connection, because Moodle is on localhost and the dev server may carry a
   * proxy meant only for the Google calls. Redirects are not followed, so a
   * platform response cannot bounce a request to an unintended host.
   */
  private static HttpURLConnection open(String urlString, String method) throws IOException {
    URL url = new URL(urlString);
    if (!"http".equals(url.getProtocol()) && !"https".equals(url.getProtocol())) {
      throw new IOException("Refusing a non HTTP URL");
    }
    // Block link local and wildcard hosts so a platform supplied key set or
    // registration URL cannot reach a cloud metadata endpoint. Loopback stays
    // reachable for the local platform used in development.
    InetAddress address = InetAddress.getByName(url.getHost());
    if (address.isLinkLocalAddress() || address.isAnyLocalAddress()
        || address.isMulticastAddress()) {
      throw new IOException("Refusing a link local address");
    }
    HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
    conn.setInstanceFollowRedirects(false);
    conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
    conn.setReadTimeout(READ_TIMEOUT_MILLIS);
    conn.setRequestMethod(method);
    return conn;
  }

  private static String readBody(HttpURLConnection conn) throws IOException {
    int status = conn.getResponseCode();
    InputStream stream =
        (status >= 200 && status < 400) ? conn.getInputStream() : conn.getErrorStream();
    StringBuilder sb = new StringBuilder();
    if (stream != null) {
      try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
        char[] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
          sb.append(buffer, 0, n);
          if (sb.length() > MAX_BODY_BYTES) {
            throw new IOException("Response body over " + MAX_BODY_BYTES + " bytes");
          }
        }
      }
    }
    if (status >= 400) {
      throw new IOException("HTTP " + status + " from " + conn.getURL() + ": " + sb);
    }
    return sb.toString();
  }
}
