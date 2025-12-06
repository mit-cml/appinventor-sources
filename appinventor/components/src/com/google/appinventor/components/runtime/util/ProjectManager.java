// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;

import java.nio.charset.StandardCharsets;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;

import java.util.ArrayList;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.util.Log;

/**
 * ProjectManager: This module provides functions used to load
 *                 projects that have been "cached" on the local
 *                 device storage of the Companion app.
 *
 */

public class ProjectManager {

  private static Form form = ReplForm.getActiveForm();
  private static String projectId;
  private static String projectName;
  private static String preamble = "(begin (require <com.google.youngandroid.runtime>) (process-repl-input -2 (begin (clear-current-form) ";
  private static String preamble1 = "(begin (require <com.google.youngandroid.runtime>) (process-repl-input -2 (begin  ";
  private static String postamble = "(call-Initialize-of-components-library))))";
  private static final String LOG_TAG = ProjectManager.class.getSimpleName();

  private static String previousScreen = "";

  private ProjectManager() {    // All functions are static
  }

  public static void setProjectInfo(String projectId, String projectName) {
    ProjectManager.projectId = projectId;
    ProjectManager.projectName = projectName;
  }

  /* We open and parse the ZIP file each time, but this isn't a particularly
     inner loop call, so it should be fine.
  */

  public static void evalScreenYail(String screenName) {
    try {
      File projectsDir = form.getApplicationContext().getExternalFilesDir("assets/__projects__");
      File projectFile = new File(projectsDir, projectName);
      ZipFile zipFile = new ZipFile(projectFile);

      for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
        ZipEntry entry = entries.nextElement();
        String entryName = entry.getName();
        // We have to loop this way because we only know the last
        // part of the entry name, we don't know the user name part
        if (entryName.endsWith(screenName + ".yail")) {
          InputStream input = zipFile.getInputStream(entry);
          String yail = convertInputStreamToString(input);
          int index = yail.indexOf(";;; ");
          yail = yail.substring(index);
          if (screenName.equals("Screen1")) {
            yail = preamble1 + "(clear-current-form) (set-form-name \"" + screenName + "\") (clear-init-thunks) " + yail + postamble;
          } else {
            yail = preamble1 + "(clear-current-form) (set-form-name \"" + screenName + "\") (rename-component \"Screen1\" \"" + screenName + "\")  (clear-init-thunks) " + yail + postamble;
          }
          previousScreen = screenName;
          Log.d(LOG_TAG, "Yail: " + yail);
          ((ReplForm)form).evalScheme(yail); // ReplForm calls runOnUiThread
        }
      }
    } catch (IOException e) {
      Log.e(LOG_TAG, "IOException in evalScreenYail", e);
    }
  }

  public static void loadAssets() {
    try {
      ArrayList<String> extensions = new ArrayList();
      File projectsDir = form.getApplicationContext().getExternalFilesDir("assets/__projects__");
      File projectFile = new File(projectsDir, projectName);
      ZipFile zipFile = new ZipFile(projectFile);
      Form form = Form.getActiveForm();

      for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
        ZipEntry entry = entries.nextElement();
        String entryName = entry.getName();
        if (entryName.startsWith("asset")) {
          String assetName = entryName;
          Log.d(LOG_TAG, "loadAssets: assetName = " + assetName);
          byte[] buffer = new byte[10240];
          int length;
          ByteArrayOutputStream array = new ByteArrayOutputStream();
          InputStream inputStream = zipFile.getInputStream(entry);
          while ((length = inputStream.read(buffer)) != -1) {
            array.write(buffer, 0, length);
          }
          AssetFetcher.UpdateLibraryAsset(assetName, array.toByteArray());
          // Lets check to see if this asset identifies and extension
          if (assetName.endsWith("extension.properties")) {
            Log.d(LOG_TAG, "Found Extension: " + assetName);
            String extensionName = getSegmentBySlash(assetName, -2);
            Log.d(LOG_TAG, "Found Extension (truncated): " + extensionName);
            if (extensionName != null) {
              Log.d(LOG_TAG, "Adding extension: " + extensionName);
              extensions.add(extensionName);
            }
          }
        }
      }
      if (!extensions.isEmpty()) {
        ((ReplForm)form).loadComponents(extensions);
      }
    } catch (Exception e) {
      Log.e(LOG_TAG, "Exception in loadAssets", e);
    }
  }

  public static void deleteProject(String projectName) {
    File projectsDir = form.getApplicationContext().getExternalFilesDir("assets/__projects__");
    File projectFile = new File(projectsDir, projectName);
    projectFile.delete();
  }

  private static String convertInputStreamToString(InputStream inputStream) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024]; // Use a reasonably sized buffer
    int length;
    // Read chunks of data until the end of the stream (-1) is reached
    while ((length = inputStream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    // Convert the output stream's contents to a String using a character set
    return result.toString(StandardCharsets.UTF_8.name());
  }

  public static String getSegmentBySlash(String input, int index) {
    if (input == null || input.isEmpty()) {
      return null;
    }

    String[] parts = input.split("/");

    if (index < 0) {
      index = parts.length + index;
    }

    if (index >= 0 && index < parts.length) {
      return parts[index];
    } else {
      return null;
    }
  }

}

