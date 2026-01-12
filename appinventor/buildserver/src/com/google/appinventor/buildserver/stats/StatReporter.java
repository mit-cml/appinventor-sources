// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.stats;

import com.google.appinventor.buildserver.Compiler;

/**
 * StatReport defines an API for the build process to record performance of build subprocesses.
 *
 * @author Evan W. Patton (ewpatton@mit.edu)
 */
public interface StatReporter {

  /**
   * Indicate to the StatReporter that a new build is about to begin for the given Compiler.
   *
   * @param compiler the compilation process in progress
   */
  void startBuild(Compiler compiler);

  /**
   * Indicate to the StatReporter that a build is entering a new stage.
   *
   * @param compiler the compilation process in progress
   * @param stage the stage being entered
   */
  void nextStage(Compiler compiler, String stage);

  /**
   * Indicate to the StatReporter that a build has finished and whether it was successful.
   *
   * @param compiler the compilation process being completed
   * @param success true if the build was a success, false otherwise
   */
  void stopBuild(Compiler compiler, boolean success);
}
