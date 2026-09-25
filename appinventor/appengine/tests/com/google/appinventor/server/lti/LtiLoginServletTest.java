// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import junit.framework.TestCase;

/**
 * Tests the login initiation helper that decides which client_id goes into the authorization
 * request. A platform supplied client_id is honored only when it matches the one registered
 * for the issuer, so an arbitrary caller supplied value is never reflected blindly.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiLoginServletTest extends TestCase {

  /** With no client_id supplied, the registered one is used. */
  public void testResolveClientIdDefaultsToRegistered() {
    assertEquals("client-123", LtiLoginServlet.resolveClientId(null, "client-123"));
    assertEquals("client-123", LtiLoginServlet.resolveClientId("", "client-123"));
  }

  /** A supplied client_id that matches the registered one is accepted. */
  public void testResolveClientIdAcceptsMatching() {
    assertEquals("client-123", LtiLoginServlet.resolveClientId("client-123", "client-123"));
  }

  /** A supplied client_id that does not match the registered one is rejected. */
  public void testResolveClientIdRejectsMismatch() {
    assertNull(LtiLoginServlet.resolveClientId("attacker-999", "client-123"));
  }
}
