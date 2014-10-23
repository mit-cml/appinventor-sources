// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
