// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import com.android.utils.ILogger;

/**
 * BaseLogger provides a rudimentary implementation of Android's ILogger interface.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class BaseLogger implements ILogger {

  @Override
  public void error(Throwable t, String msgFormat, Object... args) {
    System.err.println("[ERROR] " + msgFormat);
  }

  @Override
  public void warning(String msgFormat, Object... args) {
    System.err.println("[WARN] " + msgFormat);
  }

  @Override
  public void info(String msgFormat, Object... args) {
    System.err.println("[INFO] " + msgFormat);
  }

  @Override
  public void verbose(String msgFormat, Object... args) {
    System.err.println("[DEBUG] " + msgFormat);
  }

}
