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
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
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
  private final GcsService gcsService =  
      GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());



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
//        String readableStream = convertStreamToString(uploadedStream);
        LOG.info("################# TRYING UPLOAD STREAM ###############");
//        LOG.info(uploadedStream);
        LOG.info("################# ENDING UPLOAD STREAM ###############");
        
        byte[] buffer = new byte[8000];
        int bytesRead = 0;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        
        while ((bytesRead = uploadedStream.read(buffer)) != -1) {
          bao.write(buffer, 0, bytesRead); 
          LOG.info(String.valueOf(bytesRead));
        }
        LOG.info("################# BAO STREAM ###############");
        LOG.info(String.valueOf(bao.toByteArray().length));
        
        
        
        // set up the cloud file (options)
        String key = galleryId + "/image.png";
        FileService fileService = FileServiceFactory.getFileService();
        
        GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
        .setBucket("galleryai2")
        .setKey(key)
        .setAcl("public-read")
        // what should the mime type be?
        .setMimeType("text/html");
    
        AppEngineFile writableFile = fileService.createNewGSFile(optionsBuilder.build());
        // Open a channel to write to it
        boolean lock = true;
        FileWriteChannel writeChannel =
            fileService.openWriteChannel(writableFile, lock);
        
        /* GCS alternative way of uploading
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
        
        writeChannel.write(ByteBuffer.wrap(bao.toByteArray()));
        bao.flush();
        
        // Now finalize
        writeChannel.closeFinally();
        
        
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
//      FileItem i = (FileItem) iterator.next();
//      LOG.log(Level.INFO, String.valueOf(i.getSize()));
      FileItemStream item = iterator.next();
      LOG.log(Level.INFO, "GET IN 1ST *************");
      LOG.info(item.getContentType());
      if (item.getFieldName().equals(expectedFieldName)) {
          LOG.log(Level.INFO, "GET IN 2ND *************" + item.getFieldName());
//        LOG.log(Level.INFO, String.valueOf(item.g));
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
  
  
  private void copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead = input.read(buffer);
    while (bytesRead != -1) {
        output.write(buffer, 0, bytesRead);
        bytesRead = input.read(buffer);
    }
}
}
