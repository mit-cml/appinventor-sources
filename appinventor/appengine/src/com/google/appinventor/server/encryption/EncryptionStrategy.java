// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.encryption;

/**
 * Describes possible encryption strategies.
 *
 * <li>{@code NONE} does not use encryption at all.
 * <li>{@code READ} uses encryption only on read.
 * <li>{@code WRITE} uses encryption on both read and write.
 *
 */
public enum EncryptionStrategy implements Encryptor {
  NONE(new NopEncryptor()),
  READ(new KeyczarEncryptor() {
    @Override
    public byte[] encrypt(byte[] plain) {
      return plain;
    }}),
  WRITE(new KeyczarEncryptor());

  private final Encryptor encryptor;

  private EncryptionStrategy(Encryptor encryptor) {
    this.encryptor = encryptor;
  }

  @Override
  public byte[] decrypt(byte[] encrypted) throws EncryptionException {
    return encryptor.decrypt(encrypted);
  }

  @Override
  public byte[] encrypt(byte[] plain) throws EncryptionException {
    return encryptor.encrypt(plain);
  }
}
