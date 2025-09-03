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
public class MoveAssetsIntoPlace implements IosTask {
  @Override
  public TaskResult execute(IosCompilerContext context) {
    File assetDir = new File(context.getPaths().getProjectRootDir(), "assets");
    File[] assets = assetDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isFile() && !pathname.getName().endsWith(".mobileprovision");
      }
    });
    if (assets != null) {
      ExecutorUtils.copyFilesToDir(assets, context.getPaths().getAppDir());
    }
    return TaskResult.generateSuccess();
  }
}
