// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.communityhelp.SubmitPostService;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.json.JSONObject;
/**
 * Implementation of the user information service.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */
public class SubmitPostServiceImpl extends RemoteServiceServlet implements SubmitPostService {
  private final static String BASE_URL = "http://localhost:4200";
  private final static String API_KEY = "958543d9f5d4df2c2739b8f9156cbd18759d192d435fb4d2c08e5f61fdf3b699";
  private final FileExporter fileExporter = new FileExporterImpl();
  private static final String LINE_FEED = "\r\n";
  private static final String charset = "UTF-8";

  @Override
  public String submitPost(String userId, String username, String title, String description, int categoryId, boolean attachProject, String projectId) {
    if (!attachProject) {
      // Submit the content
      return submitContent(username, title, description, categoryId);
    } else {
      try {
        // Upload aia file
        String uploadResponse = uploadFile(userId, username, title, description, categoryId, attachProject, projectId);
        JSONObject jsonObject = new JSONObject(uploadResponse);

        String urlString = jsonObject.getString("short_path");
        String nameString = jsonObject.getString("original_filename");
        String fileSizeString = jsonObject.getString("human_filesize");

        if(urlString!=null && nameString != null && fileSizeString != null) {
          // Append the content with aia file link
          String newDescription = appendContent(description, urlString, nameString, fileSizeString);

          // Submit the content
          return submitContent(username, title, newDescription, categoryId);

        } else {
          return error();
        }

      } catch (Exception e) {
        e.printStackTrace();
        return error();
      }
    }
  }

  /*
   * Returns a generic error message in json format
   */
  private String error() {
    return "{\"errors\": [\"Something went wrong. Please try again after sometime\"]}";
  }

  public String appendContent(String descriprion, String urlString, String nameString, String fileSize) {
    StringBuilder sb = new StringBuilder();
    sb.append(descriprion.trim());
    sb.append("\n\n");
    sb.append("[");
    sb.append(nameString);
    sb.append("|attachment");
    sb.append("]");
    sb.append("(");
    sb.append(urlString);
    sb.append(")");
    sb.append(" (");
    sb.append(fileSize);
    sb.append(")");
    // example: "[helloworld.aia|attachment](uploads/default/original/1X/6abb79d76e08b0e1b05bd8cfd95dda3250c88f0e.aia) (1.36 kb)";
    return sb.toString();
  }

  public String submitContent(String username, String title, String description, int categoryId) {
    try {
      URL url = new URL(BASE_URL + "/posts.json");
      HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
      httpConn.setDoOutput(true);
      httpConn.setRequestMethod("POST");
      httpConn.setRequestProperty("api-key", API_KEY);
      httpConn.setRequestProperty("api-username", username);
      httpConn.setRequestProperty("content-type", "application/json");
      OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
      String json = "{"
        + "\"title\": \"" + title + "\","
        + "\"raw\": \"" + description + "\","
        + "\"category\": " + categoryId
      + "}";
      writer.write(json);
      writer.flush();
      writer.close();
      httpConn.getOutputStream().close();
  
      InputStream responseStream = httpConn.getResponseCode() == 200
          ? httpConn.getInputStream()
          : httpConn.getErrorStream();
      Scanner s = new Scanner(responseStream).useDelimiter("\\A");
      String response = s.hasNext() ? s.next() : "";
      return response;
    } catch(Exception e) {
      e.printStackTrace();
      return "" + e.getMessage();
    }
  }

  public String uploadFileToServer(String userId, String username, String title, String description, int categoryId, boolean attachProject, String projectId) {
    try {
      // Download project source files as a zip.
      final boolean includeProjectHistory = true;
      String zipName = null;
      // If the requester is an Admin, we include any Yail files in the
      // project in the export
      boolean includeYail = false;
      boolean includeScreenShots = includeYail;
      StorageIoInstanceHolder.getInstance().assertUserHasProject(userId, Long.parseLong(projectId));
      ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userId,
        Long.parseLong(projectId), includeProjectHistory, false, zipName, includeYail,
        includeScreenShots, false, false);
      RawFile downloadableFile = zipFile.getRawFile();
      String fileName = downloadableFile.getFileName();
      byte[] content = downloadableFile.getContent();
      URL url = new URL(BASE_URL + "/uploads.json");
      HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
      httpConn.setDoOutput(true);
      httpConn.setRequestMethod("POST");
      httpConn.setRequestProperty("api-key", API_KEY);
      httpConn.setRequestProperty("api-username", username);
      String boundary = UUID.randomUUID().toString();
      httpConn.setRequestProperty("content-type", "multipart/form-data; boundary="+boundary);
      OutputStream outputStream = httpConn.getOutputStream();
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));
      addFormField("type", "aia", writer, boundary);
      addFormField("synchronous", "true", writer, boundary);
      addFilePart("file", content, fileName, writer, outputStream, boundary);
      String response = finish(writer, httpConn, boundary);
      return response;
    } catch(Exception e) {
      return e.getMessage();
    }
  }

  @Override
  public String uploadFile(String userId, String username, String title, String description, int categoryId, boolean attachProject, String projectId) {
    try {
      // Download project source files as a zip.
      final boolean includeProjectHistory = true;
      String zipName = null;
      // If the requester is an Admin, we include any Yail files in the
      // project in the export
      boolean includeYail = false;
      boolean includeScreenShots = includeYail;
      StorageIoInstanceHolder.getInstance().assertUserHasProject(userId, Long.parseLong(projectId));
      ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userId,
        Long.parseLong(projectId), includeProjectHistory, false, zipName, includeYail,
        includeScreenShots, false, false);
      RawFile downloadableFile = zipFile.getRawFile();
      String fileName = downloadableFile.getFileName();
      byte[] content = downloadableFile.getContent();
      URL url = new URL(BASE_URL + "/uploads.json");
      HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
      httpConn.setDoOutput(true);
      httpConn.setRequestMethod("POST");
      httpConn.setRequestProperty("api-key", API_KEY);
      httpConn.setRequestProperty("api-username", username);
      String boundary = UUID.randomUUID().toString();
      httpConn.setRequestProperty("content-type", "multipart/form-data; boundary="+boundary);
      OutputStream outputStream = httpConn.getOutputStream();
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));
      addFormField("type", "aia", writer, boundary);
      addFormField("synchronous", "true", writer, boundary);
      addFilePart("file", content, fileName, writer, outputStream, boundary);
      String response = finish(writer, httpConn, boundary);
      return response;
    } catch(Exception e) {
      return e.getMessage();
    }
    
  }

  public void addFormField(String name, String value, PrintWriter writer, String boundary) {
      writer.append("--" + boundary).append(LINE_FEED);
      writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
      .append(LINE_FEED);
      writer.append("Content-Type: text/plain; charset=" + charset).append(
      LINE_FEED);
      writer.append("Content-Length: " + value.length()).append(
      LINE_FEED);
      writer.append(LINE_FEED);
      writer.append(value).append(LINE_FEED);
      writer.flush();
  }

  public void addFilePart(String fieldName, byte[] file, String name, PrintWriter writer, OutputStream outputStream, String boundary)
    throws IOException {
    writer.append("--" + boundary).append(LINE_FEED);
    writer.append(
    "Content-Disposition: form-data; name=\"" + fieldName
    + "\"; filename=\"" + name + "\"")
    .append(LINE_FEED);
    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
    writer.append("Content-Length: " + file.length).append(LINE_FEED);
    writer.append(LINE_FEED);
    writer.flush();
    
    outputStream.write(file, 0, file.length);

    outputStream.flush();
    writer.append(LINE_FEED);
    writer.flush();
  }

  public String finish(PrintWriter writer, HttpURLConnection httpConn, String boundary) throws IOException {
    String line = "";
    writer.append(LINE_FEED).flush();
    writer.append("--" + boundary + "--").append(LINE_FEED);
    writer.close();

    // checks server's status code first
    int status = httpConn.getResponseCode();
    if (status == HttpURLConnection.HTTP_OK) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpConn.getInputStream()));
        String temp = "";
        while ((temp = reader.readLine()) != null) {
          line += temp;
        }
        reader.close();
        httpConn.disconnect();
    } else {
        throw new IOException("Community server returned non-OK status: " + status);
    }

    return line;
  }

}
