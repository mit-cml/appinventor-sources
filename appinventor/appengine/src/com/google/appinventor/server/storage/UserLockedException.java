// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.server.storage;

/**
 * Thrown by MegastoreStorageIo methods
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class UserLockedException extends RuntimeException {

  public UserLockedException(String message) {
    super(message);
  }
}
