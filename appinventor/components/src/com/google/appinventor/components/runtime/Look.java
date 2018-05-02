// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.PermissionRequest;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Component that classifies images.
 *
 * @author kevinzhu@mit.edu (Kevin Zhu)
 * @author kelseyc@mit.edu (Kelsey Chan)
 */

@DesignerComponent(version = YaVersion.LOOK_COMPONENT_VERSION,
        category = ComponentCategory.EXPERIMENTAL,
        description = "Component that classifies images.")
@SimpleObject
@UsesAssets(fileNames = "look.html, look.js, group1-shard1of1, group10-shard1of1, group11-shard1of1, group12-shard1of1, group13-shard1of1, group14-shard1of1, group15-shard1of1, group16-shard1of1, group17-shard1of1, group18-shard1of1, group19-shard1of1, group2-shard1of1, group20-shard1of1, group21-shard1of1, group22-shard1of1, group23-shard1of1, group24-shard1of1, group25-shard1of1, group26-shard1of1, group27-shard1of1, group28-shard1of1, group29-shard1of1, group3-shard1of1, group30-shard1of1, group31-shard1of1, group32-shard1of1, group33-shard1of1, group34-shard1of1, group35-shard1of1, group36-shard1of1, group37-shard1of1, group38-shard1of1, group39-shard1of1, group4-shard1of1, group40-shard1of1, group41-shard1of1, group42-shard1of1, group43-shard1of1, group44-shard1of1, group45-shard1of1, group46-shard1of1, group47-shard1of1, group48-shard1of1, group49-shard1of1, group5-shard1of1, group50-shard1of1, group51-shard1of1, group52-shard1of1, group53-shard1of1, group54-shard1of1, group55-shard1of1, group6-shard1of1, group7-shard1of1, group8-shard1of1, group9-shard1of1, imagenet_classes.js, model.json, tfjs-0.10.3.js")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.CAMERA")
public final class Look extends AndroidViewComponent implements Component {
  private static final String LOG_TAG = Look.class.getSimpleName();
  private static final int IMAGE_WIDTH = 500;
  private static final int IMAGE_QUALITY = 100;

  private static final String MODEL_PREFIX = "https://storage.googleapis.com/tfjs-models/tfjs/mobilenet_v1_0.25_224/";

  // other error codes are defined in look.js
  private static final int ERROR_CLASSIFICATION_NOT_SUPPORTED = -1;
  private static final int ERROR_CLASSIFICATION_FAILED = -2;
  private static final int ERROR_CANNOT_TOGGLE_CAMERA_IN_IMAGE_MODE = -3;
  private static final int ERROR_CANNOT_CLASSIFY_IMAGE_IN_VIDEO_MODE = -4;
  private static final int ERROR_CANNOT_CLASSIFY_VIDEO_IN_IMAGE_MODE = -5;
  private static final int ERROR_INVALID_INPUT_MODE = -6;

  private final WebView webview;
  private final Form form;

  public Look(ComponentContainer container) {
    super(container);
    this.form = container.$form();
    webview = new WebView(container.$context());
    webview.getSettings().setJavaScriptEnabled(true);
    webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
    // adds a way to send strings to the javascript
    webview.addJavascriptInterface(new JsObject(), "Look");
    webview.setWebViewClient(new WebViewClient() {
      @Override
      public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        Log.d(LOG_TAG, "shouldInterceptRequest called");
        if (url.contains(MODEL_PREFIX)) {
          Log.d(LOG_TAG, "overriding " + url);
          try {
            InputStream inputStream = form.$context().getAssets().open("component/" + url.substring(MODEL_PREFIX.length()));
            if (url.endsWith(".json")) {
              return new WebResourceResponse("application/json", "UTF-8", inputStream);
            } else {
              return new WebResourceResponse("application/octet-stream", "binary", inputStream);
            }
          } catch (IOException e) {
            e.printStackTrace();
            return super.shouldInterceptRequest(view, url);
          }
        }
        Log.d(LOG_TAG, url);
        return super.shouldInterceptRequest(view, url);
      }
    });
    webview.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onPermissionRequest(PermissionRequest request) {
        Log.d(LOG_TAG, "onPermissionRequest called");
        String[] requestedResources = request.getResources();
        for (String r : requestedResources) {
          if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
          }
        }
      }
    });
    webview.loadUrl("file:///android_asset/component/look.html");
    Log.d(LOG_TAG, "Created Look component");
    container.$add(this);
  }

  @SimpleFunction(description = "Performs classification on the image at the given path and triggers the GotClassification event when classification is finished successfully.")
  public void ClassifyImageData(final String image) {
    Log.d(LOG_TAG, "Entered Classify");
    Log.d(LOG_TAG, image);

    String imagePath = (image == null) ? "" : image;
    BitmapDrawable imageDrawable;
    Bitmap scaledImageBitmap = null;

    try {
      imageDrawable = MediaUtil.getBitmapDrawable(form.$form(), imagePath);
      scaledImageBitmap = Bitmap.createScaledBitmap(imageDrawable.getBitmap(), IMAGE_WIDTH, (int) (imageDrawable.getBitmap().getHeight() * ((float) IMAGE_WIDTH) / imageDrawable.getBitmap().getWidth()), false);
    } catch (IOException ioe) {
      Log.e(LOG_TAG, "Unable to load " + imagePath);
    }

    // compression format of PNG -> not lossy
    Bitmap immagex = scaledImageBitmap;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    immagex.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, baos);
    byte[] b = baos.toByteArray();

    String imageEncodedbase64String = Base64.encodeToString(b, 0).replace("\n", "");
    Log.d(LOG_TAG, "imageEncodedbase64String: " + imageEncodedbase64String);

    webview.evaluateJavascript("classifyImageData(\"" + imageEncodedbase64String + "\");", null);
  }

  @SimpleFunction(description = "Toggles between user-facing and environment-facing camera.")
  public void ToggleCameraFacingMode() {
    webview.evaluateJavascript("toggleCameraFacingMode();", null);
  }

  @SimpleFunction(description = "Performs classification on current video frame and triggers the GotClassification event when classification is finished successfully.")
  public void ClassifyVideoData() {
    webview.evaluateJavascript("classifyVideoData();", null);
  }

  @SimpleFunction(description = "Sets the input mode to image if inputMode is \"image\" or video if inputMode is \"video\".")
  public void SetInputMode(final String inputMode) {
    webview.evaluateJavascript("setInputMode(\"" + inputMode + "\");", null);
  }

  @SimpleEvent(description = "Event indicating that the classifier is ready.")
  public void ClassifierReady() {
    EventDispatcher.dispatchEvent(this, "ClassifierReady");
  }

  @SimpleEvent(description = "Event indicating that classification has finished successfully. Result is of the form [[class1, confidence1], [class2, confidence2], ..., [class10, confidence10]].")
  public void GotClassification(YailList result) {
    EventDispatcher.dispatchEvent(this, "GotClassification", result);
  }

  @SimpleEvent(description = "Event indicating that an error has occurred.")
  public void Error(final int errorCode) {
    EventDispatcher.dispatchEvent(this, "Error", errorCode);
  }

  @Override
  public View getView() {
    return webview;
  }

  private class JsObject {
    @JavascriptInterface
    public void ready() {
      Log.d(LOG_TAG, "Entered ready");
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ClassifierReady();
        }
      });
    }

    @JavascriptInterface
    public void reportResult(final String result) {
      Log.d(LOG_TAG, "Entered reportResult: " + result);
      try {
        Log.d(LOG_TAG, "Entered try of reportResult");
        JSONArray list = new JSONArray(result);
        YailList intermediateList = YailList.makeList(JsonUtil.getListFromJsonArray(list));
        final List resultList = new ArrayList();
        for (int i = 0; i < intermediateList.size(); i++) {
          resultList.add(YailList.makeList((List) intermediateList.getObject(i)));
        }
        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            GotClassification(YailList.makeList(resultList));
          }
        });
      } catch (JSONException e) {
        Log.d(LOG_TAG, "Entered catch of reportResult");
        e.printStackTrace();
        Error(ERROR_CLASSIFICATION_FAILED);
      }
    }

    @JavascriptInterface
    public void error(final int errorCode) {
      Log.d(LOG_TAG, "Entered error: " + errorCode);
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Error(errorCode);
        }
      });
    }
  }
}

