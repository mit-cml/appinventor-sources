// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import WebKit
import CoreLocation

open class WebViewer: ViewComponent, AbstractMethodsForViewComponent, WKUIDelegate, WKNavigationDelegate, WKScriptMessageHandler, WKURLSchemeHandler {
  fileprivate var _view : WKWebView!
  fileprivate var _homeURL : String = ""
  fileprivate var _webViewString : String = ""
  fileprivate var _ignoreSSLErrors : Bool = false
  fileprivate var _followLinks : Bool = true
  fileprivate var _temporaryLink : String = ""
  fileprivate var _wantLoad = false
  private var _webviewerApiSource: String = ""
  private var _webviewerApi: WKUserScript
  private var _webAlert: UIAlertController? = nil
  var aiSchemeHandler: WKURLSchemeHandler? = nil

  public override init(_ parent: ComponentContainer) {
    do {
      if let errorPath = Bundle(for: WebViewer.self).url(forResource: "webviewer", withExtension: "js") {
        _webviewerApiSource = try String(contentsOf: errorPath)
      }
    } catch {}

    _webviewerApi = WKUserScript(source: _webviewerApiSource,
        injectionTime: .atDocumentStart, forMainFrameOnly: false)
    let controller = WKUserContentController()
    controller.addUserScript(_webviewerApi)
    let config = WKWebViewConfiguration()
    config.preferences.javaScriptEnabled = true
    config.allowsInlineMediaPlayback = true
    config.mediaTypesRequiringUserActionForPlayback =  []
    config.userContentController = controller
    super.init(parent)
    config.setURLSchemeHandler(self, forURLScheme: "appinventor")
    _view = WKWebView(frame: CGRect.zero, configuration: config)
#if DEBUG
    if #available(iOS 16.4, *) {
      _view.isInspectable = true
    }
#endif
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.allowsBackForwardNavigationGestures = true
    let swipeRecog = UISwipeGestureRecognizer(target: self, action: #selector(navigation))
    _view.addGestureRecognizer(swipeRecog)
    _view.navigationDelegate = self
    _view.uiDelegate = self
    controller.add(self, name: "webString")
    parent.add(self)
    Width = kLengthPreferred
    Height = kLengthPreferred
  }

  @objc open func Initialize() {
    if _lastSetWidth == kLengthPreferred {
      Width = kLengthFillParent
    }
    if _lastSetHeight == kLengthPreferred {
      Height = kLengthFillParent
    }
  }

  @objc open func CurrentPageTitle() -> String {
    if let title = _view.title {
      return title
    } else {
      return ""
    }
  }

  @objc open func CurrentUrl() -> String {
    if let url = _view.url {
      return url.absoluteString
    } else {
      return ""
    }
  }

  @objc open var FollowLinks : Bool {
    get {
      return _followLinks
    }
    set (newVal) {
      _followLinks = newVal
    }
  }

  @objc open var HomeUrl : String {
    get {
      return _homeURL
    }
    set(url) {
      _homeURL = url
      processURL(url)
    }
  }

  @objc open var IgnoreSslErrors : Bool {
    get {
      return _ignoreSSLErrors
    }
    set(newVal) {
      _ignoreSSLErrors = newVal
    }
  }

  //iOS will prompt for location services whenever a specific site asks
  @objc open var PromptforPermission : Bool {
    get {
      return true
    }
    set(newVal){
      form?.dispatchErrorOccurredEvent(self, "PromptForPermission",
          ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.code,
          ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.message)
    }
  }

  //iOS will prompt for location services, so status cannot be set programmatically
  @objc open var UsesLocation : Bool {
    get {
      switch CLLocationManager.authorizationStatus() {
      case .notDetermined, .denied, .restricted: return false
      case .authorizedAlways, .authorizedWhenInUse: return true
      @unknown default:
        return false
      }
    }
    set(newVal){
      form?.dispatchErrorOccurredEvent(self, "UsesLocation",
          ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.code,
          ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.message)
    }
  }

  @objc open var WebViewString : String {
    get {
      return _webViewString
    }
    set (newVal) {
      _webViewString = newVal
      let escapedValue = newVal.replace(target: "\\", withString: "\\\\")
        .replace(target: "'", withString: "\\'")
        .replace(target: "\r", withString: "\\r")
        .replace(target: "\n", withString: "\\n")
        .replace(target: "\t", withString: "\\t")
      let script = _webviewerApiSource.replace(target: "\"\"",
          withString: "'\(escapedValue)'")
      _webviewerApi = WKUserScript(source: script, injectionTime: .atDocumentStart,
          forMainFrameOnly: false)
      _view.configuration.userContentController.removeAllUserScripts()
      _view.configuration.userContentController.addUserScript(_webviewerApi)
      _view.evaluateJavaScript("window.AppInventor.updateFromBlocks('\(newVal)')")
    }
  }

  //NOTE: To load file, assumes the presence of an "assets" folder
  fileprivate func processURL(_ url: String){
    _wantLoad = true
    let url = url.addingPercentEncoding(withAllowedCharacters: CharacterSet.urlQueryAllowed) ?? ""
    let firstHashTag = url.firstIndex(of: "#") ?? url.endIndex
    let firstQuestionMark = url.firstIndex(of: "?") ?? firstHashTag
    let queryParameters = url.suffix(from: firstQuestionMark)
    if url.starts(with: "file:///android_asset/") || url.starts(with: "http://localhost/"),
        let fileURL = URL(string: url) {
      let assetPath = AssetManager.shared.pathForExistingFileAsset(fileURL.lastPathComponent)
      if !assetPath.isEmpty {
        let assetURL = URL(fileURLWithPath: assetPath)
        let url2: NSURL?
        if queryParameters.isEmpty {
          url2 = assetURL as NSURL
        } else {
          url2 = NSURL(string: "\(assetURL)?\(queryParameters)")
        }
        guard let destinationUrl = url2 as? URL else {
          form?.dispatchErrorOccurredEvent(self, "WebViewer",
              ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.code,
              ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.message)
          return
        }
        _view.loadFileURL(destinationUrl, allowingReadAccessTo: destinationUrl.deletingLastPathComponent())
      } else {
        form?.dispatchErrorOccurredEvent(self, "WebViewer",
            ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.code,
            ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.message)
      }
    } else if url.starts(with: "file:///mnt/sdcard") || url.starts(with: "file:///sdcard") {
      if let fileURL = URL(string: url){
        do {
          let assetURL = try FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false).appendingPathComponent("AppInventor/\(fileURL.lastPathComponent)")
          let url2 = NSURL(string: String(queryParameters), relativeTo: assetURL)!
          _view.load(NSURLRequest(url: url2 as URL) as URLRequest)
        } catch {
          form?.dispatchErrorOccurredEvent(self, "WebViewer",
              ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.code,
              ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.message)
        }
      } else {
        form?.dispatchErrorOccurredEvent(self, "WebViewer",
            ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.code,
            ErrorMessage.ERROR_WEB_VIEWER_MISSING_FILE.message)
      }
    } else if let newUrl = URL(string: url) {
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

  @objc open func CanGoBack() -> Bool {
    return _view.canGoBack
  }

  @objc open func CanGoForward() -> Bool {
    return _view.canGoForward
  }

  @objc open func ClearCaches() {
    _view.configuration.websiteDataStore.removeData(ofTypes: Set([WKWebsiteDataTypeDiskCache, WKWebsiteDataTypeMemoryCache, WKWebsiteDataTypeOfflineWebApplicationCache]), modifiedSince: Date(timeIntervalSince1970: 0), completionHandler: {})
  }

  //does nothing, as you cannot programmatically revoke location permissions
  @objc open func ClearLocations() {
    form?.dispatchErrorOccurredEvent(self, "ClearLocations",
        ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.code,
        ErrorMessage.ERROR_WEB_VIEWER_UNSUPPORTED_METHOD.message)
  }

  @objc open func GoBack() {
    _view.goBack()
  }

  @objc open func GoForward() {
    _view.goForward()
  }

  @objc open func GoHome() {
    processURL(_homeURL)
  }

  @objc open func GoToUrl(_ url: String) {
    processURL(url)
  }

  //reload
  @objc open func Reload() {
    _view.reload()
  }

  //Stoploading
  @objc open func StopLoading() {
    _view.stopLoading()
  }

  //ClearCookies
  @objc open func ClearCookies() {
    let dataStore = _view.configuration.websiteDataStore
    let dataTypes = Set([WKWebsiteDataTypeCookies])

    dataStore.fetchDataRecords(ofTypes: dataTypes) { records in
      dataStore.removeData(ofTypes: dataTypes, for: records) {
      }
    }
  }

  //RunJavaScript
  @objc open func RunJavaScript(_ js: String) {
    _view.evaluateJavaScript(js) { (result, error) in
      if let error = error {
        // Handle the JavaScript evaluation error
        print("JavaScript evaluation error: \(error)")
      } else if let result = result {
        // Handle the JavaScript result
        print("JavaScript result: \(result)")
      }
    }
  }

  @objc open func WebViewStringChange(_ value: String) {
    EventDispatcher.dispatchEvent(of: self, called: "WebViewStringChange", arguments: value as NSString)
  }

  @objc open func PageLoaded(_ url: String) {
    EventDispatcher.dispatchEvent(of: self, called: "PageLoaded", arguments: url as NSString)
  }
  
  open func webView(_ webView: WKWebView, didFail: WKNavigation!, withError: Error) {
    form?.dispatchErrorOccurredEvent(self, "WebViewer",
        ErrorMessage.ERROR_WEB_VIEWER_UNKNOWN_ERROR.code, withError.localizedDescription)
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
    // We want to load the page if any of the following is true:
    // 1. FollowLinks is turned on
    // 2. The blocks have explicitly requested a URL be loaded
    // 3. We are a WebView running AI extensions
    // Otherwise, do not load the page
    decisionHandler((_followLinks || _wantLoad || aiSchemeHandler != nil) ? .allow: .cancel)
  }

  open func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
    let url = webView.url?.absoluteString ?? ""
    DispatchQueue.main.async {
      self.PageLoaded(url)
    }
    _wantLoad = false
  }

  open func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage message: String,
                    initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
    self._webAlert = UIAlertController(title: message,
                                       message: nil,
                                       preferredStyle: .alert)
    self._webAlert?.addAction(UIAlertAction(title: "OK", style: .cancel) {
      _ in completionHandler()}
    )

    _container?.form?.present(self._webAlert!, animated: true)
  }

  open func webView(_ webView: WKWebView, runJavaScriptConfirmPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo,          completionHandler: @escaping (Bool) -> Void) {
    self._webAlert = UIAlertController(title: message, message: nil, preferredStyle: .actionSheet)
    self._webAlert?.addAction(UIAlertAction(title: "Ok", style: .default, handler: { (action) in
      completionHandler(true)
    }))
    self._webAlert?.addAction(UIAlertAction(title: "Cancel", style: .default, handler: { (action) in
      completionHandler(false)
    }))

    _container?.form?.present(self._webAlert!, animated: true)
  }

  open func webView(_ webView: WKWebView, runJavaScriptTextInputPanelWithPrompt prompt: String, defaultText: String?,
                    initiatedByFrame frame: WKFrameInfo,
                    completionHandler: @escaping (String?) -> Void) {
    self._webAlert = UIAlertController(title: nil, message: prompt, preferredStyle: .alert)
    self._webAlert?.addTextField { (textField) in
      textField.text = defaultText
    }
    self._webAlert!.addAction(UIAlertAction(title: "Ok", style: .default, handler: { (action) in
      if let text = self._webAlert?.textFields?.first?.text {
        completionHandler(text)
      } else {
        completionHandler(defaultText)
      }
    }))
    self._webAlert?.addAction(UIAlertAction(title: "Cancel", style: .default, handler: { (action) in
      completionHandler(nil)
    }))
    _container?.form?.present(self._webAlert!, animated: true)
  }

  open func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
    guard message.name == "webString" else {
      return  // Message intended for someone else...
    }
    if let string = message.body as? String {
      _webViewString = string
      DispatchQueue.main.async {
        self.WebViewStringChange(string)
      }
    }
  }

  @objc open func navigation(gesture: UISwipeGestureRecognizer) {
    let isRTL = UIView.userInterfaceLayoutDirection(for: _view.semanticContentAttribute) == UIUserInterfaceLayoutDirection.rightToLeft
    switch gesture.direction {
    case UISwipeGestureRecognizer.Direction.right: _ = (isRTL ? _view.goForward(): _view.goBack())
    case UISwipeGestureRecognizer.Direction.left: _ = (isRTL ? _view.goBack(): _view.goForward())
    default: break
    }
  }

  open override var view: UIView {
    get {
      return _view
    }
  }

  // MARK: WKURLSchemeHandler implementation

  public func webView(_ webView: WKWebView, start urlSchemeTask: any WKURLSchemeTask) {
    aiSchemeHandler?.webView(webView, start: urlSchemeTask)
  }

  public func webView(_ webView: WKWebView, stop urlSchemeTask: any WKURLSchemeTask) {
    aiSchemeHandler?.webView(webView, stop: urlSchemeTask)
  }
}
