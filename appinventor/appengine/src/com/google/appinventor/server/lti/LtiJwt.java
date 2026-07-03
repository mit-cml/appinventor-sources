// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JWT and key helpers for the LTI 1.3 tool, implemented with the JDK security
 * APIs and the org.json parser already on the server classpath, so no new
 * dependency is needed. Covers verifying an incoming platform JWT against the
 * platform JWKS, signing a tool JWT for grade passback, and publishing the tool
 * public key as a JWK set.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiJwt {

  private LtiJwt() {}

  static String b64u(byte[] data) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
  }

  static byte[] unb64u(String s) {
    return Base64.getUrlDecoder().decode(s);
  }

  /** SHA-256 digest of a string, for deriving a stable opaque key. */
  static byte[] sha256(String s) throws Exception {
    return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
  }

  /** Lowercase hex of a byte array, a case insensitive stable encoding. */
  static String hex(byte[] data) {
    StringBuilder sb = new StringBuilder(data.length * 2);
    for (byte b : data) {
      sb.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
    }
    return sb.toString();
  }

  /** The RS256 header the tool signs with, naming the published key. */
  static JSONObject rs256Header(String kid) {
    return new JSONObject().put("alg", "RS256").put("typ", "JWT").put("kid", kid);
  }

  static PrivateKey privateKeyFromDer(byte[] der) throws Exception {
    return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
  }

  static RSAPublicKey publicKeyFromDer(byte[] der) throws Exception {
    return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
  }

  /** Builds one JWK object for an RSA public key. */
  static JSONObject jwk(RSAPublicKey pub, String kid) {
    JSONObject jwk = new JSONObject();
    jwk.put("kty", "RSA");
    jwk.put("alg", "RS256");
    jwk.put("use", "sig");
    jwk.put("kid", kid);
    jwk.put("n", b64u(toUnsigned(pub.getModulus())));
    jwk.put("e", b64u(toUnsigned(pub.getPublicExponent())));
    return jwk;
  }

  /** Builds a one key JWK set string, standing in for a platform JWKS in tests. */
  @VisibleForTesting
  static String publicJwks(RSAPublicKey pub, String kid) {
    return new JSONObject().put("keys", new JSONArray().put(jwk(pub, kid))).toString();
  }

  /** Signs a compact RS256 JWT from the given header and payload. */
  static String sign(JSONObject header, JSONObject payload, PrivateKey key) throws Exception {
    String input = b64u(header.toString().getBytes(StandardCharsets.UTF_8)) + "."
        + b64u(payload.toString().getBytes(StandardCharsets.UTF_8));
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(key);
    sig.update(input.getBytes(StandardCharsets.US_ASCII));
    return input + "." + b64u(sig.sign());
  }

  /**
   * Verifies a compact RS256 JWT against a JWKS, returning the claims if the
   * signature, the algorithm, and the key id match. Does not check the time or
   * the claim values, the caller does that.
   */
  static JSONObject verify(String jwt, String jwksJson) throws Exception {
    String[] parts = jwt.split("\\.");
    if (parts.length != 3) {
      throw new IOException("Malformed JWT");
    }
    JSONObject header = new JSONObject(new String(unb64u(parts[0]), StandardCharsets.UTF_8));
    if (!"RS256".equals(header.optString("alg"))) {
      throw new IOException("Unexpected JWT alg: " + header.optString("alg"));
    }
    String kid = header.optString("kid", null);
    if (kid == null || kid.isEmpty()) {
      throw new IOException("JWT header has no kid");
    }
    RSAPublicKey pub = findKey(jwksJson, kid);
    if (pub == null) {
      throw new IOException("No JWKS key for kid " + kid);
    }
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initVerify(pub);
    sig.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
    if (!sig.verify(unb64u(parts[2]))) {
      throw new IOException("JWT signature invalid");
    }
    // Parse the claims only after the signature has been proven good.
    return new JSONObject(new String(unb64u(parts[1]), StandardCharsets.UTF_8));
  }

  private static RSAPublicKey findKey(String jwksJson, String kid) throws Exception {
    JSONArray keys = new JSONObject(jwksJson).optJSONArray("keys");
    if (keys == null) {
      return null;
    }
    for (int i = 0; i < keys.length(); i++) {
      JSONObject k = keys.getJSONObject(i);
      if (!"RSA".equals(k.optString("kty")) || !kid.equals(k.optString("kid"))) {
        continue;
      }
      String n = k.optString("n", "");
      String e = k.optString("e", "");
      if (n.isEmpty() || e.isEmpty()) {
        continue;
      }
      return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(
          new RSAPublicKeySpec(new BigInteger(1, unb64u(n)), new BigInteger(1, unb64u(e))));
    }
    return null;
  }

  /** Returns the unsigned big endian bytes of a BigInteger, without a leading sign byte. */
  private static byte[] toUnsigned(BigInteger v) {
    byte[] b = v.toByteArray();
    if (b.length > 1 && b[0] == 0) {
      byte[] t = new byte[b.length - 1];
      System.arraycopy(b, 1, t, 0, t.length);
      return t;
    }
    return b;
  }
}
