// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import static java.nio.file.Files.newInputStream;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.context.AndroidPaths;
import com.google.appinventor.buildserver.context.CompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.AabPaths;
import com.google.appinventor.buildserver.util.AabZipper;
import com.google.appinventor.buildserver.util.Execution;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@BuildType(aab = true)
public class RunBundletool implements AndroidTask {
  private AabPaths aab;

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

  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    this.aab = new AabPaths();

    context.getReporter().info("Creating structure");
    aab.setRoot(ExecutorUtils.createDir(context.getProject().getBuildDirectory(), "aab"));
    aab.setProtoApk(context.getPaths().getTmpPackageName());
    if (!createStructure(context)) {
      return TaskResult.generateError("Could not create AAB structure");
    }

    context.getReporter().info("Extracting protobuf resources");
    if (!extractProtobuf(context)) {
      return TaskResult.generateError("Could not extract protobuf");
    }

    context.getReporter().info("Running bundletool");
    if (!bundletool(context)) {
      return TaskResult.generateError("Could not run bundletool");
    }

    context.getReporter().info("Signing bundle");
    if (!jarsigner(context)) {
      context.getReporter().warn("We could not sign your app. In case you are using a custom keystore, please make " +
        "sure its password is set to 'android', and the key is set to 'androidkey'.");
      return TaskResult.generateError("Could not sign bundle");
    }
    return TaskResult.generateSuccess();
  }

  private boolean createStructure(CompilerContext<AndroidPaths> context) {
    // Manifest is extracted from the protobuffed APK
    aab.setManifestDir(ExecutorUtils.createDir(aab.getRoot(), "manifest"));

    // Resources are extracted from the protobuffed APK
    aab.setResDir(ExecutorUtils.createDir(aab.getRoot(), "res"));

    // Assets are extracted from the protobuffed APK
    aab.setAssetsDir(ExecutorUtils.createDir(aab.getRoot(), "assets"));

    aab.setDexDir(ExecutorUtils.createDir(aab.getRoot(), "dex"));
    context.getReporter().log("Moving dex files");
    File[] dexFiles = context.getPaths().getTmpDir().listFiles();
    if (dexFiles != null) {
      for (File dex : dexFiles) {
        if (dex.isFile() && dex.getName().endsWith(".dex")) {
          try {
            Files.move(dex, new File(aab.getDexDir(), dex.getName()));
          } catch (IOException e) {
            context.getReporter().error(e.getMessage(), true);
            return false;
          }
        }
      }
    }

    aab.setLibDir(ExecutorUtils.createDir(aab.getRoot(), "lib"));
    context.getReporter().log("Moving lib files");
    File[] libFiles = context.getPaths().getLibsDir().listFiles();
    if (libFiles != null) {
      for (File lib : libFiles) {
        try {
          Files.move(lib, new File(ExecutorUtils.createDir(aab.getRoot(), "lib"), lib.getName()));
        } catch (IOException e) {
          context.getReporter().error(e.getMessage(), true);
          return false;
        }
      }
    }

    return true;
  }

  private boolean extractProtobuf(CompilerContext<AndroidPaths> context) {
    try (ZipInputStream is = new ZipInputStream(newInputStream(aab.getProtoApk().toPath()))) {
      ZipEntry entry;
      byte[] buffer = new byte[1024];
      while ((entry = is.getNextEntry()) != null) {
        String n = entry.getName();
        File f = null;
        if (n.equals("AndroidManifest.xml")) {
          context.getReporter().log("Found AndroidManifest.xml");
          f = new File(aab.getManifestDir(), n);
        } else if (n.equals("resources.pb")) {
          context.getReporter().log("Found resources.pb");
          f = new File(aab.getRoot(), n);
        } else if (n.startsWith("assets")) {
          f = new File(aab.getAssetsDir(), n.substring(("assets").length()));
        } else if (n.startsWith("res")) {
          f = new File(aab.getResDir(), n.substring(("res").length()));
        }

        if (f != null) {
          f.getParentFile().mkdirs();
          FileOutputStream fos = new FileOutputStream(f);
          int len;
          while ((len = is.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
        }
      }

      is.close();
      return true;
    } catch (IOException e) {
      context.getReporter().error(e.getMessage(), true);
    }
    return false;
  }

  private boolean bundletool(CompilerContext<AndroidPaths> context) {
    // Create the bundle configuration
    File configFile;
    try {
      configFile = File.createTempFile("BundleConfig", ".pb.json",
          context.getPaths().getBuildDir());
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

    aab.setBase(new File(context.getProject().getBuildDirectory(), "base.zip"));

    if (!AabZipper.zipBundle(aab.getRoot(), aab.getBase(),
        aab.getRoot().getName() + File.separator)) {
      context.getReporter().error("Could not zip files for the bundle", true);
      return false;
    }

    String bundletool = context.getResources().bundletool();
    if (bundletool == null) {
      context.getReporter().error("Bundletool jar file was not found", true);
      return false;
    }

    List<String> bundletoolCommandLine = new ArrayList<String>();
    bundletoolCommandLine.add(System.getProperty("java.home") + "/bin/java");
    bundletoolCommandLine.add("-jar");
    bundletoolCommandLine.add("-mx" + context.getChildProcessRam() + "M");
    bundletoolCommandLine.add(bundletool);
    bundletoolCommandLine.add("build-bundle");
    bundletoolCommandLine.add("--modules=" + aab.getBase());
    bundletoolCommandLine.add("--config=" + configFile.getAbsolutePath());
    bundletoolCommandLine.add("--output=" + context.getPaths().getDeployFile().getAbsolutePath());
    String[] bundletoolBuildCommandLine = bundletoolCommandLine.toArray(new String[0]);

    return Execution.execute(null, bundletoolBuildCommandLine,
        System.out, System.err, Execution.Timeout.LONG);
  }

  private boolean jarsigner(CompilerContext<AndroidPaths> context) {
    List<String> jarsignerCommandLine = new ArrayList<>();

    String jarsigner = context.getResources().jarsigner();
    if (jarsigner == null) {
      context.getReporter().error("Jarsigner executable file was not found", true);
      return false;
    }

    jarsignerCommandLine.add(jarsigner);
    jarsignerCommandLine.add("-sigalg");
    jarsignerCommandLine.add("SHA256withRSA");
    jarsignerCommandLine.add("-digestalg");
    jarsignerCommandLine.add("SHA-256");
    jarsignerCommandLine.add("-keystore");
    jarsignerCommandLine.add(context.getKeystoreFilePath());
    jarsignerCommandLine.add("-storepass");
    jarsignerCommandLine.add("android");
    jarsignerCommandLine.add(context.getPaths().getDeployFile().getAbsolutePath());
    jarsignerCommandLine.add("AndroidKey");
    String[] jarsignerSignCommandLine = jarsignerCommandLine.toArray(new String[0]);

    return Execution.execute(null, jarsignerSignCommandLine,
        System.out, System.err, Execution.Timeout.SHORT);
  }
}
