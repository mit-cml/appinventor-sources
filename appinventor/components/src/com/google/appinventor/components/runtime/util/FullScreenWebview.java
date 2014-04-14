package com.google.appinventor.components.runtime.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * This util Android activity can be used as full screen webview to load webpages either 
 * locally or remotely
 */
public class FullScreenWebview extends Activity{
  private WebView mWebView; 
  private static final String TAG = "FullScreenWebView";
  public static final String ARG_LOCAL = "local";
  public static final String ARG_DOCUMENT_PATH = "doc_path";
  public static final String ARG_MESSAGE = "message";
  public static final String ARG_URL_PARAMS = "url_parameters"; // use for call for post using Intent surface
  private boolean local = false;
  private String webUrl = "";
  private String message = "";
  
  private ProgressDialog dialog;
  private ConnectivityManager connectivityManager;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    
    super.onCreate(savedInstanceState);
    connectivityManager =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    // We can extend this class capability later.

    Intent startIntent = getIntent();
    dialog = new ProgressDialog(this);
    
    // if (startIntent != null && startIntent.hasExtra(ARGUMENT_NAME)) {
    // startupValue = startIntent.getStringExtra(ARGUMENT_NAME);
    // }
    local = startIntent.getBooleanExtra(ARG_LOCAL, false);
    webUrl = startIntent.getStringExtra(ARG_DOCUMENT_PATH);
    // the data should be prepared from the caller and pass into the intent
    message = startIntent.getStringExtra(ARG_MESSAGE);
    Log.i(TAG, "message?: " + message);
    // make it full screen
    getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    

    mWebView = new WebView(this);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.setFocusable(true);
    mWebView.setVerticalScrollBarEnabled(true);
    
    mWebView.setWebViewClient(new WebViewClient() {
      public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
        dialog.show();
      }

      public void onPageFinished(WebView view, String url) {
        dialog.dismiss();
      }
    });
    //http://developer.android.com/guide/webapps/debugging.html
    mWebView.setWebChromeClient(new WebChromeClient() {
      public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        Log.d(TAG, message + " -- From line "
                             + lineNumber + " of "
                             + sourceID);
      }
    });
    
    mWebView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_UP:
          if (!v.hasFocus()) {
            v.requestFocus();
          }
          break;
        }
        return false;
      }
    });
 

    LoadHtml(local, webUrl, message);
    this.setContentView(mWebView);
  }
  
  private boolean isOnline() {
    NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
        return true;
    }
    return false;
}
  
  public void LoadHtml(boolean local, String fileName, String message) {
    Log.i(TAG, "Before load data");
    // before loading we bind webview with some inner class to interface
    // with js. The interface can be as simple as just passing the message to
    // the html + js

    mWebView.addJavascriptInterface(new Messenger(this, message), "messenger");

    // http://pivotallabs.com/users/tyler/blog/articles/1853-android-webview-loaddata-vs-loaddatawithbaseurl-
    // http://myexperiencewithandroid.blogspot.com/2011/09/android-loaddatawithbaseurl.html
    // All the local html should be stored in /assets/
    if (local) {
      try {
        // try to read out the file that has previously copied to /assets/
    	String filePath = fileName;
        BufferedInputStream in = new BufferedInputStream(getAssets().open(
        	filePath));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line);

        }
        String htmlContent = sb.toString();
        // need to check where can we put local html files. e.g. for example the privacy policy files
        // can be generated in Compiler.java and write to android_asset
        mWebView.loadDataWithBaseURL("file:///android_asset/",
        	htmlContent, "text/html", "UTF-8", null);
      } catch (IOException e) {
        Log.i(TAG, "Problem reading local file");
      }
    } else {
      
      // for now, maybe we just load a remote html for privacy
      // where should we put it? put it under some category for each user
      if (isOnline()){
    	mWebView.loadUrl(fileName);
      } else{
    	//show a dialog and close the activity
    	showExitApplicationNotification();
      }
    }
    Log.i(TAG, "After load data");

  }
  
  private void showExitApplicationNotification() {
    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    alertDialog.setTitle("Network Not Available");
    // prevents the user from escaping the dialog by hitting the Back button
    alertDialog.setCancelable(false);
    alertDialog.setMessage("Current the network is not available, connect to network first and try again.");
    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Stop and exit",
        new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        finish();
      }});
    alertDialog.show();
  }
  
  private void closeWithResults(String result){
    // the result will pass back to the Form.java and then pass to other component if needed
    Intent resultIntent;
    resultIntent = new Intent();
    setResult(Activity.RESULT_OK, resultIntent);
    finish();

  }
  
//  Intent resultIntent = new Intent();
////TODO Add extras or a data URI to this intent as appropriate.
//setResult(Activity.RESULT_OK, resultIntent);
//finish();
  
  public class Messenger{
    Activity parent;
    private String msg;
    public Messenger(Activity parent, String message){
      Log.i(TAG, "Initiate Messenger class:" + message);
      msg = message;
      this.parent = parent;
	   
    }
    public String getMessage(){
      Log.i(TAG, "message returned:" + message);
      return msg;
    }
    public void closeWithMsg(String message){
      Log.i(TAG, "closeWithMsg called with message:" + message );
      ((FullScreenWebview) parent).closeWithResults(message);
    }

  }


}
