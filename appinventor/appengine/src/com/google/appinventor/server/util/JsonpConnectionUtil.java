// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import com.google.common.base.Charsets;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.jsonp.JsonpConnectionInfo;
import com.google.appinventor.shared.storage.StorageUtil;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Helper methods for the JSONP connection to codeblocks.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class JsonpConnectionUtil {
  private static final Random RANDOM = new SecureRandom();

  private JsonpConnectionUtil() {
  }

  /**
   * Clears the JSONP connection info for the given user and project.
   *
   * @param storageIo storage instance
   * @param userId the user ID
   * @param filePrefix the prefix of the file used for saving the info
   */
  public static void clearJsonpConnectionInfo(StorageIo storageIo, String userId,
      String filePrefix) {
    String fileId = filePrefix + userId;

    storageIo.addFilesToUser(userId, fileId);
    storageIo.uploadUserFile(userId, fileId, "", StorageUtil.DEFAULT_CHARSET);
  }

  /**
   * Saves the JSONP connection info for the given user and project.
   *
   * @param storageIo storage instance
   * @param userId the user ID
   * @param filePrefix the prefix of the file used for saving the info
   * @param port the port at which the JSONP server is listening
   * @return the response data
   */
  public static byte[] saveJsonpConnectionInfo(StorageIo storageIo, String userId,
      String filePrefix, int port) {
    String fileId = filePrefix + userId;

    int secret = getSecretNumber();
    String contents = port + " " + secret;
    storageIo.addFilesToUser(userId, fileId);
    storageIo.uploadUserFile(userId, fileId, contents, StorageUtil.DEFAULT_CHARSET);
    return Integer.toString(secret).getBytes(Charsets.UTF_8);
  }

  /**
   * Retrieves the JSONP connection info and then clears it.
   *
   * @param storageIo storage instance
   * @param userId the user ID
   * @param filePrefix the prefix of the file used for saving the info
   * @return the JSONP connection info or null if it has not been saved
   */
  public static JsonpConnectionInfo retrieveJsonpConnectionInfo(StorageIo storageIo,
      String userId, String filePrefix) {
    String fileId = filePrefix + userId;

    String contents = storageIo.downloadUserFile(userId, fileId, StorageUtil.DEFAULT_CHARSET);
    // Make sure the contents is not empty before returning it.
    if (!contents.isEmpty()) {
      // We don't need the file anymore. Delete it.
      storageIo.deleteUserFile(userId, fileId);
      String[] split = contents.split(" ");
      int port = Integer.parseInt(split[0]);
      int secret = Integer.parseInt(split[1]);
      return new JsonpConnectionInfo(port, secret);
    }

    // Info has not been saved yet.
    return null;
  }

  /*
   * Returns a secret number (a positive integer).
   */
  private static int getSecretNumber() {
    int secret;
    do {
      secret = Math.abs(RANDOM.nextInt());
    } while (secret == 0);
    return secret;
  }
}
