// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import com.google.appinventor.buildserver.Project;
import com.google.appinventor.buildserver.YoungAndroidConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ExecutorUtils {
  private ExecutorUtils() {
  }

  /**
   * Creates a directory at the given {@code dir} location if a File does not already exist at
   * that location.
   *
   * @param dir the directory to create
   * @return a reference to the created directory
   */
  public static File createDir(File dir) {
    if (!dir.exists()) {
      if (!dir.mkdir()) {
        System.out.println("[WARN] Could not create directory: " + dir);
      }
    }
    return dir;
  }

  /**
   * Creates a directory called {@code name} in the directory {@code parentDir}.
   *
   * @param parentDir the parent of the new directory
   * @param name the name of the new directory
   * @return a reference to the created directory
   */
  public static File createDir(File parentDir, String name) {
    File dir = new File(parentDir, name);
    if (!dir.exists()) {
      if (!dir.mkdir()) {
        System.out.println("[WARN] Could not create directory: " + dir);
      }
    }
    return dir;
  }

  /**
   * Copies the file located at {@code srcPath} to {@code destPath}, overwriting the destination.
   *
   * @param srcPath the absolute path to the source
   * @param dstPath the absolute path to the destination
   * @return true if the file was copied, otherwise false
   */
  public static Boolean copyFile(String srcPath, String dstPath) {
    try {
      FileInputStream in = new FileInputStream(srcPath);
      FileOutputStream out = new FileOutputStream(dstPath);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Compute the path to an extension component of {@code type} in {@code project}, reusing the
   * information in {@code extTypePathCache} when possible.
   *
   * @param type the extension type
   * @param project the project containing the extension
   * @param extTypePathCache an existing cache of extracted extension information
   * @return an absolute path to the extension's directory
   */
  public static String getExtCompDirPath(String type, Project project,
      Map<String, String> extTypePathCache) {
    createDir(project.getAssetsDirectory());
    String candidate = extTypePathCache.get(type);
    if (candidate != null) {  // already computed the path
      return candidate;
    }
    candidate = project.getAssetsDirectory().getAbsolutePath() + File.separator
        + YoungAndroidConstants.EXT_COMPS_DIR_NAME + File.separator + type;
    if (new File(candidate).exists()) {  // extension has FCQN as path element
      extTypePathCache.put(type, candidate);
      return candidate;
    }
    candidate = project.getAssetsDirectory().getAbsolutePath() + File.separator
        + YoungAndroidConstants.EXT_COMPS_DIR_NAME + File.separator
        + type.substring(0, type.lastIndexOf('.'));
    if (new File(candidate).exists()) {  // extension has package name as path element
      extTypePathCache.put(type, candidate);
      return candidate;
    }
    throw new IllegalStateException("Project lacks extension directory for " + type);
  }

  /**
   * Adds the {@code values} to the given {@code key} of the {@code map}. If the {@code key} exists
   * in the {@code map}, the {@code values} are added to the existing {@code key}. Otherwise, the
   * {@code values} are inserted into {@code map} at {@code key}.
   *
   * @param map the destination map
   * @param key the target key
   * @param values a set of values to add to {@code map} at {@code key}
   */
  public static void setOrMerge(Map<String, Set<String>> map, String key, Set<String> values) {
    if (map.containsKey(key)) {
      map.get(key).addAll(values);
    } else {
      map.put(key, new HashSet<>(values));
    }
  }
}
