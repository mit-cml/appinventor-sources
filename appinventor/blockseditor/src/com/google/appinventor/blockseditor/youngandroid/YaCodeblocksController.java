// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.blockseditor.youngandroid;

import com.google.appinventor.blockseditor.jsonp.Util;

import openblocks.yacodeblocks.ExternalController;
import openblocks.yacodeblocks.FeedbackReporter;
import openblocks.yacodeblocks.NoProjectException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle callbacks from codeblocks
 */
// @VisibleForTesting
public final class YaCodeblocksController implements ExternalController {

  private final YaHttpServerMain.ServerConnection conn;
  private final String baseUrl;
  private String formPropertiesPath;

  public YaCodeblocksController(YaHttpServerMain.ServerConnection conn, String baseUrl) {
    this.conn = conn;
    this.baseUrl = baseUrl;
    this.formPropertiesPath = "";
  }

  @Override
  public void writeCodeblocksSourceToServer(String path, String contents) throws IOException {
    if (path == null) {
      throw new IOException("Can't save codeblocks source since we don't know " +
          "the path name yet!");
    }
    System.out.println("Trying to save codeblocks source");
    if (writeContentsToServer(path, contents, "codeblocks source")) {
      System.out.println("Successfully saved codeblocks source");
    } else {
      throw new IOException("Couldn't save blocks source");
    }
  }

  // @VisibleForTesting
  boolean writeContentsToServer(String path, String contents, String what) {
    try {
      HttpURLConnection httpUrlConn = conn.getConnection(baseUrl + path);
      httpUrlConn.setDoOutput(true);
      httpUrlConn.setRequestMethod("POST");
      httpUrlConn.addRequestProperty("Content-Type", "text/plain; charset=utf-8");
      httpUrlConn.connect();
      OutputStreamWriter writer = new OutputStreamWriter(httpUrlConn.getOutputStream(), "UTF-8");
      writer.write(contents);
      writer.close();
      int responseCode = httpUrlConn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        System.out.println("Saved " + what);
        return true;
      } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND ||
          responseCode == HttpURLConnection.HTTP_BAD_GATEWAY ||
          responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        FeedbackReporter.showErrorMessageWithExit(
            "The Blocks Editor is having trouble communicating with the server (error="
            + responseCode + ")\n" +
            "If this happens repeatedly, try exiting the Blocks Editor, refreshing\n" +
            "the Designer web page, and then reopening the Blocks Editor.");
      } else {
        FeedbackReporter.showSystemErrorMessage("Unexpected response code: "
            + httpUrlConn.getResponseCode()
            + " error stream: "
            + inputStreamToString(httpUrlConn.getErrorStream(), httpUrlConn.getContentEncoding()));
      }
    } catch (MalformedURLException e) {
      FeedbackReporter.showSystemErrorMessage("Can't save " + what + " for path "
          + path + ": unexpected URL type");
       e.printStackTrace();
    } catch (ProtocolException e) {
      FeedbackReporter.showSystemErrorMessage("Got ProtocolException trying to save to " + path);
       e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Returns the content of the given {@link InputStream} as a {@link String}.
   * @param inputStream the InputStream to get the contents of.
   * @param encoding the encoding for the content or null for "UTF-8"
   */
  private static String inputStreamToString(InputStream inputStream, String encoding)
      throws IOException {
    StringBuilder sb = new StringBuilder();
    if (inputStream != null) {
      if (encoding == null) {
        encoding = "UTF-8";
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
      while (true) {
        String line = reader.readLine();
        if (line != null) {
          sb.append(line).append('\n');
        } else {
          break;
        }
      }
    }
    return sb.toString();
  }

  /*
   * Retrieve the contents named by {@code path} from the server. Throws
   * {@link IOException} if an error occurred.
   */
  String getContentsFromServer(String path) throws IOException {
    URLConnection urlConnection = conn.getConnection(baseUrl + path);
    return inputStreamToString(urlConnection.getInputStream(), urlConnection.getContentEncoding());
  }

  /*
   * Download a zip file from the server, unzip it into local files
   * and return a list of pairs of names <zip file name, local name>.
   */
  Map<String, String> downloadZipFromServer(String assetsPath) throws IOException {
    HttpURLConnection httpUrlConn = conn.getConnection(baseUrl + assetsPath);
    int response = httpUrlConn.getResponseCode();
    if (response == HttpURLConnection.HTTP_NO_CONTENT) {
      System.out.println("No zip file from server. Returning empty file map.");
      return new HashMap<String, String>();
    } else if (response != HttpURLConnection.HTTP_OK) {
      throw new IOException("HTTP request for URL " + baseUrl + assetsPath +
          " returned " + response);
    }
    return Util.downloadZipFile(httpUrlConn);
  }

  /**
   * Copy the contents named by "path" from the server and put it into a temp file.
   *
   * @param path the path of the contents to retrieve.
   * @throws java.io.IOException if an error occurs.
   */
  public String downloadContentFromServer(String path) throws IOException {
    String fileName = parseBaseName(path);
    return Util.downloadFileToGivenNameElseExtension(conn.getConnection(baseUrl + path),
        fileName);
  }

  private String parseBaseName(String path) {
    final File file = new File(path);
    return file.getName();
  }

  void setFormPropertiesPath(String formPropertiesPath) {
    this.formPropertiesPath = formPropertiesPath;
  }

  @Override
  public String getFormPropertiesForProject() throws IOException, NoProjectException {
    if (formPropertiesPath.length() == 0) {
      throw new NoProjectException("The Blocks Editor does not have any project information.");
    }
    return getContentsFromServer(formPropertiesPath);
  }

}
