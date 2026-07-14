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

import org.json.JSONArray;
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
    for (String bad : new String[] {"a.b", "a.b.c.d", "a.b.c."}) {
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

  /** A token with a critical header parameter the tool does not process is rejected. */
  public void testCritHeaderIsRejected() throws Exception {
    JSONObject header = new JSONObject().put("alg", "RS256").put("kid", "test-kid")
        .put("crit", new JSONArray().put("x-unknown")).put("x-unknown", 1);
    JSONObject payload = new JSONObject().put("sub", "student-1");
    String jwt = LtiJwt.sign(header, payload, keyPair.getPrivate());
    try {
      LtiJwt.verify(jwt, jwks);
      fail("expected a token with a crit header to be rejected");
    } catch (Exception expected) {
      // expected
    }
  }

  /** A JWK for the right key id but marked for encryption does not verify a launch. */
  public void testJwkMarkedForEncryptionIsRejected() throws Exception {
    JSONObject header =
        new JSONObject().put("alg", "RS256").put("typ", "JWT").put("kid", "test-kid");
    JSONObject payload = new JSONObject().put("sub", "student-1");
    String jwt = LtiJwt.sign(header, payload, keyPair.getPrivate());
    // The same key material, published use "enc", must not be selected to verify a
    // signature (RFC 7517).
    JSONObject encJwks = new JSONObject(jwks);
    encJwks.getJSONArray("keys").getJSONObject(0).put("use", "enc");
    try {
      LtiJwt.verify(jwt, encJwks.toString());
      fail("expected a key marked for encryption to be rejected");
    } catch (Exception expected) {
      // expected
    }
  }

  /** A JWK whose key_ops is present but not an array is malformed and must not be used. */
  public void testJwkWithMalformedKeyOpsIsRejected() throws Exception {
    JSONObject header =
        new JSONObject().put("alg", "RS256").put("typ", "JWT").put("kid", "test-kid");
    JSONObject payload = new JSONObject().put("sub", "student-1");
    String jwt = LtiJwt.sign(header, payload, keyPair.getPrivate());
    // key_ops must be an array (RFC 7517 4.3). A present but malformed value, here a bare string,
    // must not be treated as unrestricted, so the key is not selected to verify.
    JSONObject badJwks = new JSONObject(jwks);
    badJwks.getJSONArray("keys").getJSONObject(0).put("key_ops", "verify");
    try {
      LtiJwt.verify(jwt, badJwks.toString());
      fail("expected a key with malformed key_ops to be rejected");
    } catch (Exception expected) {
      // expected
    }
  }

  /** A JWK whose key_ops is a well formed array that includes verify stays usable. */
  public void testJwkWithKeyOpsVerifyIsAccepted() throws Exception {
    JSONObject header =
        new JSONObject().put("alg", "RS256").put("typ", "JWT").put("kid", "test-kid");
    JSONObject payload = new JSONObject().put("sub", "student-1");
    String jwt = LtiJwt.sign(header, payload, keyPair.getPrivate());
    // A well formed key_ops that includes "verify" leaves the key usable for verification.
    JSONObject okJwks = new JSONObject(jwks);
    okJwks.getJSONArray("keys").getJSONObject(0).put("key_ops", new JSONArray().put("verify"));
    JSONObject claims = LtiJwt.verify(jwt, okJwks.toString());
    assertEquals("student-1", claims.getString("sub"));
  }

  /** A 1024 bit RSA key is refused for RS256 verification (RFC 7518 requires 2048 or more). */
  public void testUndersizedRsaKeyIsRejected() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(1024);
    KeyPair weak = gen.generateKeyPair();
    String weakJwks = LtiJwt.publicJwks((RSAPublicKey) weak.getPublic(), "test-kid");
    JSONObject header = new JSONObject().put("alg", "RS256").put("kid", "test-kid");
    JSONObject payload = new JSONObject().put("sub", "student-1");
    String jwt = LtiJwt.sign(header, payload, weak.getPrivate());
    try {
      LtiJwt.verify(jwt, weakJwks);
      fail("expected an undersized RSA key to be rejected");
    } catch (Exception expected) {
      // expected
    }
  }
}
