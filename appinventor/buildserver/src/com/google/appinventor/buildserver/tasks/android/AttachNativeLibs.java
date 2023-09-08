// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import static com.google.appinventor.buildserver.YoungAndroidConstants.EXT_COMPS_DIR_NAME;
import static com.google.appinventor.components.common.ComponentDescriptorConstants.ARM64_V8A_SUFFIX;
import static com.google.appinventor.components.common.ComponentDescriptorConstants.ARMEABI_V7A_SUFFIX;
import static com.google.appinventor.components.common.ComponentDescriptorConstants.X86_64_SUFFIX;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.YoungAndroidConstants;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.context.AndroidPaths;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;


/**
 * compiler.insertNativeLibs()
 */

@BuildType(apk = true, aab = true)
public class AttachNativeLibs implements AndroidTask {
  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    AndroidPaths paths = context.getPaths();
    paths.setLibsDir(ExecutorUtils.createDir(paths.getBuildDir(),
        YoungAndroidConstants.LIBS_DIR_NAME));
    File armeabiDir = ExecutorUtils.createDir(paths.getLibsDir(),
        YoungAndroidConstants.ARMEABI_DIR_NAME);
    File armeabiV7aDir = ExecutorUtils.createDir(paths.getLibsDir(),
        YoungAndroidConstants.ARMEABI_V7A_DIR_NAME);
    File arm64V8aDir = ExecutorUtils.createDir(paths.getLibsDir(),
        YoungAndroidConstants.ARM64_V8A_DIR_NAME);
    File x8664Dir = ExecutorUtils.createDir(paths.getLibsDir(),
        YoungAndroidConstants.X86_64_DIR_NAME);

    try {
      for (String type : context.getComponentInfo().getNativeLibsNeeded().keySet()) {
        for (String lib : context.getComponentInfo().getNativeLibsNeeded().get(type)) {
          boolean isV7a = lib.endsWith(ARMEABI_V7A_SUFFIX);
          boolean isV8a = lib.endsWith(ARM64_V8A_SUFFIX);
          boolean isx8664 = lib.endsWith(X86_64_SUFFIX);

          String sourceDirName;
          File targetDir;
          if (isV7a) {
            sourceDirName = YoungAndroidConstants.ARMEABI_V7A_DIR_NAME;
            targetDir = armeabiV7aDir;
            lib = lib.substring(0, lib.length() - ARMEABI_V7A_SUFFIX.length());
          } else if (isV8a) {
            sourceDirName = YoungAndroidConstants.ARM64_V8A_DIR_NAME;
            targetDir = arm64V8aDir;
            lib = lib.substring(0, lib.length() - ARM64_V8A_SUFFIX.length());
          } else if (isx8664) {
            sourceDirName = YoungAndroidConstants.X86_64_DIR_NAME;
            targetDir = x8664Dir;
            lib = lib.substring(0, lib.length() - X86_64_SUFFIX.length());
          } else {
            sourceDirName = YoungAndroidConstants.ARMEABI_DIR_NAME;
            targetDir = armeabiDir;
          }

          String sourcePath;
          String pathSuffix = context.getResources().getRuntimeFilesDir() + sourceDirName
              + "/" + lib;  // Java resource lookups always use "/"

          if (context.getSimpleCompTypes().contains(type)) {
            sourcePath = context.getResource(pathSuffix);
          } else if (context.getExt().contains(type)) {
            sourcePath = ExecutorUtils.getExtCompDirPath(type, context.getProject(),
                context.getExtTypePathCache()) + pathSuffix;
            targetDir = ExecutorUtils.createDir(targetDir, EXT_COMPS_DIR_NAME);
            targetDir = ExecutorUtils.createDir(targetDir, type);
          } else {
            context.getReporter().error(
                "There was an unexpected error while processing native code", true);
            return TaskResult.generateError("Unknown native lib type");
          }

          Files.copy(new File(sourcePath), new File(targetDir, lib));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      context.getReporter().error("There was an unknown error while processing native code", true);
      return TaskResult.generateError(e);
    }

    return TaskResult.generateSuccess();
  }
}
