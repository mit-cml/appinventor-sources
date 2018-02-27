// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
* Tool to generate component.json for extension.
*
* @author mouha.oumar@gmail.com (Mouhamadou O. Sall)
* @author 502470184@qq.com (ColinTree YANG)
*/

public class ExternalComponentGenerator {

  private static final String TRANSLATION_FILE_NAME = "translation.json";
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private static String externalComponentsDirPath;
  private static String androidRuntimeClassDirPath;
  private static String buildServerClassDirPath;
  private static String externalComponentsTempDirPath;
  private static boolean useFQCN = false;

  private static Map<String, List<ExternalComponentInfo>> externalComponentsByPackage =
      new TreeMap<String, List<ExternalComponentInfo>>();

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
    String simple_component_json = readFile(args[0], DEFAULT_CHARSET);
    String simple_component_build_info_json = readFile(args[1], DEFAULT_CHARSET);
    externalComponentsDirPath = args[2];
    androidRuntimeClassDirPath = args[3];
    buildServerClassDirPath = args[4];
    externalComponentsTempDirPath = args[5];
    useFQCN = Boolean.valueOf(args[6]);
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
    private String className;
    private JSONObject descriptor;
    private JSONObject buildInfo;

    ExternalComponentInfo(JSONObject descriptor, JSONObject buildInfo) {
      this.descriptor = descriptor;
      this.buildInfo = buildInfo;
      this.type = descriptor.optString("type");
      this.packageName = type.substring(0, type.lastIndexOf('.'));
      this.className = type.substring(type.lastIndexOf('.') + 1);
    }
  }

  private static Map<String, JSONObject> buildInfoAsMap(JSONArray buildInfos) throws JSONException {
    Map<String, JSONObject> result = new HashMap<String, JSONObject>();
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
      processTranslationScript(name, entry.getValue());
      generateExternalComponentDescriptors(name, entry.getValue());
      for (ExternalComponentInfo info : entry.getValue()) {
        copyIcon(name, info.type, info.descriptor);
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
    new File(extensionDirPath).mkdirs();
    writeFile(extensionDirPath + File.separator + "components.json", components, DEFAULT_CHARSET);
    // Write legacy format to transition developers
    writeFile(extensionDirPath + File.separator + "component.json",
      infos.get(0).descriptor.toString(1), DEFAULT_CHARSET);
  }


  private static void generateExternalComponentBuildFiles(String packageName, List<ExternalComponentInfo> extensions)
      throws IOException, JSONException {
    String extensionDirPath = externalComponentsDirPath + File.separator + packageName;
    String extensionTempDirPath = externalComponentsTempDirPath + File.separator + packageName;
    String extensionFileDirPath = extensionDirPath + File.separator + "files";
    copyRelatedExternalClasses(androidRuntimeClassDirPath, packageName, extensionTempDirPath);

    JSONArray buildInfos = new JSONArray();
    for (ExternalComponentInfo info : extensions) {
      JSONObject componentBuildInfo = info.buildInfo;
      JSONArray librariesNeeded = componentBuildInfo.getJSONArray("libraries");
      for (int j = 0; j < librariesNeeded.length(); ++j) { // Copy Library files for Unjar and Jaring
        String library = librariesNeeded.getString(j);
        copyFile(buildServerClassDirPath + File.separator + library,
            extensionTempDirPath + File.separator + library);
      }
      componentBuildInfo.put("libraries", new JSONArray()); //empty the libraries meta-data to avoid redundancy
      buildInfos.put(componentBuildInfo);
    }

    // Create component_build_info.json
    if (!new File(extensionFileDirPath).mkdirs()) {
      throw new IOException("Unable to create path for component_build_info.json");
    }
    if(writeFile(extensionFileDirPath + File.separator + "component_build_infos.json",
        buildInfos.toString(), DEFAULT_CHARSET)) {
      System.out.println("Extensions : Successfully created " + packageName + " build info file");
    }
    // Write out legacy component_build_info.json to transition developers
    if(writeFile(extensionFileDirPath + File.separator + "component_build_info.json",
        buildInfos.get(0).toString(), DEFAULT_CHARSET)) {
      System.out.println("Extensions : Successfully created " + packageName + " build info file");
    }
  }

  private static void copyIcon(String packageName, String type, JSONObject componentDescriptor) throws IOException, JSONException {
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
      File dstIconDir = dstIcon.getParentFile();
      if (!dstIconDir.exists()) {
        if (!dstIconDir.mkdirs()) {
          throw new IOException("Unable to create directory " + dstIconDir);
        }
      }
      System.out.println("Extensions : " + "Copying file " + image.getAbsolutePath());
      copyFile(image.getAbsolutePath(), dstIcon.getAbsolutePath());
    } else {
      System.out.println("Extensions : Skipping missing icon " + icon);
    }
  }
  
  private static void processTranslationScript(String packageName, List<ExternalComponentInfo> componentList) throws IOException, JSONException {
    String packagePath = packageName.replace('.', File.separatorChar);
    File sourceDir = new File(externalComponentsDirPath + File.separator + ".." + File.separator + ".." + File.separator + "src" + File.separator + packagePath);
    File script = new File(sourceDir, TRANSLATION_FILE_NAME);
    if (script.exists()) {
      System.out.println("Extensions : Processing translation script");
      String translation_script = readFile(script.getAbsolutePath(), DEFAULT_CHARSET);
      Map<String, JSONObject> translations = TranslationTransformer.transformTranslationsMap(new JSONObject(translation_script));
      JSONObject componentTranslations;
      for (ExternalComponentInfo component : componentList) {
        componentTranslations = translations.get(component.className);
        component.descriptor.put("translations",
          componentTranslations == null ? new JSONObject() : componentTranslations);
      }
    } else {
      System.out.println("Extensions : Skipping missing translation script " + script);
    }
  }

  private static class TranslationTransformer {

    /**
     * Converted translation.json into excepted form.
     * 
     * Sample json scriptObject:
     * {
     *   "COMPONENT-NAME": {
     *     "LOCALE-NAME": {
     *       "component":  "COMPONENT-NAME-TRANSLATION",
     *       "properties": { "PROPERTY-NAME": "PROPERTY-TRANSLATION" },
     *       "events":     { "EVENT-NAME": "EVENT-TRANSLATION" },
     *       "methods":    { "METHOD-NAME": "METHOD-TRANSLATION" },
     *       "params":     { "PARAM-NAME": "PARAM-TRANSLATION" }
     *     }
     *   }
     * }
     * 
     * @param scriptObject JSONObject (componentName, JSONObject (locale, JSONObject (translationType, String|JSONObject (name, translation))))
     * @return             HashMap (componentName, JSONObject (locale, JSONObject (name, translation)))
     */
    public static Map<String, JSONObject> transformTranslationsMap(JSONObject scriptObject) throws JSONException {
      Map<String, JSONObject> rtnMap = new HashMap<String, JSONObject>();
      Iterator<String> iterator = scriptObject.keys();
      String componentName;
      while (iterator.hasNext()) {
        componentName = iterator.next();
        rtnMap.put(componentName, transformTranslationsMapByComponent(scriptObject.getJSONObject(componentName), componentName));
      }
      return rtnMap;
    }
    
    /**
     * @param componentNameObject JSONObject (locale, JSONObject (translationType, String|JSONObject (name, translation)))
     * @param componentName       The component name
     * @return                    JSONObject (locale, JSONObject (name, translation))
     */
    private static JSONObject transformTranslationsMapByComponent(JSONObject componentNameObject, String componentName) throws JSONException {
      JSONObject rtnObject = new JSONObject();
      Iterator<String> iterator = componentNameObject.keys();
      String locale;
      while (iterator.hasNext()) {
        locale = iterator.next();
        rtnObject.put(locale, transformTranslationsMapByLocale(componentNameObject.getJSONObject(locale), componentName));
      }
      return rtnObject;
    }

    /**
     * @param localeObject  JSONObject (translationType, String|JSONObject (name, translation))
     * @param componentName The component name
     * @return              JSONObject (name, translation)
     */
    private static JSONObject transformTranslationsMapByLocale(JSONObject localeObject, String componentName) throws JSONException {
      JSONObject rtnObject = new JSONObject();
      // component
      if (localeObject.has("component")) {
        rtnObject.put("COMPONENT-" + componentName, localeObject.getString("component"));
      }
      // properties
      transformTranslationsMapByType(localeObject.getJSONObject("properties"), "PROPERTY-", rtnObject);
      // events
      transformTranslationsMapByType(localeObject.getJSONObject("events"), "EVENT-", rtnObject);
      // methods
      transformTranslationsMapByType(localeObject.getJSONObject("methods"), "METHOD-", rtnObject);
      // params
      transformTranslationsMapByType(localeObject.getJSONObject("params"), "PARAM-", rtnObject);
      
      return rtnObject;
    }

    /**
     * @param localeObject JSONObject (name, translation)
     * @param prefix       Prefix that added into JSONObject
     * @param target       Target JSONObject (prefix+name, translation)
     */
    private static void transformTranslationsMapByType(JSONObject itemObject, String prefix, JSONObject target) throws JSONException {
      if (itemObject == null) {
        return;
      }
      Iterator<String> iterator = itemObject.keys();
      String item;
      while (iterator.hasNext()) {
        item = iterator.next();
        target.put(prefix + item, itemObject.getString(item));
      }
    }

  }

  private static void generateExternalComponentOtherFiles(String packageName) throws IOException {
    String extensionDirPath = externalComponentsDirPath + File.separator + packageName;

    // Create extension.properties
    StringBuilder extensionPropertiesString = new StringBuilder();
    extensionPropertiesString.append("type=external\n");
    if (writeFile(extensionDirPath + File.separator + "extension.properties",
        extensionPropertiesString.toString(), DEFAULT_CHARSET)) {
      System.out.println("Extensions : Successfully created " + packageName + " extension properties file");
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
   * Write a file and returns true if success
   *
   * @param path the path of the file to be read
   * @param content thee content to write in
   * @param encoding the encoding system
   */
  private static boolean writeFile(String path, String content, Charset encoding) throws IOException {
    OutputStreamWriter osw = null;
    boolean result = true;
    try {
      osw = new OutputStreamWriter(new FileOutputStream(path), encoding);
      osw.write(content);
    } catch (IOException e) {
      e.printStackTrace();
      result = false;
    } finally {
      if (osw != null) {
        osw.close();
      }
    }
    return result;
  }
 

  /**
   * Copy a compiled classes related to a given extension in his package folder
   *
   * @param srcPath the folder in which to check compiled classes
   * @param extensionPackage the classpath of the extension
   * @param destPath where the compiled classes will be copied
   */
  private static void copyRelatedExternalClasses(final String srcPath, String extensionPackage,
                                                 final String destPath) throws IOException {
    File srcFolder = new File(srcPath);
    File[] files = srcFolder.listFiles();
    if (files == null) {
      return;
    }
    for (File fileEntry : files){
      if (fileEntry.isFile()) {
        if (isRelatedExternalClass(fileEntry.getAbsolutePath(), extensionPackage)) {
          System.out.println("Extensions : " + "Copying file " +
              getClassPackage(fileEntry.getAbsolutePath()).replace(".", File.separator)
              + File.separator + fileEntry.getName());
          copyFile(fileEntry.getAbsolutePath(), destPath + File.separator + fileEntry.getName());
        }
      } else if (fileEntry.isDirectory()) {
        String newDestPath=destPath + fileEntry.getAbsolutePath().substring(srcFolder.getAbsolutePath().length());
        if (!new File(newDestPath).mkdirs()) {
          throw new IOException("Unable to create temporary path for extension build");
        }
        copyRelatedExternalClasses(fileEntry.getAbsolutePath(), extensionPackage, newDestPath);
      }
    }
  }

  /**
   * Returns true if a class is related to an external component
   * Current implementation returns true for all files in the same package as that of the external component
   * A better implementation is possible but might be more complex
   * @param testClassAbsolutePath absolute path of the class file
   * @param extensionPackage package of the external component
   * @return {@code true} if the Java class file at {@code testClassAbsolutePath} is a member of
   * {@code extensionPackage}, {@code false} otherwise
   */
  private static boolean isRelatedExternalClass(final String testClassAbsolutePath, final String extensionPackage ) {
    String componentPackagePath = extensionPackage.replace(".", File.separator);

    String testClassPath = getClassPackage(testClassAbsolutePath);
    testClassPath = testClassPath.replace(".", File.separator);
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
    componentPackage = componentPackage.replace(File.separator, ".");
    return  componentPackage;

  }
}

