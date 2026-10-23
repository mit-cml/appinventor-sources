// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipSigner {

  private static final String SIGNATURE_ALGO = "SHA256withECDSA";
  private static final String CURVE_NAME = "secp256r1";
  private static final String PREFIX = "Made with MIT App Inventor";
  private static final String COMMENT_PREFIX = PREFIX + "\0";

  // Configurable cutoff date for unsigned ZIPs (Format: ISO-8601 UTC)
  public static String LEGACY_CUTOFF_DATE = "2026-01-01T00:00:00Z";

  /**
   * Signs the ZIP file and appends the signature to the EOCD comment.
   *
   * @param zipIn The input stream containing the raw zip bytes
   * @param privateKeyBase64 The Base64 encoded PKCS#8 private key
   * @return The signed ZIP file as a byte array
   */
  public static byte[] signZip(byte[] data, String privateKeyBase64)
      throws IOException, GeneralSecurityException {
    // Decode the Base64 private key string
    byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

    int eocdIdx = findEOCD(data);
    int bytesToSignLen = eocdIdx + 20;

    Signature sig = Signature.getInstance(SIGNATURE_ALGO);
    sig.initSign(privateKey);
    sig.update(data, 0, bytesToSignLen);
    byte[] signatureBytes = sig.sign();

    String base64Sig = Base64.getEncoder().encodeToString(signatureBytes);
    byte[] commentBytes = (COMMENT_PREFIX + base64Sig).getBytes(StandardCharsets.UTF_8);

    if (commentBytes.length > 65535) {
      throw new IllegalStateException("Comment exceeds max ZIP comment length (65535 bytes)");
    }

    ByteArrayOutputStream baos =
        new ByteArrayOutputStream(bytesToSignLen + 2 + commentBytes.length);
    baos.write(data, 0, bytesToSignLen);
    baos.write(commentBytes.length & 0xFF);
    baos.write((commentBytes.length >> 8) & 0xFF);
    baos.write(commentBytes);

    return baos.toByteArray();
  }

  /**
   * Verifies the signature of the ZIP file.
   *
   * @param zipIn The input stream containing the signed zip bytes
   * @param publicKeyBase64 The Base64 encoded X.509 public key
   * @return 0 for good signature 1 for invalid signature (or malformed signature data) 2 for no
   *     signature AND first entry date < legacy cutoff 3 for no signature AND first entry date >=
   *     legacy cutoff
   */
  public static int verifyZip(InputStream zipIn, String publicKeyBase64) throws IOException {
    byte[] data = zipIn.readAllBytes();

    int eocdIdx;
    try {
      eocdIdx = findEOCD(data);
    } catch (IllegalArgumentException e) {
      return 1; // Malformed ZIP / No EOCD
    }

    int bytesToSignLen = eocdIdx + 20;
    int commentLen = (data[eocdIdx + 20] & 0xFF) | ((data[eocdIdx + 21] & 0xFF) << 8);

    if (eocdIdx + 22 + commentLen > data.length) {
      return 1; // Corrupted comment length
    }

    String commentStr = new String(data, eocdIdx + 22, commentLen, StandardCharsets.UTF_8);

    // Check if the signature prefix is missing
    if (!commentStr.startsWith(COMMENT_PREFIX)) {
      long cutoffMillis = Instant.parse(LEGACY_CUTOFF_DATE).toEpochMilli();
      long firstEntryTime = getFirstEntryTime(data);

      if (firstEntryTime < cutoffMillis) {
        return 2;
      } else {
        return 3;
      }
    }

    // Signature prefix is present, verify the cryptography
    try {
      // Decode the Base64 public key string
      byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
      KeyFactory keyFactory = KeyFactory.getInstance("EC");
      PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));

      String base64Sig = commentStr.substring(COMMENT_PREFIX.length());
      byte[] signatureBytes = Base64.getDecoder().decode(base64Sig);

      Signature sig = Signature.getInstance(SIGNATURE_ALGO);
      sig.initVerify(publicKey);
      sig.update(data, 0, bytesToSignLen);

      if (sig.verify(signatureBytes)) {
        return 0; // Good signature
      } else {
        return 1; // Invalid signature
      }
    } catch (Exception e) {
      // Catch Base64 decoding errors, key spec errors, or crypto exceptions (treated as invalid)
      return 1;
    }
  }

  private static int findEOCD(byte[] data) {
    int minSearchIdx = Math.max(0, data.length - 65535 - 22);
    for (int i = data.length - 22; i >= minSearchIdx; i--) {
      if (data[i] == (byte) 0x50
          && data[i + 1] == (byte) 0x4B
          && data[i + 2] == (byte) 0x05
          && data[i + 3] == (byte) 0x06) {
        int commentLen = (data[i + 20] & 0xFF) | ((data[i + 21] & 0xFF) << 8);
        if (i + 22 + commentLen == data.length) {
          return i;
        }
      }
    }
    throw new IllegalArgumentException("Not a valid ZIP file or EOCD record not found");
  }

  private static long getFirstEntryTime(byte[] zipData) {
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
      ZipEntry entry = zis.getNextEntry();
      if (entry != null) {
        long time = entry.getTime();
        return time != -1 ? time : 0;
      }
    } catch (IOException e) {
      // Ignore stream errors and fall through
    }
    return System.currentTimeMillis();
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) return;
    String mode = args[0];

    if ("keygen".equals(mode)) {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
      keyGen.initialize(new ECGenParameterSpec(CURVE_NAME));
      KeyPair keyPair = keyGen.generateKeyPair();
      java.nio.file.Files.writeString(
          java.nio.file.Paths.get(args[1]),
          Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
      java.nio.file.Files.writeString(
          java.nio.file.Paths.get(args[2]),
          Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    } else if ("sign".equals(mode)) {
      byte[] inData = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(args[1]));
      String privKey = java.nio.file.Files.readString(java.nio.file.Paths.get(args[3])).trim();
      byte[] signedZip = signZip(inData, privKey);
      java.nio.file.Files.write(java.nio.file.Paths.get(args[2]), signedZip);
    } else if ("verify".equals(mode)) {
      byte[] inData = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(args[1]));
      String pubKey = java.nio.file.Files.readString(java.nio.file.Paths.get(args[2])).trim();
      int result = verifyZip(new ByteArrayInputStream(inData), pubKey);
      System.exit(result);
    }
  }
}
