// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.time.Duration;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * A Class for accessing AWS S3 buckets. Used to store assets.
 * The key used to fetch an asset is an MD5 hash of the asset content
 */
public class S3Access {

  private static final String SERVICE = "s3";
  private static final String ALGORITHM = "AWS4-HMAC-SHA256";
  private final String accessKey;
  private final String secretKey;
  private final String region;
  private final String bucket;
  private static final Logger LOG = Logger.getLogger(S3Access.class.getName());
  private final RetryableHttpClient client;

  public S3Access(String accessKey, String secretKey, String region, String bucket) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.region = region;
    this.bucket = bucket;
    HttpClient hclient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build();
    this.client = new RetryableHttpClient(hclient, 10, Duration.ofMillis(500));
  }

  public void store(String key, byte [] content) throws Exception {
    sendRequest(key, content, "PUT");
  }

  public byte [] get(String key) throws Exception {
    return sendRequest(key, new byte[0], "GET");
  }

  public boolean exists(String key) {
    try {
      HttpRequest request = buildSignedRequest("HEAD", key, new byte[0]).build();
      HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

      // 200 means it exists. 404 means it doesn't.
      // 403 means it might exist but you don't have permission to see it.
      return response.statusCode() == 200;

    } catch (Exception e) {
      // Intentionally return false — callers treat errors the same as non-existence
      LOG.log(Level.WARNING, "S3 HEAD failed for key: " + key, e);
      return false;
    }
  }

  private byte[] sendRequest(String key, byte[] content, String method) throws Exception {
    HttpRequest.Builder requestBuilder = buildSignedRequest(method, key, content);

    HttpResponse<byte[]> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());

    if (response.statusCode() != 200) {
      String body = new String(response.body(), StandardCharsets.UTF_8);
      throw new IOException("S3 " + method + " error " + response.statusCode()
                            + " for key '" + key + "': " + body);
    }
    return response.body();
  }

  private HttpRequest.Builder buildSignedRequest(String method, String key,
    byte[] payload) throws Exception {

    ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
    String amzDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
    String datestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    String host = bucket + ".s3." + region + ".amazonaws.com";

    // Note: We do not need to URI Encode the key because it is always a collection
    // of hex digits
    String uriPath = "/" + key;

    String payloadHash = hashBytes(payload);

    String canonicalRequest = method + "\n" +
                              uriPath + "\n" + "\n" +
                              "host:" + host + "\n" +
                              "x-amz-content-sha256:" + payloadHash + "\n" +
                              "x-amz-date:" + amzDate + "\n" + "\n" +
                              "host;x-amz-content-sha256;x-amz-date\n" +
                              payloadHash;

    String credentialScope = datestamp + "/" + region + "/" + SERVICE + "/aws4_request";
    String stringToSign = ALGORITHM + "\n" + amzDate + "\n" +
                          credentialScope + "\n" + hashString(canonicalRequest);

    byte[] signingKey = getSignatureKey(secretKey, datestamp, region, SERVICE);
    String signature = bytesToHex(hmacSha256(stringToSign, signingKey));

    String authHeader = ALGORITHM + " Credential=" + accessKey + "/" +
                        credentialScope +
                        ", SignedHeaders=host;x-amz-content-sha256;x-amz-date" +
                        ", Signature=" + signature;

    HttpRequest.Builder builder =  HttpRequest.newBuilder()
      .uri(URI.create("https://" + host + uriPath))
      .header("x-amz-date", amzDate)
      .header("x-amz-content-sha256", payloadHash)
      .header("Authorization", authHeader);

    switch (method) {
      case "GET":
        builder.GET();
        break;
      case "PUT":
        builder.PUT(HttpRequest.BodyPublishers.ofByteArray(payload));
        break;
      case "HEAD":
        builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        break;
      default:
        builder.method(method, HttpRequest.BodyPublishers.noBody());
    }

    return builder;
  }

  private static String hashBytes(byte[] data) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    return bytesToHex(md.digest(data));
  }

  private static String hashString(String text) throws Exception {
    return hashBytes(text.getBytes(StandardCharsets.UTF_8));
  }

  private static byte[] hmacSha256(String data, byte[] key) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(key, "HmacSHA256"));
    return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
  }

  private static byte[] getSignatureKey(String key, String date, String region, String service) throws Exception {
    byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
    byte[] kDate = hmacSha256(date, kSecret);
    byte[] kRegion = hmacSha256(region, kDate);
    byte[] kService = hmacSha256(service, kRegion);
    return hmacSha256("aws4_request", kService);
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}

