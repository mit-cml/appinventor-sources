// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server;

import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.GalleryService;
import com.google.appinventor.shared.rpc.project.UserProject;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling gallery's app publishing.
 *
 */
public class GalleryServlet extends OdeServlet {


  /*
   * URIs for upload requests are structured as follows:
   *    /<baseurl>/gallery_servlet/<filePath>
   */

  // Constants for accessing split URI
  /*
   * Upload kind can be: "project", "file", or "userfile".
   * Constants for these are defined in ServerLayout.
   */

  // Constants used when upload kind is "file".
  // Since the file path may contain slashes, it must be the last component in the URI.
  private static final int GALLERY_ID_INDEX = 3;
  private static final int FILE_PATH_INDEX = 4;
  
  // Logging support
  private static final Logger LOG = Logger.getLogger(UploadServlet.class.getName());

  // Object used to safely set cache headers in responses
  private static final CacheHeaders CACHE_HEADERS = new CacheHeadersImpl();

  // Content type for response header (to avoid security vulnerabilities)
  private static final String CONTENT_TYPE = "text/html; charset=utf-8";
  

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    setDefaultHeader(resp);
    UploadResponse uploadResponse;
    
    String uri = req.getRequestURI();
    // First, call split with no limit parameter.
    String[] uriComponents = uri.split("/");
    
    LOG.info("############# AT LEAST I GOT IN doPost ############");
    
    if (true) {
      long galleryId = Long.parseLong(uriComponents[GALLERY_ID_INDEX]);
      String fileName = uriComponents[FILE_PATH_INDEX];
      LOG.info(String.valueOf(galleryId));
      LOG.info(fileName);
      InputStream uploadedStream;
      try {
        uploadedStream = getRequestStream(req, ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
        String readableStream = convertStreamToString(uploadedStream);
        LOG.info("################# TRYING UPLOAD STREAM ###############");
        LOG.info(readableStream);
        LOG.info("################# ENDING UPLOAD STREAM ###############");
        
//        storeImage(uploadedStream, galleryId);
        
      } catch (Exception e) {
        throw CrashReport.createAndLogError(LOG, req, null, e);
      }

    } else {
      throw CrashReport.createAndLogError(LOG, req, null,
          new IllegalArgumentException("Unknown upload kind: "));
    }

    // Now, get the PrintWriter for the servlet response and print the UploadResponse.
    // On the client side, in the onSubmitComplete method in ode/client/utils/Uploader.java, the
    // UploadResponse value will be retrieved as a String via the
    // FormSubmitCompleteEvent.getResults() method.
//    PrintWriter out = resp.getWriter();
//    out.print(uploadResponse.formatAsHtml());

    // Set http response information
    resp.setStatus(HttpServletResponse.SC_OK);
  }

  
  // Helper method for InputStream
  private static String convertStreamToString(InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
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
