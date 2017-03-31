// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.component.ComponentImportResponse;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.component.ComponentService;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class ComponentServiceImpl extends OdeRemoteServiceServlet
    implements ComponentService {

  private static final Logger LOG =
      Logger.getLogger(ComponentServiceImpl.class.getName());

  private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  private final FileImporter fileImporter = new FileImporterImpl();

  @Override
  public ComponentImportResponse importComponentToProject(String fileOrUrl, long projectId,
      String folderPath) {
    ComponentImportResponse response = new ComponentImportResponse(ComponentImportResponse.Status.FAILED);

    if (isUnknownSource(fileOrUrl)) {
      response.setStatus(ComponentImportResponse.Status.UNKNOWN_URL);
      return response;
    }

    Map<String, byte[]> contents;
    String fileNameToDelete = null;
    try {
      if (fileOrUrl.startsWith("__TEMP__")) {
        fileNameToDelete = fileOrUrl;
        contents = extractContents(storageIo.openTempFile(fileOrUrl));
      } else {
        URL compUrl = new URL(fileOrUrl);
        contents = extractContents(compUrl.openStream());
      }
      importToProject(contents, projectId, folderPath, response);
      return response;
    } catch (FileImporterException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(fileOrUrl, projectId), e);
    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(fileOrUrl, projectId), e);
    } finally {
      if (fileNameToDelete != null) {
        try {
          storageIo.deleteTempFile(fileNameToDelete);
        } catch (Exception e) {
          throw CrashReport.createAndLogError(LOG, null,
            collectImportErrorInfo(fileOrUrl, projectId), e);
        }
      }
    }
  }

  @Override
  public void renameImportedComponent(String fullyQualifiedName, String newName,
      long projectId) {
    String fileName = "assets/external_comps/" + fullyQualifiedName + "/component.json";

    JSONObject compJson = new JSONObject(storageIo.downloadFile(
        userInfoProvider.getUserId(), projectId, fileName, StorageUtil.DEFAULT_CHARSET));
    compJson.put("name", newName);

    try {
      storageIo.uploadFile(projectId, fileName, userInfoProvider.getUserId(),
          compJson.toString(2), StorageUtil.DEFAULT_CHARSET);
    } catch (BlocksTruncatedException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "Error renaming the short name of " + fullyQualifiedName + " to " +
          newName + " in project " + projectId, e);
    }
  }

  @Override
  public void deleteImportedComponent(String fullyQualifiedName, long projectId) {
    String directory = "assets/external_comps/" + fullyQualifiedName + "/";
    for (String fileId : storageIo.getProjectSourceFiles(userInfoProvider.getUserId(), projectId)) {
      if (fileId.startsWith(directory)) {
        storageIo.deleteFile(userInfoProvider.getUserId(), projectId, fileId);
        storageIo.removeSourceFilesFromProject(userInfoProvider.getUserId(), projectId, false, fileId);
      }
    }
  }

  private Map<String, byte[]> extractContents(InputStream inputStream)
      throws IOException {
    Map<String, byte[]> contents = new HashMap<String, byte[]>();

    // assumption: the zip is non-empty
    ZipInputStream zip = new ZipInputStream(inputStream);
    ZipEntry entry;
    while ((entry = zip.getNextEntry()) != null) {
      if (entry.isDirectory())  continue;
      ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
      ByteStreams.copy(zip, contentStream);
      contents.put(entry.getName(), contentStream.toByteArray());
    }
    zip.close();

    return contents;
  }

  /**
   * Updates the name of any components in newComponents with the name specified in oldComponents.
   * This destructively modifies newComponents.
   * @param oldComponents an array of component descriptions from the old component[s].json
   * @param newComponents an array of component descriptions from the new component[s].json
   * @return A mapping of component fully-qualified class names to presentation (UI) names
   */
  private Map<String, String> applyRenames(JSONArray oldComponents, JSONArray newComponents) {
    Map<String, String> types = new HashMap<>();
    for (int i = 0; i < oldComponents.length(); i++) {
      JSONObject component = oldComponents.getJSONObject(i);
      types.put(component.getString("type"), component.getString("name"));
    }
    for (int i = 0; i < newComponents.length(); i++) {
      JSONObject component = newComponents.getJSONObject(i);
      String type = component.getString("type");
      if (!types.containsKey(type)) {
        types.put(type, component.getString("name"));
      } else {
        component.put("name", types.get(type));
      }
    }
    return types;
  }

  /**
   * Extract a mapping of component fully-quallyified class names to presentation (UI) names
   * @param components an array of component descriptions
   * @return A mapping of component fully-qualified class names to presentation (UI) names
   */
  private Map<String, String> extractTypes(JSONArray components) {
    Map<String, String> types = new HashMap<>();
    for (int i = 0; i < components.length(); i++) {
      JSONObject component = components.getJSONObject(i);
      types.put(component.getString("type"), component.getString("name"));
    }
    return types;
  }

  private void importToProject(Map<String, byte[]> contents, long projectId,
      String folderPath, ComponentImportResponse response) throws FileImporterException, IOException {
    response.setStatus(ComponentImportResponse.Status.IMPORTED);
    List<ProjectNode> compNodes = new ArrayList<ProjectNode>();
    response.setProjectId(projectId);
    List<String> sourceFiles = storageIo.getProjectSourceFiles(userInfoProvider.getUserId(), projectId);
    Map<String, String> types = new TreeMap<>();
    for (String name : contents.keySet()) {
      String destination = folderPath + "/external_comps/" + name;
      if (sourceFiles.contains(destination)) {  // Check if source File already contains component files
        // This is an upgrade, if it replaces old component files
        response.setStatus(ComponentImportResponse.Status.UPGRADED);
        JSONArray oldComponents, newComponents;
        if (StorageUtil.basename(name).equals("component.json")) { // TODO : we need a more secure check
          oldComponents = new JSONArray("[" + storageIo.downloadFile(userInfoProvider.getUserId(), projectId, destination, StorageUtil.DEFAULT_CHARSET));
          newComponents = new JSONArray("[" + new String(contents.get(name), StorageUtil.DEFAULT_CHARSET) + "]");
          types = applyRenames(oldComponents, newComponents);
          // upgrade component.json to components.json
          contents.remove(name);
          name = name.substring(0, name.length() - "component.json".length()) + "components.json";
          contents.put(name, newComponents.toString().getBytes(StorageUtil.DEFAULT_CHARSET));
        } else if (StorageUtil.basename(name).equals("components.json")) {
          oldComponents = new JSONArray(storageIo.downloadFile(userInfoProvider.getUserId(), projectId, destination, StorageUtil.DEFAULT_CHARSET));
          newComponents = new JSONArray(new String(contents.get(name), StorageUtil.DEFAULT_CHARSET));
          types = applyRenames(oldComponents, newComponents);
          contents.put(name, newComponents.toString().getBytes(StorageUtil.DEFAULT_CHARSET));
        }
      } else if(StorageUtil.basename(name).equals("components.json")) {
        String oldDestination = destination.substring(0, destination.length() - "components.json".length()) + "component.json";
        if (sourceFiles.contains(oldDestination)) {
          // old extension used component.json but new extension is components.json
          JSONArray oldComponents, newComponents;
          oldComponents = new JSONArray("[" + storageIo.downloadFile(userInfoProvider.getUserId(), projectId, destination, StorageUtil.DEFAULT_CHARSET) + "]");
          newComponents = new JSONArray(new String(contents.get(name), StorageUtil.DEFAULT_CHARSET));
          types = applyRenames(oldComponents, newComponents);
          storageIo.deleteFile(userInfoProvider.getUserId(), projectId, oldDestination);
        } else {
          // new file
          types = extractTypes(new JSONArray(new String(contents.get(name), StorageUtil.DEFAULT_CHARSET)));
        }
      } else if(StorageUtil.basename(name).equals("component.json")) {
        String altDestination = destination.substring(0, destination.length() - "component.json".length()) + "components.json";
        String arrayContent = "[" + new String(contents.get(name), StorageUtil.DEFAULT_CHARSET) + "]";
        if (sourceFiles.contains(altDestination)) {
          // potential downgrade? new extensions have components.json
          types = applyRenames(new JSONArray(storageIo.downloadFile(userInfoProvider.getUserId(), projectId, destination, StorageUtil.DEFAULT_CHARSET)), new JSONArray(arrayContent));
          // upgrade component.json to components.json
          contents.remove(name);
          contents.put(altDestination, arrayContent.getBytes(StorageUtil.DEFAULT_CHARSET));
          name = altDestination;
        } else {
          // new file; force upgrade to components.json
          types = extractTypes(new JSONArray(arrayContent));
          // upgrade component.json to components.json
          contents.remove(name);
          contents.put(altDestination, arrayContent.getBytes(StorageUtil.DEFAULT_CHARSET));
          name = altDestination;
        }
      }
      FileNode fileNode = new YoungAndroidComponentNode(StorageUtil.basename(name), destination);
      fileImporter.importFile(userInfoProvider.getUserId(), projectId,
          destination, new ByteArrayInputStream(contents.get(name)));
      compNodes.add(fileNode);
    }

    response.setComponentTypes(types);
    response.setNodes(compNodes);
  }


  private String collectImportErrorInfo(String path, long projectId) {
    return "Error importing " + path + " to project " + projectId;
  }

  private static boolean isUnknownSource(String url) {
    // TODO: check if the url is from the market place
    return false;
  }
}
