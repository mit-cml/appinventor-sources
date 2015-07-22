// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.component.ComponentInfo;
import com.google.appinventor.shared.rpc.component.ComponentService;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

public class ComponentServiceImpl extends OdeRemoteServiceServlet implements ComponentService {

  private static final Logger LOG = Logger.getLogger(ComponentServiceImpl.class.getName());

  private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  private final FileImporter fileImporter = new FileImporterImpl();

  @Override
  public List<ComponentInfo> getComponentInfos() {
    return storageIo.getComponentInfos(userInfoProvider.getUserId());
  }

  @Override
  public List<ProjectNode> importComponentToProject(ComponentInfo info, long projectId, String folderPath) {
    String gcsPath = storageIo.getGcsPath(info);
    ArrayList<ProjectNode> childNodes = new ArrayList<ProjectNode>();

    if (gcsPath == null) {
      // info may not come from the datastore so gcsPath is null
      // return an empty ArrayList rathan than null because
      // the ensuing code will not return an empty ArrayList
      return childNodes;
    }

    byte[] fileContent = storageIo.getGcsFileContent(gcsPath);

    try {
      // assumption: the zip is non-empty
      ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(fileContent));
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        ByteStreams.copy(zip, contentStream);

        String basename = StorageUtil.basename(entry.getName());

        FileNode fileNode = new FileNode(basename, folderPath + "/" + basename);
        childNodes.add(fileNode);

        fileImporter.importFile(
            userInfoProvider.getUserId(),
            projectId,
            folderPath + "/" + basename,
            new ByteArrayInputStream(contentStream.toByteArray()));
      }
      zip.close();
    } catch (ZipException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(gcsPath, projectId), e);
    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(gcsPath, projectId), e);
    } catch (FileImporterException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectImportErrorInfo(gcsPath, projectId), e);
    }

    return childNodes;
  }

  private String collectImportErrorInfo(String gcsPath, long projectId) {
    return "Error importing " + gcsPath + " to project " + projectId;
  }
}
