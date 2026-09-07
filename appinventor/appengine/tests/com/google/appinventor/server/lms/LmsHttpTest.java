// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

import junit.framework.TestCase;

/**
 * Tests for {@link LmsHttp}. The pure {@code jsonField} parser and the
 * {@code proxy()} selection are unit tested here; the networked methods are
 * exercised by the live handshake.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LmsHttpTest extends TestCase {

  public void testJsonFieldReturnsValue() throws Exception {
    assertEquals("ya29.abc",
        LmsHttp.jsonField("{\"access_token\":\"ya29.abc\",\"expires_in\":3599}", "access_token"));
  }

  public void testJsonFieldAbsentReturnsNull() throws Exception {
    assertNull(LmsHttp.jsonField("{\"access_token\":\"ya29.abc\"}", "refresh_token"));
  }

  public void testJsonFieldEmptyReturnsNull() throws Exception {
    // optString yields "" for a present-but-empty field; jsonField normalizes that to null.
    assertNull(LmsHttp.jsonField("{\"id\":\"\"}", "id"));
  }

  public void testJsonFieldMalformedThrows() {
    try {
      LmsHttp.jsonField("not json", "id");
      fail();
    } catch (IOException e) {
      // expected
    }
  }

  public void testProxyDefaultsToNoProxyWhenHostUnset() {
    System.clearProperty("lms.proxy.host");
    assertEquals(Proxy.NO_PROXY, LmsHttp.proxy());
  }

  public void testProxyUsesSocksWhenConfigured() {
    System.setProperty("lms.proxy.host", "127.0.0.1");
    System.setProperty("lms.proxy.port", "33211");
    System.setProperty("lms.proxy.type", "socks");
    try {
      Proxy p = LmsHttp.proxy();
      assertEquals(Proxy.Type.SOCKS, p.type());
      InetSocketAddress address = (InetSocketAddress) p.address();
      assertEquals("127.0.0.1", address.getHostString());
      assertEquals(33211, address.getPort());
    } finally {
      clearProxyProps();
    }
  }

  public void testProxyDefaultsToHttpTypeWhenTypeUnset() {
    System.setProperty("lms.proxy.host", "127.0.0.1");
    System.setProperty("lms.proxy.port", "8080");
    System.clearProperty("lms.proxy.type");
    try {
      assertEquals(Proxy.Type.HTTP, LmsHttp.proxy().type());
    } finally {
      clearProxyProps();
    }
  }

  private static void clearProxyProps() {
    System.clearProperty("lms.proxy.host");
    System.clearProperty("lms.proxy.port");
    System.clearProperty("lms.proxy.type");
  }
}
