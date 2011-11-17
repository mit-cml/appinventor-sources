// Copyright 2008 Google Inc. All Rights Reserved.

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
