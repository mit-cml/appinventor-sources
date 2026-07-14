// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.common.annotations.VisibleForTesting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Minimal direct HTTP helper for the LTI tool. Opens connections with no proxy,
 * because the LTI platform (Moodle) is reached on localhost while the dev server
 * may be launched with a SOCKS proxy meant only for the Google calls. Every call
 * throws on any non 2xx response, so a redirect the tool does not follow cannot
 * read as a success.
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

  static String postWithBearer(String urlString, String body, String bearer,
      String contentType) throws IOException {
    return post(urlString, body, contentType, bearer);
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
   * Opens a direct connection with no proxy. Redirects are not followed, so a
   * platform response cannot bounce the request to an unintended host.
   */
  private static HttpURLConnection open(String urlString, String method) throws IOException {
    URL url = new URL(urlString);
    boolean allowInsecure = LtiConfig.allowInsecure();
    if (!transportAllowed(url.getProtocol(), allowInsecure)) {
      throw new IOException("Refusing an insecure or non HTTP URL");
    }
    if (!hostAllowedForFetch(InetAddress.getByName(url.getHost()), allowInsecure)) {
      throw new IOException("Refusing a private or loopback address");
    }
    HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
    conn.setInstanceFollowRedirects(false);
    conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
    conn.setReadTimeout(READ_TIMEOUT_MILLIS);
    conn.setRequestMethod(method);
    return conn;
  }

  /** Whether the transport is allowed, https always and plain http only in dev. */
  @VisibleForTesting
  static boolean transportAllowed(String protocol, boolean allowInsecure) {
    return "https".equals(protocol) || ("http".equals(protocol) && allowInsecure);
  }

  /**
   * Whether a browser-facing URL, an authorization endpoint or a Deep Linking return URL, uses an
   * allowed transport, so state, tokens, and signed content are not redirected or auto posted over
   * plain http outside development (LTI Core 3.5).
   */
  static boolean browserUrlAllowed(String url, boolean allowInsecure) {
    try {
      return transportAllowed(new URL(url).getProtocol(), allowInsecure);
    } catch (java.net.MalformedURLException e) {
      return false;
    }
  }

  /**
   * Whether the tool may fetch from a resolved host. A public host is allowed, a
   * private or internal one is refused, and loopback is allowed only in
   * development, so a production tool cannot be pointed at a loopback service.
   */
  @VisibleForTesting
  static boolean hostAllowedForFetch(InetAddress address, boolean allowInsecure)
      throws IOException {
    // Loopback is reachable only in development. Judge the original address first,
    // so IPv6 loopback ::1 is caught before the embedded IPv4 reduction rewrites it
    // to a non loopback literal. Then judge the reduced form too, so a transitional
    // literal that carries loopback such as ::127.0.0.1 is gated the same way.
    if (address.isLoopbackAddress()) {
      return allowInsecure;
    }
    InetAddress host = reduceEmbeddedIpv4(address);
    if (host.isLoopbackAddress()) {
      return allowInsecure;
    }
    return !isForbiddenHost(host);
  }

  /**
   * Whether a resolved host is one a platform supplied URL must not reach, so a
   * key set or registration fetch cannot land on an internal service or a cloud
   * metadata endpoint. Loopback stays reachable for the local development
   * platform. Several IPv6 notations embed an IPv4, so such a host is reduced to
   * the address it carries and judged by the same rules.
   */
  @VisibleForTesting
  static boolean isForbiddenHost(InetAddress address) throws IOException {
    if (address.isLoopbackAddress()) {
      return false;
    }
    address = reduceEmbeddedIpv4(address);
    byte[] raw = address.getAddress();
    if (address.isLinkLocalAddress() || address.isAnyLocalAddress()
        || address.isMulticastAddress() || address.isSiteLocalAddress()) {
      return true;
    }
    if (raw.length == 16 && (raw[0] & 0xff) == 0x00 && (raw[1] & 0xff) == 0x64
        && (raw[2] & 0xff) == 0xff && (raw[3] & 0xff) == 0x9b
        && (raw[4] & 0xff) == 0x00 && (raw[5] & 0xff) == 0x01) {
      return true;   // RFC 8215 local use NAT64 64:ff9b:1::/48, not globally routable
    }
    if (raw.length == 4) {
      return (raw[0] & 0xff) == 100 && (raw[1] & 0xc0) == 0x40;   // carrier grade NAT
    }
    return (raw[0] & 0xfe) == 0xfc;   // unique local
  }

  /** Reduces a transitional IPv6 literal to the IPv4 it embeds, or returns it unchanged. */
  private static InetAddress reduceEmbeddedIpv4(InetAddress address) throws IOException {
    byte[] raw = address.getAddress();
    if (raw.length == 16) {
      byte[] embedded = embeddedIpv4(raw);
      if (embedded != null) {
        return InetAddress.getByAddress(embedded);
      }
    }
    return address;
  }

  /**
   * The IPv4 that a transitional IPv6 literal carries, or null for an ordinary
   * address. Covers the IPv4-compatible, 6to4, and NAT64 well known forms. The
   * IPv4-mapped form does not appear here because the runtime resolves it to a
   * four byte address.
   */
  private static byte[] embeddedIpv4(byte[] raw) {
    if (allZero(raw, 0, 12)) {
      return new byte[] {raw[12], raw[13], raw[14], raw[15]};
    }
    if ((raw[0] & 0xff) == 0x20 && (raw[1] & 0xff) == 0x02) {
      return new byte[] {raw[2], raw[3], raw[4], raw[5]};
    }
    if ((raw[0] & 0xff) == 0x00 && (raw[1] & 0xff) == 0x64 && (raw[2] & 0xff) == 0xff
        && (raw[3] & 0xff) == 0x9b && allZero(raw, 4, 12)) {
      return new byte[] {raw[12], raw[13], raw[14], raw[15]};
    }
    return null;
  }

  private static boolean allZero(byte[] raw, int from, int to) {
    for (int i = from; i < to; i++) {
      if (raw[i] != 0) {
        return false;
      }
    }
    return true;
  }

  private static String readBody(HttpURLConnection conn) throws IOException {
    int status = conn.getResponseCode();
    InputStream stream = isSuccessStatus(status) ? conn.getInputStream() : conn.getErrorStream();
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    if (stream != null) {
      try (InputStream in = stream) {
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) != -1) {
          bytes.write(buffer, 0, n);
          if (bytes.size() > MAX_BODY_BYTES) {
            throw new IOException("Response body over " + MAX_BODY_BYTES + " bytes");
          }
        }
      }
    }
    String body = bytes.toString(StandardCharsets.UTF_8);
    if (!isSuccessStatus(status)) {
      // Do not carry a large or third party response body into the log in full.
      String detail = body.length() > 500 ? body.substring(0, 500) : body;
      throw new IOException("HTTP " + status + " from " + conn.getURL() + ": " + detail);
    }
    return body;
  }

  /** Whether an HTTP status is a success, so a redirect or an error is not read as a body. */
  @VisibleForTesting
  static boolean isSuccessStatus(int status) {
    return status >= 200 && status < 300;
  }
}
