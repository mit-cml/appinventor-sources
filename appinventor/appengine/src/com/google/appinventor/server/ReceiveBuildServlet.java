// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.project.utils.Security;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for receiving build output files and build error information from a Build Server.
 *
 * <p>This needs to be done from a servlet that does not require login because
 * posts from the Build Server do not contain login information. To ensure
 * safety they contain an encrypted user and project ID as part of
 * their URL.
 *
 * @author markf@google.com (Mark Friedman)
 */
public class ReceiveBuildServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(ReceiveBuildServlet.class.getName());

  private final OdeAuthFilter odeFilter = new OdeAuthFilter();
  private final transient StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // URIs for receivebuild requests are structured as follows:
    //   /<baseurl>/receivebuild/encryptedUserAndProjectId/fileId
    String uriComponents[] = req.getRequestURI().split("/", 5);

    // TODO(lizlooney,user) If the URI doesn't contain enough components, the following lines
    // will throw an ArrayIndexOutOfBoundsException. We could deal with that outcome more cleanly
    // by returning an HTTP error code. This applies to all of our servlets.

    String userId;
    long projectId;
    try {
      userId = Security.decryptUserId(uriComponents[3]);
      projectId = Security.decryptProjectId(uriComponents[3]);
    } catch (EncryptionException e) {
      throw CrashReport.createAndLogError(LOG, req, null, e);
    }

    // Set the user in the OdeFilter, which is used everywhere as the UserInfoProvider.
    odeFilter.setUserFromUserId(userId, false, false);
    try {
      String buildFileDirPath = uriComponents[4];
      ZipInputStream zipInputStream = new ZipInputStream(req.getInputStream());
      while (true) {
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        if (zipEntry == null) {
          break;
        }
        String fileName = zipEntry.getName();
        byte[] fileBytes = ByteStreams.toByteArray(zipInputStream);
        if (StorageUtil.ANDROID_KEYSTORE_FILENAME.equals(fileName)) {
          LOG.info("Saving android.keystore for user: " + userId);
          storageIo.addFilesToUser(userId, StorageUtil.ANDROID_KEYSTORE_FILENAME);
          storageIo.uploadRawUserFile(userId, fileName, fileBytes);
        } else if (fileName.equals("build.status")) {
          int progress = Integer.parseInt((new String(fileBytes)).trim());
          LOG.info("Received a build.status file contents = " + progress);
          storageIo.storeBuildStatus(userId, projectId, progress);
        } else {
          String filePath = buildFileDirPath + "/" + fileName;
          LOG.info("Saving build output files: " + filePath);
          storageIo.addOutputFilesToProject(userId, projectId, filePath);
          storageIo.uploadRawFileForce(projectId, filePath, userId, fileBytes);
          storageIo.storeBuildStatus(userId, projectId, 0); // Reset for the next build
        }
      }
    } finally {
      odeFilter.removeUser();
    }
  }
}
