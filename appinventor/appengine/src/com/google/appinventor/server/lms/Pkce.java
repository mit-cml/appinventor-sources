// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Proof Key for Code Exchange (PKCE, RFC 7636) helpers for the Google Classroom
 * OAuth 2.0 flow.
 *
 * <p>The connect servlet generates a random {@link #newCodeVerifier() verifier},
 * seals it into the OAuth state, and sends its {@link #codeChallenge(String)
 * S256 challenge} to Google. The callback recovers the verifier from the state
 * and presents it on the token exchange. Because the verifier is carried only
 * inside the encrypted, tamper-evident state, an attacker who replays a captured
 * state cannot pair it with an authorization code minted for a different
 * transaction: the code's challenge would not match the sealed verifier. This
 * closes the authorization-code-injection gap that a session-less callback would
 * otherwise have (RFC 9700 section 2.1).
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class Pkce {

  /**
   * Number of random bytes in a verifier. URL-safe Base64 of 32 bytes is 43
   * characters, the minimum length RFC 7636 section 4.1 allows (43 to 128).
   */
  private static final int VERIFIER_BYTES = 32;

  private static final SecureRandom RANDOM = new SecureRandom();

  private Pkce() {}

  /**
   * Returns a new high-entropy code verifier, a URL-safe string suitable for
   * both the {@code code_verifier} parameter and storage inside the state.
   *
   * @return a fresh PKCE code verifier
   */
  static String newCodeVerifier() {
    byte[] bytes = new byte[VERIFIER_BYTES];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  /**
   * Returns the S256 code challenge for {@code codeVerifier}, that is the
   * URL-safe Base64 of its SHA-256 digest (RFC 7636 section 4.2).
   *
   * @param codeVerifier a code verifier from {@link #newCodeVerifier()}
   * @return the S256 code challenge to send to the authorization endpoint
   */
  static String codeChallenge(String codeVerifier) {
    byte[] digest;
    try {
      digest = MessageDigest.getInstance("SHA-256")
          .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
    } catch (NoSuchAlgorithmException e) {
      // Every conforming JVM ships SHA-256, so this cannot happen.
      throw new IllegalStateException("SHA-256 is not available", e);
    }
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
  }
}
