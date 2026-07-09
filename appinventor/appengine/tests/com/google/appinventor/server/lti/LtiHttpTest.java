// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import java.net.InetAddress;

import junit.framework.TestCase;

/**
 * Tests the address filter that keeps an LTI fetch from reaching an internal or
 * metadata host while leaving loopback reachable for a local platform.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiHttpTest extends TestCase {

  /** Loopback stays reachable for a local development platform. */
  public void testLoopbackIsAllowed() throws Exception {
    assertFalse(LtiHttp.isForbiddenHost(InetAddress.getByName("127.0.0.1")));
    assertFalse(LtiHttp.isForbiddenHost(InetAddress.getByName("::1")));
  }

  /** A public host is reachable. */
  public void testPublicHostIsAllowed() throws Exception {
    assertFalse(LtiHttp.isForbiddenHost(InetAddress.getByName("8.8.8.8")));
    assertFalse(LtiHttp.isForbiddenHost(InetAddress.getByName("2001:4860:4860::8888")));
  }

  /** Private, link local, and wildcard hosts are refused. */
  public void testPrivateAndLinkLocalAreRefused() throws Exception {
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("10.0.0.1")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("192.168.1.1")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("169.254.169.254")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("0.0.0.0")));
  }

  /** Carrier grade NAT and IPv6 unique local space are refused. */
  public void testCarrierGradeNatAndUniqueLocalAreRefused() throws Exception {
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("100.100.100.200")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("fc00::1")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("fd00::1")));
  }

  /** An IPv6 host that embeds a private IPv4 target is refused whichever notation carries it. */
  public void testEmbeddedIpv4IsRefused() throws Exception {
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("::169.254.169.254")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("::100.64.0.1")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("::ffff:10.0.0.1")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("2002:a9fe:a9fe::1")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("2002:c0a8:0101::1")));
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("64:ff9b::a9fe:a9fe")));
    // RFC 8215 local use NAT64 prefix is not globally routable, so refuse it.
    assertTrue(LtiHttp.isForbiddenHost(InetAddress.getByName("64:ff9b:1::a9fe:a9fe")));
  }

  /** A 6to4 host that carries a public IPv4 stays reachable. */
  public void testTransitionalIpv6OfPublicIsAllowed() throws Exception {
    assertFalse(LtiHttp.isForbiddenHost(InetAddress.getByName("2002:0808:0808::1")));
  }

  /** Only a 2xx response is a success, so a redirect the tool does not follow is a failure. */
  public void testOnlyTwoHundredsAreSuccess() {
    assertTrue(LtiHttp.isSuccessStatus(200));
    assertTrue(LtiHttp.isSuccessStatus(201));
    assertTrue(LtiHttp.isSuccessStatus(299));
    assertFalse(LtiHttp.isSuccessStatus(302));
    assertFalse(LtiHttp.isSuccessStatus(304));
    assertFalse(LtiHttp.isSuccessStatus(400));
    assertFalse(LtiHttp.isSuccessStatus(500));
  }

  /** Loopback is fetchable only in development, a public host always, a private host never. */
  public void testHostAllowedForFetch() throws Exception {
    assertTrue(LtiHttp.hostAllowedForFetch(InetAddress.getByName("127.0.0.1"), true));
    assertFalse(LtiHttp.hostAllowedForFetch(InetAddress.getByName("127.0.0.1"), false));
    assertTrue(LtiHttp.hostAllowedForFetch(InetAddress.getByName("8.8.8.8"), false));
    assertFalse(LtiHttp.hostAllowedForFetch(InetAddress.getByName("10.0.0.1"), true));
    assertFalse(LtiHttp.hostAllowedForFetch(InetAddress.getByName("169.254.169.254"), true));
  }

  /** An IPv6 literal that embeds loopback is gated by the dev flag, not reachable in production. */
  public void testEmbeddedLoopbackIsDevGated() throws Exception {
    for (String host : new String[] {"::127.0.0.1", "2002:7f00:1::", "64:ff9b::7f00:1"}) {
      assertFalse("production must refuse " + host,
          LtiHttp.hostAllowedForFetch(InetAddress.getByName(host), false));
      assertTrue("development may reach " + host,
          LtiHttp.hostAllowedForFetch(InetAddress.getByName(host), true));
    }
  }

  /** https is always allowed, plain http only in development. */
  public void testTransportAllowed() {
    assertTrue(LtiHttp.transportAllowed("https", false));
    assertTrue(LtiHttp.transportAllowed("http", true));
    assertFalse(LtiHttp.transportAllowed("http", false));
    assertFalse(LtiHttp.transportAllowed("ftp", true));
  }
}
