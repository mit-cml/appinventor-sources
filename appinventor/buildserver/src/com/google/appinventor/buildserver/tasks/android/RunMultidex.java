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
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * compiler.runMultidex()
 */
@BuildType(apk = true, aab = true)
public class RunMultidex implements AndroidTask {
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

      final Set<String> criticalJars = new HashSet<>(Arrays.asList(
          // Minimum required for Android 4.x
          context.getResources().getRuntimeFilesDir() + "appcompat.jar",
          context.getResources().getRuntimeFilesDir() + "collection.jar",
          context.getResources().getRuntimeFilesDir() + "core.jar",
          context.getResources().getRuntimeFilesDir() + "core-common.jar",
          context.getResources().getRuntimeFilesDir() + "lifecycle-common.jar",
          context.getResources().getRuntimeFilesDir() + "vectordrawable.jar",
          context.getResources().getRuntimeFilesDir() + "vectordrawable-animated.jar",

          // Extras that may be pulled
          context.getResources().getRuntimeFilesDir() + "annotation.jar",
          context.getResources().getRuntimeFilesDir() + "asynclayoutinflater.jar",
          context.getResources().getRuntimeFilesDir() + "coordinatorlayout.jar",
          context.getResources().getRuntimeFilesDir() + "core-runtime.jar",
          context.getResources().getRuntimeFilesDir() + "cursoradapter.jar",
          context.getResources().getRuntimeFilesDir() + "customview.jar",
          context.getResources().getRuntimeFilesDir() + "documentfile.jar",
          context.getResources().getRuntimeFilesDir() + "drawerlayout.jar",
          context.getResources().getRuntimeFilesDir() + "fragment.jar",
          context.getResources().getRuntimeFilesDir() + "interpolator.jar",
          context.getResources().getRuntimeFilesDir() + "legacy-support-core-ui.jar",
          context.getResources().getRuntimeFilesDir() + "legacy-support-core-utils.jar",
          context.getResources().getRuntimeFilesDir() + "lifecycle-livedata.jar",
          context.getResources().getRuntimeFilesDir() + "lifecycle-livedata-core.jar",
          context.getResources().getRuntimeFilesDir() + "lifecycle-runtime.jar",
          context.getResources().getRuntimeFilesDir() + "lifecycle-viewmodel.jar",
          context.getResources().getRuntimeFilesDir() + "loader.jar",
          context.getResources().getRuntimeFilesDir() + "localbroadcastmanager.jar",
          context.getResources().getRuntimeFilesDir() + "print.jar",
          context.getResources().getRuntimeFilesDir() + "slidingpanelayout.jar",
          context.getResources().getRuntimeFilesDir() + "swiperefreshlayout.jar",
          context.getResources().getRuntimeFilesDir() + "versionedparcelable.jar",
          context.getResources().getRuntimeFilesDir() + "viewpager.jar"
      ));

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

  /**
   * Writes out the class list for the main dex file. The format of this file is the pathname of
   * the class, including the .class extension, one per line.
   *
   * @param classesDir directory to place the main classes list
   * @param classes    the set of classes to include in the main dex file
   * @return the path to the file containing the main classes list
   */
  private String writeClassList(File classesDir, Set<String> classes) {
    File target = new File(classesDir, "main-classes.txt");
    try (PrintStream out = new PrintStream(Files.newOutputStream(target.toPath()))) {
      for (String name : new TreeSet<>(classes)) {
        out.println(name.replaceAll("\\.", "/") + ".class");
      }
      return target.getAbsolutePath();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Processes recursively the directory pointed at by {@code dir} and adds any class files
   * encountered to the {@code classes} set.
   *
   * @param dir     the directory to examine for class files
   * @param classes the Set used to record the classes
   * @param root    the root path where the recursion started, which gets stripped from the file
   *                name to determine the class name
   */
  private void recordDirectoryForMainDex(File dir, Set<String> classes, String root) {
    File[] files = dir.listFiles();
    if (files == null) {
      return;
    }
    for (File f : files) {
      if (f.isDirectory()) {
        recordDirectoryForMainDex(f, classes, root);
      } else if (f.getName().endsWith(".class")) {
        String className = f.getAbsolutePath().replace(root, "");
        className = className.substring(0, className.length() - 6);
        classes.add(className.replaceAll("/", "."));
      }
    }
  }

  /**
   * Processes the JAR file pointed at by {@code file} and adds the contained class names to
   * {@code classes}.
   *
   * @param file    a File object pointing to a JAR file
   * @param classes the Set used to record the classes
   * @throws IOException if the input file cannot be read
   */
  private void recordJarForMainDex(File file, Set<String> classes) throws IOException {
    try (ZipInputStream is = new ZipInputStream(Files.newInputStream(file.toPath()))) {
      ZipEntry entry;
      while ((entry = is.getNextEntry()) != null) {
        String className = entry.getName();
        if (className.endsWith(".class")) {
          className = className.substring(0, className.length() - 6);
          classes.add(className.replaceAll("/", "."));
        }
      }
    }
  }

  /**
   * Examines the given file and records its classes for the main dex class list.
   *
   * @param file    a File object pointing to a JAR file or a directory containing class files
   * @param classes the Set used to record the classes
   * @return the input file
   * @throws IOException if the input file cannot be read
   */
  private File recordForMainDex(File file, Set<String> classes) throws IOException {
    if (file.isDirectory()) {
      recordDirectoryForMainDex(file, classes, file.getAbsolutePath() + File.separator);
    } else if (file.getName().endsWith(".jar")) {
      recordJarForMainDex(file, classes);
    }
    return file;
  }
}
