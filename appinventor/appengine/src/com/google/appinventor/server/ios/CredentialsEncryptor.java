// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.ios;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;

/**
 * The CredentialsEncryptor class provides primitives for asymmetrically encrypting user-provided
 * credentials needed by the build server for communicating with App Store Connect APIs.
 */
public class CredentialsEncryptor {
  private static final Logger LOG = Logger.getLogger(CredentialsEncryptor.class.getName());
  private static final PublicKey KEY;

  static {
    Security.addProvider(new BouncyCastleProvider());
    final String pubkey = "/com/google/appinventor/server/ios/buildserver.pub";
    PEMParser parser;
    try (InputStream stream = CredentialsEncryptor.class.getResourceAsStream(pubkey)) {
      if (stream == null) {
        throw new ExceptionInInitializerError(
            new NullPointerException("Cannot find build server key for encrypting credentials."));
      }
      String contents = IOUtils.toString(stream);
      parser = new PEMParser(new StringReader(contents));
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
    try {
      SubjectPublicKeyInfo pki = (SubjectPublicKeyInfo) parser.readObject();
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      KEY = keyFactory.generatePublic(new X509EncodedKeySpec(pki.getEncoded()));
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * Encrypts the content using the active public key.
   *
   * @param content the content to be encrypted
   * @return the encrypted content as a byte array, or null if an error occurs
   */
  public static byte[] encrypt(String content) {
    try {
      Cipher cipher = Cipher.getInstance("RSA", "BC");
      cipher.init(Cipher.ENCRYPT_MODE, KEY);
      cipher.update(content.getBytes(StandardCharsets.UTF_8));
      return cipher.doFinal();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error encrypting credentials", e);
      return null;
    }
  }
}
