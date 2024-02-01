// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.DexExecTask;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.ExecutorUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * compiler.runMultidex()
 */
@BuildType(apk = true, aab = true)
public class RunMultidex extends DexTask implements AndroidTask {
  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    Set<String> mainDexClasses = new HashSet<>();
    List<File> inputList = new ArrayList<>();
    boolean success;
    try {
      // Set up classes for main dex file
      inputList.add(recordForMainDex(context.getPaths().getClassesDir(), mainDexClasses));
      inputList.add(recordForMainDex(new File(context.getResources().getSimpleAndroidRuntimeJar()),
          mainDexClasses));
      inputList.add(recordForMainDex(new File(context.getResources().getKawaRuntime()),
          mainDexClasses));

      /*
       * Note for future updates: This list can be obtained from an Android Studio project running
       * the following command:
       *
       * ./gradlew :app:dependencies --configuration releaseRuntimeClasspath --console=plain | \
       *     awk 'BEGIN {FS="--- "} {print $2}' | cut -d : -f2 | sort -u
       */

      final Set<String> criticalJars = getCriticalJars(context);

      for (String jar : criticalJars) {
        inputList.add(recordForMainDex(new File(context.getResource(jar)), mainDexClasses));
      }

      // Only include ACRA for the companion app
      if (context.isForCompanion()) {
        inputList.add(recordForMainDex(new File(context.getResources().getAcraRuntime()),
            mainDexClasses));
      }

      for (String jar : context.getResources().getSupportJars()) {
        if (criticalJars.contains(jar)) {  // already covered above
          continue;
        }
        inputList.add(new File(context.getResource(jar)));
      }

      // Add the rest of the libraries in any order
      for (String lib : context.getComponentInfo().getUniqueLibsNeeded()) {
        inputList.add(new File(lib));
      }

      // Add extension libraries
      Set<String> addedExtJars = new HashSet<>();
      for (String type : context.getExtCompTypes()) {
        String sourcePath = ExecutorUtils.getExtCompDirPath(type, context.getProject(),
            context.getExtTypePathCache())
            + context.getResources().getSimpleAndroidRuntimeJarPath();
        if (!addedExtJars.contains(sourcePath)) {
          inputList.add(new File(sourcePath));
          addedExtJars.add(sourcePath);
        }
      }

      // Run the dx utility
      DexExecTask dexTask = new DexExecTask();
      dexTask.setExecutable(context.getResources().getDxJar());
      dexTask.setMainDexClassesFile(writeClassList(context.getPaths().getClassesDir(),
          mainDexClasses));
      dexTask.setOutput(context.getPaths().getTmpDir().getAbsolutePath());
      dexTask.setChildProcessRamMb(context.getChildProcessRam());
      if (context.getDexCacheDir() == null) {
        dexTask.setDisableDexMerger(true);
      } else {
        ExecutorUtils.createDir(new File(context.getDexCacheDir()));
        dexTask.setDexedLibs(context.getDexCacheDir());
      }

      synchronized (context.getResources().getSyncKawaOrDx()) {
        success = dexTask.execute(inputList);
      }

      // Aggregate all of the classes.dex files output by dx
      File[] files = context.getPaths().getTmpDir().listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".dex");
        }
      });
      if (files == null) {
        throw new FileNotFoundException("Could not find classes.dex");
      }
      Collections.addAll(context.getResources().getDexFiles(), files);
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }

    if (!success) {
      return TaskResult.generateError("DX returned an error code");
    }
    return TaskResult.generateSuccess();
  }
}
