// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;
import android.content.Intent;

import android.net.Uri;

import android.os.Environment;

import android.util.Log;

import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * AssetFetcher: This module is used by the MIT AI2 Companion to fetch
 *               assets directly from the App Inventor Server.  Prior
 *               to the use of this module, the App Inventor client
 *               would fetch assets from the server and then send them
 *               to the MIT AI2 Companion. Instead we now use this
 *               module.  It is passed a list of assets to fetch
 *               (which includes extension components). We are also
 *               handed the authentication cookie for the user. We
 *               then fetch the assets from the server and place them
 *               in the appropriate directory in external
 *               storage. Finally when finished we signal the
 *               Companion that we have all of the needed assets
 *
 *               This code is part of the implementation of webRTC
 *               communication between the Companion and the App
 *               Inventor client.
 */

public class AssetFetcher {

  private static final String LOG_TAG = AssetFetcher.class.getSimpleName();

  private static final String REPL_ASSET_DIR =
    Environment.getExternalStorageDirectory().getAbsolutePath() +
    "/AppInventor/";

  // We use a single threaded executor so we only load one asset at a time!
  private static ExecutorService background = Executors.newSingleThreadExecutor();

  private static volatile boolean inError = false; // true means we are displaying the "End Application" Error dialog already
  private static final Object semaphore = new Object();

  /* We are only used statically */
  private AssetFetcher() {
  }

  public static void fetchAssets(final String cookieValue,
    final String projectId, final String uri, final String asset) {
    background.submit(new Runnable() {
        @Override
        public void run() {
          String fileName = uri + "/ode/download/file/" + projectId + "/" + asset;
          if (getFile(fileName, cookieValue, asset, 0) != null) {
            RetValManager.assetTransferred(asset);
          }
        }
      });
  }

  public static void upgradeCompanion(final String cookieValue, final String inputUri) {
    // The code below is commented out because of issues with the Google Play Store
    //
    // background.submit(new Runnable() {
    //     @Override
    //     public void run() {
    //       String [] parts = inputUri.split("/", 0);
    //       String asset = parts[parts.length-1];
    //       File assetFile = getFile(inputUri, cookieValue, asset, 0);
    //       if (assetFile != null) {
    //         try {
    //           Form form = Form.getActiveForm();
    //           Intent intent = new Intent(Intent.ACTION_VIEW);
    //           Uri packageuri = NougatUtil.getPackageUri(form, assetFile);
    //           intent.setDataAndType(packageuri, "application/vnd.android.package-archive");
    //           intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    //           form.startActivity(intent);
    //         } catch (Exception e) {
    //           Log.e(LOG_TAG, "ERROR_UNABLE_TO_GET", e);
    //           RetValManager.sendError("Unable to Install new Companion Package.");
    //         }
    //       }
    //     }
    //   });
    return;
  }

  public static void loadExtensions(String jsonString) {
    Log.d(LOG_TAG, "loadExtensions called jsonString = " + jsonString);
    try {
      ReplForm form = (ReplForm) Form.getActiveForm();
      JSONArray array = new JSONArray(jsonString);
      List<String> extensionsToLoad = new ArrayList<String>();
      if (array.length() == 0) { // No extensions
        Log.d(LOG_TAG, "loadExtensions: No Extensions");
        RetValManager.extensionsLoaded(); // This kicks things going
        return;
      }
      for (int i = 0; i < array.length(); i++) {
        String extensionName = array.optString(i);
        if (extensionName != null) {
          Log.d(LOG_TAG, "loadExtensions, extensionName = " + extensionName);
          extensionsToLoad.add(extensionName);
        } else {
          Log.e(LOG_TAG, "extensionName was null");
          return;
        }
      }
      try {
        form.loadComponents(extensionsToLoad);
        RetValManager.extensionsLoaded();
      } catch (Exception e) {
        Log.e(LOG_TAG, "Error in form.loadComponents", e);
      }
    } catch (JSONException e) {
      Log.e(LOG_TAG, "JSON Exception parsing extension string", e);
    }
  }

  private static File getFile(final String fileName, String cookieValue, String asset, int depth) {
    if (depth > 1) {
      synchronized (semaphore) { // We are protecting the inError variable
        if (inError) {
          return null;
        } else {
          inError = true;
          Form form = Form.getActiveForm();
          form.runOnUiThread(new Runnable() {
              public void run() {
                RuntimeErrorAlert.alert(Form.getActiveForm(), "Unable to load file: " + fileName,
                  "Error!", "End Application");
              }
            });
          return null;
        }
      }
    }
    try {
      boolean error = false;
      File outFile = null;
      URL url = new URL(fileName);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      if (connection != null) {
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Cookie",  "AppInventor = " + cookieValue);
        int responseCode = connection.getResponseCode();
        Log.d(LOG_TAG, "asset = " + asset + " responseCode = " + responseCode);
        outFile = new File(REPL_ASSET_DIR + asset);
        File parentOutFile = outFile.getParentFile();
        if (!parentOutFile.exists()) {
          parentOutFile.mkdirs();
        }
        BufferedInputStream in = new BufferedInputStream(connection.getInputStream(), 0x1000);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), 0x1000);
        try {
          while (true) {
            int b = in.read();
            if (b == -1) {
              break;
            }
            out.write(b);
          }
          out.flush();
        } catch (IOException e) {
          Log.e(LOG_TAG, "copying assets", e);
          error = true;
        } finally {
          out.close();
        }
        connection.disconnect();
      } else {
        error = true;           // Connection was null, failed to open?
      }
      if (error) {              // Try again recursively
        return getFile(fileName, cookieValue, asset, depth + 1);
      }
      return outFile;
    } catch (Exception e) {
      Log.e(LOG_TAG, "Exception while fetching " + fileName);
      // Try again recursively
      return getFile(fileName, cookieValue, asset, depth + 1);
    }
  }
}
