// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.ASSETS_FOLDER;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.FORM_PROPERTIES_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.SRC_FOLDER;

import com.google.appinventor.server.storage.StorageIo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Storage query helpers used by {@link ProjectModule} and
 * {@link ScreenModule} to read project files.
 */
public final class ProjectFiles {

  private static final Logger LOG = Logger.getLogger(ProjectFiles.class.getName());

  private static final String EXTERNAL_COMPS_FOLDER = ASSETS_FOLDER + "/external_comps";
  private static final String PROJECT_DIRECTORY = "youngandroidproject";
  static final String PROJECT_PROPERTIES_FILE = PROJECT_DIRECTORY + "/project.properties";

  private ProjectFiles() {
  }

  /**
   * Determine the source package path for a project by reading
   * project.properties.
   */
  public static String getPackagePath(String userId, long projectId, StorageIo storageIo) {
    try {
      String propsContent = storageIo.downloadFile(
          userId, projectId, PROJECT_PROPERTIES_FILE, "UTF-8");
      Properties props = new Properties();
      props.load(new java.io.StringReader(propsContent));

      String main = props.getProperty("main", "");
      if (main.isEmpty()) {
        return null;
      }
      int lastDot = main.lastIndexOf('.');
      if (lastDot < 0) {
        return null;
      }
      String packageName = main.substring(0, lastDot);
      return SRC_FOLDER + "/" + packageName.replace('.', '/');
    } catch (Exception e) {
      LOG.warning("Failed to read project properties: " + e.getMessage());
      return null;
    }
  }

  /**
   * List all screen names in a project.
   */
  public static List<String> listScreenNames(String userId, long projectId,
      String packagePath, StorageIo storageIo) {
    List<String> screens = new ArrayList<>();
    if (packagePath == null) {
      return screens;
    }
    try {
      List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
      for (String fileId : files) {
        if (fileId.startsWith(packagePath + "/")
            && fileId.endsWith(FORM_PROPERTIES_EXTENSION)) {
          String name = fileId.substring(fileId.lastIndexOf('/') + 1,
              fileId.length() - FORM_PROPERTIES_EXTENSION.length());
          screens.add(name);
        }
      }
    } catch (Exception e) {
      LOG.warning("Failed to list screens: " + e.getMessage());
    }
    return screens;
  }

  /**
   * List all asset file names in a project (excluding extensions).
   */
  public static List<String> listAssets(String userId, long projectId, StorageIo storageIo) {
    List<String> assets = new ArrayList<>();
    try {
      List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
      for (String fileId : files) {
        if (fileId.startsWith(ASSETS_FOLDER + "/")
            && !fileId.startsWith(EXTERNAL_COMPS_FOLDER + "/")) {
          String name = fileId.substring(ASSETS_FOLDER.length() + 1);
          assets.add(name);
        }
      }
    } catch (Exception e) {
      LOG.warning("Failed to list assets: " + e.getMessage());
    }
    return assets;
  }

  /**
   * List the component type names declared by an extension package.
   * Returns an empty list if the package's {@code components.json} is
   * missing or malformed.
   */
  public static List<String> listExtensionComponentTypes(String userId, long projectId,
      String pkg, StorageIo storageIo) {
    List<String> types = new ArrayList<>();
    try {
      String filePath = EXTERNAL_COMPS_FOLDER + "/" + pkg + "/components.json";
      String content = storageIo.downloadFile(userId, projectId, filePath, "UTF-8");
      JSONArray arr = new JSONArray(content);
      for (int i = 0; i < arr.length(); i++) {
        JSONObject comp = arr.getJSONObject(i);
        String name = comp.optString("name", "");
        if (!name.isEmpty()) {
          types.add(name);
        }
      }
    } catch (Exception e) {
      LOG.warning("Failed to read extension components for " + pkg + ": " + e.getMessage());
    }
    return types;
  }

  /**
   * List all extension package names in a project.
   */
  public static List<String> listExtensions(String userId, long projectId,
      StorageIo storageIo) {
    List<String> extensions = new ArrayList<>();
    try {
      List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
      for (String fileId : files) {
        if (fileId.startsWith(EXTERNAL_COMPS_FOLDER + "/")
            && fileId.endsWith("/components.json")) {
          String extPath = fileId.substring(EXTERNAL_COMPS_FOLDER.length() + 1);
          String extName = extPath.substring(0, extPath.indexOf('/'));
          if (!extensions.contains(extName)) {
            extensions.add(extName);
          }
        }
      }
    } catch (Exception e) {
      LOG.warning("Failed to list extensions: " + e.getMessage());
    }
    return extensions;
  }
}
