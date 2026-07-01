// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

import junit.framework.TestCase;

import org.json.JSONObject;

/**
 * Tests the LTI JWT crypto without a live platform: a sign and verify round trip
 * against a generated JWKS, and rejection of tampered or wrong key tokens. This
 * proves the make or break piece, validating the platform id_token, before the
 * live Moodle test.
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

  public void testJwksHasModulusAndExponent() {
    JSONObject set = new JSONObject(jwks);
    JSONObject key = set.getJSONArray("keys").getJSONObject(0);
    assertEquals("RSA", key.getString("kty"));
    assertEquals("test-kid", key.getString("kid"));
    assertTrue(key.getString("n").length() > 100);
    assertFalse(key.getString("e").isEmpty());
  }

  public void testSignThenVerifyRoundTrip() throws Exception {
    JSONObject header = new JSONObject().put("alg", "RS256").put("typ", "JWT").put("kid", "test-kid");
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
}
