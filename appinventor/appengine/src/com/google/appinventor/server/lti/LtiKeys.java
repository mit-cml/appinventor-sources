// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The tool RSA key pair, kept in the datastore rather than in the deployed
 * artifact, and generated on first use. The newest key signs the tool's
 * messages (the grade passback assertion, the Deep Linking response, and the
 * template reference), and the JWK set published at /lti/jwks carries every
 * stored public key, so a message signed by any of them still verifies. That is
 * what makes rotation safe. Adding a key makes it the signer while the older
 * public keys stay published until they are retired.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiKeys {

  private static final int KEY_SIZE = 2048;

  /** A signing key and the id it is published under. */
  static final class SigningKey {
    final String kid;
    final PrivateKey privateKey;

    SigningKey(String kid, PrivateKey privateKey) {
      this.kid = kid;
      this.privateKey = privateKey;
    }
  }

  private LtiKeys() {}

  /** The newest key, used to sign, generating a first key if the store is empty. */
  static SigningKey signingKey() throws Exception {
    StoredData.LtiKeyData newest = newest(keysOrGenerate());
    return new SigningKey(newest.kid, LtiJwt.privateKeyFromDer(newest.privateKey));
  }

  /** The JWK set of every stored public key, served at /lti/jwks. */
  static String jwksJson() throws Exception {
    JSONArray keys = new JSONArray();
    for (StoredData.LtiKeyData key : keysOrGenerate()) {
      keys.put(LtiJwt.jwk(LtiJwt.publicKeyFromDer(key.publicKey), key.kid));
    }
    return new JSONObject().put("keys", keys).toString();
  }

  private static List<StoredData.LtiKeyData> keysOrGenerate() {
    List<StoredData.LtiKeyData> keys = StorageIoInstanceHolder.getInstance().getLtiKeys();
    return keys.isEmpty() ? generate() : keys;
  }

  private static StoredData.LtiKeyData newest(List<StoredData.LtiKeyData> keys) {
    StoredData.LtiKeyData newest = keys.get(0);
    for (StoredData.LtiKeyData key : keys) {
      if (key.created.after(newest.created)) {
        newest = key;
      }
    }
    return newest;
  }

  /** Generates and stores one RSA key pair, returning the keys now on record. */
  private static synchronized List<StoredData.LtiKeyData> generate() {
    StorageIo storageIo = StorageIoInstanceHolder.getInstance();
    List<StoredData.LtiKeyData> keys = storageIo.getLtiKeys();
    if (!keys.isEmpty()) {
      return keys;   // another thread generated it while we waited for the lock
    }
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(KEY_SIZE);
      KeyPair pair = generator.generateKeyPair();
      StoredData.LtiKeyData created = new StoredData.LtiKeyData();
      created.kid = "ai-lti-" + LtiState.random();
      created.privateKey = pair.getPrivate().getEncoded();
      created.publicKey = pair.getPublic().getEncoded();
      created.created = new Date();
      storageIo.storeLtiKey(created.kid, created.privateKey, created.publicKey);
      return Collections.singletonList(created);
    } catch (Exception e) {
      throw new IllegalStateException("Could not generate the LTI tool key pair", e);
    }
  }
}
