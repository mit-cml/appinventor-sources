// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
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
  public List<Component> getComponents() {
    return storageIo.getComponents(userInfoProvider.getUserId());
  }

  @Override
  public List<ProjectNode> importComponentToProject(Component component, long projectId,
      String folderPath) {
    List<ProjectNode> compNodes = new ArrayList<ProjectNode>();
    String gcsPath = storageIo.getGcsPath(component);

    if (gcsPath == null) {
      // component may not come from the datastore so gcsPath is null
      return compNodes;
    }

    try {
      byte[] compContent = storageIo.getGcsFileContent(gcsPath);
      Map<String, byte[]> contents =
          extractContents(new ByteArrayInputStream(compContent));
      compNodes = importToProject(contents, projectId, folderPath);

    } catch (FileImporterException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(gcsPath, projectId), e);
    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(gcsPath, projectId), e);
    }

    return compNodes;
  }

  @Override
  public List<ProjectNode> importComponentToProject(String url, long projectId,
      String folderPath) {
    List<ProjectNode> compNodes = new ArrayList<ProjectNode>();
    if (isUnknownSource(url)) {
      return compNodes;
    }

    try {
      URL compUrl = new URL(url);
      Map<String, byte[]> contents = extractContents(compUrl.openStream());
      compNodes = importToProject(contents, projectId, folderPath);

    } catch (FileImporterException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(url, projectId), e);
    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(url, projectId), e);
    }

    return compNodes;
  }

  @Override
  public void deleteComponent(Component component) {
    if (!component.getAuthorId().equals(userInfoProvider.getUserId())) {
      throw CrashReport.createAndLogError(LOG, null,
          "The user who is deleting the component with id " + component.getId() +
          " is not the author.", null);
    }
    storageIo.deleteComponent(component);
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

  private List<ProjectNode> importToProject(Map<String, byte[]> contents, long projectId,
      String folderPath) throws FileImporterException, IOException {
    List<ProjectNode> compNodes = new ArrayList<ProjectNode>();
    List<String> sourceFiles = storageIo.getProjectSourceFiles(userInfoProvider.getUserId(), projectId);
    for (String name : contents.keySet()) {
      String destination = folderPath + "/external_comps/" + name;
      if (sourceFiles.contains(destination)) {  // Check if source File already contains component files
        compNodes.clear();
        return compNodes; // Fail the Import!!
      }
      FileNode fileNode = new YoungAndroidComponentNode(StorageUtil.basename(name), destination);
      fileImporter.importFile(userInfoProvider.getUserId(), projectId,
          destination, new ByteArrayInputStream(contents.get(name)));
      compNodes.add(fileNode);
    }
    return compNodes;
  }


  private String collectImportErrorInfo(String path, long projectId) {
    return "Error importing " + path + " to project " + projectId;
  }

  private static boolean isUnknownSource(String url) {
    // TODO: check if the url is from the market place
    return false;
  }
}
