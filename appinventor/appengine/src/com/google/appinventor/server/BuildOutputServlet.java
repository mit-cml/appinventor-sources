// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.storage.StorageUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * Servlet for downloading project source and output files.
 *
 */
public class BuildOutputServlet extends OdeServlet {

  /*
   * URIs for Build Output requests are structured as follows:
   *    /<baseurl>/<nonce>
   */

  // Logging support
  private static final Logger LOG = Logger.getLogger(BuildOutputServlet.class.getName());

  // Object used to safely set cache headers in responses
  private static final CacheHeaders CACHE_HEADERS = new CacheHeadersImpl();

  // Content type for response header (to avoid security vulnerabilities)
  private static final String CONTENT_TYPE = "text/html; charset=utf-8";

  private final FileExporter fileExporter = new FileExporterImpl();

  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Set a default http header to avoid security vulnerabilities.
    CACHE_HEADERS.setNotCacheable(resp);
    resp.setContentType(CONTENT_TYPE);

    RawFile downloadableFile;

    String userId = null;
    String nonceValue = null;

    try {
      String uri = req.getRequestURI();
      // First, call split with no limit parameter.
      String[] uriComponents = uri.split("/");
      if (uriComponents.length < 3) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      nonceValue = uriComponents[2];

      storageIo.cleanupNonces(); // This removes expired Nonce objects
                                 // (10 at a time so we don't spend too
                                 // much time doing it)

      Nonce nonce = storageIo.getNoncebyValue(nonceValue);
      if (nonce == null) {
        resp.sendError(resp.SC_NOT_FOUND, "Invalid Link");
        return;
      }
      Date now = new Date();
      if ((now.getTime() - nonce.getTimeStamp().getTime()) > 7200*1024) {
        resp.sendError(resp.SC_NOT_FOUND, "Link has timed out");
        return;
      }
      downloadableFile = fileExporter.exportProjectOutputFile(nonce.getUserId(), nonce.getProjectId(), null);

    } catch (FileNotFoundException e) {
      // This can happen if a new build is running while an attempt is made to download
      // a previous built version of the project
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
