// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import junit.framework.TestCase;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class AwsSigV4SignerTest extends TestCase {

  private static final String ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
  private static final String SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
  private static final String REGION = "us-east-1";
  private static final String SERVICE = "bedrock";
  private static final String TEST_URL =
      "https://bedrock-runtime.us-east-1.amazonaws.com/model/amazon.titan-text-express-v1/invoke";
  private static final byte[] PAYLOAD = "{\"inputText\":\"Hello\"}".getBytes(StandardCharsets.UTF_8);

  /**
   * Verifies that sign() returns an Authorization header starting with
   * "AWS4-HMAC-SHA256", that X-Amz-Date is present, and that
   * X-Amz-Security-Token is absent when no session token is provided.
   */
  public void testSignReturnsAuthorizationHeader() {
    AwsSigV4Signer signer = new AwsSigV4Signer(ACCESS_KEY, SECRET_KEY, null, REGION, SERVICE);
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Content-Type", "application/json");

    Map<String, String> signed = signer.sign("POST", TEST_URL, headers, PAYLOAD);

    assertTrue("Authorization header should be present", signed.containsKey("Authorization"));
    String auth = signed.get("Authorization");
    assertTrue("Authorization should start with AWS4-HMAC-SHA256",
        auth.startsWith("AWS4-HMAC-SHA256"));

    assertTrue("X-Amz-Date should be present", signed.containsKey("X-Amz-Date"));
    assertFalse("X-Amz-Security-Token should not be present without session token",
        signed.containsKey("X-Amz-Security-Token"));
  }

  /**
   * Verifies that when a session token is supplied, X-Amz-Security-Token
   * is included in the signed headers map.
   */
  public void testSignIncludesSessionToken() {
    String token = "AQoDYXdzEJr//example/session/token==";
    AwsSigV4Signer signer = new AwsSigV4Signer(ACCESS_KEY, SECRET_KEY, token, REGION, SERVICE);
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Content-Type", "application/json");

    Map<String, String> signed = signer.sign("POST", TEST_URL, headers, PAYLOAD);

    assertTrue("X-Amz-Security-Token should be present when session token is provided",
        signed.containsKey("X-Amz-Security-Token"));
    assertEquals("X-Amz-Security-Token value should match the provided token",
        token, signed.get("X-Amz-Security-Token"));
  }

  /**
   * Verifies that two calls with identical inputs (within the same second)
   * produce identical signatures.
   */
  public void testSignatureIsDeterministic() {
    AwsSigV4Signer signer = new AwsSigV4Signer(ACCESS_KEY, SECRET_KEY, null, REGION, SERVICE);
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Content-Type", "application/json");

    Map<String, String> signed1 = signer.sign("POST", TEST_URL, headers, PAYLOAD);
    Map<String, String> signed2 = signer.sign("POST", TEST_URL, headers, PAYLOAD);

    // If both calls happen within the same second the signatures are identical.
    // We verify the structure is consistent regardless.
    assertEquals("X-Amz-Date format should be consistent",
        signed1.get("X-Amz-Date").length(), signed2.get("X-Amz-Date").length());

    // If the timestamps are the same (same second), signatures must match exactly.
    if (signed1.get("X-Amz-Date").equals(signed2.get("X-Amz-Date"))) {
      assertEquals("Signatures should be identical for same inputs within same second",
          signed1.get("Authorization"), signed2.get("Authorization"));
    }
  }

  /**
   * Verifies the structural format of the Authorization header, including
   * all required components and the region/service scope.
   */
  public void testAuthorizationHeaderFormat() {
    AwsSigV4Signer signer = new AwsSigV4Signer(ACCESS_KEY, SECRET_KEY, null, REGION, SERVICE);
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Content-Type", "application/json");

    Map<String, String> signed = signer.sign("POST", TEST_URL, headers, PAYLOAD);

    String auth = signed.get("Authorization");
    assertNotNull("Authorization header must be present", auth);

    assertTrue("Authorization must contain 'Credential='",
        auth.contains("Credential="));
    assertTrue("Authorization must contain 'SignedHeaders='",
        auth.contains("SignedHeaders="));
    assertTrue("Authorization must contain 'Signature='",
        auth.contains("Signature="));

    // The credential scope must embed the region and service
    assertTrue("Authorization must contain the region in the credential scope",
        auth.contains("/" + REGION + "/"));
    assertTrue("Authorization must contain the service in the credential scope",
        auth.contains("/" + SERVICE + "/"));
    assertTrue("Authorization must end with aws4_request scope",
        auth.contains("/aws4_request"));

    // The access key must appear in the Credential value
    assertTrue("Authorization must contain the access key",
        auth.contains("Credential=" + ACCESS_KEY + "/"));
  }
}
