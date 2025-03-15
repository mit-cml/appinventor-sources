// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.android.ide.common.internal.AaptCruncher;
import com.android.ide.common.internal.PngCruncher;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import java.io.File;


/**
 * compiler.mergeResources()
 */
@BuildType(apk = true, aab = true)
public class MergeResources implements AndroidTask {
  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    // these should exist from earlier build steps
    File intermediates = ExecutorUtils.createDir(context.getPaths().getBuildDir(), "intermediates");
    File resDir = ExecutorUtils.createDir(intermediates, "res");
    context.getPaths().setMergedResDir(ExecutorUtils.createDir(resDir, "merged"));
    context.getPaths().setTmpPackageName(new File(
        context.getPaths().getDeployDir().getAbsolutePath() + File.separator
            + context.getProject().getProjectName() + ".ap_"));

    PngCruncher cruncher = new AaptCruncher(context.getResources().aapt(), null, null);
    if (!context.getComponentInfo().getExplodedAarLibs().mergeResources(
        context.getPaths().getMergedResDir(), context.getPaths().getResDir(), cruncher)) {
      return TaskResult.generateError("Could not merge resources");
    }
    return TaskResult.generateSuccess();
  }
}
