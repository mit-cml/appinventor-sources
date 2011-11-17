// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.server.flags;

/**
 * Thrown to indicate that value of a system property associated with a flag is illegal.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class IllegalFlagValueException extends RuntimeException {
  IllegalFlagValueException(String message) {
    super(message);
  }
}
