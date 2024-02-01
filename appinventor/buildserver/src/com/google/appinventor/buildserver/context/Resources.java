// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.context;

import com.google.appinventor.buildserver.Compiler;
import com.google.appinventor.buildserver.PathUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.imageio.ImageIO;

@SuppressWarnings("UnstableApiUsage")
public class Resources {
  private static final ConcurrentMap<String, File> RESOURCES = new ConcurrentHashMap<>();
  private final List<File> dexFiles;

  private String[] supportJars;
  private String[] supportAars;

  private File appRTxt;

  // Kawa and DX processes can use a lot of memory. We only launch one Kawa or DX process at a time.
  private static final Object SYNC_KAWA_OR_DX = new Object();

  public static final String RUNTIME_FILES_DIR = "/files/";
  public static final String RUNTIME_TOOLS_DIR = "/tools/";
  private static final String ANDROID_RUNTIME = RUNTIME_FILES_DIR + "android.jar";
  private static final String ACRA_RUNTIME = RUNTIME_FILES_DIR + "acra-4.4.0.jar";
  private static final String KAWA_RUNTIME = RUNTIME_FILES_DIR + "kawa.jar";
  private static final String SIMPLE_ANDROID_RUNTIME_JAR = RUNTIME_FILES_DIR + "AndroidRuntime.jar";
  private static final String DX_JAR = RUNTIME_TOOLS_DIR + "dx.jar";
  private static final String D8_JAR = RUNTIME_TOOLS_DIR + "d8.jar";
  private static final String APKSIGNER_JAR = RUNTIME_TOOLS_DIR + "apksigner.jar";

  public static final String YAIL_RUNTIME = RUNTIME_FILES_DIR + "runtime.scm";
  private static final String DEFAULT_ICON = RUNTIME_FILES_DIR + "ya.png";

  private static final String COMP_BUILD_INFO = RUNTIME_FILES_DIR
      + "simple_components_build_info.json";
  private static final String BUNDLETOOL_JAR = RUNTIME_TOOLS_DIR + "bundletool.jar";

  public Resources() {
    dexFiles = new ArrayList<>();
  }

  /**
   * Unpack the given resource and return a path to the extracted file.
   *
   * @param resourcePath the path to the desired resource
   * @return a path in the filesystem to the resource, or null if it is not found
   */
  public String getResource(String resourcePath) {
    synchronized (RESOURCES) {
      try {
        File file = RESOURCES.get(resourcePath);
        if (file == null) {
          String basename = PathUtil.basename(resourcePath);
          StringBuilder prefix;
          String suffix;
          int lastDot = basename.lastIndexOf(".");
          if (lastDot != -1) {
            prefix = new StringBuilder(basename.substring(0, lastDot));
            suffix = basename.substring(lastDot);
          } else {
            prefix = new StringBuilder(basename);
            suffix = "";
          }
          while (prefix.length() < 3) {
            prefix.append("_");
          }
          file = File.createTempFile(prefix.toString(), suffix);
          if (!file.setExecutable(true)) {
            System.out.println("[WARN] Could not mark resources as executable: " + file);
          }
          file.deleteOnExit();
          if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            System.out.println("[WARN] Could not make directory: " + file.getParentFile());
          }
          Files.copy(com.google.common.io.Resources.newInputStreamSupplier(
              Objects.requireNonNull(Compiler.class.getResource(resourcePath))), file);
          RESOURCES.put(resourcePath, file);
        }
        return file.getAbsolutePath();
      } catch (IOException | NullPointerException e) {
        System.out.println("[ERROR] " + e.getMessage());
        return null;
      }
    }
  }

  public List<File> getDexFiles() {
    return dexFiles;
  }

  public Object getSyncKawaOrDx() {
    return SYNC_KAWA_OR_DX;
  }

  public String getRuntimeFilesDir() {
    return Resources.RUNTIME_FILES_DIR;
  }

  public String getAndroidRuntime() {
    return getResource(Resources.ANDROID_RUNTIME);
  }

  public String getAcraRuntime() {
    return getResource(ACRA_RUNTIME);
  }

  public String getKawaRuntime() {
    return getResource(KAWA_RUNTIME);
  }

  public String getSimpleAndroidRuntimeJar() {
    return getResource(SIMPLE_ANDROID_RUNTIME_JAR);
  }

  public String getSimpleAndroidRuntimeJarPath() {
    return SIMPLE_ANDROID_RUNTIME_JAR;
  }

  public String getDxJar() {
    return getResource(DX_JAR);
  }

  public String getD8Jar() {
    return getResource(D8_JAR);
  }

  public String getApksignerJar() {
    return getResource(APKSIGNER_JAR);
  }

  public String[] getSupportJars() {
    return supportJars;
  }

  public void setSupportJars(String[] supportJars) {
    this.supportJars = supportJars;
  }

  public String[] getSupportAars() {
    return supportAars;
  }

  public void setSupportAars(String[] supportAars) {
    this.supportAars = supportAars;
  }

  public File getAppRTxt() {
    return appRTxt;
  }

  public void setAppRTxt(File appRTxt) {
    this.appRTxt = appRTxt;
  }

  public String getYailRuntime() {
    return getResource(YAIL_RUNTIME);
  }

  public BufferedImage getDefaultIcon() throws IOException {
    return ImageIO.read(Objects.requireNonNull(Compiler.class.getResource(DEFAULT_ICON)));
  }

  /**
   * Get the component build info.
   *
   * @return the build info for the built-in components
   */
  public String getCompBuildInfo() {
    try {
      return com.google.common.io.Resources.toString(
          Objects.requireNonNull(Resources.class.getResource(COMP_BUILD_INFO)),
          Charsets.UTF_8);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Get the path to the aapt executable for the current platform.
   *
   * @return absolute path to aapt
   */
  public String aapt() {
    String osName = System.getProperty("os.name");
    String aaptTool;
    if (osName.equals("Mac OS X")) {
      aaptTool = RUNTIME_TOOLS_DIR + "mac/aapt";
    } else if (osName.equals("Linux")) {
      aaptTool = RUNTIME_TOOLS_DIR + "linux/aapt";
    } else if (osName.startsWith("Windows")) {
      aaptTool = RUNTIME_TOOLS_DIR + "windows/aapt";
    } else {
      aaptTool = null;
    }
    if (aaptTool != null) {
      return getResource(aaptTool);
    }
    return null;
  }

  /**
   * Get the path to the aapt2 executable for the current platform.
   *
   * @return absolute path to aapt2
   */
  public String aapt2() {
    String osName = System.getProperty("os.name");
    String aaptTool;
    if (osName.equals("Mac OS X")) {
      aaptTool = RUNTIME_TOOLS_DIR + "mac/aapt2";
    } else if (osName.equals("Linux")) {
      aaptTool = RUNTIME_TOOLS_DIR + "linux/aapt2";
    } else if (osName.startsWith("Windows")) {
      aaptTool = RUNTIME_TOOLS_DIR + "windows/aapt2";
    } else {
      aaptTool = null;
    }
    if (aaptTool != null) {
      return getResource(aaptTool);
    }
    return null;
  }

  /**
   * Get the path to the zipalign executable for the current platform.
   *
   * @return absolute path to zipalign
   */
  public String zipalign() {
    String osName = System.getProperty("os.name");
    String zipAlignTool;
    if (osName.equals("Mac OS X")) {
      zipAlignTool = RUNTIME_TOOLS_DIR + "mac/zipalign";
    } else if (osName.equals("Linux")) {
      zipAlignTool = RUNTIME_TOOLS_DIR + "linux/zipalign";
    } else if (osName.startsWith("Windows")) {
      zipAlignTool = RUNTIME_TOOLS_DIR + "windows/zipalign";
    } else {
      zipAlignTool = null;
    }
    if (zipAlignTool != null) {
      return getResource(zipAlignTool);
    }
    return null;
  }

  /**
   * Get the path to the jarsigner executable for the current platform.
   *
   * @return absolute path to jarsigner
   */
  public String jarsigner() {
    return "jarsigner";  // Use the version of jarsigner in PATH
  }

  public String bundletool() {
    return this.getResource(BUNDLETOOL_JAR);
  }
}
