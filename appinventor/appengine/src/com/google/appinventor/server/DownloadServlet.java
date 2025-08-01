// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.utils.StringUtils;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

import com.google.appinventor.server.storage.remote.RemoteStorage;
import com.google.appinventor.server.storage.remote.RemoteStorageInstanceHolder;
import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;

import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet for downloading project source and output files.
 *
 */
public class DownloadServlet extends OdeServlet {

  /*
   * URIs for download requests are structured as follows:
   *    /<baseurl>/download/project-output/<projectId>/{<target>}
   *    /<baseurl>/download/project-source/<projectId>/{<title>}
   *    /<baseurl>/download/user-project-source/<projectIdOrName>/<userIdOrEmail>
   *    /<baseurl>/download/all-projects-source
   *    /<baseurl>/download/file/<projectId>/<file-path>
   *    /<baseurl>/download/userfile/<file-path>
   *    /<baseurl>/download/project-cached/<projectId>
   */

  // Constants for accessing split URI
  /*
   * Download kind can be: "project-output", "project-source",
   * "selected-projects-source", "all-projects-source", "file", or "userfile".
   * Constants for these are defined in ServerLayout.
   */
  private static final int DOWNLOAD_KIND_INDEX = 3;

  // PROJECT_ID_INDEX is used for more than one download kind
  private static final int PROJECT_ID_INDEX = 4;

  // Constants used when download kind is "project-output".
  // PROJECT_ID_INDEX = 4 (declared above)
  private static final int TARGET_INDEX = 5;
  private static final int SPLIT_LIMIT_PROJECT_OUTPUT = 6;

  // Constants used when download kind is "project-source".
  // Since the project title may contain slashes, it must be the last component in the URI.
  // PROJECT_ID_INDEX = 4 (declared above)
  private static final int PROJECT_TITLE_INDEX = 5;
  private static final int SPLIT_LIMIT_PROJECT_SOURCE = 6;

  // Constants used when download kind is "user-project-source".
  private static final int USER_PROJECT_USERID_INDEX = 5;
  private static final int SPLIT_LIMIT_USER_PROJECT_SOURCE = 6;

  // Constants used when download kind is "file".
  // Since the file path may contain slashes, it must be the last component in the URI.
  // PROJECT_ID_INDEX = 4 (declared above)
  private static final int FILE_PATH_INDEX = 5;
  private static final int SPLIT_LIMIT_FILE = 6;

  // Constants used when download kind is "userfile".
  // Since the file path may contain slashes, it must be the last component in the URI.
  private static final int USERFILE_PATH_INDEX = 4;
  private static final int SPLIT_LIMIT_USERFILE = 5;

  // If any file we try to export exceeds 20MB, then use the remote storage solution if
  //   configured.
  // If unconfigured, still use GAE, but it may fail for large files due to response
  //   payload limit.
  private static final int DIRECT_DOWNLOAD_MAX_FILE_SIZE = 20_000_000;

  // Only use remote downloads for specific kind of downloads (and avoid using for other
  //   files like assets, even if "larger", although impossible).
  private static final Set<String> REMOTE_DOWNLOAD_KINDS = Set.of(
      ServerLayout.DOWNLOAD_PROJECT_OUTPUT,
      ServerLayout.DOWNLOAD_PROJECT_SOURCE,
      ServerLayout.DOWNLOAD_USER_PROJECT_SOURCE,
      ServerLayout.DOWNLOAD_SELECTED_PROJECTS_SOURCE,
      ServerLayout.DOWNLOAD_ALL_PROJECTS_SOURCE
  );

  // Logging support
  private static final Logger LOG = Logger.getLogger(DownloadServlet.class.getName());

  // Object used to safely set cache headers in responses
  private static final CacheHeaders CACHE_HEADERS = new CacheHeadersImpl();

  // Content type for response header (to avoid security vulnerabilities)
  private static final String CONTENT_TYPE = "text/html; charset=utf-8";

  private final FileExporter fileExporter = new FileExporterImpl();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Set a default http header to avoid security vulnerabilities.
    CACHE_HEADERS.setNotCacheable(resp);
    resp.setContentType(CONTENT_TYPE);
    final boolean isInline = req.getParameter("inline") != null;

    RawFile downloadableFile;

    String userId = null;

    int statusCode = HttpServletResponse.SC_OK;

    try {
      String uri = req.getRequestURI();
      // First, call split with no limit parameter.
      String[] uriComponents = uri.split("/");
      String downloadKind = uriComponents[DOWNLOAD_KIND_INDEX];

      userId = userInfoProvider.getUserId();

      if (downloadKind.equals(ServerLayout.DOWNLOAD_PROJECT_OUTPUT)) {
        // Download project output file.
        uriComponents = uri.split("/", SPLIT_LIMIT_PROJECT_OUTPUT);
        long projectId = Long.parseLong(uriComponents[PROJECT_ID_INDEX]);
        String target = (uriComponents.length > TARGET_INDEX) ? uriComponents[TARGET_INDEX] : null;
        downloadableFile = fileExporter.exportProjectOutputFile(userId, projectId, target);

      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_PROJECT_SOURCE)) {
        // Download project source files as a zip.
        long projectId = Long.parseLong(uriComponents[PROJECT_ID_INDEX]);
        uriComponents = uri.split("/", SPLIT_LIMIT_PROJECT_SOURCE);
        String projectTitle = (uriComponents.length > PROJECT_TITLE_INDEX) ?
            uriComponents[PROJECT_TITLE_INDEX] : null;
        final boolean includeProjectHistory = true;
        String zipName = (projectTitle == null) ? null :
            StringUtils.normalizeForFilename(projectTitle) + ".aia";
        // If the requester is an Admin, we include any Yail files in the
        // project in the export
        boolean includeYail = userInfoProvider.getIsAdmin();
        boolean includeScreenShots = includeYail;
        StorageIoInstanceHolder.getInstance().assertUserHasProject(userId, projectId);
        ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userId,
          projectId, includeProjectHistory, false, zipName, includeYail,
          includeScreenShots, false, false, false, false);
        downloadableFile = zipFile.getRawFile();

      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_USER_PROJECT_SOURCE)) {
        if (!userInfoProvider.getIsAdmin()) {
          throw new IllegalArgumentException("Unauthorized.");
        }

        // Download project source files for the specified user project as a zip.
        uriComponents = uri.split("/", SPLIT_LIMIT_USER_PROJECT_SOURCE);

        String userIdOrEmail = uriComponents[USER_PROJECT_USERID_INDEX];
        String projectUserId;
        StorageIo storageIo = StorageIoInstanceHolder.getInstance();
        if (userIdOrEmail.contains("@")) {
          // email address
          try {
            projectUserId = storageIo.findUserByEmail(userIdOrEmail);
          } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(e.getMessage());
          }
        } else {
          projectUserId = userIdOrEmail;
        }

        String projectIdOrName = uriComponents[PROJECT_ID_INDEX];
        String projectName;
        long projectId = 0;
        try {
          // try to parse the projectIdOrName as a number, since project names
          // must start with a letter.
          projectId = Long.parseLong(projectIdOrName);
          projectName = storageIo.getProjectName(projectUserId, projectId);
        } catch (NumberFormatException e) {
          // assume we got a name instead
          for (Long pid : storageIo.getProjects(projectUserId)) {
            if (storageIo.getProjectName(projectUserId, pid).equals(projectIdOrName)) {
              projectId = pid;
            }
          }
          if (projectId == 0) {
            // didn't find project by name
            throw new IllegalArgumentException("Can't find a project named "
                + projectIdOrName + " for user id " + projectUserId);
          } else {
            projectName = projectIdOrName;
          }
        }
        String zipName;
        if (!projectName.isEmpty()) {
          zipName = projectName + "_" + projectUserId + ".aia";
        } else {
          zipName = "u" + projectUserId + "_p" + projectId + ".aia";
        }
        ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(projectUserId,
          projectId, /* include history*/ true, /* include keystore */ true, zipName, true, true, false, false, false, false);
        downloadableFile = zipFile.getRawFile();
      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_SELECTED_PROJECTS_SOURCE)) {
        String[] projectIdStrings = uriComponents[PROJECT_ID_INDEX].split("-");
        List<Long> projectIds = new ArrayList<Long>();
        for (String projectId : projectIdStrings) {
          long pid = Long.parseLong(projectId);
          StorageIoInstanceHolder.getInstance().assertUserHasProject(userId, pid);
          projectIds.add(pid);
        }
        ProjectSourceZip zipFile = fileExporter.exportSelectedProjectsSourceZip(
          userId, "selected-projects.zip", projectIds);
        downloadableFile = zipFile.getRawFile();
      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_ALL_PROJECTS_SOURCE)) {
        // Download all project source files as a zip of zips.
        ProjectSourceZip zipFile = fileExporter.exportAllProjectsSourceZip(
            userId, "all-projects.zip");
        downloadableFile = zipFile.getRawFile();

      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_FILE)) {
        // Download a specific file.
        // compute the hash and check if the hash matches the header coming in
        // (HttpServerRequest req has the header)
        uriComponents = uri.split("/", SPLIT_LIMIT_FILE);
        long projectId = Long.parseLong(uriComponents[PROJECT_ID_INDEX]);
        String filePath = (uriComponents.length > FILE_PATH_INDEX) ?
          uriComponents[FILE_PATH_INDEX] : null;
        StorageIoInstanceHolder.getInstance().assertUserHasProject(userId, projectId);
        downloadableFile = fileExporter.exportFile(userId, projectId, filePath);
        byte[] fileContent = downloadableFile.getContent();

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        // Note: We put quotes around the hash to confirm with RFC7232
        String fileHash = "\"" + byteArray2Hex(md.digest(fileContent)) + "\"";
        // if equal, return 304
        if (fileHash.equals(req.getHeader("If-None-Match"))) {
          statusCode = HttpServletResponse.SC_NOT_MODIFIED;
        }
        resp.setHeader("ETag", fileHash);
      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_USERFILE)) {
        // Download a specific user file, such as android.keystore
        uriComponents = uri.split("/", SPLIT_LIMIT_USERFILE);
        if (uriComponents.length > USERFILE_PATH_INDEX) {
          String filePath = uriComponents[USERFILE_PATH_INDEX];
          downloadableFile = fileExporter.exportUserFile(userId, filePath);
        } else {
          throw new IllegalArgumentException("Missing user file path.");
        }
      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_PROJECT_CACHED)) {
        // Download project source files as a zip.
        long projectId = Long.parseLong(uriComponents[PROJECT_ID_INDEX]);
        uriComponents = uri.split("/", SPLIT_LIMIT_PROJECT_SOURCE);
        String projectTitle = (uriComponents.length > PROJECT_TITLE_INDEX) ?
          uriComponents[PROJECT_TITLE_INDEX] : null;
        String zipName = (projectTitle == null) ? null :
          StringUtils.normalizeForFilename(projectTitle) + ".aia";
        // Set includeYail to true by default to ensure downloaded file has yail file on hand.
        boolean includeYail = true;
        StorageIoInstanceHolder.getInstance().assertUserHasProject(userId, projectId);
        ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userId,
          projectId, false, false, zipName, includeYail,
          false, false, false, false, true);
        downloadableFile = zipFile.getRawFile();
      } else {
        throw new IllegalArgumentException("Unknown download kind: " + downloadKind);
      }

      if (!isInline) {
        final String downloadObjectUrl = shouldUseRemoteStorageDownload(downloadKind, userId, downloadableFile);
        if (downloadObjectUrl != null) {
          LOG.info("File sent to Remote Storage: " + downloadableFile.getFileName());
          resp.sendRedirect(downloadObjectUrl);
          return;
        }
      }
    } catch (IllegalArgumentException e) {
      throw CrashReport.createAndLogError(LOG, req, "user=" + userId, e);
    } catch (SecurityException e) {
      // Not having appropriate permission is akin to not being able to find the project anyway,
      // so we use 404 here to not leak that the project may exist.
      final String message = "404 Not Found";
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      resp.setContentType("text/plain");
      resp.setContentLength(message.length());
      ServletOutputStream out = resp.getOutputStream();
      out.write(message.getBytes());
      out.close();
      return;
    } catch (NoSuchAlgorithmException e) {
      throw CrashReport.createAndLogError(LOG, req, "user=" + userId, e);
    }

    resp.setStatus(statusCode);

    if (statusCode == HttpServletResponse.SC_OK) {
      LOG.fine("Sending File!");
      String fileName = downloadableFile.getFileName();
      byte[] content = downloadableFile.getContent();
      // Set http response information
      final String contentDispositionHeaderValue;
      if (isInline) {
        contentDispositionHeaderValue = "inline";
      } else {
        contentDispositionHeaderValue = "attachment; filename=\"" + fileName + "\"";
      }
      resp.setHeader("content-disposition", contentDispositionHeaderValue);
      resp.setContentType(StorageUtil.getContentTypeForFilePath(fileName));
      resp.setContentLength(content.length);

      // Attach download data
      ServletOutputStream out = resp.getOutputStream();
      out.write(content);
      out.close();
    } else {                    // Not sure this is needed... we are not sending any data
      LOG.fine("File Cached, not sending File!");
      resp.setContentLength(0);
      ServletOutputStream out = resp.getOutputStream();
      out.close();
    }
  }

  private String shouldUseRemoteStorageDownload(final String downloadKind, final String userId, final RawFile file)
      throws IOException {
    if (!REMOTE_DOWNLOAD_KINDS.contains(downloadKind)) {
      // Skip non-project related downloads
      return null;
    }

    if (!RemoteStorageInstanceHolder.isRemoteConfigured(RemoteStorageInstanceHolder.Usage.EXPORT)) {
      // Skip any further checks if unconfigured
      return null;
    }

    if (file.getContent().length <= DIRECT_DOWNLOAD_MAX_FILE_SIZE) {
      // File is within the GAE limits, hence no need to use the remote option
      return null;
    }

    final String fileName = file.getFileName();
    LOG.info("Sending file to Remote Storage: " + fileName);

    final RemoteStorage remoteStorage = RemoteStorageInstanceHolder.getInstance(RemoteStorageInstanceHolder.Usage.EXPORT);
    final String objectKey = remoteStorage.getProjectExportObjectKey(downloadKind, userId, fileName);

    final String uploadUrlStr = remoteStorage.generateUploadUrl(objectKey);

    HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrlStr).openConnection();
    connection.setDoOutput(true);
    connection.setRequestMethod("PUT");
    connection.addRequestProperty("Content-Type", StorageUtil.getContentTypeForFilePath(fileName));
    // Ensure that, when downloading the file from remote, it preserves the same name
    // If this is not set, and Content-Type is set to ZIP for AIA, browsers will rewrite .aia to .zip
    connection.addRequestProperty("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    connection.setConnectTimeout(60000);
    connection.setReadTimeout(60000);
    try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream())) {
      try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(file.getContent()))) {
        ByteStreams.copy(bufferedInputStream, bufferedOutputStream);
        bufferedOutputStream.flush();
      }
    }

    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
      LOG.severe("Failed to upload output: "+ connection.getResponseCode());
      // If we fail to upload the output, return null so we try to fallback to GAE
      return null;
    }

    return remoteStorage.generateRetrieveUrl(objectKey);
  }

  private static String byteArray2Hex(final byte[] hash) {
    Formatter formatter = new Formatter();
    for (byte b : hash) {
      formatter.format("%02x", b);
    }
    return formatter.toString();
  }
}
