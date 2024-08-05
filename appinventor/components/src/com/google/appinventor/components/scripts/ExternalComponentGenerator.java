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

  /**
  * The definitions of the arguments used by this script
  *
  * args[0]: the path to simple_component.json
  * args[1]: the path to simple_component_build_info.json
  * args[2]: the path to ExternalComponentAsset.dir: "${local.build.dir}/ExternalComponents"
  * args[3]: the path to "${AndroidRuntime-class.dir}"
  * args[4]: the path to /build/classes/BuildServer/files
  * args[5]: the path to external componentsTemp directory
  */
  public static void main(String[] args) throws IOException, JSONException {
    String simple_component_json = readFile(args[0], Charset.defaultCharset());
    String simple_component_build_info_json = readFile(args[1], Charset.defaultCharset());
    externalComponentsDirPath = args[2];
    androidRuntimeClassDirPath = args[3];
    buildServerClassDirPath = args[4];
    externalComponentsTempDirPath = args[5];
    useFQCN = Boolean.parseBoolean(args[6]);
    JSONArray simpleComponentDescriptors = new JSONArray(simple_component_json);
    JSONArray simpleComponentBuildInfos = new JSONArray(simple_component_build_info_json);
    Map<String, JSONObject> buildInfos = buildInfoAsMap(simpleComponentBuildInfos);
    for (int i = 0; i < simpleComponentDescriptors.length(); i++) {
      JSONObject componentDescriptor = (JSONObject) simpleComponentDescriptors.get(i);
      if(componentDescriptor.get("external").toString().equals("true")) {
        ExternalComponentInfo info = new ExternalComponentInfo(componentDescriptor, buildInfos.get(componentDescriptor.getString("type")));
        if (!externalComponentsByPackage.containsKey(info.packageName)) {
          externalComponentsByPackage.put(info.packageName, new ArrayList<ExternalComponentInfo>());
        }
        externalComponentsByPackage.get(info.packageName).add(info);
      }
    }

    generateAllExtensions();
  }

  /**
   * Container class to store information about an extension.
   */
  private static class ExternalComponentInfo {
    private String type;
    private String packageName;
    private JSONObject descriptor;
    private JSONObject buildInfo;

    ExternalComponentInfo(JSONObject descriptor, JSONObject buildInfo) {
      this.descriptor = descriptor;
      this.buildInfo = buildInfo;
      this.type = descriptor.optString("type");
      this.packageName = type.substring(0, type.lastIndexOf('.'));
    }
  }

  private static Map<String, JSONObject> buildInfoAsMap(JSONArray buildInfos) throws JSONException {
    Map<String, JSONObject> result = new HashMap<>();
    for (int i = 0; i < buildInfos.length(); i++) {
      JSONObject componentBuildInfo = buildInfos.getJSONObject(i);
      result.put(componentBuildInfo.getString("type"), componentBuildInfo);
    }
    return result;
  }

  private static void generateAllExtensions() throws IOException, JSONException {
    System.out.println("\nExtensions : Generating extensions");
    for (Map.Entry<String, List<ExternalComponentInfo>> entry : externalComponentsByPackage.entrySet()) {
      String name = useFQCN && entry.getValue().size() == 1 ? entry.getValue().get(0).type : entry.getKey();
      String logComponentType =  "[" + name + "]";
      System.out.println("\nExtensions : Generating files " + logComponentType);
      generateExternalComponentDescriptors(name, entry.getValue());
      for (ExternalComponentInfo info : entry.getValue()) {
        copyIcon(name, info.descriptor);
        copyLicense(name, info.descriptor);
        copyAssets(name, info.descriptor);
        copyMock(name, info.descriptor);
      }
      generateExternalComponentBuildFiles(name, entry.getValue());
      generateExternalComponentOtherFiles(name);
    }
  }

  private static void generateExternalComponentDescriptors(String packageName, List<ExternalComponentInfo> infos)
      throws IOException, JSONException {
    StringBuilder sb = new StringBuilder("[");
    boolean first = true;
    for (ExternalComponentInfo info : infos) {
      if (!first) {
        sb.append(',');
      } else {
        first = false;
      }
      sb.append(info.descriptor.toString(1));
    }
    sb.append(']');
    String components = sb.toString();
    String extensionDirPath = externalComponentsDirPath + File.separator + packageName;
    ensureDirectory(extensionDirPath, "Unable to create extension directory");
    FileWriter jsonWriter = null;
    try {
      jsonWriter = new FileWriter(extensionDirPath + File.separator + "components.json");
      jsonWriter.write(components);
    } catch(IOException e) {
      e.printStackTrace();
    } finally {
      if (jsonWriter != null) {
        jsonWriter.close();
      }
    }
    // Write legacy format to transition developers
    try {
      jsonWriter = new FileWriter(extensionDirPath + File.separator + "component.json");
      jsonWriter.write(infos.get(0).descriptor.toString(1));
    } catch(IOException e) {
      e.printStackTrace();
    } finally {
      if (jsonWriter != null) {
        jsonWriter.close();
      }
    }
  }


  private static void generateExternalComponentBuildFiles(String packageName, List<ExternalComponentInfo> extensions) throws IOException {
    String extensionTempDirPath = externalComponentsTempDirPath + File.separator + packageName;
    copyRelatedExternalClasses(androidRuntimeClassDirPath, androidRuntimeClassDirPath, packageName,
        extensionTempDirPath);
    File commonConstants = new File(new File(androidRuntimeClassDirPath).getParentFile(),
        "CommonConstants");
    copyRelatedExternalClasses(commonConstants.getAbsolutePath(), commonConstants.getAbsolutePath(),
        packageName, extensionTempDirPath);

    JSONArray buildInfos = new JSONArray();
    for (ExternalComponentInfo info : extensions) {
      JSONObject componentBuildInfo = info.buildInfo;
      try {
        JSONArray librariesNeeded = componentBuildInfo.getJSONArray("libraries");
        for (int j = 0; j < librariesNeeded.length(); ++j) {
          // Copy Library files for Unjar and Jaring
          String library = librariesNeeded.getString(j);
          copyFile(buildServerClassDirPath + File.separator + library,
              extensionTempDirPath + File.separator + library);
        }
        //empty the libraries meta-data to avoid redundancy
        componentBuildInfo.put("libraries", new JSONArray());
      } catch(JSONException e) {
        // bad
        throw new IllegalStateException("Unexpected JSON exception parsing simple_components.json",
            e);
      }
      buildInfos.put(componentBuildInfo);
    }

    // Create component_build_info.json
    String extensionDirPath = externalComponentsDirPath + File.separator + packageName;
    String extensionFileDirPath = extensionDirPath + File.separator + "files";
    ensureDirectory(extensionFileDirPath, "Unable to create path for component_build_info.json");
    FileWriter extensionBuildInfoFile = null;
    try {
      extensionBuildInfoFile = new FileWriter(extensionFileDirPath + File.separator + "component_build_infos.json");
      extensionBuildInfoFile.write(buildInfos.toString());
      System.out.println("Extensions : Successfully created " + packageName + " build info file");

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (extensionBuildInfoFile != null) {
        extensionBuildInfoFile.flush();
        extensionBuildInfoFile.close();
      }
    }
    // Write out legacy component_build_info.json to transition developers
    try {
      extensionBuildInfoFile = new FileWriter(extensionFileDirPath + File.separator + "component_build_info.json");
      extensionBuildInfoFile.write(buildInfos.get(0).toString());
    } catch (IOException|JSONException e) {
      e.printStackTrace();
    } finally {
      if (extensionBuildInfoFile != null) {
        extensionBuildInfoFile.close();
      }
    }
  }

  private static void copyIcon(String packageName, JSONObject componentDescriptor)
      throws IOException, JSONException {
    String icon = componentDescriptor.getString("iconName");
    if (icon.equals("") || icon.startsWith("http:") || icon.startsWith("https:")) {
      // Icon will be loaded from the web
      return;
    }
    String packagePath = packageName.replace('.', File.separatorChar);
    File sourceDir = new File(externalComponentsDirPath + File.separator + ".." + File.separator + ".." + File.separator + "src" + File.separator + packagePath);
    File image = new File(sourceDir, icon);
    if (image.exists()) {
      File dstIcon = new File(externalComponentsDirPath + File.separator + packageName + File.separator + icon);
      ensureDirectory(dstIcon.getParent(), "Unable to create directory " + dstIcon.getParent());
      System.out.println("Extensions : " + "Copying file " + image.getAbsolutePath());
      copyFile(image.getAbsolutePath(), dstIcon.getAbsolutePath());
    } else {
      System.out.println("Extensions : Skipping missing icon " + icon);
    }
  }

  private static void copyLicense(String packageName, JSONObject componentDescriptor)
      throws IOException, JSONException {
    String license = componentDescriptor.getString("licenseName");
    if("".equals(license) || license.startsWith("http:") || license.startsWith("https:")) {
      // License will be loaded from the web
      return;
    }
    String packagePath = packageName.replace('.', File.separatorChar);
    File sourceDir = new File(externalComponentsDirPath + File.separator + ".." + File.separator + ".." + File.separator + "src" + File.separator + packagePath);
    File licenseFile = new File(sourceDir, license);
    if(licenseFile.exists()) {
      File destinationLicense = new File(externalComponentsDirPath + File.separator + packageName + File.separator + license);
      ensureDirectory(destinationLicense.getParent(), "Unable to create directory " + destinationLicense.getParent());
      System.out.println("Extensions : " + "Copying file " + licenseFile.getAbsolutePath());
      copyFile(licenseFile.getAbsolutePath(), destinationLicense.getAbsolutePath());
    } else {
      System.out.println("Extensions : Skipping missing license " + license);
    }
  }

  private static void copyAssets(String packageName, JSONObject componentDescriptor)
      throws IOException, JSONException {
    JSONArray assets = componentDescriptor.optJSONArray("assets");
    if (assets == null) {
      return;
    }

    // Get asset source directory
    String packagePath = packageName.replace('.', File.separatorChar);
    File sourceDir = new File(externalComponentsDirPath + File.separator + ".." + File.separator + ".." + File.separator + "src" + File.separator + packagePath);
    File assetSrcDir = new File(sourceDir, "assets");
    if (!assetSrcDir.exists() || !assetSrcDir.isDirectory()) {
      return;
    }

    // Get asset dest directory
    File destDir = new File(externalComponentsDirPath + File.separator + packageName + File.separator);
    File assetDestDir = new File(destDir, "assets");
    ensureFreshDirectory(assetDestDir.getPath(),
        "Unable to delete the assets directory for the extension.");

    // Copy assets
    for (int i = 0; i < assets.length(); i++) {
      String asset = assets.getString(i);
      if (!asset.isEmpty()) {
        if (!copyFile(assetSrcDir.getAbsolutePath() + File.separator + asset,
            assetDestDir.getAbsolutePath() + File.separator + asset)) {
          throw new IllegalStateException("Unable to copy asset to destination.");
        }
      }
    }
  }

  public static void copyMock(String packageName, JSONObject componentDescriptor) throws JSONException, IOException {
    boolean nonVisible = componentDescriptor.getBoolean("nonVisible");
    if (nonVisible) {
      return;
    }

    String packagePath = packageName.replace('.', File.separatorChar);
    File sourceDir = new File(externalComponentsDirPath + File.separator + ".." + File.separator + ".." + File.separator + "src", File.separator + packagePath);
    File mockFile = new File(sourceDir, "mocks" + File.separator + componentDescriptor.getString("name") + ".mock.js");

    if (!mockFile.exists()) {
      System.out.println("Extensions : Skipping missing mock file " + mockFile.getAbsolutePath());
      return;
    }

    File destDir = new File(externalComponentsDirPath + File.separator + packageName + File.separator);
    File mockDestDir = new File(destDir, "mocks");
    ensureFreshDirectory(mockDestDir.getPath(),
            "Unable to delete the mock directory for the extension.");

    File mockDestFile = new File(mockDestDir, componentDescriptor.getString("name") + ".mock.js");
    System.out.println("Extensions : " + "Copying mock file " + mockFile.getAbsolutePath());
    copyFile(mockFile.getAbsolutePath(), mockDestFile.getAbsolutePath());
  }

  private static void generateExternalComponentOtherFiles(String packageName) throws IOException {
    String extensionDirPath = externalComponentsDirPath + File.separator + packageName;

    // Create extension.properties
    StringBuilder extensionPropertiesString = new StringBuilder();
    extensionPropertiesString.append("type=external\n");
    FileWriter extensionPropertiesFile = new FileWriter(extensionDirPath + File.separator + "extension.properties");
    try {
      extensionPropertiesFile.write(extensionPropertiesString.toString());
      System.out.println("Extensions : Successfully created " + packageName + " extension properties file");
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      extensionPropertiesFile.flush();
      extensionPropertiesFile.close();
    }
  }

  /**
  * Read a file and returns its content
  *
  * @param path the path of the file to be read
  * @param encoding the encoding system
  */
  private static String readFile(String path, Charset encoding) throws IOException{
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  /**
   * Copy one file to another. If destination file does not exist, it is created.
   *
   * @param srcPath absolute path to source file
   * @param dstPath absolute path to destination file
   * @return  {@code true} if the copy succeeds, {@code false} otherwise
   */
  private static Boolean copyFile(String srcPath, String dstPath) {
    try {
      FileInputStream in = new FileInputStream(srcPath);
      FileOutputStream out = new FileOutputStream(dstPath);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0 ) {
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

  /**
   * Copy a compiled classes related to a given extension in his package folder.
   *
   * @param basedir the root directory where classes were compiled
   * @param srcPath the folder in which to check compiled classes
   * @param extensionPackage the classpath of the extension
   * @param destPath where the compiled classes will be copied
   */
  private static void copyRelatedExternalClasses(final String basedir, final String srcPath,
      String extensionPackage, final String destPath) throws IOException {
    File srcFolder = new File(srcPath);
    File[] files = srcFolder.listFiles();
    if (files == null) {
      return;
    }
    for (File fileEntry : files) {
      if (fileEntry.isFile()) {
        if (isRelatedExternalClass(basedir, fileEntry.getAbsolutePath(), extensionPackage)) {
          System.out.println("Extensions : Copying file "
              + getClassPackage(basedir, fileEntry.getAbsolutePath()).replace(".", File.separator)
              + File.separator + fileEntry.getName());
          copyFile(fileEntry.getAbsolutePath(), destPath + File.separator + fileEntry.getName());
        }
      } else if (fileEntry.isDirectory()) {
        String newDestPath = destPath + fileEntry.getAbsolutePath()
            .substring(srcFolder.getAbsolutePath().length());
        ensureDirectory(newDestPath, "Unable to create temporary path for extension build");
        copyRelatedExternalClasses(basedir, fileEntry.getAbsolutePath(), extensionPackage,
            newDestPath);
      }
    }
  }

  /**
   * Returns true if a class is related to an external component.
   * The current implementation returns true for all files in the same package as that of the
   * external component. A better implementation is possible but might be more complex
   *
   * @param basedir the root directory where classes were compiled
   * @param testClassAbsolutePath absolute path of the class file
   * @param extensionPackage package of the external component
   * @return {@code true} if the Java class file at {@code testClassAbsolutePath} is a member of
   * {@code extensionPackage}, {@code false} otherwise
   */
  private static boolean isRelatedExternalClass(final String basedir,
      final String testClassAbsolutePath, final String extensionPackage) {
    if (!testClassAbsolutePath.endsWith(".class")) {  // Ignore things that aren't class files...
      return false;
    }
    String componentPackagePath = extensionPackage.replace(".", File.separator);

    String testClassPath = getClassPackage(basedir, testClassAbsolutePath);
    testClassPath = testClassPath.replace(".", File.separator);
    return testClassPath.startsWith(componentPackagePath);
  }

  private static String getClassPackage(String parentPath, String classAbsolutePath) {
    if (!parentPath.endsWith("/")) {
      parentPath += "/";
    }
    parentPath = parentPath.replace("/", File.separator);
    String componentPackage = classAbsolutePath.substring(classAbsolutePath.indexOf(parentPath) + parentPath.length());
    componentPackage = componentPackage.substring(0, componentPackage.lastIndexOf(File.separator));
    componentPackage = componentPackage.replace(File.separator, ".");
    return  componentPackage;
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
