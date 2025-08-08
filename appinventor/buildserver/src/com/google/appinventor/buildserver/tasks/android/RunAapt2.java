// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.ASSETS_FOLDER;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.context.AndroidPaths;
import com.google.appinventor.buildserver.context.CompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.Execution;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@BuildType(aab = true)
public class RunAapt2 implements AndroidTask {
  CompilerContext<AndroidPaths> context;
  File resourcesZip;

  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    this.context = context;

    final File buildDir = context.getPaths().getBuildDir();
    ExecutorUtils.createDir(buildDir, "generated/src");
    final File symbolOutputDir = ExecutorUtils.createDir(buildDir, "generated/symbols");
    // Need to make sure assets directory exists otherwise aapt2 will fail.
    context.getPaths().setAssetsDir(ExecutorUtils.createDir(buildDir, ASSETS_FOLDER));

    String aapt2Tool = context.getResources().aapt2();
    if (aapt2Tool == null) {
      return TaskResult.generateError("Could not find a suitable AAPT tool for this OS");
    }

    if (!this.runAapt2Compile(aapt2Tool)) {
      return TaskResult.generateError("Error while compiling with AAPT2");
    }
    if (!this.runAapt2Link(aapt2Tool, symbolOutputDir)) {
      return TaskResult.generateError("Error while linking with AAPT2");
    }
    return TaskResult.generateSuccess();
  }

  private boolean runAapt2Compile(String aapt2Tool) {
    resourcesZip = new File(context.getPaths().getResDir(), "resources.zip");

    List<String> aapt2CommandLine = new ArrayList<>();
    aapt2CommandLine.add(aapt2Tool);
    aapt2CommandLine.add("compile");
    aapt2CommandLine.add("--dir");
    aapt2CommandLine.add(context.getPaths().getMergedResDir().getAbsolutePath());
    aapt2CommandLine.add("-o");
    aapt2CommandLine.add(resourcesZip.getAbsolutePath());
    aapt2CommandLine.add("--no-crunch");
    String[] aapt2CompileCommandLine = aapt2CommandLine.toArray(new String[0]);

    if (!Execution.execute(null, aapt2CompileCommandLine,
        System.out, System.err, Execution.Timeout.MEDIUM)) {
      context.getReporter().error("Could not execute AAPT2 compile step");
      return false;
    }

    return true;
  }

  private boolean runAapt2Link(String aapt2Tool, File symbolOutputDir) {
    context.getResources().setAppRTxt(new File(symbolOutputDir, "R.txt"));

    List<String> aapt2CommandLine = new ArrayList<>();
    aapt2CommandLine.add(aapt2Tool);
    aapt2CommandLine.add("link");
    aapt2CommandLine.add("--proto-format");
    aapt2CommandLine.add("-o");
    aapt2CommandLine.add(context.getPaths().getTmpPackageName().getAbsolutePath());
    aapt2CommandLine.add("-I");
    aapt2CommandLine.add(context.getResources().getAndroidRuntime());
    aapt2CommandLine.add("-R");
    aapt2CommandLine.add(resourcesZip.getAbsolutePath());
    aapt2CommandLine.add("-A");
    aapt2CommandLine.add(context.getPaths().getAssetsDir().getAbsolutePath());
    aapt2CommandLine.add("--manifest");
    aapt2CommandLine.add(context.getPaths().getManifest().getAbsolutePath());
    aapt2CommandLine.add("--output-text-symbols");
    aapt2CommandLine.add(context.getResources().getAppRTxt().getAbsolutePath());
    aapt2CommandLine.add("--auto-add-overlay");
    aapt2CommandLine.add("--no-version-vectors");
    aapt2CommandLine.add("--no-auto-version");
    aapt2CommandLine.add("--no-version-transitions");
    aapt2CommandLine.add("--no-resource-deduping");
    String[] aapt2LinkCommandLine = aapt2CommandLine.toArray(new String[0]);

    if (!Execution.execute(null, aapt2LinkCommandLine,
        System.out, System.err, Execution.Timeout.MEDIUM)) {
      context.getReporter().error("Could not execute AAPT2 link step");
      return false;
    }

    return true;
  }
}
