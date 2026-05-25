// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Signs HTTP requests using AWS Signature Version 4.
 *
 * <p>This is a package-private helper used by AWS-based LLM providers (e.g.
 * Bedrock) to authenticate requests without any external AWS SDK dependency.
 * Only JDK built-ins are used for cryptography ({@code javax.crypto.Mac} and
 * {@code java.security.MessageDigest}).
 */
class AwsSigV4Signer {

  private static final String ALGORITHM = "AWS4-HMAC-SHA256";
  private static final DateTimeFormatter DATE_TIME_FMT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
  private static final DateTimeFormatter DATE_FMT =
      DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

  private final String accessKey;
  private final String secretKey;
  private final String sessionToken;
  private final String region;
  private final String service;

  /**
   * Creates a new signer.
   *
   * @param accessKey    AWS access key ID
   * @param secretKey    AWS secret access key
   * @param sessionToken AWS session token (may be {@code null} or empty for
   *                     long-term credentials)
   * @param region       AWS region (e.g. {@code "us-east-1"})
   * @param service      AWS service name (e.g. {@code "bedrock"})
   */
  AwsSigV4Signer(String accessKey, String secretKey, String sessionToken,
      String region, String service) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.sessionToken = sessionToken;
    this.region = region;
    this.service = service;
  }

  /**
   * Signs an HTTP request and returns a new header map containing all headers
   * that must be set on the outgoing request.
   *
   * <p>The returned map includes:
   * <ul>
   *   <li>Every entry from {@code existingHeaders} (copied through unchanged)</li>
   *   <li>{@code Host} — extracted from the URL</li>
   *   <li>{@code X-Amz-Date} — current UTC time in ISO 8601 basic format</li>
   *   <li>{@code X-Amz-Security-Token} — only when a session token is set</li>
   *   <li>{@code Authorization} — the SigV4 credential string</li>
   * </ul>
   *
   * @param method          HTTP method (e.g. {@code "POST"})
   * @param urlString       full request URL
   * @param existingHeaders headers already intended for the request (may be
   *                        {@code null})
   * @param payload         request body bytes (may be empty but not null)
   * @return a new {@link LinkedHashMap} with all required headers
   */
  Map<String, String> sign(String method, String urlString,
      Map<String, String> existingHeaders, byte[] payload) {
    // 1. Current UTC time
    ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
    String timestamp = DATE_TIME_FMT.format(now);  // e.g. "20250101T120000Z"
    String datestamp = DATE_FMT.format(now);        // e.g. "20250101"

    // 2. Parse URL
    URL url;
    try {
      url = new URL(urlString);
    } catch (Exception e) {
      throw new RuntimeException("Invalid URL: " + urlString, e);
    }
    String host = url.getHost();
    if (url.getPort() != -1) {
      host = host + ":" + url.getPort();
    }

    // Canonicalize path: URL-decode each segment then re-encode it
    String rawPath = url.getPath();
    if (rawPath == null || rawPath.isEmpty()) {
      rawPath = "/";
    }
    String canonicalPath = canonicalizePath(rawPath);

    // Query string (already in the URL, pass through as-is)
    String queryString = url.getQuery();
    if (queryString == null) {
      queryString = "";
    }

    // 3. Assemble the set of headers that will be signed.
    //    We use a TreeMap so they are automatically sorted alphabetically.
    TreeMap<String, String> headersToSign = new TreeMap<>();
    headersToSign.put("host", host);
    headersToSign.put("x-amz-date", timestamp);
    if (existingHeaders != null) {
      for (Map.Entry<String, String> entry : existingHeaders.entrySet()) {
        String lcKey = entry.getKey().toLowerCase();
        if ("content-type".equals(lcKey)) {
          headersToSign.put(lcKey, entry.getValue().trim());
        }
      }
    }
    if (sessionToken != null && !sessionToken.isEmpty()) {
      headersToSign.put("x-amz-security-token", sessionToken);
    }

    // 4. Build canonical headers string and signed-headers string
    StringBuilder canonicalHeadersSb = new StringBuilder();
    StringBuilder signedHeadersSb = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String> entry : headersToSign.entrySet()) {
      canonicalHeadersSb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
      if (!first) {
        signedHeadersSb.append(";");
      }
      signedHeadersSb.append(entry.getKey());
      first = false;
    }
    String canonicalHeaders = canonicalHeadersSb.toString();
    String signedHeaders = signedHeadersSb.toString();

    // 5. Hash the payload
    String payloadHash = sha256Hex(payload);

    // 6. Build canonical request
    String canonicalRequest = method + "\n"
        + canonicalPath + "\n"
        + queryString + "\n"
        + canonicalHeaders + "\n"
        + signedHeaders + "\n"
        + payloadHash;

    // 7. Build string-to-sign
    String credentialScope = datestamp + "/" + region + "/" + service + "/aws4_request";
    String stringToSign = ALGORITHM + "\n"
        + timestamp + "\n"
        + credentialScope + "\n"
        + sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));

    // 8. Derive signing key
    byte[] kDate = hmacSha256(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), datestamp);
    byte[] kRegion = hmacSha256(kDate, region);
    byte[] kService = hmacSha256(kRegion, service);
    byte[] kSigning = hmacSha256(kService, "aws4_request");

    // 9. Compute signature
    String signature = hexEncode(hmacSha256(kSigning, stringToSign));

    // 10. Build Authorization header
    String authorization = ALGORITHM
        + " Credential=" + accessKey + "/" + credentialScope
        + ", SignedHeaders=" + signedHeaders
        + ", Signature=" + signature;

    // Build the result map: start with existing headers, then add SigV4 headers
    Map<String, String> result = new LinkedHashMap<>();
    if (existingHeaders != null) {
      result.putAll(existingHeaders);
    }
    result.put("Host", host);
    result.put("X-Amz-Date", timestamp);
    if (sessionToken != null && !sessionToken.isEmpty()) {
      result.put("X-Amz-Security-Token", sessionToken);
    }
    result.put("Authorization", authorization);

    return result;
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  /**
   * Canonicalizes a URL path by URL-decoding each segment and then
   * percent-encoding it again (RFC 3986 unreserved characters are not encoded).
   */
  private static String canonicalizePath(String path) {
    String[] segments = path.split("/", -1);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < segments.length; i++) {
      if (i > 0) {
        sb.append("/");
      }
      String decoded;
      try {
        decoded = URLDecoder.decode(segments[i], "UTF-8");
      } catch (Exception e) {
        decoded = segments[i];
      }
      sb.append(uriEncode(decoded));
    }
    return sb.toString();
  }

  /**
   * Percent-encodes a string according to RFC 3986, leaving unreserved
   * characters ({@code A-Z a-z 0-9 - _ . ~}) unencoded.
   */
  private static String uriEncode(String value) {
    try {
      // URLEncoder encodes space as '+' and leaves some characters that RFC
      // 3986 requires encoded, so we fix up afterwards.
      String encoded = URLEncoder.encode(value, "UTF-8");
      // Replace '+' (space) back to '%20', and unescape chars that URLEncoder
      // over-encodes but RFC 3986 allows unencoded in unreserved set.
      encoded = encoded.replace("+", "%20")
          .replace("%7E", "~");   // tilde is unreserved
      return encoded;
    } catch (Exception e) {
      throw new RuntimeException("UTF-8 unavailable", e);
    }
  }

  /**
   * Computes {@code HMAC-SHA256(key, data)} where {@code data} is a UTF-8
   * string.
   */
  private static byte[] hmacSha256(byte[] key, String data) {
    return hmacSha256(key, data.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Computes {@code HMAC-SHA256(key, data)}.
   */
  private static byte[] hmacSha256(byte[] key, byte[] data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      return mac.doFinal(data);
    } catch (Exception e) {
      throw new RuntimeException("HmacSHA256 unavailable", e);
    }
  }

  /**
   * Returns the lowercase hex SHA-256 digest of {@code data}.
   */
  private static String sha256Hex(byte[] data) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return hexEncode(md.digest(data));
    } catch (Exception e) {
      throw new RuntimeException("SHA-256 unavailable", e);
    }
  }

  /**
   * Encodes a byte array as a lowercase hexadecimal string.
   */
  private static String hexEncode(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }
}
