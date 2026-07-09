// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import junit.framework.TestCase;

/**
 * Tests that an in flight launch state is spent exactly once, and that peeking at
 * it to validate the token first does not spend it, so a transient failure before
 * validation leaves the state for the honest platform to retry.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiStateTest extends TestCase {

  /** Peek reads the state without spending it, consume spends it once. */
  public void testPeekDoesNotSpendButConsumeDoes() {
    String[] made = LtiState.create("http://platform.example.org");
    String state = made[0];
    String nonce = made[1];

    LtiState.Entry peeked = LtiState.peek(state);
    assertNotNull(peeked);
    assertEquals(nonce, peeked.nonce);
    // A second peek still finds it, so a failed validation does not burn it.
    assertNotNull(LtiState.peek(state));

    // Consume spends it, and it cannot be read or spent again.
    assertNotNull(LtiState.consume(state));
    assertNull(LtiState.peek(state));
    assertNull(LtiState.consume(state));
  }

  /** An unknown or null state is not found by either peek or consume. */
  public void testUnknownStateIsNull() {
    assertNull(LtiState.peek("no-such-state"));
    assertNull(LtiState.peek(null));
    assertNull(LtiState.consume("no-such-state"));
  }

  /** A Deep Linking token returns its saved context once, then never again. */
  public void testDeepLinkTokenIsConsumedOnce() {
    String token = LtiState.createDeepLink("https://platform.example.org/return", "opaque-data",
        "deployment-1", "http://platform.example.org", "teacher-42");

    LtiState.DeepLink first = LtiState.consumeDeepLink(token);
    assertNotNull(first);
    assertEquals("https://platform.example.org/return", first.returnUrl);
    assertEquals("http://platform.example.org", first.issuer);
    assertEquals("teacher-42", first.teacherUserId);

    // Spent once, so a replay of the same token, and an unknown or null token, find nothing.
    assertNull(LtiState.consumeDeepLink(token));
    assertNull(LtiState.consumeDeepLink("no-such-token"));
    assertNull(LtiState.consumeDeepLink(null));
  }
}
