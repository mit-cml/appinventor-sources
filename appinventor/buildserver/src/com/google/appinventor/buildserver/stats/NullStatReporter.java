// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.stats;

import com.google.appinventor.buildserver.Compiler;

/**
 * NullStatReporter is a StatReporter that does nothing in response to a stat report. Use this
 * reporter when you are not interested in collecting statistics about build performance.
 *
 * @author Evan W. Patton (ewpatton@mit.edu)
 */
public class NullStatReporter implements StatReporter {
  @Override
  public void startBuild(Compiler compiler) {
  }

  @Override
  public void nextStage(Compiler compiler, String stage) {
  }

  @Override
  public void stopBuild(Compiler compiler, boolean success) {
  }
}
