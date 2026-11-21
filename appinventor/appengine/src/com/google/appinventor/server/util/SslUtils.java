// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SslUtils {

  public static SSLSocketFactory createSocketFactoryFromCertString(String certPem)
      throws Exception {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    Certificate ca;

    // Convert the String to an InputStream
    try (InputStream certInput =
        new ByteArrayInputStream(certPem.getBytes(StandardCharsets.UTF_8))) {
      ca = cf.generateCertificate(certInput);
    }

    // Initialize an in-memory KeyStore
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null, null);
    keyStore.setCertificateEntry("mit-ca", ca);

    // Bind the keystore to a TrustManager
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(keyStore);

    // Build the SSLContext
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, tmf.getTrustManagers(), null);

    return sslContext.getSocketFactory();
  }
}
