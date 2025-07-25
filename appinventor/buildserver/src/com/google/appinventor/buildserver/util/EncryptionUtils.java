// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2023-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;

public class EncryptionUtils {
  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static InputStream decryptFile(File source, File key) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] content;
    try (FileInputStream in = new FileInputStream(source)) {
      content = IOUtils.toByteArray(in);
    }
    PEMParser parser = new PEMParser(new FileReader(key));
    Object object = parser.readObject();
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
      if (object instanceof PrivateKeyInfo) {
        PKCS8EncodedKeySpec p8s = new PKCS8EncodedKeySpec(((PrivateKeyInfo) object).getEncoded());
        RSAPrivateCrtKey rprv = (RSAPrivateCrtKey) keyFactory.generatePrivate(p8s);
        Cipher cipher = Cipher.getInstance("RSA", "BC");
        cipher.init(Cipher.DECRYPT_MODE, rprv);
        cipher.update(content);
        return new ByteArrayInputStream(cipher.doFinal());
      }
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new UnsupportedOperationException("RSA decryption not supported on this machine.");
    } catch (NoSuchPaddingException | IllegalBlockSizeException | InvalidKeySpecException
             | BadPaddingException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
    throw new IllegalStateException("Unable to find private key for decrypting user credentials.");
  }
}
