// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.component.Component;
import com.google.appinventor.shared.rpc.component.ComponentService;
import com.google.appinventor.shared.storage.StorageUtil;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
  public boolean importComponentToProject(Component component, long projectId,
      String folderPath) {
    String gcsPath = storageIo.getGcsPath(component);

    if (gcsPath == null) {
      // component may not come from the datastore so gcsPath is null
      return false;
    }

    try {
      byte[] compContent = storageIo.getGcsFileContent(gcsPath);
      Map<String, byte[]> contents =
          extractContents(new ByteArrayInputStream(compContent));
      importToProject(contents, projectId, folderPath);

    } catch (FileImporterException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(gcsPath, projectId), e);
    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(gcsPath, projectId), e);
    }

    return true;
  }

  @Override
  public boolean importComponentToProject(String url, long projectId,
      String folderPath) {
    if (isUnknownSource(url)) {
      return false;
    }

    try {
      URL compUrl = new URL(url);
      Map<String, byte[]> contents = extractContents(compUrl.openStream());
      importToProject(contents, projectId, folderPath);

    } catch (FileImporterException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(url, projectId), e);
    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(url, projectId), e);
    }

    return true;
  }

  @Override
  public void deleteComponent(Component component) {
    if (!component.getAuthorId().equals(userInfoProvider.getUserId())) {
      throw CrashReport.createAndLogError(LOG, null,
          "The user who is deleting the component with id " + component.id() +
          " is not the author.", null);
    }
    storageIo.deleteComponent(component);
  }

  private Map<String, byte[]> extractContents(InputStream inputStream)
      throws IOException {
    Map<String, byte[]> contents = new HashMap<String, byte[]>();

    // assumption: the zip is non-empty
    ZipInputStream zip = new ZipInputStream(inputStream);
    ZipEntry entry;
    while ((entry = zip.getNextEntry()) != null) {
      ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
      ByteStreams.copy(zip, contentStream);
      contents.put(entry.getName(), contentStream.toByteArray());
    }
    zip.close();

    return contents;
  }

  private void importToProject(Map<String, byte[]> contents, long projectId,
      String folderPath) throws FileImporterException, IOException {
    for (String name : contents.keySet()) {
      String destination = folderPath + "/external_comps/" + name;
      fileImporter.importFile(userInfoProvider.getUserId(), projectId,
          destination, new ByteArrayInputStream(contents.get(name)));
    }
  }

  private String collectImportErrorInfo(String path, long projectId) {
    return "Error importing " + path + " to project " + projectId;
  }

  private static boolean isUnknownSource(String url) {
    // TODO: check if the url is from the market place
    return false;
  }
}
