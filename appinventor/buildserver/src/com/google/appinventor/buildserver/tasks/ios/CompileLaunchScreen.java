// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import com.google.appinventor.buildserver.util.Execution;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@BuildType(ipa = true, asc = true)
public class CompileLaunchScreen implements IosTask {
  @Override
  public TaskResult execute(IosCompilerContext context) {
    String icon = context.getProject().getIcon();
    if (icon.isEmpty()) {
      return TaskResult.generateSuccess();
    }
    String launchScreenTemplate = context.getResource("/files/LaunchScreenTemplate.storyboard");
    File launchScreen = new File(context.getPaths().getTmpDir(), "LaunchScreen.storyboard");
    try (BufferedReader reader = new BufferedReader(new FileReader(launchScreenTemplate));
         BufferedWriter writer = new BufferedWriter(new FileWriter(launchScreen))) {
      String line;
      while ((line = reader.readLine()) != null) {
        writer.write(line.replace("ya.png", icon));
      }
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }
    String[] args = getArgs(context, launchScreen);
    if (Execution.execute(context, args)) {
      return TaskResult.generateSuccess();
    }
    return TaskResult.generateError("Failed to compile launch screen");
  }

  private static String[] getArgs(IosCompilerContext context, File launchScreen) {
    File base = new File(context.getPaths().getAppDir(), "Base.lproj");
    return new String[] {
      "xcrun",
      "ibtool",
        "--errors",
        "--warnings",
        "--notices",
        "--auto-activate-custom-fonts",
        "--output-format",
        "human-readable-text",
        "--module",
        "PlayerApp",
        "--target-device",
        "iphone",
        "--target-device",
        "ipad",
        "--minimum-deployment-target",
        "12.0",
        "--compilation-directory",
        base.getAbsolutePath(),
        launchScreen.getAbsolutePath()
    };
  }
}
