// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import org.eclipse.jdt.core.compiler.CompilationProgress;

/**
 * NOPCompilationProgress is an implementation of JDT Core's CompilationProgress that does nothing.
 * It is passed to the BatchCompiler during the generation of R.class files from the application
 * and any AAR library dependencies.
 *
 * @author ewpatton@mit.edu
 */
public class NOPCompilationProgress extends CompilationProgress {

  @Override
  public void begin(int remainingWork) {
    // do nothing
  }

  @Override
  public void done() {
    // do nothing
  }

  @Override
  public boolean isCanceled() {
    // never cancelable
    return false;
  }

  @Override
  public void setTaskName(String name) {
    // do nothing
  }

  @Override
  public void worked(int workIncrement, int remainingWork) {
    // do nothing
  }

}
