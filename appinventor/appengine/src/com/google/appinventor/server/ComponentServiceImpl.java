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
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

public class ComponentServiceImpl extends OdeRemoteServiceServlet implements ComponentService {
  private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  private final FileImporter fileImporter = new FileImporterImpl();

  @Override
  public List<ComponentInfo> getComponentInfos() {
    return storageIo.getComponentInfos(userInfoProvider.getUserId());
  }

  @Override
  public List<ProjectNode> importComponentToProject(ComponentInfo info, ProjectNode parentNode) {
    String gcsPath = storageIo.getGcsPath(info);

    if (gcsPath == null) {
      // todo: handel the case that the datastore cannot find the
      // corresponding entry of info
      return null;
    }

    byte[] fileContent = storageIo.getGcsFileContent(gcsPath);
    ArrayList<ProjectNode> childNodes = new ArrayList<ProjectNode>();

    try {
      ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(fileContent));
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        ByteStreams.copy(zip, contentStream);

        String basename = StorageUtil.basename(entry.getName());
        FileNode fileNode = new FileNode(basename, parentNode.getFileId() + "/" + basename);
        childNodes.add(fileNode);

        fileImporter.importFile(
            userInfoProvider.getUserId(),
            parentNode.getProjectId(),
            fileNode.getFileId(),
            new ByteArrayInputStream(contentStream.toByteArray()));
      }
      zip.close();
    } catch (ZipException e) {
      // todo: handle exception
    } catch (IOException e) {
      // todo: handle exception
    } catch (FileImporterException e) {
      // todo: handle exception
    }

    return childNodes;
  }
}
