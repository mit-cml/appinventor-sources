package com.google.appinventor.buildserver;

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

  public static File createDir(File dir) {
    if (!dir.exists()) {
      if (!dir.mkdir()) {
        System.out.println("[WARN] Could not create directory: " + dir);
      }
    }
    return dir;
  }

  public static File createDir(File parentDir, String name) {
    File dir = new File(parentDir, name);
    if (!dir.exists()) {
      if (!dir.mkdir()) {
        System.out.println("[WARN] Could not create directory: " + dir);
      }
    }
    return dir;
  }

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
    }
    catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public static String getExtCompDirPath(String type, Project project, Map<String, String> extTypePathCache) {
    createDir(project.getAssetsDirectory());
    String candidate = extTypePathCache.get(type);
    if (candidate != null) {  // already computed the path
      return candidate;
    }
    candidate = project.getAssetsDirectory().getAbsolutePath() + File.separator +
        YoungAndroidConstants.EXT_COMPS_DIR_NAME + File.separator + type;
    if (new File(candidate).exists()) {  // extension has FCQN as path element
      extTypePathCache.put(type, candidate);
      return candidate;
    }
    candidate = project.getAssetsDirectory().getAbsolutePath() + File.separator +
        YoungAndroidConstants.EXT_COMPS_DIR_NAME + File.separator + type.substring(0, type.lastIndexOf('.'));
    if (new File(candidate).exists()) {  // extension has package name as path element
      extTypePathCache.put(type, candidate);
      return candidate;
    }
    throw new IllegalStateException("Project lacks extension directory for " + type);
  }

  public static void setOrMerge(Map<String, Set<String>> map, String key, Set<String> values) {
    if (map.containsKey(key)) {
      map.get(key).addAll(values);
    } else {
      map.put(key, new HashSet<>(values));
    }
  }
}
