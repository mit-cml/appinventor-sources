// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.UserProject;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for uploading files.
 *
 */
public class UploadServlet extends OdeServlet {
  // Logging support
  private static final Logger LOG = Logger.getLogger(UploadServlet.class.getName());

  // Object used to safely set cache headers in responses
  private static final CacheHeaders CACHE_HEADERS = new CacheHeadersImpl();

  // Content type for response header (to avoid security vulnerabilities)
  private static final String CONTENT_TYPE = "text/html; charset=utf-8";

  private final FileImporter fileImporter = new FileImporterImpl();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    setDefaultHeader(resp);

    // URIs for upload requests are structured as follows:
    //  /upload/{(project/<projectname>)|(file/<projectId>/<filename>)
    String uriComponents[] = req.getRequestURI().split("/", 6);
    String uploadKind = uriComponents[3];

    UploadResponse uploadResponse;

    try {
      if (uploadKind.equals("project")) {
        String projectName = uriComponents[4];
        InputStream uploadedStream;
        try {
          uploadedStream = getRequestStream(req, ServerLayout.UPLOAD_PROJECT_ARCHIVE_FORM_ELEMENT);
        } catch (Exception e) {
          throw CrashReport.createAndLogError(LOG, req, null, e);
        }

        try {
          UserProject userProject = fileImporter.importProject(userInfoProvider.getUserId(),
              projectName, uploadedStream);
          String info = userProject.toString();
          uploadResponse = new UploadResponse(UploadResponse.Status.SUCCESS, 0, info);
        } catch (FileImporterException e) {
          uploadResponse = e.uploadResponse;
        }
      } else if (uploadKind.equals("file")) {
        long projectId = Long.parseLong(uriComponents[4]);
        String fileName = uriComponents[5];
        InputStream uploadedStream;
        try {
          uploadedStream = getRequestStream(req, ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
        } catch (Exception e) {
          throw CrashReport.createAndLogError(LOG, req, null, e);
        }

        try {
          long modificationDate = fileImporter.importFile(userInfoProvider.getUserId(),
              projectId, fileName, uploadedStream);
          uploadResponse = new UploadResponse(UploadResponse.Status.SUCCESS, modificationDate);
        } catch (FileImporterException e) {
          uploadResponse = e.uploadResponse;
        }
      } else {
        throw CrashReport.createAndLogError(LOG, req, null,
            new IllegalArgumentException("Unknown upload kind: " + uploadKind));
      }

      // Now, get the PrintWriter for the servlet response and print the UploadResponse.
      // On the client side, in the onSubmitComplete method in ode/client/utils/Uploader.java, the
      // UploadResponse value will be retrieved as a String via the
      // FormSubmitCompleteEvent.getResults() method.
      PrintWriter out = resp.getWriter();
      out.print(uploadResponse.formatAsHtml());

    } catch (IOException e) {
      throw CrashReport.createAndLogError(LOG, req, null, e);
    }

    // Set http response information
    resp.setStatus(HttpServletResponse.SC_OK);
  }

  private InputStream getRequestStream(HttpServletRequest req, String expectedFieldName)
      throws Exception {
    ServletFileUpload upload = new ServletFileUpload();
    FileItemIterator iterator = upload.getItemIterator(req);
    while (iterator.hasNext()) {
      FileItemStream item = iterator.next();
      if (item.getFieldName().equals(expectedFieldName)) {
        return item.openStream();
      }
    }

    throw new IllegalArgumentException("Field " + expectedFieldName + " not found in upload");
  }

  /**
   * Set a default http header to avoid security vulnerabilities.
   */
  private static void setDefaultHeader(HttpServletResponse resp) {
    CACHE_HEADERS.setNotCacheable(resp);
    resp.setContentType(CONTENT_TYPE);
  }
}
