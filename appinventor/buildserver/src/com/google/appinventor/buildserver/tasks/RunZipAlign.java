// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.CompilerContext;
import com.google.appinventor.buildserver.Execution;
import com.google.appinventor.buildserver.ExecutorUtils;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.interfaces.Task;

import java.io.File;

/**
 * compiler.runZipAlign()
 */
@BuildType(apk = true)
public class RunZipAlign implements Task {
  @Override
  public TaskResult execute(CompilerContext context) {
    String zipAlignTool = context.getResources().zipalign();
    if (zipAlignTool == null) {
      return TaskResult.generateError("Could not find a suitable ZipAlign tool for this OS");
    }

    File zipAlignedApk = new File(context.getPaths().getTmpDir(), "zipaligned.apk");
    // zipalign -f 4 infile.zip outfile.zip
    String[] zipAlignCommandLine = {
        zipAlignTool, "-f", "4",
        context.getPaths().getDeployFile().getAbsolutePath(),
        zipAlignedApk.getAbsolutePath()
    };

    if (!Execution.execute(null, zipAlignCommandLine,
        System.out, System.err)) {
      TaskResult.generateError("Error while running ZipAlign tool");
    }

    if (!ExecutorUtils.copyFile(zipAlignedApk.getAbsolutePath(),
        context.getPaths().getDeployFile().getAbsolutePath())) {
      TaskResult.generateError("Error while copying ZipAlign'ed APK");
    }

    return TaskResult.generateSuccess();
  }
}
