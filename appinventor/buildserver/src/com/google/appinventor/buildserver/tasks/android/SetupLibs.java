// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.Compiler;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;

import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

/**
 * Sets up any host system specific shared libraries.
 */
@BuildType(apk = true, aab = true)
public class SetupLibs implements AndroidTask {
  public static final String RUNTIME_TOOLS_DIR =
      com.google.appinventor.buildserver.context.Resources.RUNTIME_TOOLS_DIR;

  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    String osName = System.getProperty("os.name");
    if (osName.equals("Linux")) {
      ensureLib("/tmp/lib64", "libc++.so",
          RUNTIME_TOOLS_DIR + "linux/lib64/libc++.so");
    } else if (osName.startsWith("Windows")) {
      ensureLib(System.getProperty("java.io.tmpdir"), "libwinpthread-1.dll",
          RUNTIME_TOOLS_DIR + "windows/libwinpthread-1.dll");
    }
    return TaskResult.generateSuccess();
  }

  private void ensureLib(String tempdir, String name, String resource) {
    try {
      File outFile = new File(tempdir, name);
      if (outFile.exists()) {
        return;
      }
      File tmpLibDir = new File(tempdir);
      tmpLibDir.mkdirs();
      Files.copy(Resources.newInputStreamSupplier(Compiler.class.getResource(resource)), outFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
