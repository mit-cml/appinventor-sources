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

import java.util.ArrayList;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
  public void importComponentToProject(ComponentInfo info, long projectId) {
    String gcsPath = storageIo.getGcsPath(info);
    if (gcsPath == null) {
      // todo: handel the case that the datastore cannot find the
      // corresponding entry of info
    } else {
      byte[] fileContent = storageIo.getGcsFileContent(gcsPath);
      try {
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(fileContent));
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
          ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
          ByteStreams.copy(zip, contentStream);

          fileImporter.importFile(
              userInfoProvider.getUserId(),
              projectId,
              "assets/" + entry.getName(),
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
    }
  }
}
