// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import org.json.JSONArray;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;

import android.util.Log;

import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.appinventor.components.runtime.util.SdkLevel;

import com.google.appinventor.components.runtime.util.ProjectManager;

/**
 * Create a An activity that display's the Repl's startup splash screen
 * which will introduce the REPL and ask for necessary permissions.
 *
 */
public final class ProjectsActivity extends AppInventorCompatActivity {

  WebView webview;
  Handler handler;

  public class JavaInterface {
    Context mContext;

    public JavaInterface(Context context) {
      mContext = context;
    }

    @JavascriptInterface
    public String listProjects() {
      java.io.File projectsDir = ProjectsActivity.this.getApplicationContext().getExternalFilesDir("assets/__projects__");
      Log.d("ProjectsActivity", "projectsDir = " + projectsDir.getPath());
      String [] files = projectsDir.list();
      JSONArray retval = new JSONArray();
      for (String file : files ) {
        retval.put(file);
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
    // Uncomment the line below to enable debugging
    // the splash screen (splash.html)
    //
    // webview.setWebContentsDebuggingEnabled(true);
    webview.addJavascriptInterface(android, "Android");
    webview.loadUrl("file:///android_asset/projects.html");
  }

}
