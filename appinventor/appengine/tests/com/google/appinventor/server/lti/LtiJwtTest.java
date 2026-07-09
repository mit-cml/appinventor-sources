// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

import junit.framework.TestCase;

import org.json.JSONObject;

/**
 * Tests the LTI JWT crypto without a live platform. It runs a sign and verify
 * round trip against a generated JWKS, and rejects tampered or wrong key tokens.
 * This proves the make or break piece, validating the platform id_token, before
 * the live Moodle test.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiJwtTest extends TestCase {

  private KeyPair keyPair;
  private String jwks;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    keyPair = gen.generateKeyPair();
    jwks = LtiJwt.publicJwks((RSAPublicKey) keyPair.getPublic(), "test-kid");
  }

  /** The published JWK set carries the RSA modulus, exponent, and key id. */
  public void testJwksHasModulusAndExponent() {
    JSONObject set = new JSONObject(jwks);
    JSONObject key = set.getJSONArray("keys").getJSONObject(0);
    assertEquals("RSA", key.getString("kty"));
    assertEquals("test-kid", key.getString("kid"));
    assertTrue(key.getString("n").length() > 100);
    assertFalse(key.getString("e").isEmpty());
  }

  /** A token the tool signs verifies against the matching JWKS and keeps its claims. */
  public void testSignThenVerifyRoundTrip() throws Exception {
    JSONObject header =
        new JSONObject().put("alg", "RS256").put("typ", "JWT").put("kid", "test-kid");
    JSONObject payload = new JSONObject()
        .put("iss", "http://localhost:8081")
        .put("aud", "client-123")
        .put("nonce", "nonce-abc")
        .put("sub", "student-1")
        .put("email", "student@example.com");
    String jwt = LtiJwt.sign(header, payload, keyPair.getPrivate());

    JSONObject claims = LtiJwt.verify(jwt, jwks);
    assertEquals("http://localhost:8081", claims.getString("iss"));
    assertEquals("nonce-abc", claims.getString("nonce"));
    assertEquals("student@example.com", claims.getString("email"));
  }

  /** A token whose signature bytes were altered is rejected. */
  public void testTamperedTokenIsRejected() throws Exception {
    JSONObject header = new JSONObject().put("alg", "RS256").put("kid", "test-kid");
    JSONObject payload = new JSONObject().put("sub", "student-1");
    String jwt = LtiJwt.sign(header, payload, keyPair.getPrivate());
    // Flip the last few signature characters.
    String tampered = jwt.substring(0, jwt.length() - 4) + "AAAA";
    try {
      LtiJwt.verify(tampered, jwks);
      fail("expected a tampered token to be rejected");
    } catch (Exception expected) {
      // expected
    }
  }

  /** A token signed by a key that is not in the JWKS is rejected. */
  public void testTokenSignedByDifferentKeyIsRejected() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair other = gen.generateKeyPair();
    JSONObject header = new JSONObject().put("alg", "RS256").put("kid", "test-kid");
    JSONObject payload = new JSONObject().put("sub", "student-1");
    String jwt = LtiJwt.sign(header, payload, other.getPrivate());
    try {
      LtiJwt.verify(jwt, jwks);
      fail("expected a token signed by a different key to be rejected");
    } catch (Exception expected) {
      // expected
    }
  }

  /** A token that claims a non RS256 algorithm is rejected, blocking algorithm confusion. */
  public void testNonRs256TokenIsRejected() throws Exception {
    String header = LtiJwt.b64u(new JSONObject().put("alg", "HS256").put("kid", "test-kid")
        .toString().getBytes(StandardCharsets.UTF_8));
    String payload = LtiJwt.b64u(new JSONObject().put("sub", "student-1")
        .toString().getBytes(StandardCharsets.UTF_8));
    try {
      LtiJwt.verify(header + "." + payload + ".AAAA", jwks);
      fail("expected a non RS256 token to be rejected");
    } catch (Exception expected) {
      // expected
    }
  }

  /** A token with no key id is rejected. */
  public void testTokenWithoutKidIsRejected() throws Exception {
    JSONObject header = new JSONObject().put("alg", "RS256");
    JSONObject payload = new JSONObject().put("sub", "student-1");
    String jwt = LtiJwt.sign(header, payload, keyPair.getPrivate());
    try {
      LtiJwt.verify(jwt, jwks);
      fail("expected a token with no key id to be rejected");
    } catch (Exception expected) {
      // expected
    }
  }

  /** A token that is not three dot separated parts is rejected before any crypto. */
  public void testMalformedTokenShapeIsRejected() throws Exception {
    for (String bad : new String[] {"a.b", "a.b.c.d"}) {
      try {
        LtiJwt.verify(bad, jwks);
        fail("expected a malformed token to be rejected: " + bad);
      } catch (Exception expected) {
        // expected
      }
    }
  }

  /** A token whose key id is not published in the JWKS is rejected at key lookup. */
  public void testTokenWithUnknownKidIsRejected() throws Exception {
    JSONObject header = new JSONObject().put("alg", "RS256").put("kid", "unpublished-kid");
    JSONObject payload = new JSONObject().put("sub", "student-1");
    String jwt = LtiJwt.sign(header, payload, keyPair.getPrivate());
    try {
      LtiJwt.verify(jwt, jwks);
      fail("expected a token with an unknown key id to be rejected");
    } catch (Exception expected) {
      // expected
    }
  }
}
