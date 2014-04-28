// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.webkit.JavascriptInterface;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.EclairUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Component for displaying web pages
 * This is a very limited form of browser.  You can view web pages and
 * click on links. It also handles  Javascript. There are lots of things that could be added,
 * but this component is mostly for viewing individual pages.  It's not intended to take
 * the place of the browser.
 *
 * @author halabelson@google.com (Hal Abelson)
 */

@DesignerComponent(version = YaVersion.WEBVIEWER_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Component for viewing Web pages.  The Home URL can be " +
        "specified in the Designer or in the Blocks Editor.  The view can be set " +
        "to follow links when they are tapped, and users can fill in Web forms. " +
        "Warning: This is not a full browser.  For example, pressing the phone's " +
        "hardware Back key will exit the app, rather than move back in the " +
        "browser history.")


// TODO(halabelson): Integrate control of the Back key, when we provide it

@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class WebViewer extends AndroidViewComponent {

  private final WebView webview;

  // URL for the WebViewer to load initially
  private String homeUrl;

  // whether or not to follow links when they are tapped
  private boolean followLinks;

  // Whether or not to prompt for permission in the WebViewer
  private boolean prompt = true;

  // allows passing strings to javascript
  WebViewInterface wvInterface;

  /**
   * Creates a new WebViewer component.
   *
   * @param container  container the component will be placed in
   */
  public WebViewer(ComponentContainer container) {
    super(container);

    webview = new WebView(container.$context());
    webview.setWebViewClient(new WebViewerClient());
    webview.getSettings().setJavaScriptEnabled(true);
    webview.setFocusable(true);
    // adds a way to send strings to the javascript
    wvInterface = new WebViewInterface(webview.getContext());
    webview.addJavascriptInterface(wvInterface, "AppInventor");
    // enable pinch zooming and zoom controls
    webview.getSettings().setBuiltInZoomControls(true);

    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR)
      EclairUtil.setupWebViewGeoLoc(this, webview, container.$context());

    container.$add(this);

    webview.setOnTouchListener(new View.OnTouchListener() {
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

    // set the initial default properties.  Height and Width
    // will be fill-parent, which will be the default for the web viewer.

    followLinks = true;
    HomeUrl("");
    Width(LENGTH_FILL_PARENT);
    Height(LENGTH_FILL_PARENT);
  }

  /**
   * Gets the web view string
   *
   * @return string
   */
  @SimpleProperty(description = "Gets the WebView's String, which is viewable through " +
      "Javascript in the WebView as the window.AppInventor object",
      category = PropertyCategory.BEHAVIOR)
  public String WebViewString() {
    return wvInterface.getWebViewString();
  }

  /**
   * Sets the web view string
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void WebViewString(String newString) {
    wvInterface.setWebViewString(newString);
  }

  @Override
  public View getView() {
    return webview;
  }

  // Create a class so we can override the default link following behavior.
  // The handler doesn't do anything on its own.  But returning true means that
  // this do nothing will override the default WebVew behavior.  Returning
  // false means to let the WebView handle the Url.  In other words, returning
  // true will not follow the link, and returning false will follow the link.
  private class WebViewerClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return !followLinks;
    }
  }

  // Components don't normally override Width and Height, but we do it here so that
  // the automatic width and height will be fill parent.
  @Override
  @SimpleProperty()
  public void Width(int width) {
    if (width == LENGTH_PREFERRED) {
      width = LENGTH_FILL_PARENT;
    }
    super.Width(width);
  }

  @Override
  @SimpleProperty()
  public void Height(int height) {
    if (height == LENGTH_PREFERRED) {
      height = LENGTH_FILL_PARENT;
    }
    super.Height(height);
  }


  /**
   * Returns the URL of the page the WebVewier should load
   *
   * @return URL of the page the WebVewier should load
   */
  @SimpleProperty(
      description = "URL of the page the WebViewer should initially open to.  " +
          "Setting this will load the page.",
      category = PropertyCategory.BEHAVIOR)
  public String HomeUrl() {
    return homeUrl;
  }

  /**
   * Specifies the URL of the page the WebVewier should load
   *
   * @param url URL of the page the WebVewier should load
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty()
  public void HomeUrl(String url) {
    homeUrl = url;
    // clear the history, since changing Home is a kind of reset
    webview.clearHistory();
    webview.loadUrl(homeUrl);
  }

  /**
   * Returns the URL currently being viewed
   *
   * @return URL of the page being viewed
   */
  @SimpleProperty(
      description = "URL of the page currently viewed.   This could be different from the " +
          "Home URL if new pages were visited by following links.",
      category = PropertyCategory.BEHAVIOR)
  public String CurrentUrl() {
    return (webview.getUrl() == null) ? "" : webview.getUrl();
  }

  /**
   * Returns the title of the page currently being viewed
   *
   * @return title of the page being viewed
   */
  @SimpleProperty(
      description = "Title of the page currently viewed",
      category = PropertyCategory.BEHAVIOR)
  public String CurrentPageTitle() {
    return (webview.getTitle() == null) ? "" : webview.getTitle();
  }


  /** Indicates whether to follow links when they are tapped in the WebViewer
   * @return true or false
   */
  @SimpleProperty(
      description = "Determines whether to follow links when they are tapped in the WebViewer.  " +
          "If you follow links, you can use GoBack and GoForward to navigate the browser history. ",
      category = PropertyCategory.BEHAVIOR)
  public boolean FollowLinks() {
    return followLinks;
  }


  /** Determines whether to follow links when they are tapped
   *
   * @param follow
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void FollowLinks(boolean follow) {
    followLinks = follow;
  }


  /**
   * Loads the  page from the home URL.  This happens automatically when
   * home URL is changed.
   */
  @SimpleFunction(
      description = "Loads the home URL page.  This happens automatically when " +
          "the home URL is changed.")
  public void GoHome() {
    webview.loadUrl(homeUrl);
  }

  /**
   *  Go back to the previously viewed page.
   */
  @SimpleFunction(
      description = "Go back to the previous page in the history list.  " +
          "Does nothing if there is no previous page.")
  public void GoBack() {
    if (webview.canGoBack()) {
      webview.goBack();
    }
  }

  /**
   *  Go forward in the history list
   */
  @SimpleFunction(
      description = "Go forward to the next page in the history list.   " +
          "Does nothing if there is no next page.")
  public void GoForward() {
    if (webview.canGoForward()) {
      webview.goForward();
    }
  }

  /**
   *  @return true if the WebViewer can go forward in the history list
   */
  @SimpleFunction(
      description = "Returns true if the WebViewer can go forward in the history list.")
  public boolean CanGoForward() {
    return webview.canGoForward();
  }


  /**
   *  @return true if the WebViewer can go back in the history list
   */
  @SimpleFunction(
      description = "Returns true if the WebViewer can go back in the history list.")
  public boolean CanGoBack() {
    return webview.canGoBack();
  }


  /**
   *  Load the given URL
   */
  @SimpleFunction(
      description = "Load the page at the given URL.")
  public void GoToUrl(String url) {
    webview.loadUrl(url);
  }

  /**
   * Specifies whether or not this WebViewer can access the JavaScript
   * Location API.
   *
   * @param uses -- Whether or not the API is available
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(userVisible = false,
      description = "Whether or not to give the application permission to use the Javascript geolocation API. " +
          "This property is available only in the designer.")
  public void UsesLocation(boolean uses) {
    // We don't actually do anything here (the work is in the MockWebViewer)
  }

  /**
   * Determine if the user should be prompted for permission to use the geolocation API while in
   * the webviewer.
   *
   * @return true if prompting is  required.  False assumes permission is granted.
   */

  @SimpleProperty(description = "If True, then prompt the user of the WebView to give permission to access the geolocation API. " +
      "If False, then assume permission is granted.")
  public boolean PromptforPermission() {
    return prompt;
  }

  /**
   * Determine if the user should be prompted for permission to use the geolocation API while in
   * the webviewer.
   *
   * @param prompt set to true to require prompting. False assumes permission is granted.
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(userVisible = true)
  public void PromptforPermission(boolean prompt) {
    this.prompt = prompt;
  }

  /**
   * Clear Stored Location permissions. When the geolocation API is used in
   * the WebViewer, the end user is prompted on a per URL basis for whether
   * or not permission should be granted to access their location. This
   * function clears this information for all locations.
   *
   * As the permissions interface is not available on phones older then
   * Eclair, this function is a no-op on older phones.
   */

  @SimpleFunction(description = "Clear stored location permissions.")
  public void ClearLocations() {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR)
      EclairUtil.clearWebViewGeoLoc();
  }

  /**
   * Allows the setting of properties to be monitored from the javascript
   * in the WebView
   */
  public class WebViewInterface {
    Context mContext;
    String webViewString;

    /** Instantiate the interface and set the context */
    WebViewInterface(Context c) {
      mContext = c;
      webViewString = " ";
    }

    /**
     * Gets the web view string
     *
     * @return string
     */
    @JavascriptInterface
    public String getWebViewString() {
      return webViewString;
    }

    /**
     * Sets the web view string
     */
    public void setWebViewString(String newString) {
      webViewString = newString;
    }

  }
}

