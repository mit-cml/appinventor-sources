// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import junit.framework.TestCase;

/**
 * Tests for {@link Pkce}.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class PkceTest extends TestCase {

  public void testCodeChallengeMatchesRfc7636Vector() {
    // The worked S256 example from RFC 7636 Appendix B: a fixed verifier and the
    // exact challenge it must produce. This pins the SHA-256 + URL-safe Base64.
    assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
        Pkce.codeChallenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
  }

  public void testNewCodeVerifierIsUrlSafeAndLongEnough() {
    String verifier = Pkce.newCodeVerifier();
    // RFC 7636 section 4.1 allows 43 to 128 characters from the unreserved set.
    assertTrue("unexpected length " + verifier.length(),
        verifier.length() >= 43 && verifier.length() <= 128);
    assertTrue(verifier.matches("[A-Za-z0-9_-]+"));
  }

  public void testNewCodeVerifierIsDistinctEachCall() {
    assertFalse(Pkce.newCodeVerifier().equals(Pkce.newCodeVerifier()));
  }

  public void testCodeChallengeIsUrlSafeWithoutPadding() {
    // S256 of any verifier is a 32-byte digest, 43 URL-safe Base64 characters with
    // no '=' padding and no '+' or '/'.
    String challenge = Pkce.codeChallenge(Pkce.newCodeVerifier());
    assertEquals(43, challenge.length());
    assertTrue(challenge.matches("[A-Za-z0-9_-]+"));
  }
}
