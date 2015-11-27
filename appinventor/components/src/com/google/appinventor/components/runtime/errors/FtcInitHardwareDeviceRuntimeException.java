// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.errors;

/**
 * Runtime exception that wraps an exception caught during FTC hardware device initialization.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class FtcInitHardwareDeviceRuntimeException extends RuntimeException {

  public FtcInitHardwareDeviceRuntimeException(Throwable e) {
    super(e);
  }
}
