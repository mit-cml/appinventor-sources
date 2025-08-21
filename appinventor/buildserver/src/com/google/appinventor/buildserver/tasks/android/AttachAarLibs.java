// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.AARLibraries;
import com.google.appinventor.buildserver.util.AARLibrary;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * compiler.attachAarLibraries()
 */

@BuildType(apk = true, aab = true)
public class AttachAarLibs implements AndroidTask {
  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    final File explodedBaseDir = ExecutorUtils.createDir(context.getPaths().getBuildDir(),
        "exploded-aars");
    final File generatedDir = ExecutorUtils.createDir(context.getPaths().getBuildDir(),
        "generated");
    final File genSrcDir = ExecutorUtils.createDir(generatedDir, "src");
    context.getComponentInfo().setExplodedAarLibs(new AARLibraries(genSrcDir));
    final Set<String> processedLibs = new HashSet<>();

    // Attach the Android support libraries (needed by every app)
    context.getComponentInfo().getLibsNeeded().put("ANDROID", new HashSet<>(Arrays.asList(
        context.getResources().getSupportAars())));

    // walk components list for libraries ending in ".aar"
    try {
      for (String type : context.getComponentInfo().getLibsNeeded().keySet()) {
        Iterator<String> i = context.getComponentInfo().getLibsNeeded().get(type).iterator();
        while (i.hasNext()) {
          String libname = i.next();
          String sourcePath = "";
          if (libname.endsWith(".aar")) {
            i.remove();
            if (!processedLibs.contains(libname)) {
              if (context.getSimpleCompTypes().contains(type) || "ANDROID".equals(type)) {
                final String pathSuffix = context.getResources().getRuntimeFilesDir() + libname;
                sourcePath = context.getResource(pathSuffix);
              } else if (context.getExtCompTypes().contains(type)) {
                final String pathSuffix = "/aars/" + libname;
                sourcePath = ExecutorUtils.getExtCompDirPath(type, context.getProject(),
                    context.getExtTypePathCache()) + pathSuffix;
              } else {
                context.getReporter().error("Unknown component type: " + type, true);
                return TaskResult.generateError("Error while attaching AAR libraries");
              }
              // explode libraries into ${buildDir}/exploded-aars/<package>/
              AARLibrary aarLib = new AARLibrary(new File(sourcePath));
              aarLib.unpackToDirectory(explodedBaseDir);
              context.getComponentInfo().getExplodedAarLibs().add(aarLib);
              processedLibs.add(libname);
            }
          }
        }
      }
    } catch (IOException e) {
      context.getReporter().error("There was an unknown error while adding AAR libraries", true);
      return TaskResult.generateError(e);
    }

    return TaskResult.generateSuccess();
  }
}
