// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server.encryption;

/**
 * Encrypts/decrypts byte arrays.
 *
 */
public interface Encryptor {
  /**
   * Encrypts plain text byte array.
   *
   * @param plain plain text byte array
   * @return encrypted byte array
   * @throws EncryptionException if there's a problem with the encryption
   *         process
   */
  byte[] encrypt(byte[] plain) throws EncryptionException;

  /**
   * Decrypts encrypted byte array.
   *
   * @param encrypted encrypted byte array
   * @return decrypted byte array
   * @throws EncryptionException if there's a problem with the decryption
   *         process
   */
  byte[] decrypt(byte[] encrypted) throws EncryptionException;
}
