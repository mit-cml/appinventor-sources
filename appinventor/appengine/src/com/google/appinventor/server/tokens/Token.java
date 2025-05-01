// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.tokens;

import com.google.appinventor.server.flags.Flag;

import org.keyczar.Crypter;
import org.keyczar.util.Base64Coder;
import org.keyczar.exceptions.KeyczarException;

import com.google.protobuf.ByteString;

import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Token {

  private static Flag<String> tokenKey = Flag.createFlag("token.key", "WEB-INF/tokenkey");
  private static Flag<String> stokenKey = Flag.createFlag("stoken.key", "");
  private static Crypter crypter;

  private static final String HMAC_ALGORITHM = "HmacSHA256";

  static {
    try {
      crypter = new Crypter(tokenKey.get());
    } catch (KeyczarException e) {
      e.printStackTrace();
    }
  }

  private Token() {
  }

  public static synchronized String makeSSOToken(String userId) {
    try {
      TokenProto.token newToken = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.SSOLOGIN)
        .setUuid(userId)
        .setTs(System.currentTimeMillis()).build();
      return Base64Coder.encode(crypter.encrypt(newToken.toByteArray()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static synchronized String makeUUIDReturnToken(String email, String uuid) {
    try {
      TokenProto.token newToken = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.UUIDRETURN)
        .setUuid(uuid)
        .setName(email)
        .setTs(System.currentTimeMillis()).build();
      return Base64Coder.encode(crypter.encrypt(newToken.toByteArray()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static synchronized String makeAccountCreateToken(String userId, String name) {
    try {
      TokenProto.token newToken = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.CREATEACCOUNT)
        .setUuid(userId)
        .setName(name)
        .setTs(System.currentTimeMillis()).build();
      return Base64Coder.encode(crypter.encrypt(newToken.toByteArray()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static synchronized String makeAccountDeletionToken(String userId, String email) {
    try {
      TokenProto.token newToken = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.DELETEACCOUNT)
        .setUuid(userId)
        .setName(email)         // Yes, we are using the name field
        .setTs(System.currentTimeMillis()).build();
      return Base64Coder.encode(crypter.encrypt(newToken.toByteArray()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Verify a Keyczar encrypted token.
   *
   * @param inToken encrypted token
   * @returns decoded token
   * @throws TokenException on any error
   */

  public static TokenProto.token verifyToken(String inToken) throws TokenException {
    try {
      byte [] decrypted = crypter.decrypt(Base64Coder.decode(inToken));
      TokenProto.token newToken = TokenProto.token.parseFrom(decrypted);
      return newToken;
    } catch (Exception e) {
      throw new TokenException(e.getMessage());
    }
  }

  /**
   * Verify an HMAC Signed token.
   *
   * WARNING: These tokens are *not* encrypted, their contents is visible
   * to the end-user (but usually encrypted in transit.
   *
   * @param inEnvelope base64 encoded envelope which contains the token
   * @returns decoded token
   * @throws TokenException on any error
  */

  public static TokenProto.token verifySToken(String inEnvelope) throws TokenException {
    String key = stokenKey.get();
    if (key.isEmpty()) {
      throw new TokenException("stoken.key may not be empty!");
    }
    try {
      TokenProto.envelope envelope = TokenProto.envelope.parseFrom(Base64Coder.decode(inEnvelope));
      SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_ALGORITHM);
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKeySpec);
      byte [] signature = hmac.doFinal(envelope.getUnsigned().toByteArray());
      if (!Arrays.equals(signature, envelope.getSignature().toByteArray())) {
        throw new TokenException("Signature does not match");
      }
      return TokenProto.token.parseFrom(envelope.getUnsigned().toByteArray());
    } catch (Exception e) {
      throw new TokenException(e.getMessage());
    }
  }

}
