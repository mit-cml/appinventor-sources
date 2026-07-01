// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server side store for the OIDC state and nonce of an in-flight LTI launch.
 * Kept in memory rather than in a cookie because the launch arrives as a cross
 * site form post, where a SameSite cookie set during login initiation would not
 * be returned. Single instance dev spike only.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiState {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final long TTL_MILLIS = 10 * 60 * 1000L;
  private static final Map<String, Entry> STORE = new ConcurrentHashMap<>();

  private static final class Entry {
    final String nonce;
    final long ts;

    Entry(String nonce, long ts) {
      this.nonce = nonce;
      this.ts = ts;
    }
  }

  private LtiState() {}

  static String random() {
    byte[] b = new byte[24];
    RANDOM.nextBytes(b);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }

  /** Mints a state and nonce, stores them, and returns {state, nonce}. */
  static String[] create() {
    String state = random();
    String nonce = random();
    STORE.put(state, new Entry(nonce, System.currentTimeMillis()));
    sweep();
    return new String[] {state, nonce};
  }

  /**
   * Consumes a state once and returns its nonce, or null if the state is
   * unknown or expired.
   */
  static String consumeNonce(String state) {
    if (state == null) {
      return null;
    }
    Entry e = STORE.remove(state);
    if (e == null || System.currentTimeMillis() - e.ts > TTL_MILLIS) {
      return null;
    }
    return e.nonce;
  }

  private static void sweep() {
    long now = System.currentTimeMillis();
    STORE.entrySet().removeIf(en -> now - en.getValue().ts > TTL_MILLIS);
  }
}
