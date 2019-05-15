// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit
import WebKit

/// NOTE: There's a currently a bug where the UIWebView will intercept touches, even though it is
///       not in view.  Ie. if a UIWebView overlaps with UIButtons.
@available(iOS 11.3, *)
open class WebViewNode: ARNodeBase, ARWebView {
  var _webViewNode: SCNNode
  var _webViewGeometry: SCNPlane = SCNPlane(width: 0.125, height: 0.175)
  var _webView: UIWebView = UIWebView(frame: CGRect(x: 0, y: 0, width: 400, height: 672))
  var _url: String = ""
  
  @objc init(_ container: ARNodeContainer) {
    self._webViewNode = SCNNode(geometry: self._webViewGeometry)
    super.init(container: container, node: self._webViewNode)
    DispatchQueue.main.async {
      self.setupWebNode()
    }
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupWebNode() {
    DispatchQueue.main.async {
      self._webViewGeometry.firstMaterial?.diffuse.contents = self._webView
      self._webViewGeometry.firstMaterial?.isDoubleSided = true
    }
  }
  
  // MARK: Properties
  
  @objc open var HomeUrl: String {
    get {
      return _url
    }
    set(urlString) {
      _url = urlString
      if let url = Foundation.URL(string: urlString) {
        let request = URLRequest(url: url)
        DispatchQueue.main.async {
          self._webView.loadRequest(request)
        }
      } else {
        _container?.form?.dispatchErrorOccurredEvent(self, "HomeUrl", ErrorMessage.ERROR_WEBVIEWNODE_MALFORMED_URL.code)
      }
    }
  }
  
  open var isUserInteractionEnabled: Bool {
    get {
      return _webView.isUserInteractionEnabled
    }
    set(enabled) {
      _webView.isUserInteractionEnabled = enabled
    }
  }
  
  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_webViewGeometry.width)
    }
    set(width) {
      _webViewGeometry.width = UnitHelper.centimetersToMeters(abs(width))
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_webViewGeometry.height)
    }
    set(height) {
      _webViewGeometry.height = UnitHelper.centimetersToMeters(abs(height))
    }
  }
  
  // FillColor is not user accessible
  @objc open override var FillColor: Int32 {
    get {
      return 0
    }
    set(color) {}
  }
  
  // Texture is not user accessible
  @objc open override var Texture: String {
    get {
      return ""
    }
    set(path) {}
  }
  
  // TextureOpacity is not user accessible
  @objc open override var TextureOpacity: Int32 {
    get {
      return 1
    }
    set(opacity) {}
  }
  
  // MARK: Methods
  
  @objc open func CanGoBack() -> Bool {
    return _webView.canGoBack
  }
  
  @objc open func CanGoForward() -> Bool {
    return _webView.canGoForward
  }
  
  @objc open func GoBack() {
    if _webView.canGoBack {
      _webView.goBack()
      _webView.reload()
    }
  }
  
  @objc open func GoForward() {
    if _webView.canGoForward {
      _webView.goForward()
      _webView.reload()
    }
  }
  
  @objc open func Reload() {
    _webView.reload()
  }
  
  @objc open func GoToUrl(_ url: String) {
    if let url = Foundation.URL(string: url) {
      let request = URLRequest(url: url)
      DispatchQueue.main.async {
        self._webView.loadRequest(request)
      }
    }
  }
  
  @objc open func GoHome() {
    if let url = Foundation.URL(string: _url) {
      let request = URLRequest(url: url)
      DispatchQueue.main.async {
        self._webView.loadRequest(request)
      }
    }
  }
}
