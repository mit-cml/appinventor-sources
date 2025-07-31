// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage.remote;

import com.google.appinventor.server.flags.Flag;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class RemoteStorageProviderS3 extends RemoteStorage {
  private static final Flag<String> ENDPOINT = Flag.createFlag("remotestorage.s3.endpoint", null);
  private static final Flag<String> BUCKET_NAME = Flag.createFlag("remotestorage.s3.bucketname", null);
  private static final Flag<String> BUCKET_REGION = Flag.createFlag("remotestorage.s3.bucketregion", null);
  private static final Flag<String> ACCESS_KEY_ID = Flag.createFlag("remotestorage.s3.accesskeyid", null);
  private static final Flag<String> SECRET_ACCESS_KEY = Flag.createFlag("remotestorage.s3.secretaccesskey", null);

  // Upload URLs should be valid for 15 minutes
  private static final int UPLOAD_EXPIRATION_SECONDS = 900;
  // Retrieve URLs for 5 minutes
  private static final int RETRIEVE_EXPIRATION_SECONDS = 300;

  private static final String ALGORITHM = "AWS4-HMAC-SHA256";
  private static final String SERVICE = "s3";
  private static final String REQUEST_TYPE = "aws4_request";

  private final String endpoint;
  private final String bucketName;
  private final String bucketRegion;
  private final String accessKeyId;
  private final String secretAccessKey;

  public RemoteStorageProviderS3() {
    this.endpoint = validateOptionalParameter(ENDPOINT.get());
    this.bucketName = validateRequiredParameter(BUCKET_NAME.get(), "bucketName");
    this.bucketRegion = validateRequiredParameter(BUCKET_REGION.get(), "bucketRegion");
    this.accessKeyId = validateRequiredParameter(ACCESS_KEY_ID.get(), "accesskeyid");
    this.secretAccessKey = validateRequiredParameter(SECRET_ACCESS_KEY.get(), "secretaccesskey");
  }

  private String validateRequiredParameter(final String param, final String paramName) {
    if (param == null || param.isBlank()) {
      throw new IllegalArgumentException("Missing required parameter: " + paramName);
    }

    return param.strip();
  }

  private String validateOptionalParameter(final String param) {
    if (param == null || param.isBlank()) {
      return null;
    }

    return param.strip();
  }

  @Override
  public String generateUploadUrl(final String objectKey) {
    return generatePresignedUrl(objectKey, "PUT", UPLOAD_EXPIRATION_SECONDS);
  }

  @Override
  public String generateRetrieveUrl(final String objectKey) {
    return generatePresignedUrl(objectKey, "GET", RETRIEVE_EXPIRATION_SECONDS);
  }

  // AWS Presigned URL Generation
  private String generatePresignedUrl(final String objectKey, final String httpMethod, final int expirationSeconds) {
    // Current time
    long now = Instant.now().getEpochSecond();
    String date = Instant.ofEpochSecond(now).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String timestamp = Instant.ofEpochSecond(now).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));

    String endpoint = getEndpoint();
    String credentialsScope = accessKeyId + "/" + date + "/" + bucketRegion + "/" + SERVICE + "/" + REQUEST_TYPE;

    // Canonical request
    String canonicalUri = "/" + objectKey;
    String canonicalQueryString = "X-Amz-Algorithm=" + ALGORITHM +
        "&X-Amz-Credential=" + urlEncode(credentialsScope) +
        "&X-Amz-Date=" + timestamp +
        "&X-Amz-Expires=" + expirationSeconds +
        "&X-Amz-SignedHeaders=" + "host";
    String canonicalHeaders = "host:" + endpoint + "\n";
    String signedHeaders = "host";
    String payloadHash = "UNSIGNED-PAYLOAD";
    String canonicalRequest = httpMethod + "\n" +
        canonicalUri + "\n" +
        canonicalQueryString + "\n" +
        canonicalHeaders + "\n" +
        signedHeaders + "\n" +
        payloadHash;

    // String to sign
    String stringToSign = ALGORITHM + "\n" +
        timestamp + "\n" +
        date + "/" + bucketRegion + "/" + SERVICE + "/" + REQUEST_TYPE + "\n" +
        toHex(hash(canonicalRequest));

    // Signing key
    byte[] kSecret = ("AWS4" + secretAccessKey).getBytes();
    byte[] kDate = hmacSha256(kSecret, date);
    byte[] kRegion = hmacSha256(kDate, bucketRegion);
    byte[] kService = hmacSha256(kRegion, SERVICE);
    byte[] kSigning = hmacSha256(kService, REQUEST_TYPE);

    // Signature
    String signature = toHex(hmacSha256(kSigning, stringToSign));

    // Construct URL
    return "https://" + endpoint + "/" + objectKey +
        "?" + canonicalQueryString +
        "&X-Amz-Signature=" + signature;
  }

  private String getEndpoint() {
    if (endpoint != null) {
      return endpoint;
    }

    return bucketName + ".s3." + bucketRegion + ".amazonaws.com";
  }

  // Utility methods for hashing and URL encoding
  private static byte[] hash(String data) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(data.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException("Unable to compute hash", e);
    }
  }

  private static byte[] hmacSha256(byte[] key, String data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
      mac.init(secretKeySpec);
      return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException("Unable to compute HMAC-SHA256", e);
    }
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8)
        .replace("+", "%20")
        .replace("*", "%2A")
        .replace("%7E", "~");
  }

  private static String toHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte aByte : bytes) {
      result.append(String.format("%02x", aByte));
    }
    return result.toString();
  }
}
