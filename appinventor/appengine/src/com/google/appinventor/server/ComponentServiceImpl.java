// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.component.ComponentImportResponse;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.component.Component;
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
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

  private void importToProject(Map<String, byte[]> contents, long projectId,
      String folderPath, ComponentImportResponse response) throws FileImporterException, IOException {
    List<ProjectNode> compNodes = new ArrayList<ProjectNode>();
    response.setProjectId(projectId);
    List<String> sourceFiles = storageIo.getProjectSourceFiles(userInfoProvider.getUserId(), projectId);
    boolean extensionUpgrade = false;
    String oldCompName; // name with which extension is already imported, we have to grab from the old component files
    for (String name : contents.keySet()) {
      String destination = folderPath + "/external_comps/" + name;
      if (sourceFiles.contains(destination)) {  // Check if source File already contains component files
        // This is an upgrade, if it replaces old component files
        extensionUpgrade = true;
        if (StorageUtil.basename(name).equals("component.json")) { // TODO : we need a more secure check
          // We modify the name property of the new component.json to match that of the already imported component.json
          JSONObject oldCompJson = new JSONObject(storageIo.downloadFile(
              userInfoProvider.getUserId(), projectId, destination, StorageUtil.DEFAULT_CHARSET));
          oldCompName = oldCompJson.getString("name");
          String componentJSONString = new String(contents.get(name), StorageUtil.DEFAULT_CHARSET);
          JSONObject newCompJSon = new JSONObject(componentJSONString);
          newCompJSon.put("name", oldCompName); //change the name to the same as that of already imported component
          componentJSONString = newCompJSon.toString(1); // 1 is the indent factor, let it look beautiful
          contents.put(name, componentJSONString.getBytes(StorageUtil.DEFAULT_CHARSET));
        }
      }
      FileNode fileNode = new YoungAndroidComponentNode(StorageUtil.basename(name), destination);
      fileImporter.importFile(userInfoProvider.getUserId(), projectId,
          destination, new ByteArrayInputStream(contents.get(name)));
      compNodes.add(fileNode);
    }

    if (extensionUpgrade) {
      response.setStatus(ComponentImportResponse.Status.UPGRADED);
    } else {
      response.setStatus(ComponentImportResponse.Status.IMPORTED);
    }
    String type = contents.keySet().iterator().next(); // get an element
    type = type.substring(0, type.indexOf('/')); // get the type
    response.setComponentType(type);
    response.setNodes(compNodes);
    return;
  }


  private String collectImportErrorInfo(String path, long projectId) {
    return "Error importing " + path + " to project " + projectId;
  }

  private static boolean isUnknownSource(String url) {
    // TODO: check if the url is from the market place
    return false;
  }
}
