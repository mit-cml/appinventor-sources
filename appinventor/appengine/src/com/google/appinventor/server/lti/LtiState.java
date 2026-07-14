// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.common.annotations.VisibleForTesting;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * Server side store for the short lived context of an in flight LTI exchange.
 * Holds the OIDC state and nonce of a launch, and the return context of a Deep
 * Linking selection, both consumed exactly once. Kept in memory rather than in
 * a cookie because these flows arrive as cross site form posts, where a
 * SameSite cookie set earlier would not be returned. Single instance dev spike
 * only.
 *
 * <p>The state is single use but not yet bound to the browser that began the
 * login, so a captured state and token could be replayed into another browser.
 * Binding the state to a SameSite None cookie closes this and is tracked as a
 * known limitation before any production use.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiState {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int TOKEN_BYTES = 24;
  private static final long TTL_MILLIS = 10 * 60 * 1000L;
  private static final long DEEP_LINK_TTL_MILLIS = 30 * 60 * 1000L;
  // Bound the in memory maps. The state map is filled by unauthenticated login initiations,
  // which create entries faster than the TTL sweep removes them, so without a cap a flood
  // grows memory without limit. When the cap is exceeded the oldest entries are dropped down
  // to EVICT_TO (an in flight login just created its entry and is far from the oldest), which
  // degrades gracefully under a flood instead of exhausting memory.
  private static final int MAX_ENTRIES = 50_000;
  private static final int EVICT_TO = 45_000;
  private static final Map<String, Entry> STORE = new ConcurrentHashMap<>();
  private static final Map<String, DeepLink> DEEP_LINKS = new ConcurrentHashMap<>();

  static final class Entry {
    final String nonce;
    final String issuer;
    private final long ts;

    Entry(String nonce, String issuer, long ts) {
      this.nonce = nonce;
      this.issuer = issuer;
      this.ts = ts;
    }
  }

  /**
   * The platform context of one Deep Linking selection, saved while the teacher
   * is choosing a template, so the picker form only carries an opaque one time
   * token instead of the platform return url.
   */
  static final class DeepLink {
    final String returnUrl;
    final String data;
    final String deploymentId;
    final String issuer;
    final String teacherUserId;
    private final long ts;

    DeepLink(String returnUrl, String data, String deploymentId, String issuer,
        String teacherUserId) {
      this.returnUrl = returnUrl;
      this.data = data;
      this.deploymentId = deploymentId;
      this.issuer = issuer;
      this.teacherUserId = teacherUserId;
      this.ts = System.currentTimeMillis();
    }
  }

  private LtiState() {}

  static String random() {
    byte[] b = new byte[TOKEN_BYTES];
    RANDOM.nextBytes(b);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }

  /** Mints a state and nonce for a platform, stores them, returns {state, nonce}. */
  static String[] create(String issuer) {
    String state = random();
    String nonce = random();
    STORE.put(state, new Entry(nonce, issuer, System.currentTimeMillis()));
    sweep();
    return new String[] {state, nonce};
  }

  /**
   * Returns the entry for a state without spending it, or null if the state is
   * unknown or expired, so the launch can validate the token before the state is
   * consumed and a transient failure does not burn a launch the platform can retry.
   */
  static Entry peek(String state) {
    if (state == null) {
      return null;
    }
    Entry e = STORE.get(state);
    if (e == null || System.currentTimeMillis() - e.ts > TTL_MILLIS) {
      return null;
    }
    return e;
  }

  /**
   * Consumes a state once and returns its entry, holding the nonce and the
   * platform issuer, or null if the state is unknown or expired.
   */
  static Entry consume(String state) {
    if (state == null) {
      return null;
    }
    Entry e = STORE.remove(state);
    if (e == null || System.currentTimeMillis() - e.ts > TTL_MILLIS) {
      return null;
    }
    return e;
  }

  /** Saves a Deep Linking selection context and returns its one time token. */
  static String createDeepLink(String returnUrl, String data, String deploymentId,
      String issuer, String teacherUserId) {
    String token = random();
    DEEP_LINKS.put(token, new DeepLink(returnUrl, data, deploymentId, issuer, teacherUserId));
    sweep();
    return token;
  }

  /**
   * Consumes a Deep Linking token once and returns its context, or null if the
   * token is unknown or expired.
   */
  static DeepLink consumeDeepLink(String token) {
    if (token == null) {
      return null;
    }
    DeepLink dl = DEEP_LINKS.remove(token);
    if (dl == null || System.currentTimeMillis() - dl.ts > DEEP_LINK_TTL_MILLIS) {
      return null;
    }
    return dl;
  }

  private static void sweep() {
    long now = System.currentTimeMillis();
    STORE.entrySet().removeIf(en -> now - en.getValue().ts > TTL_MILLIS);
    DEEP_LINKS.entrySet().removeIf(en -> now - en.getValue().ts > DEEP_LINK_TTL_MILLIS);
    capOldest(STORE, e -> e.ts, MAX_ENTRIES, EVICT_TO);
    capOldest(DEEP_LINKS, d -> d.ts, MAX_ENTRIES, EVICT_TO);
  }

  /**
   * Drops the oldest entries once a map exceeds {@code maxEntries}, down to {@code evictTo}.
   * The hysteresis means the O(n log n) selection runs at most once every
   * {@code maxEntries - evictTo} inserts rather than on every insert once at the cap.
   */
  @VisibleForTesting
  static <V> void capOldest(Map<String, V> map, ToLongFunction<V> tsOf, int maxEntries,
      int evictTo) {
    int size = map.size();
    if (size <= maxEntries) {
      return;
    }
    map.entrySet().stream()
        .sorted(Comparator.comparingLong(en -> tsOf.applyAsLong(en.getValue())))
        .limit(Math.max(0L, (long) size - evictTo))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList())
        .forEach(map::remove);
  }
}
