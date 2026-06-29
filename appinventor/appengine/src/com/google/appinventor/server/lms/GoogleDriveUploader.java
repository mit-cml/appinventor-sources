// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

/**
 * Uploads a file to a user's Google Drive through the Drive REST API, matching
 * the prototype's {@link HttpURLConnection} pattern and avoiding a new
 * client-library dependency. Used by the Google Classroom integration to place a
 * student's exported project into their Drive.
 *
 * <p>A resumable upload is used rather than a multipart upload because an
 * App Inventor project with media assets routinely exceeds the five megabyte
 * ceiling of a multipart request. A resumable upload carries no such limit: a
 * session is opened with the file metadata, then the bytes are sent in a single
 * follow-up request to the session URI.
 *
 * <p>The caller supplies an access token granted the {@code drive.file} scope, so
 * this class has no dependency on configuration or storage.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public final class GoogleDriveUploader {

  /**
   * Endpoint that opens a resumable upload session. The {@code fields} query
   * parameter is carried through to the final response, so the completed upload
   * returns the new file's id and its browser link.
   */
  private static final String INITIATE_ENDPOINT =
      "https://www.googleapis.com/upload/drive/v3/files"
          + "?uploadType=resumable&fields=id,webViewLink";

  private static final String OCTET_STREAM = "application/octet-stream";

  private GoogleDriveUploader() {}

  /**
   * Uploads {@code content} to the authenticated user's Drive and returns a
   * browser link to the new file.
   *
   * @param accessToken an OAuth access token with the drive.file scope
   * @param fileName the name to give the file in Drive
   * @param content the raw file bytes
   * @return the {@code webViewLink} for opening the new file in a browser
   * @throws IOException if the upload fails or no link is returned
   */
  public static String uploadFile(String accessToken, String fileName, byte[] content)
      throws IOException {
    String sessionUri = openSession(accessToken, fileName, content.length);
    return uploadContent(sessionUri, accessToken, content);
  }

  /**
   * Opens a resumable upload session for a file of the given name and size, and
   * returns the session URI taken from the response {@code Location} header.
   */
  private static String openSession(String accessToken, String fileName, int contentLength)
      throws IOException {
    HttpURLConnection conn = LmsHttp.open(INITIATE_ENDPOINT);
    try {
      LmsHttp.applyTimeouts(conn);
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setRequestProperty("X-Upload-Content-Type", OCTET_STREAM);
      conn.setRequestProperty("X-Upload-Content-Length", Integer.toString(contentLength));
      // One-shot call: do not reuse a pooled keep-alive socket, which can surface
      // as an "Unexpected end of file from server" once the socket has gone stale.
      conn.setRequestProperty("Connection", "close");
      byte[] metadata =
          ("{\"name\":" + JSONObject.quote(fileName) + "}").getBytes(StandardCharsets.UTF_8);
      conn.setFixedLengthStreamingMode(metadata.length);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(metadata);
      }
      int status = conn.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        throw new IOException(
            "Drive upload could not start (HTTP " + status + "): " + LmsHttp.errorBody(conn));
      }
      String sessionUri = conn.getHeaderField("Location");
      if (sessionUri == null) {
        throw new IOException("Drive resumable session init returned no session URI");
      }
      return sessionUri;
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Uploads the file bytes to the resumable session URI and returns the new
   * file's browser link from the final response.
   */
  private static String uploadContent(String sessionUri, String accessToken, byte[] content)
      throws IOException {
    HttpURLConnection conn = LmsHttp.open(sessionUri);
    try {
      LmsHttp.applyTimeouts(conn);
      conn.setRequestMethod("PUT");
      conn.setDoOutput(true);
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);
      conn.setRequestProperty("Content-Type", OCTET_STREAM);
      // One-shot call: do not reuse a pooled keep-alive socket (see openSession).
      conn.setRequestProperty("Connection", "close");
      conn.setFixedLengthStreamingMode(content.length);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(content);
      }
      String json = LmsHttp.readBody(conn, "Drive upload");
      String webViewLink = LmsHttp.jsonField(json, "webViewLink");
      if (webViewLink == null) {
        throw new IOException("Drive upload returned no file link");
      }
      return webViewLink;
    } finally {
      conn.disconnect();
    }
  }
}
