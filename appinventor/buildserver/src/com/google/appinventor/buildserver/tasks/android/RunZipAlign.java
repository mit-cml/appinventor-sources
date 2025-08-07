// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.Execution;
import com.google.appinventor.buildserver.util.ExecutorUtils;
import java.io.File;

/**
 * compiler.runZipAlign()
 */
@BuildType(apk = true)
public class RunZipAlign implements AndroidTask {
  @Override
  public TaskResult execute(AndroidCompilerContext context) {
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
        System.out, System.err, Execution.Timeout.SHORT)) {
      return TaskResult.generateError("Error while running ZipAlign tool");
    }

    if (!ExecutorUtils.copyFile(zipAlignedApk.getAbsolutePath(),
        context.getPaths().getDeployFile().getAbsolutePath())) {
      return TaskResult.generateError("Error while copying ZipAlign'ed APK");
    }

    return TaskResult.generateSuccess();
  }
}
