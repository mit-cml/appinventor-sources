// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.ios;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;

public class CertificateRequestGenerator {
  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private CertificateRequestGenerator() {
    // Cannot be instantiated
  }

  public static void generateKeyPair(User user) throws IOException {
    try {
      KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
      keystore.load(null, "android".toCharArray());
      KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
      rsaGen.initialize(2048);
      KeyPair keyPair = rsaGen.generateKeyPair();
      long now = System.currentTimeMillis();
      X500Name name = new X500Name("emailAddress=" + user.getUserEmail() + ", CN=" + user.getUserEmail() + ", C=US");
      BigInteger timestamp = new BigInteger(Long.toString(now));
      Date start = new Date(now);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(start);
      calendar.add(Calendar.YEAR, 1);
      Date end = calendar.getTime();
      ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
      JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(name, timestamp, start, end, name, keyPair.getPublic());
      SubjectKeyIdentifier ski = new SubjectKeyIdentifier(keyPair.getPublic().getEncoded());
      certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.14"), false, ski);
      Certificate cert = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certBuilder.build(contentSigner));
      keystore.setKeyEntry("AndroidKey", keyPair.getPrivate(), "android".toCharArray(), new Certificate[] { cert });
      ByteArrayOutputStream saved = new ByteArrayOutputStream();
      keystore.store(saved, "android".toCharArray());
      final StorageIo storageIo = StorageIoInstanceHolder.getInstance();
      storageIo.uploadRawUserFile(user.getUserId(), "android.keystore", saved.toByteArray());
    } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | OperatorCreationException e) {
      e.printStackTrace();
    }
  }

  public static byte[] generateCertificateRequest(User user) throws IOException {
    final StorageIo storageIo = StorageIoInstanceHolder.getInstance();
    if (!storageIo.getUserFiles(user.getUserId()).contains("android.keystore")) {
      generateKeyPair(user);
    }
    byte[] keystore = StorageIoInstanceHolder.getInstance()
        .downloadRawUserFile(user.getUserId(), StorageUtil.ANDROID_KEYSTORE_FILENAME);
    PrivateKey privateKey;
    PublicKey publicKey;
    try {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(new ByteArrayInputStream(keystore), "android".toCharArray());
      Key key = keyStore.getKey("AndroidKey", "android".toCharArray());
      privateKey = (PrivateKey) key;
      publicKey = keyStore.getCertificate("AndroidKey").getPublicKey();
      JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
      ContentSigner signer = csBuilder.build(privateKey);
      PKCS10CertificationRequest request =
          new PKCS10CertificationRequestBuilder(new X500Name("emailAddress=" + user.getUserEmail() + ", CN=" + user.getUserEmail() + ", C=US"),
              SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()))
          .build(signer);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PEMWriter pemwriter = new PEMWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
      pemwriter.writeObject(request);
      pemwriter.close();
      return out.toByteArray();
    } catch (UnrecoverableKeyException | NoSuchAlgorithmException | OperatorCreationException | KeyStoreException | CertificateException e) {
      e.printStackTrace();
      throw new SecurityException(e);
    }
  }
}
