// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
// This work is licensed under a Creative Commons Attribution 3.0 Unported License.

package com.google.appinventor.components.runtime.util;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import com.google.appinventor.components.runtime.Form;

public class PackageInstaller {

  private static final String LOG_TAG = "PackageInstaller(AppInventor)";
  private static final String REPL_ASSET_DIR = "/sdcard/AppInventor/assets/";

  // We don't instantiate this, we just have static methods
  private PackageInstaller() {
  }

  public static void doPackageInstall(final Form form, final String inurl) {
    AsynchUtil.runAsynchronously(new Runnable() {
        @Override
        public void run() {
          try {
            URL url = new URL(inurl);
            URLConnection conn = url.openConnection();
            File rootDir = new File(REPL_ASSET_DIR);
            InputStream instream = new BufferedInputStream(conn.getInputStream());
            File apkfile = new File(rootDir + "/package.apk");
            FileOutputStream apkOut = new FileOutputStream(apkfile);
            byte [] buffer = new byte[32768];
            int len;
            while ((len = instream.read(buffer, 0, 32768)) > 0) {
              apkOut.write(buffer, 0, len);
            }
            instream.close();
            apkOut.close();
            // Call Package Manager Here
            Log.d(LOG_TAG, "About to Install package from " + inurl);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri packageuri = Uri.fromFile(new File(rootDir + "/package.apk"));
            intent.setDataAndType(packageuri, "application/vnd.android.package-archive");
            form.startActivity(intent);
          } catch (Exception e) {
          form.dispatchErrorOccurredEvent(form, "PackageInstaller",
            ErrorMessages.ERROR_WEB_UNABLE_TO_GET, inurl);
          }
        }
      });
  }
}