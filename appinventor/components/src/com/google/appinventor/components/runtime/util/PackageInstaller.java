// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
// This work is licensed under a Creative Commons Attribution 3.0 Unported License.

package com.google.appinventor.components.runtime.util;

import android.content.ActivityNotFoundException;
import android.content.Intent;

import android.net.Uri;

import android.os.Environment;

import android.util.Log;

import com.google.appinventor.components.runtime.Form;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

public class PackageInstaller {

  private static final String LOG_TAG = "PackageInstaller(AppInventor)";
  private static final String REPL_ASSET_DIR =
    Environment.getExternalStorageDirectory().getAbsolutePath() +
    "/AppInventor/assets/";

  // We don't instantiate this, we just have static methods
  private PackageInstaller() {
  }

  public static void doPackageInstall(final Form form, final String inurl) {
    AsynchUtil.runAsynchronously(new Runnable() {
        @Override
        public void run() {
          Uri packageuri = null;
          try {
            URL url = new URL(inurl);
            URLConnection conn = url.openConnection();
            File rootDir = new File(REPL_ASSET_DIR);
            InputStream instream = new BufferedInputStream(conn.getInputStream());
            File apkfile = new File(rootDir + "/package.apk");
            FileOutputStream apkOut = new FileOutputStream(apkfile);
            byte[] buffer = new byte[32768];
            int len;
            while ((len = instream.read(buffer, 0, 32768)) > 0) {
              apkOut.write(buffer, 0, len);
            }
            instream.close();
            apkOut.close();
            // Call Package Manager Here
            Log.d(LOG_TAG, "About to Install package from " + inurl);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            packageuri = NougatUtil.getPackageUri(form, apkfile);
            intent.setDataAndType(packageuri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            form.startActivity(intent);
          } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, "Unable to install package", e);
            form.dispatchErrorOccurredEvent(form, "PackageInstaller",
              ErrorMessages.ERROR_UNABLE_TO_INSTALL_PACKAGE, packageuri);
          } catch (Exception e) {
            Log.e(LOG_TAG, "ERROR_UNABLE_TO_GET", e);
            form.dispatchErrorOccurredEvent(form, "PackageInstaller",
              ErrorMessages.ERROR_WEB_UNABLE_TO_GET, inurl);
          }
        }
      });
  }
}
