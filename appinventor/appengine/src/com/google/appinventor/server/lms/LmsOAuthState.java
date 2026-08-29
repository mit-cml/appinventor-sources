// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.encryption.EncryptionStrategy;
import com.google.common.annotations.VisibleForTesting;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Mints and verifies the opaque {@code state} value carried through the Google
 * Classroom OAuth redirect.
 *
 * <p>Google's callback arrives without an App Inventor session: it is a
 * cross-origin redirect from Google, so the session cookie cannot be relied on.
 * The id of the user who began the flow, and the PKCE code verifier minted for
 * it, are therefore carried inside this state rather than read from a cookie.
 *
 * <p>The state is the issue timestamp, the PKCE code verifier, and the user id,
 * encrypted with the symmetric {@link EncryptionStrategy#WRITE} key (the same key
 * that protects the stored refresh token). Encryption makes the state both
 * unreadable and tamper evident, so the callback can trust what it recovers, and
 * a short time to live bounds how long a captured state stays usable. The state
 * is not single use, but a replay is harmless: sealing the verifier here binds
 * the authorization code to the transaction that started the flow (see
 * {@link Pkce}), and Google's authorization code is itself single use.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public final class LmsOAuthState {

  /** How long a freshly minted state remains valid. */
  private static final long TTL_MILLIS = TimeUnit.MINUTES.toMillis(10);

  /**
   * Tolerance for clock skew between the instance that mints the state and the one
   * that verifies it; a state dated slightly in the future is still accepted.
   */
  private static final long CLOCK_SKEW_MILLIS = TimeUnit.SECONDS.toMillis(30);

  /**
   * Field separator. A space never occurs in the timestamp (digits) or the
   * URL-safe Base64 code verifier, the two fields that precede the user id. The
   * user id is the unambiguous remainder of the payload, so it may contain any
   * character.
   */
  private static final String SEPARATOR = " ";

  private LmsOAuthState() {}

  /** The data sealed in a state: the user id and the PKCE code verifier. */
  public static final class Payload {
    private final String userId;
    private final String codeVerifier;

    Payload(String userId, String codeVerifier) {
      this.userId = userId;
      this.codeVerifier = codeVerifier;
    }

    public String userId() {
      return userId;
    }

    public String codeVerifier() {
      return codeVerifier;
    }
  }

  /**
   * Mints a state sealing {@code userId} and {@code codeVerifier}, valid for the
   * next ten minutes.
   *
   * @param userId the id of the user beginning the OAuth flow
   * @param codeVerifier the PKCE code verifier minted for this flow
   * @return a URL-safe opaque state string
   * @throws EncryptionException if the state cannot be encrypted
   * @throws IllegalArgumentException if either argument is null or empty
   */
  public static String create(String userId, String codeVerifier) throws EncryptionException {
    return create(userId, codeVerifier, System.currentTimeMillis());
  }

  @VisibleForTesting
  static String create(String userId, String codeVerifier, long issuedAtMillis)
      throws EncryptionException {
    if (userId == null || userId.isEmpty()) {
      throw new IllegalArgumentException("userId must not be null or empty");
    }
    if (codeVerifier == null || codeVerifier.isEmpty()) {
      throw new IllegalArgumentException("codeVerifier must not be null or empty");
    }
    // The user id is placed last so it is the unambiguous remainder on verify; the
    // verifier precedes it and is URL-safe Base64, so it never holds the separator.
    String payload = issuedAtMillis + SEPARATOR + codeVerifier + SEPARATOR + userId;
    byte[] encrypted = EncryptionStrategy.WRITE.encrypt(payload.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
  }

  /**
   * Recovers the {@link Payload} from a state minted by {@link #create}, or
   * returns {@code null} if the state is malformed, tampered with, or expired.
   *
   * @param state a state string produced by {@link #create}
   * @return the sealed payload, or {@code null} if the state is not valid
   */
  public static Payload verify(String state) {
    return verify(state, System.currentTimeMillis());
  }

  @VisibleForTesting
  static Payload verify(String state, long nowMillis) {
    if (state == null || state.isEmpty()) {
      return null;
    }
    String payload;
    try {
      byte[] decrypted = EncryptionStrategy.WRITE.decrypt(Base64.getUrlDecoder().decode(state));
      payload = new String(decrypted, StandardCharsets.UTF_8);
    } catch (EncryptionException | IllegalArgumentException e) {
      // Decryption failure (tampered or wrong key) or invalid Base64.
      return null;
    }
    int firstSep = payload.indexOf(SEPARATOR);
    int secondSep = (firstSep < 0) ? -1 : payload.indexOf(SEPARATOR, firstSep + 1);
    if (firstSep <= 0 || secondSep < 0) {
      return null;
    }
    long issuedAtMillis;
    try {
      issuedAtMillis = Long.parseLong(payload.substring(0, firstSep));
    } catch (NumberFormatException e) {
      return null;
    }
    long age = nowMillis - issuedAtMillis;
    if (age < -CLOCK_SKEW_MILLIS || age > TTL_MILLIS) {
      return null;
    }
    String codeVerifier = payload.substring(firstSep + 1, secondSep);
    String userId = payload.substring(secondSep + 1);
    if (codeVerifier.isEmpty() || userId.isEmpty()) {
      return null;
    }
    return new Payload(userId, codeVerifier);
  }
}
