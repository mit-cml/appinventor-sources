// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.ASSETS_FOLDER;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.Signatures;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.Execution;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * compiler.aapt()
 */
// RunAapt
@BuildType(apk = true)
public class RunAapt implements AndroidTask {
  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    // Need to make sure assets directory exists otherwise aapt will fail.
    context.getPaths().setAssetsDir(
        ExecutorUtils.createDir(context.getProject().getBuildDirectory(),
            ASSETS_FOLDER));

    File sourceOutputDir = ExecutorUtils.createDir(context.getPaths().getBuildDir(),
        "generated/src");
    File symbolOutputDir = ExecutorUtils.createDir(context.getPaths().getBuildDir(),
        "generated/symbols");

    String aaptTool = context.getResources().aapt();
    if (aaptTool == null) {
      return TaskResult.generateError("Could not find a suitable AAPT tool for this OS");
    }

    List<String> aaptPackageCommandLineArgs = new ArrayList<>();
    aaptPackageCommandLineArgs.add(aaptTool);
    aaptPackageCommandLineArgs.add("package");
    aaptPackageCommandLineArgs.add("-v");
    aaptPackageCommandLineArgs.add("-f");
    aaptPackageCommandLineArgs.add("-M");
    aaptPackageCommandLineArgs.add(context.getPaths().getManifest().getAbsolutePath());
    aaptPackageCommandLineArgs.add("-S");
    aaptPackageCommandLineArgs.add(context.getPaths().getMergedResDir().getAbsolutePath());
    aaptPackageCommandLineArgs.add("-A");
    aaptPackageCommandLineArgs.add(context.getPaths().getAssetsDir().getAbsolutePath());
    aaptPackageCommandLineArgs.add("-I");
    aaptPackageCommandLineArgs.add(context.getResources().getAndroidRuntime());
    aaptPackageCommandLineArgs.add("-F");
    aaptPackageCommandLineArgs.add(context.getPaths().getTmpPackageName().getAbsolutePath());
    if (!context.getComponentInfo().getExplodedAarLibs().isEmpty()) {
      // If AARs are used, generate R.txt for later processing
      String packageName = Signatures.getPackageName(context.getProject().getMainClass());
      aaptPackageCommandLineArgs.add("-m");
      aaptPackageCommandLineArgs.add("-J");
      aaptPackageCommandLineArgs.add(sourceOutputDir.getAbsolutePath());
      aaptPackageCommandLineArgs.add("--custom-package");
      aaptPackageCommandLineArgs.add(packageName);
      aaptPackageCommandLineArgs.add("--output-text-symbols");
      aaptPackageCommandLineArgs.add(symbolOutputDir.getAbsolutePath());
      aaptPackageCommandLineArgs.add("--no-version-vectors");
      context.getResources().setAppRTxt(new File(symbolOutputDir, "R.txt"));
    }
    String[] aaptPackageCommandLine = aaptPackageCommandLineArgs.toArray(new String[0]);
    // Using System.err and System.out on purpose. Don't want to pollute build messages with
    // tools output
    if (!Execution.execute(null, aaptPackageCommandLine,
        System.out, System.err, Execution.Timeout.MEDIUM)) {
      return TaskResult.generateError("Error running AAPT");
    }

    return TaskResult.generateSuccess();
  }
}
