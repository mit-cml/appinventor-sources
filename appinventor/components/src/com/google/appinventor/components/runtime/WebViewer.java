// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.EclairUtil;
import com.google.appinventor.components.runtime.util.FroyoUtil;
import com.google.appinventor.components.runtime.util.FroyoWebViewClient;
import com.google.appinventor.components.runtime.util.HoneycombWebViewClient;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;


/**
 * Component for viewing Web pages.
 *
 * ![WebViewer icon](images/webviewer.png)
 *
 * The {@link #HomeUrl()} can be specified in the Designer or in the Blocks Editor. The view can be
 * set to follow links when they are tapped, and users can fill in Web forms.
 *
 * **Warning:** This is not a full browser. For example, pressing the phone's hardware Back key
 * will exit the app, rather than move back in the browser history.
 *
 * You can use the {@link #WebViewString(String)} property to communicate between your app and
 * Javascript code running in the `WebViewer` page. In the app, you get and set
 * {@link #WebViewString(String)}. In the `WebViewer`, you include Javascript that references the
 * `window.AppInventor` object, using the methods `getWebViewString()` and `setWebViewString(text)`.
 *
 * For example, if the `WebViewer` opens to a page that contains the Javascript command
 * ```javascript
 * document.write("The answer is" + window.AppInventor.getWebViewString());
 * ```
 * and if you set {@link #WebViewString(String)} to "hello", then the web page will show
 * ```
 * The answer is hello.
 * ```
 * And if the Web page contains Javascript that executes the command
 * ```javascript
 * windowAppInventor.setWebViewString("hello from Javascript"),
 * ```
 * then the value of the {@link #WebViewString()} property will be
 * ```
 * hello from Javascript.
 * ```
 * Calling `setWebViewString` from JavaScript will also run the {@link #WebViewStringChange(String)}
 * event so that the blocks can handle when the {@link #WebViewString(String)} property changes.
 *
 * Beginning with release nb184a, you can specify a HomeUrl beginning with `http://localhost/`
 * to reference assets both in the Companion and in compiled apps. Previously, apps needed to use
 * `file:///android_asset/` in compiled apps and `/sdcard/AppInventor/assets/` in the Companion.
 * Both of these options will continue to work but the `http://localhost/` approach will work in
 * both scenarios. You may also use "file:///appinventor_asset/" which provides more security by
 * preventing the use of asynchronous requests from JavaScript in your assets from going out to the
 * web.
 *
 * @internaldoc
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
        "browser history." +
        "<p />You can use the WebViewer.WebViewString property to communicate " +
        "between your app and Javascript code running in the Webviewer page. " +
        "In the app, you get and set WebViewString.  " +
        "In the WebViewer, you include Javascript that references the window.AppInventor " +
        "object, using the methoods </em getWebViewString()</em> and <em>setWebViewString(text)</em>.  " +
        "<p />For example, if the WebViewer opens to a page that contains the Javascript command " +
        "<br /> <em>document.write(\"The answer is\" + window.AppInventor.getWebViewString());</em> " +
        "<br />and if you set WebView.WebVewString to \"hello\", then the web page will show " +
        "</br ><em>The answer is hello</em>.  " +
        "<br />And if the Web page contains Javascript that executes the command " +
        "<br /><em>window.AppInventor.setWebViewString(\"hello from Javascript\")</em>, " +
        "<br />then the value of the WebViewString property will be " +
        "<br /><em>hello from Javascript</em>. ",
    iconName = "images/webviewer.png")

// TODO(halabelson): Integrate control of the Back key, when we provide it

@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class WebViewer extends AndroidViewComponent {

  private final WebView webview;

  // URL for the WebViewer to load initially
  private String homeUrl;

  // whether or not to follow links when they are tapped
  private boolean followLinks = true;

  // Whether or not to prompt for permission in the WebViewer
  private boolean prompt = true;

  // ignore SSL Errors (mostly certificate errors. When set
  // self signed certificates should work.

  private boolean ignoreSslErrors = false;

  // allows passing strings to javascript
  WebViewInterface wvInterface;

  // Flag to mark whether we have received permission to read external storage
  private boolean havePermission = false;

  /**
   * Creates a new WebViewer component.
   *
   * @param container  container the component will be placed in
   */
  @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
  public WebViewer(ComponentContainer container) {
    super(container);

    webview = new WebView(container.$context());
    resetWebViewClient();       // Set up the web view client
    final WebSettings settings = webview.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setAllowFileAccess(true);
    webview.setFocusable(true);
    // adds a way to send strings to the javascript
    wvInterface = new WebViewInterface();
    webview.addJavascriptInterface(wvInterface, "AppInventor");
    // enable pinch zooming and zoom controls
    settings.setBuiltInZoomControls(true);

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

    HomeUrl("");
    Width(LENGTH_FILL_PARENT);
    Height(LENGTH_FILL_PARENT);
  }

  /**
   * Gets the `WebView`'s String, which is viewable through Javascript in the `WebView` as the
   * `window.AppInventor` object.
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
   *
   * @suppressdoc
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void WebViewString(String newString) {
    wvInterface.setWebViewStringFromBlocks(newString);
  }

  @Override
  public View getView() {
    return webview;
  }

  // Create a class so we can override the default link following behavior.
  // The handler doesn't do anything on its own.  But returning true means that
  // this do nothing will override the default WebView behavior.  Returning
  // false means to let the WebView handle the Url.  In other words, returning
  // true will not follow the link, and returning false will follow the link.
  private class WebViewerClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return !followLinks;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      BeforePageLoad(url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      PageLoaded(url);
    }

    @Override
    public void onReceivedError(WebView view, final int errorCode, final String description, final String failingUrl) {
      container.$form().runOnUiThread(new Runnable() {
        public void run() {
          ErrorOccurred(errorCode, description, failingUrl);
        }
      });
    }
  }

  // Components don't normally override Width and Height, but we do it here so that
  // the automatic width and height will be fill parent.

  /**
   * Specifies the horizontal width of the `%type%`, measured in pixels.
   * @param  width in pixels
   */
  @Override
  @SimpleProperty()
  public void Width(int width) {
    if (width == LENGTH_PREFERRED) {
      width = LENGTH_FILL_PARENT;
    }
    super.Width(width);
  }

  /**
   * Specifies the `%type%`'s vertical height, measured in pixels.
   * @param  height in pixels
   */
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
   * Specifies the URL of the page the `WebViewer` should initially open to. Setting this will
   * load the page.
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
    loadUrl("HomeUrl", homeUrl);
  }

  /**
   * Returns the URL currently being viewed. This could be different from the {@link #HomeUrl()}
   * if new pages were visited by following links.
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


  /**
   * Determines whether to follow links when they are tapped in the `WebViewer`. If you follow
   * links, you can use {@link #GoBack()} and {@link #GoForward()} to navigate the browser history.
   * @param follow
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void FollowLinks(boolean follow) {
    followLinks = follow;
    resetWebViewClient();
  }

  /**
   * Determine whether or not to ignore SSL errors. Set to `true`{:.logic.block} to ignore errors.
   * Use this to accept self signed certificates from websites.
   *
   * @return true or false
   *
   */
  @SimpleProperty(
      description = "Determine whether or not to ignore SSL errors. Set to true to ignore " +
          "errors. Use this to accept self signed certificates from websites.",
      category = PropertyCategory.BEHAVIOR)
  public boolean IgnoreSslErrors() {
    return ignoreSslErrors;
  }

  /**
   * Determines whether or not to ignore SSL Errors
   *
   * @suppressdoc
   * @param ignoreSslErrors set to true to ignore SSL errors
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty()
  public void IgnoreSslErrors(boolean ignoreSslErrors) {
    this.ignoreSslErrors = ignoreSslErrors;
    resetWebViewClient();
  }

  /**
   * Loads the  page from the home URL.  This happens automatically when
   * home URL is changed.
   */
  @SimpleFunction(
      description = "Loads the home URL page.  This happens automatically when " +
          "the home URL is changed.")
  public void GoHome() {
    loadUrl("GoHome", homeUrl);
  }

  /**
   * Go back to the previous page in the history list. Does nothing if there is no previous page.
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
   * Go forward to the next page in the history list. Does nothing if there is no next page.
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
   * Load the page at the given URL.
   */
  @SimpleFunction(
      description = "Load the page at the given URL.")
  public void GoToUrl(String url) {
    loadUrl("GoToUrl", url);
  }

  /**
   * Stop loading a page.
   */
  @SimpleFunction(
      description = "Stop loading a page.")
  public void StopLoading() {
    webview.stopLoading();
  }

  /**
   * Reload the current page.
   */
  @SimpleFunction(
      description = "Reload the current page.")
  public void Reload() {
    webview.reload();
  }

  /**
   * Specifies whether or not this `WebViewer` can access the JavaScript
   * geolocation API.
   *
   * @param uses -- Whether or not the API is available
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(userVisible = false,
      description = "Whether or not to give the application permission to use the Javascript geolocation API. " +
          "This property is available only in the designer.",
      category = PropertyCategory.BEHAVIOR)
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
   * the `WebViewer`. If `true`{:.logic.block}, prompt the user of the `WebViewer` to give
   * permission to access the geolocation API. If `false`{:.logic.block}, assume permission is
   * granted.
   *
   * @param prompt set to true to require prompting. False assumes permission is granted.
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(userVisible = true, category = PropertyCategory.BEHAVIOR)
  public void PromptforPermission(boolean prompt) {
    this.prompt = prompt;
  }

  /**
   * Clear Stored Location permissions. When the geolocation API is used in
   * the `WebViewer`, the end user is prompted on a per URL basis for whether
   * or not permission should be granted to access their location. This
   * function clears this information for all locations.
   *
   *  As the permissions interface is not available on phones older then
   *  Eclair, this function is a no-op on older phones.
   */
  @SimpleFunction(description = "Clear stored location permissions.")
  public void ClearLocations() {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR)
      EclairUtil.clearWebViewGeoLoc();
  }

  private void resetWebViewClient() {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB) {
      webview.setWebViewClient(new HoneycombWebViewClient(followLinks, ignoreSslErrors,
          container.$form(), this));
    } else if (SdkLevel.getLevel() >= SdkLevel.LEVEL_FROYO) {
      webview.setWebViewClient(new FroyoWebViewClient<>(followLinks, ignoreSslErrors,
          container.$form(), this));
    } else {
      webview.setWebViewClient(new WebViewerClient());
    }
  }

  /**
   * Clear the internal webview cache, both ram and disk. This is useful
   * when using the `WebViewer` to poll a page that may not be sending
   * appropriate cache control headers.
   */
  @SimpleFunction(description = "Clear WebView caches.")
  public void ClearCaches() {
    webview.clearCache(true);
  }

   /**
   * Clear the webview's cookies. This is useful if you want to
   * sign the user out of a website that uses them to store logins.
   */
  @SimpleFunction(description = "Clear WebView cookies.")
  public void ClearCookies() {
    CookieManager cookieManager = CookieManager.getInstance();
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_LOLLIPOP) {
      cookieManager.removeAllCookies(null);
    } else {
      cookieManager.removeAllCookie();
    }
  }

   /**
   * Run JavaScript in the current page.
   */
  @SimpleFunction(description = "Run JavaScript in the current page.")
  public void RunJavaScript(String js) {
    // evaluateJavascript() was added in API 19
    // and is therefore not used here for compatibility purposes.
    webview.loadUrl("javascript:(function(){" + js + "})()");
  }

  /**
   * Event that runs when the `AppInventor.setWebViewString` method is called from JavaScript.
   * The new {@link #WebViewString()} is given by the `value`{:.variable.block} parameter.
   * @param value
   */
  @SimpleEvent(description = "When the JavaScript calls AppInventor.setWebViewString this event is run.")
  public void WebViewStringChange(String value) {
    EventDispatcher.dispatchEvent(this, "WebViewStringChange", value);
  }

  @SimpleEvent(description = "When a page is about to load this event is run.")
  public void BeforePageLoad(String url) {
    EventDispatcher.dispatchEvent(this, "BeforePageLoad", url);
  }

  @SimpleEvent(description = "When a page is finished loading this event is run.")
  public void PageLoaded(String url) {
    EventDispatcher.dispatchEvent(this, "PageLoaded", url);
  }

  @SimpleEvent(description = "When an error occurs this event is run.")
  public void ErrorOccurred(int errorCode, String description, String failingUrl) {
    EventDispatcher.dispatchEvent(this, "ErrorOccurred", errorCode, description, failingUrl);
  }

  private void loadUrl(final String caller, final String url) {
    if (!havePermission && MediaUtil.isExternalFileUrl(container.$form(), url)) {
      container.$form().askPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
          new PermissionResultHandler() {
            @Override
            public void HandlePermissionResponse(String permission, boolean granted) {
              if (granted) {
                havePermission = true;
                webview.loadUrl(url);
              } else {
                container.$form().dispatchPermissionDeniedEvent(WebViewer.this, caller,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
              }
            }
          });
      return;
    }
    webview.loadUrl(url);
  }

  /**
   * Allows the setting of properties to be monitored from the javascript
   * in the WebView
   */
  public class WebViewInterface {
    String webViewString;

    /** Instantiate the interface */
    WebViewInterface() {
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
    @JavascriptInterface
    public void setWebViewString(final String newString) {
      webViewString = newString;

      container.$form().runOnUiThread(new Runnable() {
        public void run() {
          WebViewStringChange(newString);
        }
      });
    }

    public void setWebViewStringFromBlocks(final String newString) {
      webViewString = newString;
    }

  }
}

