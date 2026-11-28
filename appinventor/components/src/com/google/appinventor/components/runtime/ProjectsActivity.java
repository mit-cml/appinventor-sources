// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.appinventor.components.runtime.util.SdkLevel;

import com.google.appinventor.components.runtime.util.ProjectIconUtil;
import com.google.appinventor.components.runtime.util.ProjectManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Create a An activity that display's the Repl's startup splash screen
 * which will introduce the REPL and ask for necessary permissions.
 *
 */
public final class ProjectsActivity extends AppInventorCompatActivity {

  private static final int COMPANION_HEADER_COLOR = 0xFFA5CF47;

  WebView webview;
  Handler handler;

  private static String getProjectIcon(File project) {
    try {
      byte[] data = ProjectIconUtil.readIcon(project);
      if (data == null) {
        return "";
      }
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeByteArray(data, 0, data.length, options);
      if (options.outWidth <= 0 || options.outHeight <= 0) {
        return "";
      }
      // Only send a thumbnail to the WebView, even when the project has a large icon.
      options.inSampleSize = 1;
      while (options.outWidth / options.inSampleSize > 192
          || options.outHeight / options.inSampleSize > 192) {
        options.inSampleSize *= 2;
      }
      options.inJustDecodeBounds = false;
      Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
      if (bitmap == null) {
        return "";
      }
      try {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
          return "";
        }
        return "data:image/png;base64," + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP);
      } finally {
        bitmap.recycle();
      }
    } catch (IOException | IllegalArgumentException e) {
      Log.w("ProjectsActivity", "Unable to read saved project icon: " + project.getName(), e);
      return "";
    }
  }

  public class JavaInterface {
    Context mContext;

    public JavaInterface(Context context) {
      mContext = context;
    }

    @JavascriptInterface
    public String listProjects() {
      File projectsDir = ProjectsActivity.this.getApplicationContext().getExternalFilesDir("assets/__projects__");
      if (projectsDir == null) {
        Log.w("ProjectsActivity", "projectsDir is null");
        return "[]";
      }
      Log.d("ProjectsActivity", "projectsDir = " + projectsDir.getPath());
      File[] files = projectsDir.listFiles();
      if (files == null) {
        return "[]";
      }
      JSONArray retval = new JSONArray();
      for (File file : files) {
        if (!file.isFile() || file.getName().startsWith(".")) {
          continue;
        }
        try {
          JSONObject entry = new JSONObject();
          entry.put("name", file.getName());
          entry.put("lastModified", file.lastModified());
          entry.put("icon", getProjectIcon(file));
          retval.put(entry);
        } catch (JSONException e) {
          Log.e("ProjectsActivity", "Error building project entry", e);
        }
      }
      return retval.toString();
    }

    @JavascriptInterface
    public void loadProject(String projectName) {
      Form form = Form.getActiveForm();
      ((ReplForm)form).setInAppLibrary();
      ProjectManager.setProjectInfo("0", projectName);
      ProjectManager.loadAssets();
      ProjectManager.evalScreenYail("Screen1");
      finished();
    }

    @JavascriptInterface
    public void finished() {
      ProjectsActivity.this.handler.post(new Runnable() {
          @Override
          public void run() {
            ProjectsActivity.this.webview.destroy();
            ProjectsActivity.this.finish();
          }
        });
    }

    @JavascriptInterface
    public void deleteProject(String projectName) {
      ProjectManager.deleteProject(projectName);
    }

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    JavaInterface android = new JavaInterface(this);
    handler = new Handler();
    webview = new WebView(this);
    WebSettings webSettings = webview.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDatabaseEnabled(true);
    webSettings.setDomStorageEnabled(true);
    String databasePath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
    webSettings.setDatabasePath(databasePath);

    webview.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onExceededDatabaseQuota(String url, String databaseIdentifier,
          long currentQuota, long estimatedSize, long totalUsedQuota,
          WebStorage.QuotaUpdater quotaUpdater) {
          quotaUpdater.updateQuota(5 * 1024 * 1024);
        }
      });
    setContentView(webview);
    hideTitleBar();
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }
    styleStatusBar();
    // Uncomment the line below to enable debugging
    // the splash screen (splash.html)
    //
    // webview.setWebContentsDebuggingEnabled(true);
    webview.addJavascriptInterface(android, "Android");
    webview.loadUrl("file:///android_asset/projects.html");
  }

  private void styleStatusBar() {
    Window window = getWindow();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      // The decor view draws behind the transparent status bar on edge-to-edge displays.
      window.getDecorView().setBackgroundColor(COMPANION_HEADER_COLOR);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      // Android 5 only supports light status bar icons.
      int color = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? COMPANION_HEADER_COLOR : 0xFF516623;
      window.setStatusBarColor(color);
    }
    WindowCompat.getInsetsController(window, window.getDecorView())
        .setAppearanceLightStatusBars(true);
  }
}
