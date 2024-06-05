package com.google.appinventor.components.runtime;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.assets.SDK26Helper;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.YailDictionary;
import java.util.List;
import java.util.zip.ZipInputStream;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;


@SimpleObject
public abstract class BaseAiComponent extends AndroidNonvisibleComponent{
    private static final String LOG_TAG = BaseAiComponent.class.getSimpleName();
    private static String TRANSFER_MODEL_PREFIX = null;
    private static String PERSONAL_MODEL_PREFIX = null;
    private static final String MODEL_PATH_SUFFIX = ".mdl";
    private WebView webview = null;
    private static final int ERROR_WEBVIEWER_REQUIRED = -7;
    private static final int ERROR_CLASSIFICATION_FAILED = -2;
    private static final int ERROR_INVALID_MODEL_FILE = -8;
    private static final int ERROR_MODEL_REQUIRED = -9;
    private static final String ERROR_WEBVIEWER_NOT_SET =
    "You must specify a WebViewer using the WebViewer designer property before you can call %1s";
    private String modelPath = null;
    private String assetPath = null;
    private String jsInterface = null;
    private List<String> labels = Collections.emptyList();
    

    protected BaseAiComponent(Form form) {
        super(form);
        requestHardwareAcceleration(form);
        WebView.setWebContentsDebuggingEnabled(true);
    }
    
   
    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView(WebView webview) {
      this.webview = webview;
      webview.getSettings().setJavaScriptEnabled(true);
      webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
      // adds a way to send strings to the javascript
      webview.addJavascriptInterface(new JsObject(), jsInterface);
      if(jsInterface.equals("PersonalImageClassifier")){
          TRANSFER_MODEL_PREFIX="https://appinventor.mit.edu/personal-image-classifier/transfer/";
          PERSONAL_MODEL_PREFIX="https://appinventor.mit.edu/personal-image-classifier/personal/";
      }else if(jsInterface.equals("PersonalAudioClassifier")){
          TRANSFER_MODEL_PREFIX="https://appinventor.mit.edu/personal-audio-classifier/transfer/";
          PERSONAL_MODEL_PREFIX="https://appinventor.mit.edu/personal-audio-classifier/personal/";
      }
      webview.setWebViewClient(new WebViewClient() {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
          Log.d(LOG_TAG, "shouldInterceptRequest called");
          InputStream file = null;
          String charSet;
          String contentType;

          if (url.endsWith(".json")) {
            contentType = "application/json";
            charSet = "UTF-8";
          } else {
            contentType = "application/octet-stream";
            charSet = "binary";
          }

          try {
            if (url.contains(TRANSFER_MODEL_PREFIX)) {
              Log.d(LOG_TAG, "overriding " + url);

              file = form.openAsset(url.substring(TRANSFER_MODEL_PREFIX.length()));
            } else if (url.contains(PERSONAL_MODEL_PREFIX)) {
              Log.d(LOG_TAG, "overriding " + url);

              String fileName = url.substring(PERSONAL_MODEL_PREFIX.length());
              ZipInputStream zipInputStream = new ZipInputStream(MediaUtil.openMedia(form, modelPath));
              ZipEntry zipEntry;

              while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(fileName)) {
                  int zipEntrySize = (int) zipEntry.getSize();
                  byte[] fileBytes = new byte[zipEntrySize];

                  zipInputStream.read(fileBytes, 0, zipEntrySize);
                  file = new ByteArrayInputStream(fileBytes);
                  break;
                }
              }
              
              zipInputStream.close();
            }

            if (file != null) {
              if (SdkLevel.getLevel() >= SdkLevel.LEVEL_LOLLIPOP) {
                Map<String, String> responseHeaders = new HashMap<>();
                responseHeaders.put("Access-Control-Allow-Origin", "*");
                return new WebResourceResponse(contentType, charSet, 200, "OK", responseHeaders, file);
              } else {
                return new WebResourceResponse(contentType, charSet, file);
              }
            }  
          } catch (IOException e) {
            e.printStackTrace();
            return super.shouldInterceptRequest(view, url);
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
    }
  
    public void Initialize() {
        Log.d(LOG_TAG, "webview = " + webview);
        if (webview == null) {
          form.dispatchErrorOccurredEvent(this, "WebViewer",
              ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_WEBVIEWER_REQUIRED, LOG_TAG,
              "You must specify a WebViewer component in the WebViewer property.");
        }
    
        Log.d(LOG_TAG, "modelPath = " + modelPath);
        if (modelPath == null) {
          form.dispatchErrorOccurredEvent(this, "Model",
              ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_MODEL_REQUIRED, LOG_TAG,
              "You must provide a model file in the Model property");
        }
      }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT + ":com.google.appinventor.runtime.components.WebViewer")
    @SimpleProperty(userVisible = false)
    public void WebViewer(final WebViewer webviewer) {
      if (BaseAiComponent.this instanceof PersonalImageClassifier) {
          assetPath = "personal_image_classifier.html";
          jsInterface="PersonalImageClassifier";
      } else if (BaseAiComponent.this instanceof PersonalAudioClassifier) {
          assetPath = "personal_audio_classifier.html";
          jsInterface="PersonalAudioClassifier";
      }
      // implement checks for other AI components
      Runnable next = new Runnable() {
        public void run() {
          if (webviewer != null) {
            configureWebView((WebView) webviewer.getView());
            webview.requestLayout();
            Log.d(LOG_TAG, "isHardwareAccelerated? " + webview.isHardwareAccelerated());
            webview.loadUrl(form.getAssetPath(assetPath));
          }
        }
      };
      if (SDK26Helper.shouldAskForPermission(form)) {
        SDK26Helper.askForPermission(this, next);
      } else {
        next.run();
      }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
    defaultValue = "")
    @SimpleProperty(userVisible = false)
    public void Model(String path) {
      Log.d(LOG_TAG, "Personal model path: " + path);
       // implement checks for other AI components suffix
      if (path.endsWith(MODEL_PATH_SUFFIX)) {
        modelPath = path;
      } else {
        form.dispatchErrorOccurredEvent(this, "Model",
            ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INVALID_MODEL_FILE, LOG_TAG,
            "Invalid model file format: files must be of format " + MODEL_PATH_SUFFIX);
      }
    }
        
    public Form getForm() {
      return form;
    }

    private static void requestHardwareAcceleration(Activity activity) {
        activity.getWindow().setFlags(LayoutParams.FLAG_HARDWARE_ACCELERATED, LayoutParams.FLAG_HARDWARE_ACCELERATED);
    }
    
    @SimpleEvent(description = "Event indicating that the classifier is ready.")
    public abstract void ClassifierReady();
  
    @SimpleEvent(description = "Event indicating that classification has finished successfully. Result is of the form [[class1, confidence1], [class2, confidence2], ..., [class10, confidence10]].")
    public abstract void GotClassification(YailDictionary result);
  
    @SimpleEvent(description = "Event indicating that an error has occurred.")
    public abstract void Error(final int errorCode);

    private void assertWebView(String method) {
      if (webview == null) {
        throw new RuntimeException(String.format(ERROR_WEBVIEWER_NOT_SET, method));
      }
    }
  
    private static List<String> parseLabels(String labels) {
      List<String> result = new ArrayList<>();
      try {
        JSONArray arr = new JSONArray(labels);
        for (int i = 0; i < arr.length(); i++) {
          result.add(arr.getString(i));
        }
      } catch (JSONException e) {
        throw new YailRuntimeError("Got unparsable array from Javascript", "RuntimeError");
      }
      return result;
    }

  private class JsObject {
    @JavascriptInterface
    public void ready(String labels) {
      Log.d(LOG_TAG, "Entered ready");
      BaseAiComponent.this.labels = parseLabels(labels);
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
        final YailDictionary resultDict = new YailDictionary();
        for (int i = 0; i < list.length(); i++) {
          JSONArray pair = list.getJSONArray(i);
          resultDict.put(pair.getString(0), pair.getDouble(1));
        }
        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            GotClassification(resultDict);
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
