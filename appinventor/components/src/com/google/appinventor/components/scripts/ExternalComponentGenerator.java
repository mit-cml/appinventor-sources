// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
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

  private static ArrayList<String> externalComponents = new ArrayList<String>();
  private static ArrayList<JSONObject> externalComponentDescriptors = new ArrayList<JSONObject>();
  private static ArrayList<JSONObject> externalComponentBuildInfos = new ArrayList<JSONObject>();

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
    JSONArray simpleComponentDescriptors = new JSONArray(simple_component_json);
    JSONArray simpleComponentBuildInfos = new JSONArray(simple_component_build_info_json);
    for (int i = 0; i < simpleComponentDescriptors.length(); i++) {
      JSONObject componentDescriptor = (JSONObject) simpleComponentDescriptors.get(i);
      if(componentDescriptor.get("external").toString().equals("true")) {
        externalComponents.add(componentDescriptor.get("type").toString());
        externalComponentDescriptors.add(componentDescriptor);
      }
    }
    for (int i = 0; i < simpleComponentBuildInfos.length(); i++) {
      JSONObject componentBuildInfo = (JSONObject) simpleComponentBuildInfos.get(i);
      if (externalComponents.contains(componentBuildInfo.get("type").toString())) {
        externalComponentBuildInfos.add(componentBuildInfo);
      }
    }

    generateExternalComponentsList();
    generateAllExtensions();
  }


  private static void generateExternalComponentsList() throws IOException{
    // Create external_components.txt
    System.out.println("\nExtensions : Generating external_components.txt");
    FileWriter externalComponentList =  new FileWriter(externalComponentsDirPath + File.separator + "external_components.txt");
    for (int j = 0; j < externalComponents.size(); j++) {
      externalComponentList.write(externalComponents.get(j));
      externalComponentList.write("\n");
    }
    externalComponentList.flush();
    externalComponentList.close();
    System.out.println("Extensions : Successfully created external_components.txt");
  }

  private static void generateAllExtensions() throws IOException {
    System.out.println("\nExtensions : Generating extensions");
    for (int i = 0; i < externalComponents.size(); ++i) {
      String componentType = externalComponents.get(i);
      String logComponentType =  "[" + componentType + "]";
      System.out.println("\nExtensions : Generating files " + logComponentType);
      JSONObject componentDescriptor = getComponentDescriptor(componentType);
      JSONObject componentBuildInfo = getComponentBuildInfo(componentType);
      if (componentDescriptor == null || componentBuildInfo == null) {
        System.out.println("Extensions : Failed generating files " + logComponentType);
      }
      System.out.println("Extensions : Generating component.json " + logComponentType);
      generateExternalComponentDescriptor(componentDescriptor);
      System.out.println("Extensions : Generating build files " + logComponentType);
      generateExternalComponentBuildFiles(componentBuildInfo);
      System.out.println("Extensions : Generating other files " + logComponentType);
      generateExternalComponentOtherFiles(componentDescriptor);

    }
  }



  private static void generateExternalComponentDescriptor(JSONObject componentDescriptor) throws IOException {
    // Create component.json
    String extensionDirPath = externalComponentsDirPath + File.separator + componentDescriptor.get("type").toString();
    new File(extensionDirPath).mkdirs();
    FileWriter componentJsonFile = new FileWriter(extensionDirPath + File.separator + "component.json");
    try {
      componentJsonFile.write(componentDescriptor.toString(1));
      System.out.println("Extensions : Successfully created "+ componentDescriptor.get("type") +" json file");
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      componentJsonFile.flush();
      componentJsonFile.close();
    }
  }



  private static void generateExternalComponentBuildFiles(JSONObject componentBuildInfo) throws IOException {
    String componentType = componentBuildInfo.get("type").toString();
    String extensionDirPath = externalComponentsDirPath + File.separator + componentType;
    String extensionTempDirPath = externalComponentsTempDirPath + File.separator + componentType;
    String  extensionFileDirPath = extensionDirPath + File.separator + "files";
    JSONArray librariesNeeded = componentBuildInfo.getJSONArray("libraries");
    String extensionClassPath = componentType.substring(0,componentType.lastIndexOf(".")).replace('.', File.separatorChar);
    String extensionTempClassDirPath = extensionTempDirPath + File.separator + extensionClassPath;
    new File(extensionTempClassDirPath).mkdirs();
    copyRelatedExternalClasses(androidRuntimeClassDirPath, componentType, extensionTempClassDirPath);

    for (int j = 0; j < librariesNeeded.length(); ++j) { // Copy Library files for Unjar and Jaring
      String library = librariesNeeded.getString(j);
      copyFile(buildServerClassDirPath + File.separator + library,
          extensionTempDirPath + File.separator + library);
    }

//      copyFile(externalComponentsTempDirPath + File.separator + componentType + ".jar",
//          componentFileDirectory + File.separator + "AndroidRuntime.jar");
    componentBuildInfo.put("libraries", new JSONArray()); //empty the libraries meta-data to avoid redundancy

    // Create component_build_info.json
    new File(extensionFileDirPath).mkdirs();
    FileWriter extensionBuildInfoFile = new FileWriter(extensionFileDirPath + File.separator + "component_build_info.json");
    try {
      extensionBuildInfoFile.write(componentBuildInfo.toString(1));
      System.out.println("Extensions : Successfully created " + componentType + " build info file");

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      extensionBuildInfoFile.flush();
      extensionBuildInfoFile.close();
    }
  }


  private static void generateExternalComponentOtherFiles(JSONObject componentDescriptor) throws IOException {

    String componentType = componentDescriptor.get("type").toString();
    String extensionDirPath = externalComponentsDirPath + File.separator + componentType;

    // Create extension.properties
    StringBuilder extensionPropertiesString = new StringBuilder();
    extensionPropertiesString.append("type=external\n");
    FileWriter extensionPropertiesFile = new FileWriter(extensionDirPath + File.separator + "extension.properties");
    try {
      extensionPropertiesFile.write(extensionPropertiesString.toString());
      System.out.println("Extensions : Successfully created " + componentType + " extension properties file");
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
  * Read external_components.txt (a file containing the type of the extension)
  * and returns an ArrayList containing those components
  *
  * @param fileName the path of the file to be read
  */
  private static ArrayList<String> fileToArray(String fileName) throws FileNotFoundException{
    Scanner sc = new Scanner(new File(fileName));
    ArrayList<String> components = new ArrayList<String>();
    while (sc.hasNextLine()) {
      components.add(sc.nextLine());
    }
    return components;
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
   * Copy a compiled classes related to a given extension in his package folder
   *
   * @param srcPath the folder in which to check compiled classes
   * @param componentType the classpath of the extension
   * @param destPath where the compiled classes will be copied
   */
  private static void copyRelatedExternalClasses(final String srcPath, String componentType, final String destPath) throws IOException {
    File srcFolder = new File(srcPath);
    for (File fileEntry : srcFolder.listFiles()){
      if (fileEntry.isFile()) {
        if (isRelatedExternalClass(fileEntry.getAbsolutePath(), componentType)) {
          System.out.println("Extensions : " + "Copying file " + getClassPackage(fileEntry.getAbsolutePath()).replace(".", File.separator)
              + File.separator + fileEntry.getName());
          copyFile(fileEntry.getAbsolutePath(), destPath + File.separator + fileEntry.getName());
        }
      } else if (fileEntry.isDirectory()) {
        copyRelatedExternalClasses(fileEntry.getAbsolutePath(), componentType, destPath);
      }
    }
  }

  /**
   * Returns true if a class is related to an external component
   * Current implementation returns true for all files in the same package as that of the external component
   * A better implementation is possible but might be more complex
   * @param testClassAbsolutePath absolute path of the class file
   * @param externalComponentType classpath of the external component
   * @return
   */
  private static boolean isRelatedExternalClass(final String testClassAbsolutePath, final String externalComponentType ) {
    String componentName = externalComponentType.substring(externalComponentType.lastIndexOf('.') + 1);
    String componentPath = externalComponentType.replace(".", File.separator);
    String componentPackagePath = componentPath.substring(0, componentPath.lastIndexOf(File.separator));

    String testClassPath = getClassPackage(testClassAbsolutePath);
    testClassPath.replace(".", File.separator);
    if (testClassPath.startsWith(componentPackagePath)) {
      return true;
    }
    return false;
  }

  private static String getClassPackage(String classAbsolutePath) {
    String parentPath = "/appinventor/components/build/classes/AndroidRuntime/";
    parentPath = parentPath.replace("/", File.separator);
    String componentPackage = classAbsolutePath.substring(classAbsolutePath.indexOf(parentPath) + parentPath.length());
    componentPackage = componentPackage.substring(0, componentPackage.lastIndexOf(File.separator));
    componentPackage.replace(File.separator, ".");
    return  componentPackage;

  }

  private static JSONObject getComponentDescriptor(String componentType) {
    JSONObject componentDescriptor = null;
    for (int i = 0; i < externalComponentDescriptors.size(); ++i) {
      JSONObject descriptor = externalComponentDescriptors.get(i);
      if (descriptor.getString("type").equals(componentType)) {
        componentDescriptor = descriptor;
        return componentDescriptor;
      }
    }
    return componentDescriptor;
  }

  private static JSONObject getComponentBuildInfo(String componentType) {
    JSONObject componentBuildInfo = null;
    for (int i = 0; i < externalComponentBuildInfos.size(); ++i) {
      JSONObject buildInfo = externalComponentBuildInfos.get(i);
      if (buildInfo.getString("type").equals(componentType)) {
        componentBuildInfo = buildInfo;
        return componentBuildInfo;
      }
    }
    return componentBuildInfo;
  }


}

