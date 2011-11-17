// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server.encryption;

/**
 * Pass-through encryptor.
 *
 */
class NopEncryptor implements Encryptor {
  /**
   * {@inheritDoc}
   *
   * <p>Returns the received array.
   */
  @Override
  public byte[] decrypt(byte[] encrypted) {
    return encrypted;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the received array.
   */
  @Override
  public byte[] encrypt(byte[] plain) {
    return plain;
  }
}
