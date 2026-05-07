// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class DexTask {
  /**
   * Maps JAR file references to their precomputed hash code representing the target dex file
   * created during pre-dexing.
   */
  static final Map<File, String> PREDEX_CACHE = new HashMap<>();

  /**
   * Retrieves the set of critical JARs in the build context.
   *
   * @param context the build context
   * @return a set of relative paths for JARs needed for the base build
   */
  static Set<String> getCriticalJars(AndroidCompilerContext context) {
    return new HashSet<>(Arrays.asList(
        // Minimum required for Android 4.x
        context.getResources().getRuntimeFilesDir() + "annotation-experimental.jar",
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
  }

  /**
   * Writes out the class list for the main dex file. The format of this file is the pathname of
   * the class, including the .class extension, one per line.
   *
   * @param classesDir directory to place the main classes list
   * @param classes    the set of classes to include in the main dex file
   * @return the path to the file containing the main classes list
   */
  static String writeClassList(File classesDir, Set<String> classes) {
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
   * /Writes out the class rules for the main dex file. The file is formatted as a Proguard rules
   * file and is used by d8 to trace the classes needed by the main dex.
   *
   * @param classesDir directory to place the main classes rules
   * @param classes    the set of classes to serve as a starting point for the trace
   * @return the path to the file containing the main classes rules
   */
  static String writeClassRules(File classesDir, Set<String> classes) {
    File target = new File(classesDir, "main-classes.rules");
    try (PrintStream out = new PrintStream(Files.newOutputStream(target.toPath()))) {
      for (String name : new TreeSet<>(classes)) {
        out.println("-keep class " + name);
      }
      return target.getAbsolutePath();
    } catch (IOException e) {
      return null;
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
  static void recordJarForMainDex(File file, Set<String> classes) throws IOException {
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
   * Processes recursively the directory pointed at by {@code dir} and adds any class files
   * encountered to the {@code classes} set.
   *
   * @param dir     the directory to examine for class files
   * @param classes the Set used to record the classes
   * @param root    the root path where the recursion started, which gets stripped from the file
   *                name to determine the class name
   */
  static void recordDirectoryForMainDex(File dir, Set<String> classes, String root) {
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
   * Examines the given file and records its classes for the main dex class list.
   *
   * @param file    a File object pointing to a JAR file or a directory containing class files
   * @param classes the Set used to record the classes
   * @return the input file
   * @throws IOException if the input file cannot be read
   */
  static File recordForMainDex(File file, Set<String> classes) throws IOException {
    if (file.isDirectory()) {
      recordDirectoryForMainDex(file, classes, file.getAbsolutePath() + File.separator);
    } else if (file.getName().endsWith(".jar")) {
      recordJarForMainDex(file, classes);
    }
    return file;
  }

  /**
   * Determines the destination of a dex cache operation for {@code inputFile}. Note that this
   * function only determines where the file should live. There is no guarantee that the returned
   * value exists yet.
   *
   * @param inputFile the source JAR being considered for dexing
   * @param cacheDir the cache directory
   * @return a dex file relative to {@code cacheDir}
   */
  static File getDexFileName(File inputFile, File cacheDir) throws IOException {
    String hashed = getHashFor(inputFile);
    return new File(cacheDir, "dex-cached-" + hashed + ".dex");
  }

  /**
   * Computes a hash code for the contents of the given {@code inputFile}.
   *
   * @param inputFile the source file
   * @return a hash of the file's content
   */
  @SuppressWarnings("UnstableApiUsage")
  static String getHashFor(File inputFile) throws IOException {
    String retval = PREDEX_CACHE.get(inputFile);
    if (retval != null) {
      return retval;
    }
    // add a hash of the original file path
    HashFunction hashFunction = Hashing.md5();
    HashCode hashCode = hashFunction.hashBytes(Files.readAllBytes(inputFile.toPath()));
    retval = hashCode.toString();
    PREDEX_CACHE.put(inputFile, retval);
    return retval;
  }
}
