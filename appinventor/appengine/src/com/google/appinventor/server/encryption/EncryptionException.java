// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server.encryption;

/**
 * Thrown by the encryption components.
 *
 */
public class EncryptionException extends Exception {
  public EncryptionException(Throwable t) {
    super(t);
  }
  public EncryptionException(String s) {
    super(s);
  }
}
