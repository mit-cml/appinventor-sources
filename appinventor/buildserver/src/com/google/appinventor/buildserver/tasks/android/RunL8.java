// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0\

package com.google.appinventor.buildserver.tasks.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.util.Execution;

/**
 * Runs L8 to dex the `desugar_jdk_libs` library and bundle it with Apk/Aab
 */
public class RunL8 {

  public boolean execute(AndroidCompilerContext context, Path configJson) throws IOException {
    context.getReporter().info("Running L8 to desugar core-library");
    Path tempOutputDir = Files.createTempDirectory("core-desugar");
    tempOutputDir.toFile().deleteOnExit();

    List<String> arguments = new ArrayList<>();
    List<String> javaArgs = new ArrayList<>();
    arguments.add("java");
    javaArgs.add("-Xmx" + context.getChildProcessRam() + "M");
    javaArgs.add("-Xss8m");
    javaArgs.add("-cp");
    javaArgs.add(context.getResources().getD8Jar());
    javaArgs.add("com.android.tools.r8.L8");
    javaArgs.add("--lib");
    javaArgs.add(context.getResources().getAndroidRuntime());
    javaArgs.add("--desugared-lib");
    javaArgs.add(configJson.toString());
    javaArgs.add("--output");
    javaArgs.add(tempOutputDir.toString());
    javaArgs.add("--min-api");
    javaArgs.add(Integer.toString(AndroidBuildUtils.computeMinSdk(context)));
    javaArgs.add(context.getResources().getDesugarJdkLibs());
    javaArgs.add(context.getResources().getDesugarJdkConfig());

    File javaArgsFile = new File(context.getPaths().getTmpDir(), "l8arguments.txt");
    try (PrintStream ps = new PrintStream(new FileOutputStream(javaArgsFile))) {
      for (String arg : javaArgs) {
        ps.println(arg);
      }
    }
    arguments.add("@" + javaArgsFile.getAbsolutePath());
    synchronized (context.getResources().getSyncKawaOrDx()) {
      boolean result = Execution.execute(context.getPaths().getTmpDir(),
          arguments.toArray(new String[0]), System.out, System.err, Execution.Timeout.LONG);
      if (result) {
        Path classesDex = tempOutputDir.resolve("classes.dex");
        classesDex.toFile().deleteOnExit();
        if (!classesDex.toFile().exists()) {
          throw new FileNotFoundException("Could not find classes.dex of desugar_jdk_libs");
        }
        Files.copy(classesDex, resolveNextClassesDex(context.getPaths().getTmpDir()));

        // Delete L8 relevant caches
        classesDex.toFile().delete();
        tempOutputDir.toFile().delete();
        return true;
      } else {
        return false;
      }
    }
  }

  private Path resolveNextClassesDex(File dir) {
    Path dirPath = dir.toPath();
    // classes2.dex, classes3.dex, classesN.dex ... — find first gap
    int index = 2;
    while (true) {
      Path candidate = dirPath.resolve("classes" + index + ".dex");
      if (!Files.exists(candidate)) {
        return candidate;
      }
      index++;
    }
  }
}
