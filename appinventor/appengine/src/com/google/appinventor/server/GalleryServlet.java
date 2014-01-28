// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;
import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.GalleryService;
import com.google.appinventor.shared.rpc.project.UserProject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling gallery's app publishing.
 *
 */
public class GalleryServlet extends OdeServlet {
  
  private static int BUFFER_SIZE = 1024 * 1024 * 10;

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
  

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    setDefaultHeader(resp);
    UploadResponse uploadResponse;
    
    String uri = req.getRequestURI();
    // First, call split with no limit parameter.
    String[] uriComponents = uri.split("/");
        
    if (true) {
      String requestType = uriComponents[REQUEST_TYPE_INDEX];
      LOG.info("######### GOT IN URI");
      LOG.info(requestType);

      long project_Id = -1;
      String user_Id = "-1";
      if (requestType.equalsIgnoreCase("apps")) {
        project_Id = Long.parseLong(uriComponents[GALLERY_OR_USER_ID_INDEX]);
      } else if (requestType.equalsIgnoreCase("user")) {
        user_Id = uriComponents[GALLERY_OR_USER_ID_INDEX];
      }
//      String fileName = uriComponents[FILE_PATH_INDEX];
      InputStream uploadedStream;
      try {
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
        if (requestType.equalsIgnoreCase("apps")) {
          key = "gallery/projects/" + project_Id + "/image";
          LOG.info("######## THIS IS A GALLERY REQUEST");
        } else if (requestType.equalsIgnoreCase("user")) {
          key =  "user/" + user_Id + "/image";
          LOG.info("######## THIS IS A USER REQUEST");
        }
        
        FileService fileService = FileServiceFactory.getFileService();
        
        GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
        .setBucket("galleryai2")
        .setKey(key)
        .setAcl("public-read")
        .setMimeType("image/jpeg")
        .setCacheControl("no-cache");
        AppEngineFile writableFile = fileService.createNewGSFile(optionsBuilder.build());
        
        // Open a channel to write to it
        boolean lock = true;
        FileWriteChannel writeChannel =
            fileService.openWriteChannel(writableFile, lock);
        writeChannel.write(ByteBuffer.wrap(bao.toByteArray()));

        /* GCS alternative way of uploading (create or replace)
        private final GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        GcsFileOptions options = new GcsFileOptions.Builder()
        .acl("public_read")
        .build();
        GcsFilename filename = new GcsFilename("galleryai2", key);
        GcsOutputChannel outputChannel = 
            gcsService.createOrReplace(filename, options);                 

        // Copying InputStream to GcsOutputChannel
        try {
            copy(uploadedStream, Channels.newOutputStream(outputChannel));
        } finally {
            outputChannel.close();
            uploadedStream.close();
        }    
        */
                       
        // Now finalize
        bao.flush();
        writeChannel.closeFinally();

        uploadResponse = new UploadResponse(UploadResponse.Status.SUCCESS);
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
