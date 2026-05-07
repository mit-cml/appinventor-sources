// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import com.google.appinventor.buildserver.util.ExecutorUtils;
import java.io.File;
import java.io.FileFilter;

@BuildType(ipa = true, asc = true)
public class MoveYailIntoPlace implements IosTask {
  @Override
  public TaskResult execute(IosCompilerContext context) {
    File[] files = context.getSourcePackageDir().listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".yail");
      }
    });
    if (files != null) {
      ExecutorUtils.copyFilesToDir(files, context.getPaths().getAppDir());
    } else {
      return TaskResult.generateError("No YAIL files present");
    }
    return TaskResult.generateSuccess();
  }
}
