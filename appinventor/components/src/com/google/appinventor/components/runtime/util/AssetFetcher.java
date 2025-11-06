// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;

import android.os.Build;
import android.util.Log;

import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
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

  private static Context context = ReplForm.getActiveForm();
  private static HashDatabase db = new HashDatabase(context);

  private static final String LOG_TAG = AssetFetcher.class.getSimpleName();

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

    background.submit(new Runnable() {
        @Override
        public void run() {
          String [] parts = inputUri.split("/", 0);
          String asset = parts[parts.length-1];
          File assetFile = getFile(inputUri, cookieValue, asset, 0);
          if (assetFile != null) {
            try {
              Form form = Form.getActiveForm();
              Intent intent = new Intent(Intent.ACTION_VIEW);
              Uri packageuri = NougatUtil.getPackageUri(form, assetFile);
              intent.setDataAndType(packageuri, "application/vnd.android.package-archive");
              intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
              form.startActivity(intent);
            } catch (Exception e) {
              Log.e(LOG_TAG, "ERROR_UNABLE_TO_GET", e);
              RetValManager.sendError("Unable to Install new Companion Package.");
            }
          }
        }
      });
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
        RetValManager.sendError("Unable to load extensions." + e);
      }
    } catch (JSONException e) {
      Log.e(LOG_TAG, "JSON Exception parsing extension string", e);
    }
  }

  private static File getFile(final String fileName, String cookieValue, final String asset,
      int depth) {
    Form form = Form.getActiveForm();
    if (depth > 1) {
      synchronized (semaphore) { // We are protecting the inError variable
        if (!inError) {
          inError = true;
          form.runOnUiThread(new Runnable() {
            public void run() {
              RuntimeErrorAlert.alert(Form.getActiveForm(),
                  "Unable to load file: " + fileName,
                  "Error!", "End Application");
            }
          });
        }
        return null;
      }
    }

    String destinationFilename = asset;
    File outFile = getDestinationFile(form, asset);
    if (asset.endsWith("/classes.jar")) {
      destinationFilename = asset.substring(0, asset.lastIndexOf("/") + 1) + outFile.getName();
    }
    Log.d(LOG_TAG, "target file = " + outFile);

    // Starting with Android Upside Down Cake (SDK 34), we need to make the files that may be
    // dynamically loaded be read only.
    final boolean makeReadonly = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        && asset.contains("/external_comps/") && asset.endsWith("/classes.jar");

    HttpURLConnection connection = null;
    int responseCode = 0;
    String fileHash = null;
    boolean error = false;

    try {
      URL url = new URL(fileName);
      connection = (HttpURLConnection) url.openConnection();
      if (connection != null) {
        connection.addRequestProperty("Cookie",  "AppInventor = " + cookieValue);
        HashFile hashFile = db.getHashFile(destinationFilename);
        if (hashFile != null && outFile.exists()) {
          connection.addRequestProperty("If-None-Match", hashFile.getHash()); // get old_hash from database
        }
        connection.setRequestMethod("GET");
        responseCode = connection.getResponseCode();
        Log.d(LOG_TAG, "asset = " + asset + " responseCode = " + responseCode);
        File parentOutFile = outFile.getParentFile();
        fileHash = connection.getHeaderField("ETag"); // only save when status code is 200

        if (responseCode == 304) { // We already have the file stored
          return outFile;
        }

        if (parentOutFile == null || (!parentOutFile.exists() && !parentOutFile.mkdirs())) {
          throw new IOException("Unable to create assets directory " + parentOutFile);
        }

        // Before we attempt to write to the outFile, make sure that we can. If it is a classes.jar
        // file, we may already have a read-only version stored.
        if (!outFile.canWrite()) {
          outFile.setWritable(true);
        }
        BufferedInputStream in = new BufferedInputStream(connection.getInputStream(), 0x1000);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile), 0x1000);
        //noinspection TryFinallyCanBeTryWithResources
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
          if (makeReadonly) {
            Log.i(LOG_TAG, "Making file read-only: " + outFile.getAbsolutePath());
            if (!outFile.setReadOnly()) {
              throw new IOException("Unable to make " + outFile + " read-only.");
            }
          }
        }
      } else {
        error = true;           // Connection was null, failed to open?
      }
      if (error) {              // Try again recursively
        return getFile(fileName, cookieValue, asset, depth + 1);
      }
    } catch (Exception e) {
      Log.e(LOG_TAG, "Exception while fetching " + fileName, e);
      // Try again recursively
      return getFile(fileName, cookieValue, asset, depth + 1);
    } finally {
      if (makeReadonly) {
        // Note that this covers the case where an extension was previously loaded on an Android
        // device prior to Android 14 and cached. After upgrading, we won't re-download the file
        // but it needs to be marked read-only otherwise a SecurityException will be thrown when
        // trying to load the extension.
        outFile.setReadOnly();
      }
      if (connection != null) {
        connection.disconnect();
      }
    }

    if (responseCode == 200) {                             // Should the case...
      Date timeStamp = new Date();
      HashFile file = new HashFile(destinationFilename, fileHash, timeStamp);
      if (db.getHashFile(destinationFilename) == null) {
        db.insertHashFile(file);
      } else {
        db.updateHashFile(file);
      }
      return outFile;
    } else {
      return null;
    }
  }

  /**
   * Get the destination file for the asset.
   *
   * Generally, the assets are stored in the external storage directory of the app. However, if the
   * asset is an external component and the device is running Android Upside Down Cake (SDK 34) or
   * later, the asset is stored in the cache directory of the app due to some devices mounting the
   * external storage using sdcardfs, which does not support the necessary file operations.
   *
   * @param form The form for the Android context
   * @param asset The asset to get the destination file for
   * @return The destination file for the asset
   */
  private static File getDestinationFile(Form form, String asset) {
    if (asset.contains("/external_comps/")
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      File dest = new File(form.getCacheDir(), asset.substring("assets/".length()));
      File parent = dest.getParentFile();
      if (parent == null) {
        throw new IllegalStateException("Unable to determine parent directory for " + dest);
      }
      if (!parent.exists() && !parent.mkdirs()) {
        throw new YailRuntimeError("Unable to create directory " + parent, "Extensions");
      }
      String filename;
      if (asset.endsWith("/classes.jar")) {
        filename = parent.getName() + ".jar";
      } else {
        String[] parts = asset.split("/");
        filename = parts[parts.length - 1];
      }
      return new File(parent, filename);
    }
    return new File(QUtil.getReplAssetPath(form, true), asset.substring("assets/".length()));
  }

  private static String byteArray2Hex(final byte[] hash) {
    Formatter formatter = new Formatter();
    for (byte b : hash) {
      formatter.format("%02x", b);
    }
    return formatter.toString();
  }
}
