// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
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

  static PrivateKey loadPrivateKey(String file) throws Exception {
    byte[] der = Files.readAllBytes(Paths.get(file));
    return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
  }

  static RSAPublicKey loadPublicKey(String file) throws Exception {
    byte[] der = Files.readAllBytes(Paths.get(file));
    return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
  }

  /** Builds the JWK set string for the tool public key, served at /lti/jwks. */
  static String publicJwks(RSAPublicKey pub, String kid) {
    JSONObject jwk = new JSONObject();
    jwk.put("kty", "RSA");
    jwk.put("alg", "RS256");
    jwk.put("use", "sig");
    jwk.put("kid", kid);
    jwk.put("n", b64u(toUnsigned(pub.getModulus())));
    jwk.put("e", b64u(toUnsigned(pub.getPublicExponent())));
    return new JSONObject().put("keys", new JSONArray().put(jwk)).toString();
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
    RSAPublicKey pub = findKey(jwksJson, header.optString("kid", null));
    if (pub == null) {
      throw new IOException("No JWKS key for kid " + header.optString("kid", null));
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
      if (!"RSA".equals(k.optString("kty"))) {
        continue;
      }
      if (kid != null && !kid.equals(k.optString("kid"))) {
        continue;
      }
      BigInteger n = new BigInteger(1, unb64u(k.getString("n")));
      BigInteger e = new BigInteger(1, unb64u(k.getString("e")));
      return (RSAPublicKey) KeyFactory.getInstance("RSA")
          .generatePublic(new RSAPublicKeySpec(n, e));
    }
    return null;
  }

  /** Returns the unsigned big-endian bytes of a non-negative BigInteger. */
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
