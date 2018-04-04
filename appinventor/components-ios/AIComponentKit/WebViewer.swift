// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import WebKit
import CoreLocation

open class WebViewer: ViewComponent, AbstractMethodsForViewComponent, WKNavigationDelegate, WKScriptMessageHandler {
  fileprivate var _view : WKWebView
  fileprivate var _homeURL : String = ""
  fileprivate var _webViewString : String = ""
  fileprivate var _ignoreSSLErrors : Bool = false
  fileprivate var _followLinks : Bool = true
  fileprivate var _temporaryLink : String = ""
  fileprivate var _wantLoad = false

  public override init(_ parent: ComponentContainer) {
    var code = ""

    do {
      if let errorPath = Bundle(for: WebViewer.self).url(forResource: "webviewer", withExtension: "js") {
        code = try String(contentsOf: errorPath)
      }
    } catch {}

    let script = WKUserScript(source: code, injectionTime: .atDocumentStart, forMainFrameOnly: false)
    let controller = WKUserContentController()
    controller.addUserScript(script)
    let config = WKWebViewConfiguration()
    config.userContentController = controller
    _view = WKWebView(frame: CGRect.zero, configuration: config)
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.allowsBackForwardNavigationGestures = true
    super.init(parent)
    let swipeRecog = UISwipeGestureRecognizer(target: self, action: #selector(navigation))
    _view.addGestureRecognizer(swipeRecog)
    _view.navigationDelegate = self
    controller.add(self, name: "webString")
    parent.add(self)
  }

  open func CurrentPageTitle() -> String {
    if let title = _view.title {
      return title
    } else {
      return ""
    }
  }

  open func CurrentUrl() -> String {
    if let url = _view.url {
      return url.absoluteString
    } else {
      return ""
    }
  }

  open var FollowLinks : Bool {
    get {
      return _followLinks
    }
    set (newVal) {
      _followLinks = newVal
    }
  }

  open var HomeUrl : String {
    get {
      return _homeURL
    }
    set(url) {
      _homeURL = url
      processURL(url)
    }
  }

  open var IgnoreSslErrors : Bool {
    get {
      return _ignoreSSLErrors
    }
    set(newVal) {
      _ignoreSSLErrors = newVal
    }
  }

  //iOS will prompt for location services whenever a specific site asks
  open var PromptforPermission : Bool {
    get {
      return true
    }
    set(newVal){
      _container.form.dispatchErrorOccurredEvent(self, "PromptForPermission", ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.code, ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.message)
    }
  }

  //iOS will prompt for location services, so status cannot be set programmatically
  open var UsesLocation : Bool {
    get {
      switch CLLocationManager.authorizationStatus() {
      case .notDetermined, .denied, .restricted: return false
      case .authorizedAlways, .authorizedWhenInUse: return true
      }
    }
    set(newVal){
      _container.form.dispatchErrorOccurredEvent(self, "UsesLocation", ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.code, ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.message)
    }
  }

  open var WebViewString : String {
    get {
      return _webViewString
    }
    set (newVal) {
      _webViewString = newVal
      _view.evaluateJavaScript("window.AppInventor.updateFromBlocks('\(newVal)')");
    }
  }

  //NOTE: To load file, assumes the presence of an "assets" folder
  fileprivate func processURL(_ url: String){
    _wantLoad = true
    if url.starts(with: "file:///android_asset/"), let fileURL = URL(string: url) {
      if let assetURL = Bundle.main.url(forResource: fileURL.lastPathComponent, withExtension: nil, subdirectory: "assets"){
        _view.loadFileURL(assetURL, allowingReadAccessTo: assetURL.deletingLastPathComponent())
      } else {
        _container.form.dispatchErrorOccurredEvent(self, "WebViewer", ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.code, ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.message)
      }
    } else if url.starts(with: "file:///mnt/sdcard") {
      if let fileURL = URL(string: url){
        do {
          let assetURL = try FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false).appendingPathComponent("AppInventor/\(fileURL.lastPathComponent)")
          _view.loadFileURL(assetURL, allowingReadAccessTo: assetURL.deletingLastPathComponent())
        } catch {
          _container.form.dispatchErrorOccurredEvent(self, "WebViewer", ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.code, ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.message)
        }
      } else {
        _container.form.dispatchErrorOccurredEvent(self, "WebViewer", ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.code, ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.message)
      }
    }
    else if let newUrl = URL(string: url) {
      _view.load(URLRequest(url: newUrl))
    } else {
      do {
        if let errorPath = Bundle(for: WebViewer.self).url(forResource: "webview-error", withExtension: "html") {
          let contents = try String(format: String(contentsOf: errorPath), "An error occured when trying to load", url, "The specified url is invalid. Please check the URL.")
          _view.loadHTMLString(contents, baseURL: nil)
        }
      } catch {}
    }
  }

  open func CanGoBack() -> Bool {
    return _view.canGoBack
  }

  open func CanGoForward() -> Bool {
    return _view.canGoForward
  }

  open func ClearCaches() {
    _view.configuration.websiteDataStore.removeData(ofTypes: Set([WKWebsiteDataTypeDiskCache, WKWebsiteDataTypeMemoryCache, WKWebsiteDataTypeOfflineWebApplicationCache]), modifiedSince: Date(timeIntervalSince1970: 0), completionHandler: {})
  }

  //does nothing, as you cannot programmatically revoke location permissions
  open func ClearLocations() {
    _container.form.dispatchErrorOccurredEvent(self, "ClearLocations", ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.code, ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.message)
  }

  open func GoBack() {
    _view.goBack()
  }

  open func GoForward() {
    _view.goForward()
  }

  open func GoHome() {
    processURL(_homeURL)
  }

  open func GoToUrl(_ url: String) {
    processURL(url)
  }

  open func webView(_ webView: WKWebView, didFail: WKNavigation!, withError: Error) {
    _container.form.dispatchErrorOccurredEvent(self, "WebViewer", ErrorMessage.ERROR_WEB_VIEWER_UNKNOWN_ERROR.code, withError.localizedDescription)
  }

  //TODO: add support for other languages (for the generic error message)
  open func webView(_ webView: WKWebView, didFailProvisionalNavigation: WKNavigation!, withError: Error) {
    if (withError as NSError).code != NSURLErrorCancelled {
      _wantLoad = true
      do {
        if let errorPath = Bundle(for: WebViewer.self).url(forResource: "webview-error", withExtension: "html") {
          let contents = try String(format: String(contentsOf: errorPath), "An error occured when trying to load", _temporaryLink, withError.localizedDescription)
          _view.loadHTMLString(contents, baseURL: nil)
        }
      } catch {}
    }
  }

  open func webView(_ webView: WKWebView, didReceive challenge: URLAuthenticationChallenge, completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
    if _ignoreSSLErrors {
      let cred = URLCredential(trust: challenge.protectionSpace.serverTrust!)
      completionHandler(.useCredential, cred)
    } else {
      completionHandler(.performDefaultHandling, nil)
    }
  }

  open func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
    if let url = navigationAction.request.url?.absoluteString {
      _temporaryLink = url
    }
    decisionHandler((_followLinks || _wantLoad) ? .allow: .cancel)
  }

  open func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
    _wantLoad = false
  }


  open func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
    if let string = message.body as? String {
      _webViewString = string
    }
  }

  open func navigation(gesture: UISwipeGestureRecognizer) {
    let isRTL = UIView.userInterfaceLayoutDirection(for: _view.semanticContentAttribute) == UIUserInterfaceLayoutDirection.rightToLeft
    switch gesture.direction {
    case UISwipeGestureRecognizerDirection.right: isRTL ? _view.goForward(): _view.goBack()
    case UISwipeGestureRecognizerDirection.left: isRTL ? _view.goBack(): _view.goForward()
    default: break
    }
  }

  open override var view: UIView {
    get {
      return _view
    }
  }
}

