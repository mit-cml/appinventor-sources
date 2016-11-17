// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.GalleryService;
import com.google.appinventor.shared.rpc.project.GallerySettings;

/**
 * Servlet for handling gallery's app publishing.
 *
 */
public class GalleryServlet extends OdeServlet {

  private static int BUFFER_SIZE = 1024 * 1024 * 10;
  private static int MAX_IMAGE_FILE_SIZE = 1024 * 1024 * 5;

  /*
   * URIs for upload requests are structured as follows:
   *    /<baseurl>/gallery_servlet/apps/galleryid/<filePath>
   *    /<baseurl>/gallery_servlet/user/userid/<filePath>
   */

  // Constants for accessing split URI
  /*
   * Upload kind can be: "project", "file", or "userfile".
   * Constants for these are defined in ServerLayout.
   */

  // Constants used when upload kind is "file".
  // Since the file path may contain slashes, it must be the last component in the URI.
  private static final int REQUEST_TYPE_INDEX = 3;
  private static final int GALLERY_OR_USER_ID_INDEX = 4;
  private static final int FILE_PATH_INDEX = 5;

  // Logging support
  private static final Logger LOG = Logger.getLogger(UploadServlet.class.getName());

  // Object used to safely set cache headers in responses
  private static final CacheHeaders CACHE_HEADERS = new CacheHeadersImpl();

  // Content type for response header (to avoid security vulnerabilities)
  private static final String CONTENT_TYPE = "text/html; charset=utf-8";

  private final GalleryService galleryService = new GalleryServiceImpl();
  private static final boolean DEBUG = Flag.createFlag("appinventor.debugging", false).get();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    setDefaultHeader(resp);
    UploadResponse uploadResponse;

    String uri = req.getRequestURI();
    // First, call split with no limit parameter.
    String[] uriComponents = uri.split("/");

    if (true) {
      String requestType = uriComponents[REQUEST_TYPE_INDEX];
      if (DEBUG) {
        LOG.info("######### GOT IN URI");
        LOG.info(requestType);
      }

      long project_Id = -1;
      String user_Id = "-1";
      if (requestType.equalsIgnoreCase("apps")) {
        project_Id = Long.parseLong(uriComponents[GALLERY_OR_USER_ID_INDEX]);
      } else if (requestType.equalsIgnoreCase("user")) {
        //the code below doesn't check if user_Id is the id of current user
        //user_Id = uriComponents[GALLERY_OR_USER_ID_INDEX];
        user_Id = userInfoProvider.getUserId();
      }
      InputStream uploadedStream;
      try {
        if(req.getContentLength() < MAX_IMAGE_FILE_SIZE){
          uploadedStream = getRequestStream(req, ServerLayout.UPLOAD_FILE_FORM_ELEMENT);

          // Converts the input stream to byte array
          byte[] buffer = new byte[8000];
          int bytesRead = 0;
          ByteArrayOutputStream bao = new ByteArrayOutputStream();
          while ((bytesRead = uploadedStream.read(buffer)) != -1) {
            bao.write(buffer, 0, bytesRead);
          }
          // Set up the cloud file (options)
          String key = "";
          GallerySettings settings = galleryService.loadGallerySettings();
          if (requestType.equalsIgnoreCase("apps")) {
            key = settings.getProjectImageKey(project_Id);
          } else if (requestType.equalsIgnoreCase("user")) {
            key =  settings.getUserImageKey(user_Id);
          }

          // setup cloud
          GcsService gcsService = GcsServiceFactory.createGcsService();
          GcsFilename filename = new GcsFilename(settings.getBucket(), key);
          GcsFileOptions options = new GcsFileOptions.Builder().mimeType("image/jpeg")
                  .acl("public-read").cacheControl("no-cache").build();
          GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
          writeChannel.write(ByteBuffer.wrap(bao.toByteArray()));

          // Now finalize
          writeChannel.close();

          uploadResponse = new UploadResponse(UploadResponse.Status.SUCCESS);
        }else{
          /*file exceeds size of MAX_IMAGE_FILE_SIZE*/
          uploadResponse = new UploadResponse(UploadResponse.Status.FILE_TOO_LARGE);
        }

        // Now, get the PrintWriter for the servlet response and print the UploadResponse.
        // On the client side, in the onSubmitComplete method in ode/client/utils/Uploader.java, the
        // UploadResponse value will be retrieved as a String via the
        // FormSubmitCompleteEvent.getResults() method.
        PrintWriter out = resp.getWriter();
        out.print(uploadResponse.formatAsHtml());

      } catch (Exception e) {
        throw CrashReport.createAndLogError(LOG, req, null, e);
      }
      // Set http response information
      resp.setStatus(HttpServletResponse.SC_OK);
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

  private InputStream getRequestStream(HttpServletRequest req, String expectedFieldName)
      throws Exception {
    ServletFileUpload upload = new ServletFileUpload();
    FileItemIterator iterator = upload.getItemIterator(req);
    while (iterator.hasNext()) {
      FileItemStream item = iterator.next();
//      LOG.info(item.getContentType());
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

  /**
   * Helper method for converting input stream
   * @param input
   * @param output
   * @throws IOException
   */
  private void copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead = input.read(buffer);
    while (bytesRead != -1) {
        output.write(buffer, 0, bytesRead);
        bytesRead = input.read(buffer);
    }
  }
}
