// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
