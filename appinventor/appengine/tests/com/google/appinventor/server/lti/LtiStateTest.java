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
    assertEquals("opaque-data", first.data);
    assertEquals("deployment-1", first.deploymentId);
    assertEquals("http://platform.example.org", first.issuer);
    assertEquals("teacher-42", first.teacherUserId);

    // Spent once, so a replay of the same token, and an unknown or null token, find nothing.
    assertNull(LtiState.consumeDeepLink(token));
    assertNull(LtiState.consumeDeepLink("no-such-token"));
    assertNull(LtiState.consumeDeepLink(null));
  }

  /**
   * The cap keeps the maps bounded under a flood of login initiations. Over the cap the oldest
   * entries are dropped down to evictTo, and a freshly created entry (far from the oldest)
   * survives; under the cap nothing is removed.
   */
  public void testCapOldestEvictsOldestDownToEvictTo() {
    java.util.Map<String, Long> map = new java.util.concurrent.ConcurrentHashMap<>();
    // Timestamps equal the age rank: k1 is oldest, k5 is newest.
    for (long i = 1; i <= 5; i++) {
      map.put("k" + i, i);
    }
    // Cap 3, evict down to 2: over the cap, so the three oldest go and the two newest remain.
    LtiState.capOldest(map, v -> v, 3, 2);
    assertEquals(2, map.size());
    assertTrue(map.containsKey("k4"));
    assertTrue(map.containsKey("k5"));
    assertFalse(map.containsKey("k1"));
    assertFalse(map.containsKey("k2"));
    assertFalse(map.containsKey("k3"));
    // Under the cap it is a no op.
    LtiState.capOldest(map, v -> v, 10, 5);
    assertEquals(2, map.size());
  }
}
