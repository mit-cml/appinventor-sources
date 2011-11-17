// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.storage.StorageUtil;

import java.io.IOException;
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
   *    /<baseurl>/download/all-projects-source
   *    /<baseurl>/download/file/<projectId>/<file-path>
   * </pre>
   */

  // Constants for accessing split URI
  /*
   * Download kind can be: "project-output", "project-source",
   * "all-projects-source", or "file".
   * Constants for these are defined in ServerLayout.
   */
  private static final int DOWNLOAD_KIND_INDEX = 3;
  private static final int PROJECT_ID_INDEX = 4;

  // NOTE(lizlooney) - currently all the SPLIT_LIMIT_... constants are 6, but
  // the code does not assume that.

  // Constants used when download kind is "project-output".
  private static final int TARGET_INDEX = 5;
  private static final int SPLIT_LIMIT_PROJECT_OUTPUT = 6;

  // Constants used when download kind is "project-source".
  // Since the project title may contain slashes, it must be the last component in the URI.
  private static final int PROJECT_TITLE_INDEX = 5;
  private static final int SPLIT_LIMIT_PROJECT_SOURCE = 6;

  // Constants used when download kind is "project-source".
  // Since the file path may contain slashes, it must be the last component in the URI.
  private static final int FILE_PATH_INDEX = 5;
  private static final int SPLIT_LIMIT_FILE = 6;


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

    try {
      String uri = req.getRequestURI();
      // First, call split with no limit parameter.
      String[] uriComponents = uri.split("/");
      String downloadKind = uriComponents[DOWNLOAD_KIND_INDEX];
      String userId = userInfoProvider.getUserId();
      long projectId = 0;
      // When downloading all projects, no project id is specified.
      if (!downloadKind.equals(ServerLayout.DOWNLOAD_ALL_PROJECTS_SOURCE)) {
        projectId = Long.parseLong(uriComponents[PROJECT_ID_INDEX]);
      }

      if (downloadKind.equals(ServerLayout.DOWNLOAD_PROJECT_OUTPUT)) {
        // Download project output file.
        uriComponents = uri.split("/", SPLIT_LIMIT_PROJECT_OUTPUT);
        String target = (uriComponents.length > TARGET_INDEX) ? uriComponents[TARGET_INDEX] : null;
        downloadableFile = fileExporter.exportProjectOutputFile(userId, projectId, target);

      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_PROJECT_SOURCE)) {
        // Download project source files as a zip.
        uriComponents = uri.split("/", SPLIT_LIMIT_PROJECT_SOURCE);
        String projectTitle = (uriComponents.length > PROJECT_TITLE_INDEX) ?
            uriComponents[PROJECT_TITLE_INDEX] : null;
        final boolean includeProjectHistory = true;
        String zipName = (projectTitle == null) ? null :
            StringUtils.normalizeForFilename(projectTitle) + ".zip";
        ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userId,
            projectId, includeProjectHistory, false, zipName);
        downloadableFile = zipFile.getRawFile();

      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_ALL_PROJECTS_SOURCE)) {
        // Download all project source files as a zip of zips.
        ProjectSourceZip zipFile = fileExporter.exportAllProjectsSourceZip(
            userId, "all-projects.zip");
        downloadableFile = zipFile.getRawFile();

      } else if (downloadKind.equals(ServerLayout.DOWNLOAD_FILE)) {
        // Download a specific file.
        uriComponents = uri.split("/", SPLIT_LIMIT_FILE);
        String filePath = (uriComponents.length > FILE_PATH_INDEX) ?
            uriComponents[FILE_PATH_INDEX] : null;
        downloadableFile = fileExporter.exportFile(userId, projectId, filePath);

      } else {
        throw new IllegalArgumentException("Unknown download kind: " + downloadKind);
      }
    } catch (IllegalArgumentException e) {
      throw CrashReport.createAndLogError(LOG, req, null, e);
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
