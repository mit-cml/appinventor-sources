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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Signs and verifies ZIP files by embedding an ECDSA (NIST P-256) signature in the ZIP comment.
 *
 * <p>Comment format: {@code "Made with MIT App Inventor" NUL <key-id> ":" <base64 DER signature>}
 *
 * <p>The signed message is the ZIP bytes up to (not including) the EOCD comment length field,
 * followed by the key id as a 2-byte big-endian integer, so the key id is bound into the signature.
 *
 * <p>Key file format (public and private): one key per line, {@code <key-id> <base64 DER key>}.
 * Blank lines and lines starting with '#' are ignored. Public keys are X.509 SubjectPublicKeyInfo,
 * private keys are PKCS#8. A private key file holds exactly one key.
 * Note: public keys can also be provided as a one line base64encoded version of the keyfile
 * which is what we do here.
 *
 */
public class ZipSigner {

  private static final String SIGNATURE_ALGO = "SHA256withECDSA";
  private static final String CURVE_NAME = "secp256r1";
  private static final String PREFIX = "Made with MIT App Inventor";
  private static final String COMMENT_PREFIX = PREFIX + "\0";
  private static final char KEY_ID_SEPARATOR = ':';

  public static final int MAX_KEY_ID = 65535;

  private static final String PUBLIC_KEY_FILE_HEADER =
      "# MIT App Inventor ZIP signing public keys\n"
          + "# Format: <key-id> <base64 DER SubjectPublicKeyInfo>\n";
  private static final String PRIVATE_KEY_FILE_HEADER =
      "# MIT App Inventor ZIP signing private key -- keep this secret\n"
          + "# Format: <key-id> <base64 DER PKCS#8>\n";

  /** Exit status for usage errors and operational failures; distinct from verify's 0-3. */
  public static final int EXIT_ERROR = 64;

  // Configurable cutoff date for unsigned ZIPs (Format: ISO-8601 UTC)
  public static String LEGACY_CUTOFF_DATE = "2026-01-01T00:00:00Z";

  // ---------------------------------------------------------------------------------------------
  // Key ids
  // ---------------------------------------------------------------------------------------------

  /** Parses a key id: 1-5 ASCII digits, value 0..MAX_KEY_ID. */
  public static int parseKeyId(String s) {
    if (s.isEmpty() || s.length() > 5) {
      throw new IllegalArgumentException(
          "invalid key id '" + s + "' (must be 0-" + MAX_KEY_ID + ")");
    }
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c < '0' || c > '9') {
        throw new IllegalArgumentException(
            "invalid key id '" + s + "' (must be 0-" + MAX_KEY_ID + ")");
      }
    }
    int value = Integer.parseInt(s);
    if (value > MAX_KEY_ID) {
      throw new IllegalArgumentException(
          "invalid key id '" + s + "' (must be 0-" + MAX_KEY_ID + ")");
    }
    return value;
  }

  private static void checkKeyId(int keyId) {
    if (keyId < 0 || keyId > MAX_KEY_ID) {
      throw new IllegalArgumentException("key id must be 0-" + MAX_KEY_ID);
    }
  }

  private static byte[] keyIdBytes(int keyId) {
    return new byte[] {(byte) (keyId >>> 8), (byte) keyId};
  }

  // ---------------------------------------------------------------------------------------------
  // Key files
  // ---------------------------------------------------------------------------------------------

  /** Parses key file text into a sorted map of key id to Base64 DER key. */
  public static SortedMap<Integer, String> parseKeyFile(String text) {
    SortedMap<Integer, String> keys = new TreeMap<>();
    String[] lines = text.split("\\R", -1);
    for (int i = 0; i < lines.length; i++) {
      int lineno = i + 1;
      String line = lines[i].trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      String[] parts = line.split("\\s+");
      if (parts.length != 2) {
        throw new IllegalArgumentException("line " + lineno + ": expected '<key-id> <base64-key>'");
      }
      int keyId;
      try {
        keyId = parseKeyId(parts[0]);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("line " + lineno + ": " + e.getMessage());
      }
      try {
        Base64.getDecoder().decode(parts[1]);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("line " + lineno + ": invalid base64 key");
      }
      if (keys.put(keyId, parts[1]) != null) {
        throw new IllegalArgumentException("line " + lineno + ": duplicate key id " + keyId);
      }
    }
    return keys;
  }

  public static SortedMap<Integer, String> loadKeyFile(Path path) throws IOException {
    String text = Files.readString(path);
    try {
      return parseKeyFile(text);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(path + ": " + e.getMessage());
    }
  }

  /**
   * Parses key file contents that have been Base64 encoded as a whole, so a complete key file fits
   * on one line, for example as a value in a properties file. Whitespace in the encoded string is
   * ignored, so the value may be split across lines with properties-file continuations. Produce the
   * value with, e.g., {@code base64 -w0 public.keys}.
   *
   * @param encodedKeyFile Base64 encoding of the UTF-8 key file text
   * @return Map of key id to Base64 DER key, as returned by {@link #parseKeyFile}
   * @throws IllegalArgumentException if the string is not valid Base64 or the decoded key file is
   *     malformed
   */
  public static SortedMap<Integer, String> parseKeyFileBase64(String encodedKeyFile) {
    byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(encodedKeyFile.replaceAll("\\s+", ""));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("key data is not valid Base64: " + e.getMessage());
    }
    return parseKeyFile(new String(decoded, StandardCharsets.UTF_8));
  }

  /** Loads a private key file, which must contain exactly one key. */
  public static Map.Entry<Integer, String> loadPrivateKeyFile(Path path) throws IOException {
    SortedMap<Integer, String> keys = loadKeyFile(path);
    if (keys.size() != 1) {
      throw new IllegalArgumentException(
          path + ": expected exactly one private key, found " + keys.size());
    }
    return keys.entrySet().iterator().next();
  }

  /**
   * Generates a P-256 key pair. Writes the private key to a new file (refuses to overwrite) and
   * adds the public key to the public key file, creating it if needed.
   */
  public static void generateKey(int keyId, Path privateKeyPath, Path publicKeyPath)
      throws IOException, GeneralSecurityException {
    checkKeyId(keyId);

    String existing = Files.exists(publicKeyPath) ? Files.readString(publicKeyPath) : "";
    SortedMap<Integer, String> keys;
    try {
      keys = parseKeyFile(existing);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(publicKeyPath + ": " + e.getMessage());
    }
    if (keys.containsKey(keyId)) {
      throw new IllegalArgumentException("key id " + keyId + " already exists in " + publicKeyPath);
    }

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
    keyGen.initialize(new ECGenParameterSpec(CURVE_NAME));
    KeyPair keyPair = keyGen.generateKeyPair();
    String privB64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    String pubB64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    writeNewPrivateFile(privateKeyPath, PRIVATE_KEY_FILE_HEADER + keyId + " " + privB64 + "\n");

    StringBuilder addition = new StringBuilder();
    if (existing.isEmpty()) {
      addition.append(PUBLIC_KEY_FILE_HEADER);
    } else if (!existing.endsWith("\n")) {
      addition.append('\n');
    }
    addition.append(keyId).append(' ').append(pubB64).append('\n');
    Files.writeString(
        publicKeyPath, addition, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
  }

  /** Creates a new file (failing if it exists), owner read/write only where POSIX is supported. */
  private static void writeNewPrivateFile(Path path, String content) throws IOException {
    try {
      Files.createFile(
          path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
    } catch (UnsupportedOperationException e) {
      Files.createFile(path);
    }
    Files.writeString(path, content, StandardOpenOption.WRITE);
  }

  /** Returns one line per key: key id and SHA-256 fingerprint of the DER key. */
  public static String listKeys(Map<Integer, String> publicKeys) throws GeneralSecurityException {
    StringBuilder sb = new StringBuilder();
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    for (Map.Entry<Integer, String> e : new TreeMap<>(publicKeys).entrySet()) {
      byte[] digest = md.digest(Base64.getDecoder().decode(e.getValue()));
      StringBuilder hex = new StringBuilder();
      for (byte b : digest) {
        hex.append(String.format("%02x", b));
      }
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(String.format("%5d  SHA256:%s", e.getKey(), hex));
    }
    return sb.toString();
  }

  // ---------------------------------------------------------------------------------------------
  // Signing and verification
  // ---------------------------------------------------------------------------------------------

  /**
   * Signs the ZIP file and writes the key id and signature into the EOCD comment.
   *
   * @param data The raw zip bytes
   * @param privateKeyBase64 The Base64 encoded PKCS#8 private key
   * @param keyId The key id (0-65535) identifying the matching public key
   * @return The signed ZIP file as a byte array
   */
  public static byte[] signZip(byte[] data, String privateKeyBase64, int keyId)
      throws IOException, GeneralSecurityException {
    checkKeyId(keyId);
    byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

    int eocdIdx = findEOCD(data);
    int bytesToSignLen = eocdIdx + 20;

    Signature sig = Signature.getInstance(SIGNATURE_ALGO);
    sig.initSign(privateKey);
    sig.update(data, 0, bytesToSignLen);
    sig.update(keyIdBytes(keyId));
    byte[] signatureBytes = sig.sign();

    String base64Sig = Base64.getEncoder().encodeToString(signatureBytes);
    byte[] commentBytes =
        (COMMENT_PREFIX + keyId + KEY_ID_SEPARATOR + base64Sig).getBytes(StandardCharsets.UTF_8);

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
   * Verifies the signature of the ZIP file, selecting the public key by the key id embedded in the
   * signature.
   *
   * @param data The raw zip bytes
   * @param publicKeys Map of key id to Base64 encoded X.509 public key
   * @return 0 for good signature; 1 for invalid signature, unknown key id, or malformed data; 2 for
   *     no signature AND first entry date &lt; legacy cutoff; 3 for no signature AND first entry
   *     date &gt;= legacy cutoff
   */
  public static int verifyZip(byte[] data, String encodedKeys)
      throws IOException {

    Map<Integer, String> publicKeys = parseKeyFileBase64(encodedKeys);

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
      return firstEntryTime < cutoffMillis ? 2 : 3;
    }

    // Extract the key id and select the public key
    String body = commentStr.substring(COMMENT_PREFIX.length());
    int sepIdx = body.indexOf(KEY_ID_SEPARATOR);
    if (sepIdx < 0) {
      return 1; // No key id
    }
    int keyId;
    try {
      keyId = parseKeyId(body.substring(0, sepIdx));
    } catch (IllegalArgumentException e) {
      return 1; // Malformed key id
    }
    String publicKeyBase64 = publicKeys.get(keyId);
    if (publicKeyBase64 == null) {
      return 1; // Unknown key id
    }

    try {
      byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
      KeyFactory keyFactory = KeyFactory.getInstance("EC");
      PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));

      byte[] signatureBytes = Base64.getDecoder().decode(body.substring(sepIdx + 1));

      Signature sig = Signature.getInstance(SIGNATURE_ALGO);
      sig.initVerify(publicKey);
      sig.update(data, 0, bytesToSignLen);
      sig.update(keyIdBytes(keyId));

      return sig.verify(signatureBytes) ? 0 : 1;
    } catch (Exception e) {
      // Base64 decoding errors, key spec errors, or crypto exceptions are treated as invalid
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
}

