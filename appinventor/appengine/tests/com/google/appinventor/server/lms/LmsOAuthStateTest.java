// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.encryption.KeyczarEncryptor;

import junit.framework.TestCase;

/**
 * Tests for {@link LmsOAuthState}. This state helper only uses the symmetric
 * encryption key, not the datastore, so the test extends {@link TestCase}
 * directly rather than the heavier datastore test base.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LmsOAuthStateTest extends TestCase {

  private static final String KEYSTORE_ROOT_PATH =
      TestUtils.APP_INVENTOR_ROOT_DIR + "/appengine/build/war/";  // must end with a slash

  private static final String USER_ID = "1234-user-uuid";
  private static final String VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";

  // A fixed reference time so the time-to-live tests do not depend on the clock.
  private static final long ISSUED_AT = 1_000_000_000_000L;
  private static final long ONE_MINUTE = 60 * 1000L;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    KeyczarEncryptor.rootPath.setForTest(KEYSTORE_ROOT_PATH);
  }

  public void testRoundTripReturnsUserIdAndVerifier() throws Exception {
    String state = LmsOAuthState.create(USER_ID, VERIFIER);
    LmsOAuthState.Payload payload = LmsOAuthState.verify(state);
    assertNotNull(payload);
    assertEquals(USER_ID, payload.userId());
    assertEquals(VERIFIER, payload.codeVerifier());
  }

  public void testFreshStateWithinTtlAccepted() throws Exception {
    String state = LmsOAuthState.create(USER_ID, VERIFIER, ISSUED_AT);
    LmsOAuthState.Payload payload = LmsOAuthState.verify(state, ISSUED_AT + 9 * ONE_MINUTE);
    assertNotNull(payload);
    assertEquals(USER_ID, payload.userId());
  }

  public void testExpiredStateRejected() throws Exception {
    String state = LmsOAuthState.create(USER_ID, VERIFIER, ISSUED_AT);
    assertNull(LmsOAuthState.verify(state, ISSUED_AT + 11 * ONE_MINUTE));
  }

  public void testTamperedStateRejected() throws Exception {
    String state = LmsOAuthState.create(USER_ID, VERIFIER);
    char[] chars = state.toCharArray();
    int mid = chars.length / 2;
    chars[mid] = (chars[mid] == 'A') ? 'B' : 'A';  // flip one ciphertext character
    assertNull(LmsOAuthState.verify(new String(chars)));
  }

  public void testGarbageStateReturnsNull() {
    assertNull(LmsOAuthState.verify("not a valid state"));
    assertNull(LmsOAuthState.verify(""));
    assertNull(LmsOAuthState.verify(null));
  }

  public void testEmptyUserIdRejected() {
    try {
      LmsOAuthState.create("", VERIFIER);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    } catch (Exception e) {
      fail("expected IllegalArgumentException, got " + e);
    }
  }

  public void testEmptyVerifierRejected() {
    try {
      LmsOAuthState.create(USER_ID, "");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    } catch (Exception e) {
      fail("expected IllegalArgumentException, got " + e);
    }
  }

  public void testDistinctStatesForSameInputs() throws Exception {
    // Even with identical inputs the ciphertext differs, because the encryption
    // uses a random initialization vector; two flows never produce the same state.
    String first = LmsOAuthState.create(USER_ID, VERIFIER);
    String second = LmsOAuthState.create(USER_ID, VERIFIER);
    assertFalse(first.equals(second));
  }

  public void testUserIdWithSpaceRoundTrips() throws Exception {
    // The user id is the unambiguous remainder of the payload, so even a value
    // containing the separator survives the round trip.
    String userIdWithSpace = "user id with spaces";
    String state = LmsOAuthState.create(userIdWithSpace, VERIFIER);
    LmsOAuthState.Payload payload = LmsOAuthState.verify(state);
    assertNotNull(payload);
    assertEquals(userIdWithSpace, payload.userId());
    assertEquals(VERIFIER, payload.codeVerifier());
  }

  public void testStateAtExactTtlBoundaryAccepted() throws Exception {
    // age == TTL is still valid; the check rejects only age > TTL.
    String state = LmsOAuthState.create(USER_ID, VERIFIER, ISSUED_AT);
    assertNotNull(LmsOAuthState.verify(state, ISSUED_AT + 10 * ONE_MINUTE));
  }

  public void testFutureDatedStateRejected() throws Exception {
    // A state dated further in the future than the clock-skew tolerance is rejected.
    String state = LmsOAuthState.create(USER_ID, VERIFIER, ISSUED_AT);
    assertNull(LmsOAuthState.verify(state, ISSUED_AT - ONE_MINUTE));  // 60s exceeds the tolerance
  }

  public void testStateWithinClockSkewToleranceAccepted() throws Exception {
    // A state dated slightly in the future, as cross-instance clock skew can cause, is accepted.
    String state = LmsOAuthState.create(USER_ID, VERIFIER, ISSUED_AT);
    assertNotNull(LmsOAuthState.verify(state, ISSUED_AT - 5000));  // 5s of skew, within tolerance
  }
}
