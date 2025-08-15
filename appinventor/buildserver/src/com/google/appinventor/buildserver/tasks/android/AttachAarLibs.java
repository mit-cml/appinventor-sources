// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.YoungAndroidConstants;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.context.AndroidPaths;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.AARLibraries;
import com.google.appinventor.buildserver.util.AARLibrary;
import com.google.appinventor.buildserver.util.ExecutorUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.ASSETS_FOLDER;


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

    // Gather AAR assets to be added to apk's Asset directory.
    // The assets directory have been created before this.
    File mergedAssetDir = ExecutorUtils.createDir(context.getProject().getBuildDirectory(),
            ASSETS_FOLDER);

    // Root directory for JNI native (.so) libraries
    AndroidPaths paths = context.getPaths();
    File libsDir = ExecutorUtils.createDir(paths.getBuildDir(), YoungAndroidConstants.LIBS_DIR_NAME);
    paths.setLibsDir(libsDir);

    // walk components list for libraries ending in ".aar"
    try {
      final HashSet<String> attachedAARs = new HashSet<>();
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

              File aarFile = new File(sourcePath);
              // Resolve possible conflicts
              final String packageName = getAarPackageName(aarFile);
              if (packageName == null || packageName.trim().isEmpty()) {
                context.getReporter().error("Unable to read packageName from: " + aarFile.getName(), true);
                return TaskResult.generateError("Unable to read packageName from: " + aarFile.getName());
              }
              if (!attachedAARs.contains(packageName)) {
                // explode libraries into ${buildDir}/exploded-aars/<package>/
                AARLibrary aarLib = new AARLibrary(aarFile);
                aarLib.unpackToDirectory(explodedBaseDir);
                context.getComponentInfo().getExplodedAarLibs().add(aarLib);
                // Attach assets files & jni libraries if available
                copyAssetsAndJni(aarFile, mergedAssetDir, libsDir);
                processedLibs.add(libname);
                attachedAARs.add(packageName);
              } else {
                System.out.println("Skip attaching duplicate AAR: " + aarFile.getName());
              }
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

  private void copyAssetsAndJni(File aarFile, File mergedAssetDir, File libsDir) throws IOException {
    try (ZipFile zip = new ZipFile(aarFile)) {
      Enumeration<? extends ZipEntry> entries = zip.entries();

      final Set<String> supportedABIs = new HashSet<>();
      supportedABIs.add("arm64-v8a");
      supportedABIs.add("armeabi-v7a");
      supportedABIs.add("x86");
      supportedABIs.add("x86_64");

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        File targetFile = null;
        final String entryName = entry.getName();

        if (entryName.startsWith("assets/")) {
          if (!entry.isDirectory()) {
            targetFile = new File(mergedAssetDir, entryName.substring("assets/".length()));
            if (!targetFile.getParentFile().exists()) {
              // The target file may contain subfolders.
              targetFile.getParentFile().mkdirs();
            }
          }
        } else if (entryName.startsWith("jni/") && entryName.endsWith(".so")) {
          final String[] array = entryName.split("/", 3);
          if (array.length < 3) {
            continue;
          }
          final String abi = array[1];
          if (!supportedABIs.contains(abi)) {
            System.out.println("Skip merging not supported ABI: " + abi);
            continue;
          }
          final String libName = array[2];
          if (!entry.isDirectory()) {
            File parentFile = new File(libsDir, abi);
            if (!parentFile.exists()) {
              // Create the parent ABI directory if absent
              parentFile.mkdir();
            }
            targetFile = new File(parentFile, libName);
          }
        }
        if (targetFile != null && !targetFile.exists()) {
          // Copy file contents from ZIP entry
          try (InputStream is = zip.getInputStream(entry);
               OutputStream os = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
              os.write(buffer, 0, bytesRead);
            }
          }
        }
      }
    }
  }

  private String getAarPackageName(File aarFile) throws IOException {
    try (ZipFile zip = new ZipFile(aarFile)) {
      ZipEntry manifestEntry = zip.getEntry("AndroidManifest.xml");
      if (manifestEntry == null) {
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
          ZipEntry e = entries.nextElement();
          if (!e.isDirectory() && e.getName().endsWith("AndroidManifest.xml")) {
            manifestEntry = e;
            break;
          }
        }
      }
      if (manifestEntry == null) {
        return null; // No manifest found
      }

      try (InputStream in = zip.getInputStream(manifestEntry);
           BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.contains("package=\"")) {
            int start = line.indexOf("package=\"") + "package=\"".length();
            int end = line.indexOf("\"", start);
            return line.substring(start, end);
          }
        }
      }

    }
    return null;
  }
}
