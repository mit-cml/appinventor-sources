// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package edu.mit.appinventor.ai.posenet;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Deleteable;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.OnPauseListener;
import com.google.appinventor.components.runtime.OnResumeListener;
import com.google.appinventor.components.runtime.OnStopListener;
import com.google.appinventor.components.runtime.PermissionResultHandler;
import com.google.appinventor.components.runtime.WebViewer;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.YailList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@DesignerComponent(version = 20191008,
    category = ComponentCategory.EXTENSION,
    description = "An extension that embeds a Posenet model.",
    iconName = "aiwebres/icon.png",
    nonVisible = true)
@SimpleObject(external = true)
@UsesAssets(fileNames = "posenet.min.js, index.html, app.js, group1-shard1of1.bin, " +
    "model-stride16.json, tf-converter.min.js, tf-core.min.js")
@UsesPermissions({Manifest.permission.CAMERA})
public class PosenetExtension extends AndroidNonvisibleComponent
    implements OnResumeListener, OnPauseListener, OnStopListener, Deleteable {
  private static final String LOG_TAG = PosenetExtension.class.getSimpleName();
  private static final String ERROR_WEBVIEWER_NOT_SET =
      "You must specify a WebViewer using the WebViewer designer property before you can call %1s";
  private static final int ERROR_JSON_PARSE_FAILED = 101;
  private static final String MODEL_URL =
      "https://storage.googleapis.com/tfjs-models/savedmodel/posenet/mobilenet/quant2/050/";
  private static final String BACK_CAMERA = "Back";
  private static final String FRONT_CAMERA = "Front";

  private WebView webview = null;
  private final Map<String, YailList> keyPoints = new ConcurrentHashMap<>();
  private double minPoseConfidence = 0.1;
  private double minPartConfidence = 0.5;
  private String cameraMode = FRONT_CAMERA;
  private boolean initialized = false;
  private boolean enabled = true;
  private String backgroundImage = "";

  /**
   * Creates a new PosenetExtension extension.
   *
   * @param form the container that this component will be placed in
   */
  public PosenetExtension(Form form) {
    super(form);
    requestHardwareAcceleration(form);
    WebView.setWebContentsDebuggingEnabled(true);
    keyPoints.put("nose", YailList.makeEmptyList());
    keyPoints.put("leftEye", YailList.makeEmptyList());
    keyPoints.put("rightEye", YailList.makeEmptyList());
    keyPoints.put("leftEar", YailList.makeEmptyList());
    keyPoints.put("rightEar", YailList.makeEmptyList());
    keyPoints.put("leftShoulder", YailList.makeEmptyList());
    keyPoints.put("rightShoulder", YailList.makeEmptyList());
    keyPoints.put("leftElbow", YailList.makeEmptyList());
    keyPoints.put("rightElbow", YailList.makeEmptyList());
    keyPoints.put("leftWrist", YailList.makeEmptyList());
    keyPoints.put("rightWrist", YailList.makeEmptyList());
    keyPoints.put("leftHip", YailList.makeEmptyList());
    keyPoints.put("rightHip", YailList.makeEmptyList());
    keyPoints.put("leftKnee", YailList.makeEmptyList());
    keyPoints.put("rightKnee", YailList.makeEmptyList());
    keyPoints.put("leftAnkle", YailList.makeEmptyList());
    keyPoints.put("rightAnkle", YailList.makeEmptyList());
    Log.d(LOG_TAG, "Created Posenet extension");
  }

  @SuppressLint("SetJavaScriptEnabled")
  private void configureWebView(WebView webview) {
    this.webview = webview;
    webview.getSettings().setJavaScriptEnabled(true);
    webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
    webview.addJavascriptInterface(new AppInventorTFJS(), "PosenetExtension");
    webview.setWebViewClient(new WebViewClient() {
      @Override
      public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        final String url = request.getUrl().toString();
        Log.d(LOG_TAG, "shouldInterceptRequest called");
        if (url.startsWith(MODEL_URL)) {
          Log.d(LOG_TAG, "overriding " + url);
          InputStream is;
          try {
            is = form.openAssetForExtension(PosenetExtension.this,
                url.substring(MODEL_URL.length()));
            String contentType, charSet;
            if (url.endsWith(".json")) {
              contentType = "application/json";
              charSet = "UTF-8";
            } else {
              contentType = "application/octet-stream";
              charSet = "binary";
            }
            if (SdkLevel.getLevel() >= SdkLevel.LEVEL_LOLLIPOP) {
              Map<String, String> responseHeaders = new HashMap<>();
              responseHeaders.put("Access-Control-Allow-Origin", "*");
              return new WebResourceResponse(contentType, charSet, 200, "OK", responseHeaders, is);
            } else {
              return new WebResourceResponse(contentType, charSet, is);
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        Log.d(LOG_TAG, url);
        return super.shouldInterceptRequest(view, request);
      }
    });
    webview.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onPermissionRequest(final PermissionRequest request) {
        Log.d(LOG_TAG, "onPermissionRequest called");
        String[] requestedResources = request.getResources();
        for (String r : requestedResources) {
          if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            form.askPermission(permission.CAMERA, new PermissionResultHandler() {
              @Override
              public void HandlePermissionResponse(String permission, boolean granted) {
                if (granted) {
                  request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                } else {
                  form.dispatchPermissionDeniedEvent(PosenetExtension.this, "Enable", permission);
                }
              }
            });
          }
        }
      }
    });
  }

  @SuppressWarnings("squid:S00100")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT
      + ":com.google.appinventor.components.runtime.WebViewer")
  @SimpleProperty(userVisible = false)
  public void WebViewer(WebViewer webviewer) {
    if (webviewer != null) {
      configureWebView((WebView) webviewer.getView());
      webview.requestLayout();
    }
    try {
      Log.d(LOG_TAG, "isHardwareAccelerated? " + webview.isHardwareAccelerated());
      webview.loadUrl(form.getAssetPathForExtension(this, "index.html"));
    } catch(FileNotFoundException e) {
      Log.e(LOG_TAG, "Unable to load tensorflow", e);
    }
  }

  public void Initialize() {
    if (webview != null) {
      initialized = true;
    }
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "0.1")
  @SimpleProperty
  public void MinPoseConfidence(double minPoseConfidence) {
    this.minPoseConfidence = minPoseConfidence;
    if (initialized) {
      webview.evaluateJavascript("minPoseConfidence = " + minPoseConfidence + ";", null);
    }
  }

  @SimpleProperty(description = "The minimum confidence required to detect a pose.")
  public double MinPoseConfidence() {
    return minPoseConfidence;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "0.5")
  @SimpleProperty
  public void MinPartConfidence(double minPartConfidence) {
    this.minPartConfidence = minPartConfidence;
    if (initialized) {
      webview.evaluateJavascript("minPartConfidence = " + minPartConfidence + ";", null);
    }
  }

  @SimpleProperty(description = "The minimum amount of confidence to detect a body part.")
  public double MinPartConfidence() {
    return minPartConfidence;
  }

  @SimpleProperty(description = "A list of points representing body parts that met the minimum " +
      "part confidence threshold.")
  public YailList KeyPoints() {
    List<YailList> keyPoints = new ArrayList<>();
    for (YailList point : this.keyPoints.values()) {
      if (point.size() == 2) {
        keyPoints.add(point);
      }
    }
    return YailList.makeList(keyPoints);
  }

  private static void addIfValid(YailList point1, YailList point2, List<YailList> skeleton) {
    if (point1.size() == 2 && point2.size() == 2) {
      skeleton.add(YailList.makeList(new Object[] { point1, point2 }));
    }
  }

  @SimpleProperty(description = "A list of pairs of points representing connections between " +
      "valid body parts.")
  public YailList Skeleton() {
    List<YailList> skeleton = new ArrayList<>();
    final YailList lWrist = keyPoints.get("leftWrist");
    final YailList lElbow = keyPoints.get("leftElbow");
    final YailList lShoulder = keyPoints.get("leftShoulder");
    final YailList rShoulder = keyPoints.get("rightShoulder");
    final YailList rElbow = keyPoints.get("rightElbow");
    final YailList rWrist = keyPoints.get("rightWrist");
    final YailList lHip = keyPoints.get("leftHip");
    final YailList lKnee = keyPoints.get("leftKnee");
    final YailList lAnkle = keyPoints.get("leftAnkle");
    final YailList rHip = keyPoints.get("rightHip");
    final YailList rKnee = keyPoints.get("rightKnee");
    final YailList rAnkle = keyPoints.get("rightAnkle");
    addIfValid(lWrist, lElbow, skeleton);
    addIfValid(lElbow, lShoulder, skeleton);
    addIfValid(lShoulder, rShoulder, skeleton);
    addIfValid(rShoulder, rElbow, skeleton);
    addIfValid(rElbow, rWrist, skeleton);
    addIfValid(lShoulder, lHip, skeleton);
    addIfValid(rShoulder, rHip, skeleton);
    addIfValid(lHip, rHip, skeleton);
    addIfValid(lHip, lKnee, skeleton);
    addIfValid(lKnee, lAnkle, skeleton);
    addIfValid(rHip, rKnee, skeleton);
    addIfValid(rKnee, rAnkle, skeleton);
    return YailList.makeList(skeleton);
  }

  @SimpleProperty(description = "Position of the nose in the video frame as an (x, y) pair. " +
      "If no nose is detected, it returns an empty list.")
  public YailList Nose() {
    return keyPoints.get("nose");
  }

  @SimpleProperty(description = "Position of the left eye in the video frame as an (x, y) pair. " +
      "If no left eye is detected, it returns an empty list.")
  public YailList LeftEye() {
    return keyPoints.get("leftEye");
  }

  @SimpleProperty(description = "Position of the right eye in the video frame as an (x, y) pair. " +
      "If no right eye is detected, it returns an empty list.")
  public YailList RightEye() {
    return keyPoints.get("rightEye");
  }

  @SimpleProperty(description = "Position of the left ear in the video frame as an (x, y) pair. " +
      "If no left ear is detected, it returns an empty list.")
  public YailList LeftEar() {
    return keyPoints.get("leftEar");
  }

  @SimpleProperty(description = "Position of the right ear in the video frame as an (x, y) pair. " +
      "If no right ear is detected, it returns an empty list.")
  public YailList RightEar() {
    return keyPoints.get("rightEar");
  }

  @SimpleProperty(description = "Position of the left shoulder in the video frame as an (x, y) pair. " +
      "If no left shoulder is detected, it returns an empty list.")
  public YailList LeftShoulder() {
    return keyPoints.get("leftShoulder");
  }

  @SimpleProperty(description = "Position of the right shoulder in the video frame as an (x, y) pair. " +
      "If no right shoulder is detected, it returns an empty list.")
  public YailList RightShoulder() {
    return keyPoints.get("rightShoulder");
  }

  @SimpleProperty(description = "Position of the left elbow in the video frame as an (x, y) pair. " +
      "If no left elbow is detected, it returns an empty list.")
  public YailList LeftElbow() {
    return keyPoints.get("leftElbow");
  }

  @SimpleProperty(description = "Position of the right elbow in the video frame as an (x, y) pair. " +
      "If no right eblow is detected, it returns an empty list.")
  public YailList RightElbow() {
    return keyPoints.get("rightElbow");
  }

  @SimpleProperty(description = "Position of the left wrist in the video frame as an (x, y) pair. " +
      "If no left wrist is detected, it returns an empty list.")
  public YailList LeftWrist() {
    return keyPoints.get("leftWrist");
  }

  @SimpleProperty(description = "Position of the right wrist the video frame as an (x, y) pair. " +
      "If no right wrist is detected, it returns an empty list.")
  public YailList RightWrist() {
    return keyPoints.get("rightWrist");
  }

  @SimpleProperty(description = "Position of the left hip in the video frame as an (x, y) pair. " +
      "If no left hip is detected, it returns an empty list.")
  public YailList LeftHip() {
    return keyPoints.get("leftHip");
  }

  @SimpleProperty(description = "Position of the right hip in the video frame as an (x, y) pair. " +
      "If no right hip is detected, it returns an empty list.")
  public YailList RightHip() {
    return keyPoints.get("rightHip");
  }

  @SimpleProperty(description = "Position of the left knee in the video frame as an (x, y) pair. " +
      "If no left knee is detected, it returns an empty list.")
  public YailList LeftKnee() {
    return keyPoints.get("leftKnee");
  }

  @SimpleProperty(description = "Position of the right knee in the video frame as an (x, y) pair. " +
      "If no right knee is detected, it returns an empty list.")
  public YailList RightKnee() {
    return keyPoints.get("rightKnee");
  }

  @SimpleProperty(description = "Position of the left ankle in the video frame as an (x, y) pair. " +
      "If no left ankle is detected, it returns an empty list.")
  public YailList LeftAnkle() {
    return keyPoints.get("leftAnkle");
  }

  @SimpleProperty(description = "Position of the right ankle in the video frame as an (x, y) pair. " +
      "If no right ankle is detected, it returns an empty list.")
  public YailList RightAnkle() {
    return keyPoints.get("rightAnkle");
  }

  @SimpleProperty(description = "BackGround Image.")
  public String backgroundImage() {
    return backgroundImage;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    this.enabled = enabled;
    if (initialized) {
      assertWebView("Enabled");
      webview.evaluateJavascript(enabled ? "startVideo();" : "stopVideo();", null);
    }
  }

  @SimpleProperty(description = "Enables or disables pose detection.")
  public boolean Enabled() {
    return enabled;
  }

  @SuppressWarnings("squid:S00100")
  @SimpleEvent(description = "Event indicating that the classifier is ready.")
  public void ModelReady() {
    EventDispatcher.dispatchEvent(this, "ModelReady");
  }

  @SuppressWarnings("squid:S00100")
  @SimpleEvent(description = "Event indicating that an error has occurred.")
  public void Error(int errorCode, String errorMessage) {
    EventDispatcher.dispatchEvent(this, "Error", errorCode, errorMessage);
  }

  @SuppressWarnings("squid:S00100")
  @SimpleEvent(description = "Event indicating that classification has finished successfully. "
      + "Result is of the form [[class1, confidence1], ...]")
  public void PoseUpdated() {
    EventDispatcher.dispatchEvent(this, "PoseUpdated");
  }

  @SimpleProperty(description = "Configures Posenet to use the front or " +
      "back camera on the device.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHOICES,
      editorArgs = {BACK_CAMERA, FRONT_CAMERA}, defaultValue = FRONT_CAMERA)
  public void UseCamera(String mode) {
    if (BACK_CAMERA.equals(mode) || FRONT_CAMERA.equals(mode)) {
      cameraMode = mode;
      if (initialized) {
        boolean frontFacing = mode.equals(FRONT_CAMERA);
        webview.evaluateJavascript("setCameraFacingMode(" + frontFacing + ");", null);
      }
    } else {
      form.dispatchErrorOccurredEvent(this, "UseCamera", ErrorMessages.ERROR_EXTENSION_ERROR,
          1, LOG_TAG, "Invalid camera selection. Must be either 'Front' or 'Back'.");
    }
  }

  @SimpleProperty
  public String UseCamera() {
    return cameraMode;
  }

  private static void requestHardwareAcceleration(Activity activity) {
    activity.getWindow().setFlags(LayoutParams.FLAG_HARDWARE_ACCELERATED,
        LayoutParams.FLAG_HARDWARE_ACCELERATED);
  }

  @SuppressWarnings("SameParameterValue")
  private void assertWebView(String method) {
    if (webview == null) {
      throw new IllegalStateException(String.format(ERROR_WEBVIEWER_NOT_SET, method));
    }
  }

  @Override
  public void onDelete() {
    if (initialized && webview != null) {
      webview.evaluateJavascript("teardown();", null);
      webview = null;
    }
  }

  @Override
  public void onPause() {
    if (initialized && webview != null) {
      webview.evaluateJavascript("stopVideo();", null);
    }
  }

  @Override
  public void onResume() {
    if (initialized && enabled && webview != null) {
      webview.evaluateJavascript("startVideo();", null);
    }
  }

  @Override
  public void onStop() {
    if (initialized && webview != null) {
      webview.evaluateJavascript("teardown();", null);
      webview = null;
    }
  }

  private class AppInventorTFJS {
    @JavascriptInterface
    public void ready() {
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ModelReady();
          if (enabled) {
            MinPoseConfidence(minPoseConfidence);
            MinPartConfidence(minPartConfidence);
            UseCamera(cameraMode);
          }
        }
      });
    }

    @JavascriptInterface
    public void reportImage(final String dataUrl) {
      Log.d(LOG_TAG, "reportImage "  + dataUrl);
      if (dataUrl != null) {
        backgroundImage = dataUrl.substring(dataUrl.indexOf(",") + 1);
      }
    }

    @JavascriptInterface
    public void reportResult(final String result) {
      try {
        JSONArray points = new JSONArray(result);
        Log.d(LOG_TAG, "reportResult "  + result);
        for (int i = 0; i < points.length(); i++) {
          JSONObject keypoint = points.getJSONObject(i);
          String part = keypoint.getString("part");
          double score = keypoint.getDouble("score");
          JSONObject position = keypoint.getJSONObject("position");
          double x = position.getDouble("x");
          double y = position.getDouble("y");
          YailList coord = score < minPartConfidence ? YailList.makeEmptyList() :
              YailList.makeList(new Double[]{x, y});
          keyPoints.put(part, coord);
        }
        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            PoseUpdated();
          }
        });
      } catch (JSONException e) {
        Error(ERROR_JSON_PARSE_FAILED, e.getMessage());
        Log.e(LOG_TAG, "Error parsing JSON from web view", e);
      }
    }

    @JavascriptInterface
    public void error(final int errorCode, final String errorMessage) {
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Error(errorCode, errorMessage);
        }
      });
    }
  }

  /*
[
  {
    "score": 0.9774239659309387,
    "part": "nose",
    "position": {
      "x": 267.1227299205044,
      "y": 187.68879851402593
    }
  },
  {
    "score": 0.9963725805282593,
    "part": "leftEye",
    "position": {
      "x": 234.9916820860746,
      "y": 146.99433979235195
    }
  },
  {
    "score": 0.9987560510635376,
    "part": "rightEye",
    "position": {
      "x": 302.15201965688965,
      "y": 144.59289193850512
    }
  },
  {
    "score": 0.8036010265350342,
    "part": "leftEar",
    "position": {
      "x": 199.4495185718202,
      "y": 161.26186080843382
    }
  },
  {
    "score": 0.9632443785667419,
    "part": "rightEar",
    "position": {
      "x": 354.6639554207785,
      "y": 156.1914276658443
    }
  },
  {
    "score": 0.6833674907684326,
    "part": "leftShoulder",
    "position": {
      "x": 121.80112818667766,
      "y": 339.615599872076
    }
  },
  {
    "score": 0.384988397359848,
    "part": "rightShoulder",
    "position": {
      "x": 434.7243766561586,
      "y": 334.99848661366957
    }
  },
  {
    "score": 0.04785492271184921,
    "part": "leftElbow",
    "position": {
      "x": 28.56935592562138,
      "y": 480.3350437454312
    }
  },
  {
    "score": 0.035571545362472534,
    "part": "rightElbow",
    "position": {
      "x": 47.22059319033258,
      "y": 469.10025613349774
    }
  },
  {
    "score": 0.009528351947665215,
    "part": "leftWrist",
    "position": {
      "x": 21.559486076845815,
      "y": 475.2071269074378
    }
  },
  {
    "score": 0.009086783975362778,
    "part": "rightWrist",
    "position": {
      "x": 399.65732364766086,
      "y": 290.05473175941154
    }
  },
  {
    "score": 0.02234589494764805,
    "part": "leftHip",
    "position": {
      "x": 237.29010188230995,
      "y": 459.97510765031063
    }
  },
  {
    "score": 0.02040420100092888,
    "part": "rightHip",
    "position": {
      "x": 53.97932657163744,
      "y": 528.3431917603252
    }
  },
  {
    "score": 0.011202405206859112,
    "part": "leftKnee",
    "position": {
      "x": 40.48313288103077,
      "y": 536.5313256693165
    }
  },
  {
    "score": 0.013752722181379795,
    "part": "rightKnee",
    "position": {
      "x": 69.12760987755848,
      "y": 471.6126202142726
    }
  },
  {
    "score": 0.012224797159433365,
    "part": "leftAnkle",
    "position": {
      "x": 26.075266698647738,
      "y": 548.9613315515351
    }
  },
  {
    "score": 0.008277447894215584,
    "part": "rightAnkle",
    "position": {
      "x": 26.12573670504389,
      "y": 547.5285758749086
    }
  }
]
   */
}
