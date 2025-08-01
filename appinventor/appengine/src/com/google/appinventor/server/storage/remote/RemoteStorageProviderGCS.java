// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage.remote;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appinventor.server.flags.Flag;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;


public final class RemoteStorageProviderGCS extends RemoteStorage {
  private static final Flag<String> BUCKET_NAME = Flag.createFlag("remotestorage.gcp.bucketname", null);

  // Upload URLs should be valid for 15 minutes
  private static final int UPLOAD_EXPIRATION_SECONDS = 900;
  // Retrieve URLs for 5 minutes
  private static final int RETRIEVE_EXPIRATION_SECONDS = 300;

  private static final String GCS_BASE_URL = "https://storage.googleapis.com";

  private final AppIdentityService appIdentityService;
  private final String bucketName;

  public RemoteStorageProviderGCS() {
    SystemProperty.Environment.Value server = SystemProperty.environment.value();
    if (server != SystemProperty.Environment.Value.Production) {
      throw new UnsupportedOperationException("GCS Remote Storage can only be used in Production environment!");
    }

    this.appIdentityService = AppIdentityServiceFactory.getAppIdentityService();

    final String bucketNameFlag = BUCKET_NAME.get();
    if (bucketNameFlag == null || bucketNameFlag.isBlank()) {
      this.bucketName = this.appIdentityService.getDefaultGcsBucketName();
    } else {
      this.bucketName = bucketNameFlag;
    }
  }

  @Override
  public String generateUploadUrl(final String objectKey) {
    return createPresignedUrlV4(objectKey, "PUT", UPLOAD_EXPIRATION_SECONDS);
  }

  @Override
  public String generateRetrieveUrl(final String objectKey) {
    return createPresignedUrlV4(objectKey, "GET", RETRIEVE_EXPIRATION_SECONDS);
  }

  private String createPresignedUrlV4(final String objectKey, final String httpMethod, final int expirationSeconds) {
    // Get the service account email from App Identity Service
    String serviceAccountEmail = appIdentityService.getServiceAccountName();

    String timestamp = Instant.now().toString().replaceAll("[:-]", "").substring(0, 15) + "Z";
    String datestamp = timestamp.substring(0, 8);

    // Credential scope
    String credentialScope = String.format("%s/auto/storage/goog4_request", datestamp);
    String credential = String.format("%s/%s", serviceAccountEmail, credentialScope);

    // Canonical request components
    String canonicalUri = "/" + bucketName + "/" + objectKey;
    String canonicalQueryString = String.format(
        "X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=%s&X-Goog-Date=%s&X-Goog-Expires=%d&X-Goog-SignedHeaders=host",
        urlEncode(credential),
        timestamp,
        expirationSeconds
    );

    String canonicalHeaders = "host:storage.googleapis.com\n";
    String signedHeaders = "host";
    String payloadHash = "UNSIGNED-PAYLOAD";

    // Create canonical request
    String canonicalRequest = String.format("%s\n%s\n%s\n%s\n%s\n%s",
        httpMethod,
        canonicalUri,
        canonicalQueryString,
        canonicalHeaders,
        signedHeaders,
        payloadHash
    );

    // Create string to sign
    String stringToSign = String.format("GOOG4-RSA-SHA256\n%s\n%s\n%s",
        timestamp,
        credentialScope,
        sha256Hex(canonicalRequest)
    );

    // Sign the string using App Identity Service
    AppIdentityService.SigningResult signingResult = appIdentityService.signForApp(
        stringToSign.getBytes(StandardCharsets.UTF_8)
    );

    String signature = bytesToHex(signingResult.getSignature());

    // Build final URL
    return String.format("%s%s?%s&X-Goog-Signature=%s",
        GCS_BASE_URL,
        canonicalUri,
        canonicalQueryString,
        urlEncode(signature)
    );
  }

  /**
   * URL encode a string
   */
  private static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8)
        .replace("+", "%20")
        .replace("*", "%2A")
        .replace("%7E", "~");
  }

  /**
   * Calculate SHA256 hash and return as hex string
   */
  private static String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Unable to compute hash", e);
    }
  }

  /**
   * Convert byte array to hex string
   */
  private static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }
}
