// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tool to generate component.json for extension.
 *
 * @author mouha.oumar@gmail.com (Mouhamadou O. Sall)
 */
public class ExternalComponentGenerator {

  private static String externalComponentsDirPath;
  private static String androidRuntimeClassDirPath;
  private static String buildServerClassDirPath;
  private static String externalComponentsTempDirPath;
  private static boolean useFQCN = false;

  private static Map<String, List<ExternalComponentInfo>> externalComponentsByPackage =
      new TreeMap<>();


  public static void main(String[] args) throws IOException, JSONException {
    if (args.length < 7) {
      throw new IllegalArgumentException("Insufficient arguments provided.");
    }

    initializePaths(args);
    JSONArray simpleComponentDescriptors = new JSONArray(readFile(args[0], Charset.defaultCharset()));
    JSONArray simpleComponentBuildInfos = new JSONArray(readFile(args[1], Charset.defaultCharset()));
    Map<String, JSONObject> buildInfos = buildInfoAsMap(simpleComponentBuildInfos);

    processComponentDescriptors(simpleComponentDescriptors, buildInfos);
    generateAllExtensions();
  }


  private static void initializePaths(String[] args) {
    externalComponentsDirPath = args[2];
    androidRuntimeClassDirPath = args[3];
    buildServerClassDirPath = args[4];
    externalComponentsTempDirPath = args[5];
    useFQCN = Boolean.parseBoolean(args[6]);
  }


  private static void processComponentDescriptors(JSONArray simpleComponentDescriptors, Map<String, JSONObject> buildInfos) throws JSONException {
    for (int i = 0; i < simpleComponentDescriptors.length(); i++) {
      JSONObject componentDescriptor = simpleComponentDescriptors.getJSONObject(i);
      if (componentDescriptor.getBoolean("external")) {
        ExternalComponentInfo info = new ExternalComponentInfo(componentDescriptor, buildInfos.get(componentDescriptor.getString("type")));
        externalComponentsByPackage.computeIfAbsent(info.packageName, k -> new ArrayList<>()).add(info);
      }
    }
  }


  private static void generateAllExtensions() throws IOException, JSONException {
    System.out.println("\nExtensions : Generating extensions");
    for (Map.Entry<String, List<ExternalComponentInfo>> entry : externalComponentsByPackage.entrySet()) {
      String name = useFQCN && entry.getValue().size() == 1 ? entry.getValue().get(0).type : entry.getKey();
      System.out.println("\nExtensions : Generating files [" + name + "]");
      generateExternalComponentDescriptors(name, entry.getValue());
      for (ExternalComponentInfo info : entry.getValue()) {
        copyIcon(name, info.descriptor);
        copyLicense(name, info.descriptor);
        copyAssets(name, info.descriptor);
      }
      generateExternalComponentBuildFiles(name, entry.getValue());
      generateExternalComponentOtherFiles(name);
    }
  }


  private static void generateExternalComponentDescriptors(String packageName, List<ExternalComponentInfo> infos) throws IOException, JSONException {
    String components = createComponentsJson(infos);
    String extensionDirPath = externalComponentsDirPath + File.separator + packageName;
    ensureDirectory(extensionDirPath, "Unable to create extension directory");

    writeFile(extensionDirPath + File.separator + "components.json", components);
    writeFile(extensionDirPath + File.separator + "component.json", infos.get(0).descriptor.toString(1));
  }


  private static String createComponentsJson(List<ExternalComponentInfo> infos) throws JSONException {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < infos.size(); i++) {
      if (i > 0) sb.append(',');
      sb.append(infos.get(i).descriptor.toString(1));
    }
    sb.append(']');
    return sb.toString();
  }


  private static void generateExternalComponentBuildFiles(String packageName, List<ExternalComponentInfo> extensions) throws IOException {
    String extensionTempDirPath = externalComponentsTempDirPath + File.separator + packageName;
    copyRelatedExternalClasses(androidRuntimeClassDirPath, androidRuntimeClassDirPath, packageName, extensionTempDirPath);

    File commonConstants = new File(new File (androidRuntimeClassDirPath).getParentFile(), "CommonConstants");
    copyRelatedExternalClasses(commonConstants.getAbsolutePath(), commonConstants.getAbsolutePath(), packageName, extensionTempDirPath);

    JSONArray buildInfos = new JSONArray();
    for (ExternalComponentInfo info : extensions) {
      JSONObject componentBuildInfo = info.buildInfo;
      processLibraries(componentBuildInfo, buildInfos, extensionTempDirPath);
      buildInfos.put(componentBuildInfo);
    }

    createBuildInfoFiles(packageName, buildInfos);
  }


  private static void processLibraries(JSONObject componentBuildInfo, JSONArray buildInfos, String extensionTempDirPath) throws JSONException {
    try {
      JSONArray librariesNeeded = componentBuildInfo.getJSONArray("libraries");
      for (int j = 0; j < librariesNeeded.length(); ++j) {
        String library = librariesNeeded.getString(j);
        copyFile(buildServerClassDirPath + File.separator + library, extensionTempDirPath + File.separator + library);
      }
      componentBuildInfo.put("libraries", new JSONArray());
    } catch (JSONException e) {
      throw new IllegalStateException("Unexpected JSON exception parsing simple_components.json", e);
    }
  }


  private static void createBuildInfoFiles(String packageName, JSONArray buildInfos) throws IOException {
    String extensionDirPath = externalComponentsDirPath + File.separator + packageName;
    String extensionFileDirPath = extensionDirPath + File.separator + "files";
    ensureDirectory(extensionFileDirPath, "Unable to create path for component_build_info.json");

    writeFile(extensionFileDirPath + File.separator + "component_build_infos.json", buildInfos.toString());
    writeFile(extensionFileDirPath + File.separator + "component_build_info.json", buildInfos.get(0).toString());
  }


  private static void copyIcon(String packageName, JSONObject componentDescriptor) throws IOException, JSONException {
    String icon = componentDescriptor.getString("iconName");
    if (icon.isEmpty() || icon.startsWith("http:") || icon.startsWith("https:")) {
      return;
    }
    copyFileFromSource(packageName, icon, "icon");
  }


  private static void copyLicense(String packageName, JSONObject componentDescriptor) throws IOException, JSONException {
    String license = componentDescriptor.getString("licenseName");
    if (license.isEmpty() || license.startsWith("http:") || license.startsWith("https:")) {
      return;
    }
    copyFileFromSource(packageName, license, "license");
  }


  private static void copyFileFromSource(String packageName, String fileName, String fileType) throws IOException {
    String packagePath = packageName.replace('.', File.separatorChar);
    File sourceDir = new File(externalComponentsDirPath + File.separator + ".." + File.separator + ".." + File.separator + "src" + File.separator + packagePath);
    File file = new File(sourceDir, fileName);
    if (file.exists()) {
      File destinationFile = new File(externalComponentsDirPath + File.separator + packageName + File.separator + fileName);
      ensureDirectory(destinationFile.getParent(), "Unable to create directory " + destinationFile.getParent());
      System.out.println("Extensions : Copying " + fileType + " file " + file.getAbsolutePath());
      copyFile(file.getAbsolutePath(), destinationFile.getAbsolutePath());
    } else {
      System.out.println("Extensions : Skipping missing " + fileType + " " + fileName);
    }
  }


  private static void copyAssets(String packageName, JSONObject componentDescriptor) throws IOException, JSONException {
    JSONArray assets = componentDescriptor.optJSONArray("assets");
    if (assets == null) {
      return;
    }

    String packagePath = packageName.replace('.', File.separatorChar);
    File sourceDir = new File(externalComponentsDirPath + File.separator + ".." + File.separator + ".." + File.separator + "src" + File.separator + packagePath);
    File assetSrcDir = new File(sourceDir, "assets");
    if (!assetSrcDir.exists() || !assetSrcDir.isDirectory()) {
      return;
    }

    File destDir = new File(externalComponentsDirPath + File.separator + packageName + File.separator + "assets");
    ensureFreshDirectory(destDir.getPath(), "Unable to delete the assets directory for the extension.");

    for (int i = 0; i < assets.length(); i++) {
      String asset = assets.getString(i);
      if (!asset.isEmpty()) {
        if (!copyFile(assetSrcDir.getAbsolutePath() + File.separator + asset, destDir.getAbsolutePath() + File.separator + asset)) {
          throw new IllegalStateException("Unable to copy asset to destination.");
        }
      }
    }
  }


  private static void generateExternalComponentOtherFiles(String packageName) throws IOException {
    String extensionDirPath = externalComponentsDirPath + File.separator + packageName;

    StringBuilder extensionPropertiesString = new StringBuilder();
    extensionPropertiesString.append("type=external\n");
    writeFile(extensionDirPath + File.separator + "extension.properties", extensionPropertiesString.toString());
  }


  private static void writeFile(String path, String content) throws IOException {
    try (FileWriter fileWriter = new FileWriter(path)) {
      fileWriter.write(content);
      System.out.println("Extensions : Successfully created " + path);
    } catch (IOException e) {
      throw new IOException("Error writing to file: " + path, e);
    }
  }


  private static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }


  private static Boolean copyFile(String srcPath, String dstPath) {
    try (FileInputStream in = new FileInputStream(srcPath);
         FileOutputStream out = new FileOutputStream(dstPath)) {
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }


  private static void copyRelatedExternalClasses(final String basedir, final String srcPath,
      String extensionPackage, final String destPath) throws IOException {
    File srcFolder = new File(srcPath);
    File[] files = srcFolder.listFiles();
    if (files == null) {
      return;
    }
    for (File fileEntry : files) {
      if (fileEntry.isFile() && isRelatedExternalClass(basedir, fileEntry.getAbsolutePath(), extensionPackage)) {
        System.out.println("Extensions : Copying file " + getClassPackage(basedir, fileEntry.getAbsolutePath()).replace(".", File.separator) + File.separator + fileEntry.getName());
        copyFile(fileEntry.getAbsolutePath(), destPath + File.separator + fileEntry.getName());
      } else if (fileEntry.isDirectory()) {
        String newDestPath = destPath + fileEntry.getAbsolutePath().substring(srcFolder.getAbsolutePath().length());
        ensureDirectory(newDestPath, "Unable to create temporary path for extension build");
        copyRelatedExternalClasses(basedir, fileEntry.getAbsolutePath(), extensionPackage, newDestPath);
      }
    }
  }


  private static boolean isRelatedExternalClass(final String basedir, final String testClassAbsolutePath, final String extensionPackage) {
    if (!testClassAbsolutePath.endsWith(".class")) {
      return false;
    }
    String componentPackagePath = extensionPackage.replace(".", File.separator);
    String testClassPath = getClassPackage(basedir, testClassAbsolutePath).replace(".", File.separator);
    return testClassPath.startsWith(componentPackagePath);
  }


  private static String getClassPackage(String parentPath, String classAbsolutePath) {
    if (!parentPath.endsWith("/")) {
      parentPath += "/";
    }
    parentPath = parentPath.replace("/", File.separator);
    String componentPackage = classAbsolutePath.substring(classAbsolutePath.indexOf(parentPath) + parentPath.length());
    componentPackage = componentPackage.substring(0, componentPackage.lastIndexOf(File.separator));
    return componentPackage.replace(File.separator, ".");
  }


  private static boolean deleteRecursively(File dirOrFile) {
    if (dirOrFile.isFile()) {
      return dirOrFile.delete();
    } else {
      boolean result = true;
      File[] children = dirOrFile.listFiles();
      if (children != null) {
        for (File child : children) {
          result = result && deleteRecursively(child);
        }
      }
      return result && dirOrFile.delete();
    }
  }


  private static void ensureFreshDirectory(String path, String errorMessage) throws IOException {
    File file = new File(path);
    if (file.exists() && !deleteRecursively(file)) {
      throw new IOException(errorMessage);
    }
    if (!file.mkdirs()) {
      throw new IOException(errorMessage);
    }
  }


  private static void ensureDirectory(String path, String errorMessage) throws IOException {
    File file = new File(path);
    if (!file.exists() && !file.mkdirs()) {
      throw new IOException(errorMessage);
    }
    }
  }