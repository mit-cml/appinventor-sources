// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.android.sdklib.build.ApkBuilder;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * compiler.runApkBuilder
 */
@BuildType(apk = true)
public class RunApkBuilder implements AndroidTask {
  private static final Logger LOG = Logger.getLogger(RunApkBuilder.class.getName());

  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    try {
      ApkBuilder apkBuilder = new ApkBuilder(
          context.getPaths().getDeployFile().getAbsolutePath(),
          context.getPaths().getTmpPackageName().getAbsolutePath(),
          context.getPaths().getTmpDir().getAbsolutePath() + File.separator + "classes.dex",
          null,
          context.getReporter().getSystemOut()
      );
      if (context.getResources().getDexFiles().size() > 1) {
        for (File f : context.getResources().getDexFiles()) {
          if (!f.getName().equals("classes.dex")) {
            apkBuilder.addFile(f, f.getName());
          }
        }
      }
      if (!context.getComponentInfo().getNativeLibsNeeded().isEmpty()) {
        // Need to add native libraries...
        apkBuilder.addNativeLibraries(context.getPaths().getLibsDir());
      }
      apkBuilder.sealApk();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unable to run ApkBuilder", e);
      return TaskResult.generateError(e);
    }
    return TaskResult.generateSuccess();
  }
}
