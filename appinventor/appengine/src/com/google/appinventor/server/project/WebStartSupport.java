// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project;

import com.google.common.collect.Lists;
import com.google.appinventor.server.Server;
import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.project.utils.JavaWebStart;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The base class for supporting JavaWebStart enabled programs.
 *
 */
public abstract class WebStartSupport {
  protected final StorageIo storageIo;
  private final UserInfoProvider userProvider;

  protected WebStartSupport(StorageIo storageIo, UserInfoProvider userProvider) {
    this.storageIo = storageIo;
    this.userProvider = userProvider;
  }

  /**
   * Sends a JNLP file.
   *
   * @param req  HTTP request initiating the launch
   * @param resp  HTTP response
   * @param purpose  the purpose of using JavaWebStart, implementation dependent
   */
  public abstract void sendJnlpFile(HttpServletRequest req, HttpServletResponse resp,
      String purpose);

  /**
   * Handles a GET request.
   *
   * @param req  HTTP request
   * @param resp  HTTP response
   * @param userId  user ID
   * @param projectId  project ID
   * @param fileName  name of requested file
   */
  public abstract void doGet(HttpServletRequest req, HttpServletResponse resp, String userId,
      long projectId, String fileName);

  /**
   * Handles a POST request.
   *
   * @param req  HTTP request
   * @param resp  HTTP response
   * @param userId  user ID
   * @param projectId  project ID
   * @param fileName  name of posted file
   */
  public abstract void doPost(HttpServletRequest req, HttpServletResponse resp, String userId,
      long projectId, String fileName);


  protected StorageIo getStorageIo() {
    return storageIo;
  }

  protected UserInfoProvider getUserProvider() {
    return userProvider;
  }

  /**
   * Prepares a JNLP file for launching a JSONP server, such as Codeblocks.
   *
   * @param req  HTTP request for Web Start
   * @param javaVersion  the required Java version
   * @param maxHeapSize  max-heap-size value
   * @param jar  the deploy jar file for the application
   * @param mainClass  main class of the application
   * @param title  title
   * @param description  description
   * @return  generated JNLP file
   */
  protected String prepareJnlpForJsonpServer(HttpServletRequest req,
      String javaVersion, String maxHeapSize, String jar, String mainClass, String title,
      String description) throws EncryptionException {

    String baseUrl = JavaWebStart.getWebStartBaseUrl(req);
    // for the jnlp file, the web start path doesn't need a valid project id
    String projectPath = JavaWebStart.getWebStartProjectPath(userProvider, 0);
//    String codebase = baseUrl + projectPath;
    String codebase = baseUrl;  // Don't include the projectPath, it is too long.
                                // We have modified the server to not require it
                                // for downloading the BlocksEditor jar file.

    List<String> args = Lists.newArrayList();
    args.add(baseUrl);     // base URL for the WebStartFileServlet
    args.add(projectPath + '/');

    return JavaWebStart.generateJnlpFile(codebase, title, description,
        javaVersion, maxHeapSize, mainClass, Collections.singletonList(jar), args);
  }

  /**
   * Sends a file as a response to an HTTP request
   *
   * @param resp  HTTP request
   * @param fileName  name of file to send
   * @param contentType  content type of file data
   * @param data  file data
   * @throws IOException
   */
  public static void sendFileResponse(HttpServletResponse resp, String fileName, String contentType,
      byte[] data) throws IOException {
    // Set http response information
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setHeader("content-disposition", "attachment; filename=\"" + fileName + "\"");
    resp.setContentType(contentType);
    resp.setContentLength(data.length);

    // Attach file data
    OutputStream out = resp.getOutputStream();
    out.write(data);
    out.close();
  }
}
