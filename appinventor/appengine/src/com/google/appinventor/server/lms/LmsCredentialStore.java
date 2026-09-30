// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.encryption.EncryptionStrategy;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.storage.StorageUtil;

import java.nio.charset.StandardCharsets;

/**
 * Stores and retrieves a user's Google OAuth refresh token for the Google
 * Classroom integration.
 *
 * <p>The refresh token is encrypted at rest with {@link EncryptionStrategy#WRITE}
 * (Keyczar, keyed from {@code WEB-INF/keystore}) and persisted as a per-user file
 * through {@link StorageIo}, the same mechanism that stores the Android keystore
 * and the App Store credentials. The plaintext token is never written to storage.
 *
 * <p>Because it uses the existing {@link StorageIo} user-file methods, this store
 * adds nothing to the StorageIo interface and works unchanged on any storage
 * backend that implements it.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LmsCredentialStore {

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  /**
   * Encrypts and stores the Google refresh token for a user, replacing any
   * existing value.
   *
   * @param userId the App Inventor user id
   * @param refreshToken the plaintext Google OAuth refresh token
   * @throws EncryptionException if the token cannot be encrypted
   */
  public void saveGoogleRefreshToken(String userId, String refreshToken)
      throws EncryptionException {
    if (userId == null || userId.isEmpty()) {
      throw new IllegalArgumentException("userId must not be empty");
    }
    if (refreshToken == null || refreshToken.isEmpty()) {
      throw new IllegalArgumentException("refreshToken must not be empty");
    }
    byte[] encrypted =
        EncryptionStrategy.WRITE.encrypt(refreshToken.getBytes(StandardCharsets.UTF_8));
    storageIo.uploadRawUserFile(
        userId, StorageUtil.LMS_GOOGLE_REFRESH_TOKEN_FILENAME, encrypted);
  }

  /**
   * Loads and decrypts the Google refresh token for a user.
   *
   * @param userId the App Inventor user id
   * @return the plaintext refresh token, or {@code null} if none is stored
   * @throws EncryptionException if the stored token cannot be decrypted
   */
  public String getGoogleRefreshToken(String userId) throws EncryptionException {
    if (!hasGoogleCredential(userId)) {
      return null;
    }
    byte[] encrypted =
        storageIo.downloadRawUserFile(userId, StorageUtil.LMS_GOOGLE_REFRESH_TOKEN_FILENAME);
    return new String(EncryptionStrategy.WRITE.decrypt(encrypted), StandardCharsets.UTF_8);
  }

  /**
   * Returns whether a Google refresh token is stored for the given user.
   *
   * @param userId the App Inventor user id
   * @return {@code true} if a token is stored for the user
   */
  public boolean hasGoogleCredential(String userId) {
    if (userId == null || userId.isEmpty()) {
      return false;
    }
    return storageIo.getUserFiles(userId).contains(
        StorageUtil.LMS_GOOGLE_REFRESH_TOKEN_FILENAME);
  }
}
