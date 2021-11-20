// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.appinventor.buildserver.util.AabZipper;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Callable;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This Callable class will convert the compiled files into an Android App Bundle.
 * An AAB file structure looks like this:
 * - assets.pb
 * - resources.pb
 * - native.pb
 * - manifest/AndroidManifest.xml
 * - dex/
 * - res/
 * - assets/
 * - lib/
 */
public class AabCompiler implements Callable<Boolean> {

  // These regexes were taken from a project compiled via Android Studio
  private static final String[] NONCOMPRESSIBLE_EXTS = new String[] {
      "**.3[gG]2",
      "**.3[gG][pP]",
      "**.3[gG][pP][pP]",
      "**.3[gG][pP][pP]2",
      "**.[aA][aA][cC]",
      "**.[aA][mM][rR]",
      "**.[aA][wW][bB]",
      "**.[gG][iI][fF]",
      "**.[iI][mM][yY]",
      "**.[jJ][eE][tT]",
      "**.[jJ][pP][eE][gG]",
      "**.[jJ][pP][gG]",
      "**.[mM]4[aA]",
      "**.[mM]4[vV]",
      "**.[mM][iI][dD]",
      "**.[mM][iI][dD][iI]",
      "**.[mM][kK][vV]",
      "**.[mM][pP]2",
      "**.[mM][pP]3",
      "**.[mM][pP]4",
      "**.[mM][pP][eE][gG]",
      "**.[mM][pP][gG]",
      "**.[oO][gG][gG]",
      "**.[oO][pP][uU][sS]",
      "**.[pP][nN][gG]",
      "**.[rR][tT][tT][tT][lL]",
      "**.[sS][mM][fF]",
      "**.[tT][fF][lL][iI][tT][eE]",
      "**.[wW][aA][vV]",
      "**.[wW][eE][bB][mM]",
      "**.[wW][eE][bB][pP]",
      "**.[wW][mM][aA]",
      "**.[wW][mM][vV]",
      "**.[xX][mM][fF]"
  };

  private PrintStream out;
  private File buildDir;
  private int mx;

  private AabPaths aab;
  private String originalDexDir = null;
  private File originalLibsDir = null;

  private String bundletool = null;
  private String jarsigner = null;

  private String deploy = null;
  private String keystore = null;

  private class AabPaths {
    private File root = null;
    private File base = null;
    private File protoApk = null;

    private File assetsDir = null;
    private File dexDir = null;
    private File libDir = null;
    private File manifestDir = null;
    private File resDir = null;

    public File getRoot() {
      return root;
    }

    public void setRoot(File root) {
      this.root = root;
    }

    public File getBase() {
      return base;
    }

    public void setBase(File base) {
      this.base = base;
    }

    public File getProtoApk() {
      return protoApk;
    }

    public void setProtoApk(File protoApk) {
      this.protoApk = protoApk;
    }

    public File getAssetsDir() {
      return assetsDir;
    }

    public void setAssetsDir(File assetsDir) {
      this.assetsDir = assetsDir;
    }

    public File getDexDir() {
      return dexDir;
    }

    public void setDexDir(File dexDir) {
      this.dexDir = dexDir;
    }

    public File getLibDir() {
      return libDir;
    }

    public void setLibDir(File libDir) {
      this.libDir = libDir;
    }

    public File getManifestDir() {
      return manifestDir;
    }

    public void setManifestDir(File manifestDir) {
      this.manifestDir = manifestDir;
    }

    public File getResDir() {
      return resDir;
    }

    public void setResDir(File resDir) {
      this.resDir = resDir;
    }
  }

  public AabCompiler(PrintStream out, File buildDir, int mx) {
    assert out != null;
    assert buildDir != null;
    assert mx > 0;

    this.out = out;
    this.buildDir = buildDir;
    this.mx = mx;

    aab = new AabPaths();
  }

  public AabCompiler setDexDir(String dexDir) {
    this.originalDexDir = dexDir;
    return this;
  }

  public AabCompiler setLibsDir(File libsDir) {
    this.originalLibsDir = libsDir;
    return this;
  }

  public AabCompiler setBundletool(String bundletool) {
    this.bundletool = bundletool;
    return this;
  }

  public AabCompiler setDeploy(String deploy) {
    this.deploy = deploy;
    return this;
  }

  public AabCompiler setKeystore(String keystore) {
    this.keystore = keystore;
    return this;
  }

  public AabCompiler setJarsigner(String jarsigner) {
    this.jarsigner = jarsigner;
    return this;
  }

  public AabCompiler setProtoApk(File apk) {
    aab.setProtoApk(apk);
    return this;
  }

  private static File createDir(File parentDir, String name) {
    File dir = new File(parentDir, name);
    if (!dir.exists()) {
      dir.mkdir();
    }
    return dir;
  }

  @Override
  public Boolean call() {
    out.println("___________Creating structure");
    aab.setRoot(createDir(buildDir, "aab"));
    if (!createStructure()) {
      return false;
    }

    out.println("___________Extracting protobuf resources");
    if (!extractProtobuf()) {
      return false;
    }

    out.println("________Running bundletool");
    if (!bundletool()) {
      return false;
    }

    out.println("________Signing bundle");
    if (!jarsigner()) {
      return false;
    }
    return true;
  }

  private boolean createStructure() {
    // Manifest is extracted from the protobuffed APK
    aab.setManifestDir(createDir(aab.root, "manifest"));

    // Resources are extracted from the protobuffed APK
    aab.setResDir(createDir(aab.root, "res"));

    // Assets are extracted from the protobuffed APK
    aab.setAssetsDir(createDir(aab.root, "assets"));

    aab.setDexDir(createDir(aab.root, "dex"));
    File[] dexFiles = new File(originalDexDir).listFiles();
    if (dexFiles != null) {
      for (File dex : dexFiles) {
        if (dex.isFile()) {
          try {
            Files.move(dex, new File(aab.dexDir, dex.getName()));
          } catch (IOException e) {
            e.printStackTrace();
            return false;
          }
        }
      }
    }

    aab.setLibDir(createDir(aab.root, "lib"));
    File[] libFiles = originalLibsDir.listFiles();
    if (libFiles != null) {
      for (File lib : libFiles) {
        try {
          Files.move(lib, new File(createDir(aab.root, "lib"), lib.getName()));
        } catch (IOException e) {
          e.printStackTrace();
          return false;
        }
      }
    }

    return true;
  }

  private boolean extractProtobuf() {
    try (ZipInputStream is = new ZipInputStream(new FileInputStream(aab.getProtoApk()))) {
      ZipEntry entry;
      byte[] buffer = new byte[1024];
      while ((entry = is.getNextEntry()) != null) {
        String n = entry.getName();
        File f = null;
        if (n.equals("AndroidManifest.xml")) {
          f = new File(aab.getManifestDir(), n);
        } else if (n.equals("resources.pb")) {
          f = new File(aab.getRoot(), n);
        } else if (n.startsWith("assets")) {
          f = new File(aab.getAssetsDir(), n.substring(("assets").length()));
        } else if (n.startsWith("res")) {
          f = new File(aab.getResDir(), n.substring(("res").length()));
        }

        if (f != null) {
          f.getParentFile().mkdirs();
          try (FileOutputStream fos = new FileOutputStream(f)) {
            int len;
            while ((len = is.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
          } catch (IOException e) {
            e.printStackTrace();
            return false;
          }
        }
      }

      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private boolean bundletool() {
    // Create the bundle configuration
    File configFile;
    try {
      configFile = File.createTempFile("BundleConfig", ".pb.json");
    } catch (IOException e) {
      throw new RuntimeException("Unable to generate bundle config", e);
    }
    try (FileOutputStream out = new FileOutputStream(configFile)) {
      JSONObject config = new JSONObject();
      JSONObject compression = new JSONObject();
      JSONArray uncompressedGlob = new JSONArray();
      config.put("compression", compression);
      compression.put("uncompressedGlob", uncompressedGlob);
      for (String ext : NONCOMPRESSIBLE_EXTS) {
        uncompressedGlob.put(ext);
      }
      out.write(config.toString().getBytes(StandardCharsets.UTF_8));
    } catch (JSONException | IOException e) {
      throw new RuntimeException("Unable to generate AAB", e);
    }

    aab.setBase(new File(buildDir, "base.zip"));

    if (!AabZipper.zipBundle(aab.getRoot(), aab.getBase(), aab.getRoot().getName() + File.separator)) {
      return false;
    }

    List<String> bundletoolCommandLine = new ArrayList<String>();
    bundletoolCommandLine.add(System.getProperty("java.home") + "/bin/java");
    bundletoolCommandLine.add("-jar");
    bundletoolCommandLine.add("-mx" + mx + "M");
    bundletoolCommandLine.add(bundletool);
    bundletoolCommandLine.add("build-bundle");
    bundletoolCommandLine.add("--modules=" + aab.getBase());
    bundletoolCommandLine.add("--config=" + configFile.getAbsolutePath());
    bundletoolCommandLine.add("--output=" + deploy);
    String[] bundletoolBuildCommandLine = bundletoolCommandLine.toArray(new String[0]);

    return Execution.execute(null, bundletoolBuildCommandLine, System.out, System.err);
  }

  private boolean jarsigner() {
    List<String> jarsignerCommandLine = new ArrayList<String>();
    jarsignerCommandLine.add(jarsigner);
    jarsignerCommandLine.add("-sigalg");
    jarsignerCommandLine.add("SHA256withRSA");
    jarsignerCommandLine.add("-digestalg");
    jarsignerCommandLine.add("SHA-256");
    jarsignerCommandLine.add("-keystore");
    jarsignerCommandLine.add(keystore);
    jarsignerCommandLine.add("-storepass");
    jarsignerCommandLine.add("android");
    jarsignerCommandLine.add(deploy);
    jarsignerCommandLine.add("AndroidKey");
    String[] jarsignerSignCommandLine = jarsignerCommandLine.toArray(new String[0]);

    return Execution.execute(null, jarsignerSignCommandLine, System.out, System.err);
  }
}
