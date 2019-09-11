// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.storage.StorageUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

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
   */

  // Constants for accessing split URI
  /*
   * Download kind can be: "project-output", "project-source",
   * "all-projects-source", "file", or "userfile".
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

    RawFile downloadableFile;

    String userId = null;

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
          includeScreenShots, false, false);
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
          for (Long pid: storageIo.getProjects(projectUserId)) {
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
          projectId, /* include history*/ true, /* include keystore */ true, zipName, true, true, false, false);
        downloadableFile = zipFile.getRawFile();

      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_ALL_PROJECTS_SOURCE)) {
        // Download all project source files as a zip of zips.
        ProjectSourceZip zipFile = fileExporter.exportAllProjectsSourceZip(
            userId, "all-projects.zip");
        downloadableFile = zipFile.getRawFile();

      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_FILE)) {
        // Download a specific file.
        uriComponents = uri.split("/", SPLIT_LIMIT_FILE);
        long projectId = Long.parseLong(uriComponents[PROJECT_ID_INDEX]);
        String filePath = (uriComponents.length > FILE_PATH_INDEX) ?
            uriComponents[FILE_PATH_INDEX] : null;
        downloadableFile = fileExporter.exportFile(userId, projectId, filePath);

      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_USERFILE)) {
        // Download a specific user file, such as android.keystore
        uriComponents = uri.split("/", SPLIT_LIMIT_USERFILE);
        if (uriComponents.length > USERFILE_PATH_INDEX) {
          String filePath = uriComponents[USERFILE_PATH_INDEX];
          downloadableFile = fileExporter.exportUserFile(userId, filePath);
        } else {
          throw new IllegalArgumentException("Missing user file path.");
        }

      } else {
        throw new IllegalArgumentException("Unknown download kind: " + downloadKind);
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
    }

    String fileName = downloadableFile.getFileName();
    byte[] content = downloadableFile.getContent();

    // Set http response information
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setHeader("content-disposition", "attachment; filename=\"" + fileName + "\"");
    resp.setContentType(StorageUtil.getContentTypeForFilePath(fileName));
    resp.setContentLength(content.length);

    // Attach download data
    ServletOutputStream out = resp.getOutputStream();
    out.write(content);
    out.close();
  }
}
