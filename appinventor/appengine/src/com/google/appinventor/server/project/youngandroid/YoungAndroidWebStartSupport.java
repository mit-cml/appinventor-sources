// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.appinventor.common.youngandroid.YaHttpServerConstants;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.project.WebStartSupport;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.util.JsonpConnectionUtil;
import com.google.appinventor.server.util.ModifiedHeaders;
import com.google.appinventor.server.util.ResourceUtil;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.shared.storage.StorageUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of {@link WebStartSupport} for Young Android projects.
 *
 */
public final class YoungAndroidWebStartSupport extends WebStartSupport {

  // Logging support
  private static final Logger LOG = Logger.getLogger(YoungAndroidWebStartSupport.class.getName());

  // Content type for response header (to avoid security vulnerabilities)
  private static final String CONTENT_TYPE = "text/html; charset=utf-8";

  /**
   * Creates a YoungAndroidWebStartSupport.
   *
   * @param storageIo the StorageIo instance
   * @param userProvider the user provider
   */
  public YoungAndroidWebStartSupport(StorageIo storageIo, UserInfoProvider userProvider) {
    super(storageIo, userProvider);
  }

  /**
   * {@inheritDoc}
   *
   * The purpose parameter must be one of the following values:
   * <ul>
   * <li>{@link ServerLayout#WEBSTART_JNLP_PURPOSE_CODEBLOCKS}</li>
   * </ul>
   */
  @Override
  public void sendJnlpFile(HttpServletRequest req, HttpServletResponse resp, String purpose) {
    try {
      if (purpose.equals(ServerLayout.WEBSTART_JNLP_PURPOSE_CODEBLOCKS)) {
        // see if we can avoid sending the content again
        if (ModifiedHeaders.notModified(req)) {
          resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          return;
        }
        // Create jnlp file and send it back
        String jnlp = prepareJnlpForJsonpServer(req, "1.5+", "925m",
            ResourceUtil.CODEBLOCKS_JAR, ResourceUtil.CODEBLOCKS_MAIN,
            "App Inventor for Android Blocks Editor",  // title
            "App Inventor for Android Blocks Editor"); // description
        ModifiedHeaders.setHeaders(resp);
        sendFileResponse(resp, "AppInventorForAndroidCodeblocks.jnlp",
            "application/x-java-jnlp-file; charset=utf-8", jnlp.getBytes(Charsets.UTF_8));
      } else {
        throw new IllegalArgumentException("Unknown WebStart purpose: " + purpose);
      }
    } catch (IllegalArgumentException e) {
      throw CrashReport.createAndLogError(LOG, req, null, e);
    } catch (EncryptionException e) {
      throw CrashReport.createAndLogError(LOG, req, null, e);
    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, req, null, e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * Handle GET requests from codeblocks.
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp, String userId,
      long projectId, String fileName) {
    try {
      byte[] data;
      String contentType = "text/plain; charset=utf-8"; // default
      if (fileName.equals(ResourceUtil.CODEBLOCKS_JAR)) {
        // Special case for the codeblocks deploy jar. projectId is ignored.
        // see if we can avoid sending the content again
        if (ModifiedHeaders.notModified(req)) {
          resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          return;
        }
        // Jar files for a web start application need to be properly signed.
        // The codeblocks jar is signed during the ant build.
        data = ResourceUtil.downloadSignedCodeblocksJar();
        ModifiedHeaders.setHeaders(resp);

      } else if (0 == fileName.compareToIgnoreCase(YaHttpServerConstants.STARTER_PHONEAPP_APK)) {
        if (!fileName.equals(YaHttpServerConstants.STARTER_PHONEAPP_APK)) {
          throw CrashReport.createAndLogError(LOG, req, 
              "Bad starter app name, user=" + userId + ", project=" + projectId, 
              null);
        }
        // Special case for the phone component APK. projectId is ignored.
        // Does not need to be signed.
        data = ResourceUtil.downloadStarterAppApk();

      } else if (fileName.startsWith(YaHttpServerConstants.ASSETS_ZIPFILE)) {
        // note: if an attempt to read a particular asset file gives a RuntimeException,
        // we will put a file with that name in the zip prefixed with "bad" (hence
        // all bad asset files should be in directory "badassets/".
        data = getAssetsZipFile(userId, projectId);
        if (data.length == 0) {
          sendNoContentResponse(resp, fileName);
          return;
        }
        fileName = storageIo.getProjectName(userId, projectId) +
          YaHttpServerConstants.ASSET_ZIPFILE_SUFFIX;
        contentType = StorageUtil.getContentTypeForFilePath(fileName);

      } else {
        // Codeblocks is getting a project source file.
        data = getStorageIo().downloadRawFile(userId, projectId, fileName);
      }
      sendFileResponse(resp, fileName, contentType, data);
    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, req, null, e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * Handle Post requests from codeblocks.
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp, String userId,
      long projectId, String fileName) {
    try {
      String encoding = req.getCharacterEncoding();
      if (encoding == null) {
        encoding = "UTF-8";
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          req.getInputStream(), encoding));
      String content;
      try {
        content = CharStreams.toString(reader);
      } finally {
        reader.close();
      }
      if (fileName.startsWith(YaHttpServerConstants.CODEBLOCKS_SAVE_PORT)) {
        // Special case for saving the codeblocks port. projectId is ignored.
        int port = Integer.parseInt(content);
        byte[] data = JsonpConnectionUtil.saveJsonpConnectionInfo(getStorageIo(), userId,
            YaHttpServerConstants.CODEBLOCKS_INFO_FILE_PREFIX, port);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain; charset=utf-8");
        OutputStream out = resp.getOutputStream();
        out.write(data);
        out.close();

      } else {
        // Codeblocks is posting a project source file.
        if (!content.endsWith("\n")) {
          content = content + "\n";
        }
        List<String> sourceFiles = getStorageIo().getProjectSourceFiles(userId, projectId);
        if (!sourceFiles.contains(fileName)) {
          getStorageIo().addSourceFilesToProject(userId, projectId, false, fileName);
        }
        getStorageIo().uploadFileForce(projectId, fileName, userId, content, StorageUtil.DEFAULT_CHARSET);
        // TODO(sharon): technically this should probably return HttpServletResponse.SC_CREATED (201)
        // I'm not sure whether it really matters.
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENT_TYPE);
      }
    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, req, null, e);
    }
  }

  @VisibleForTesting
  public byte[] getAssetsZipFile(String userId, long projectId) throws IOException {
    List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
    List<String> assetFiles = new ArrayList<String>();
    for (String file : files) {
      if (file.startsWith(YoungAndroidProjectService.ASSETS_FOLDER)) {
        assetFiles.add(file);
      }
    }
    if (assetFiles.size() != 0) {
      String fileName;
      // create a zip file containing the asset files, even if there is only 1
      ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
      ZipOutputStream out = new ZipOutputStream(zipFile);
      for (String file : assetFiles) {
        try {
          byte[] data = storageIo.downloadRawFile(userId, projectId, file);
          out.putNextEntry(new ZipEntry(file));
          out.write(data, 0, data.length);
          out.closeEntry();
        } catch (RuntimeException e) {
          // don't let one bad asset spoil the whole bunch (but do leave
          // a marker in the zip file that the asset was bad so that it can
          // be reported to the user)
          out.putNextEntry(new ZipEntry("bad" + file));
          out.closeEntry();
        }
      }
      out.close();
      return zipFile.toByteArray();
    } else {
      return new byte[0];
    }
  }

  private void sendNoContentResponse(HttpServletResponse resp, String fileName) {
    // Set http response information
    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
