// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.storage.StorageUtil;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Set a default http header to avoid security vulnerabilities.
    CACHE_HEADERS.setNotCacheable(resp);
    resp.setContentType(CONTENT_TYPE);
    if ("store=1".equals(req.getQueryString())) {  // Play Store companion adds this for Chrome to
                                                   // do the right thing w.r.t. the download
      String body = "<!DOCTYPE html><html><head><meta http-equiv=\"refresh\" content=\"0; url=" +
          req.getRequestURI() + "\" /></head><body></body></html>";
      resp.setContentLength(body.length());
      ServletOutputStream os = resp.getOutputStream();
      os.write(body.getBytes());
      os.close();
      return;
    }

    RawFile downloadableFile;

    String userId = null;
    String nonceValue = null;

    try {
      String uri = req.getRequestURI();
      // First, call split with no limit parameter.
      String[] uriComponents = uri.split("/");
      System.err.println("Parts: " + Arrays.toString(uriComponents));
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
      if (uriComponents.length == 4) {
        if ("manifest.plist".equals(uriComponents[3])) {
          String host = req.getHeader("Host");
          if (host == null) {
            resp.sendError(400, "Bad request");
            return;
          }
          downloadableFile = getManifest(host, nonce);
        } else if ("app.ipa".equals(uriComponents[3])) {
          downloadableFile = fileExporter.exportProjectOutputFile(nonce.getUserId(),
              nonce.getProjectId(), null, ".ipa");
        } else if ("small.png".equals(uriComponents[3])) {
          downloadableFile = fileExporter.exportProjectOutputFile(nonce.getUserId(),
              nonce.getProjectId(), null, "small.png");
        } else if ("large.png".equals(uriComponents[3])) {
          downloadableFile = fileExporter.exportProjectOutputFile(nonce.getUserId(),
              nonce.getProjectId(), null, "large.png");
        } else {
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
          return;
        }
      } else {
        downloadableFile = fileExporter.exportProjectOutputFile(nonce.getUserId(),
            nonce.getProjectId(), null, "apk");
      }

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

  public RawFile getManifest(String host, Nonce nonce) throws IOException {
    RawFile manifest = fileExporter.exportProjectOutputFile(nonce.getUserId(), nonce.getProjectId(), null, "manifest.plist");
    String content = new String(manifest.getContent());
    String base = "https://" + host + "/b/";
    content = content.replace("PACKAGE-URL", base + nonce.getNonceValue() + "/app.ipa");
    content = content.replace("ICON-URL", base + nonce.getNonceValue() + "/small.png");
    content = content.replace("FULL-IMAGE-URL", base + nonce.getNonceValue() + "/large.png");
    return new RawFile(manifest.getFileName(), content.getBytes());
  }
}
