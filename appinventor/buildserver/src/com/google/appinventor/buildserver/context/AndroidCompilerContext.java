// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.context;

/**
 * The AndroidCompilerContext provides a context for builds targeting Android.
 */
public class AndroidCompilerContext extends CompilerContext<AndroidPaths> {
  public AndroidCompilerContext() {
    super(new AndroidPaths());
  }
}
