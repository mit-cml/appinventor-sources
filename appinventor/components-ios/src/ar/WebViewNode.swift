// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import WebKit
import UIKit

/// NOTE: There's a currently a bug where the UIWebView will intercept touches, even though it is
///       not in view.  Ie. if a UIWebView overlaps with UIButtons.
@available(iOS 14.0, *)
open class WebViewNode: ARNodeBase, ARWebView {
  private var _webView: WKWebView = WKWebView(frame: CGRect(x: 0, y: 0, width: 400, height: 672))
  private var _url: String = ""
  private var _width: Float = 0.125
  private var _height: Float = 0.175
  
  @objc init(_ container: ARNodeContainer) {
    // Create a plane mesh for the web view
    let mesh = MeshResource.generatePlane(width: 0.125, depth: 0.175)
    super.init(container: container, mesh: mesh)
    
    DispatchQueue.main.async {
      self.setupWebEntity()
    }
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupWebEntity() {
    DispatchQueue.main.async {
      // Create material with web view content
      self.updateWebViewTexture()
    }
  }
  
  private func updateWebViewTexture() {
    // Convert UIView to texture
    guard let image = webViewToImage() else { return }
    
    do {
      var material = SimpleMaterial()
      
      if #available(iOS 15.0, *) {
        let texture = try TextureResource.generate(from: image.cgImage!, options: .init(semantic: .color))
        material.baseColor = MaterialColorParameter.texture(texture)
      } else {
        //fail gracefully
      }

      
      _modelEntity.model?.materials = [material]
    } catch {
      print("Failed to create texture from web view: \(error)")
    }
  }
  
  private func webViewToImage() -> UIImage? {
    let renderer = UIGraphicsImageRenderer(bounds: _webView.bounds)
    return renderer.image { context in
      _webView.layer.render(in: context.cgContext)
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
          self._webView.load(request)
          // Update texture after loading
          DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            self.updateWebViewTexture()
          }
        }
      } else {
        _container?.form?.dispatchErrorOccurredEvent(self, "HomeUrl", ErrorMessage.ERROR_WEBVIEWNODE_MALFORMED_URL.code)
      }
    }
  }
  
  @objc open var isUserInteractionEnabled: Bool {
    get {
      return _webView.isUserInteractionEnabled
    }
    set(enabled) {
      _webView.isUserInteractionEnabled = enabled
    }
  }
  
  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_width)
    }
    set(width) {
      _width = UnitHelper.centimetersToMeters(abs(width))
      updateMesh()
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_height)
    }
    set(height) {
      _height = UnitHelper.centimetersToMeters(abs(height))
      updateMesh()
    }
  }
  
  private func updateMesh() {
    let mesh = MeshResource.generatePlane(width: _width, depth: _height)
    
    // Preserve existing materials when updating mesh
    let existingMaterials = _modelEntity.model?.materials ?? []
    _modelEntity.model = ModelComponent(mesh: mesh, materials: existingMaterials)
    
    // Update web view frame
    DispatchQueue.main.async {
      let screenWidth = CGFloat(self._width * 3200) // Scale factor for resolution
      let screenHeight = CGFloat(self._height * 3200)
      self._webView.frame = CGRect(x: 0, y: 0, width: screenWidth, height: screenHeight)
      self.updateWebViewTexture()
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
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
        self.updateWebViewTexture()
      }
    }
  }
  
  @objc open func GoForward() {
    if _webView.canGoForward {
      _webView.goForward()
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
        self.updateWebViewTexture()
      }
    }
  }
  
  @objc open func Reload() {
    _webView.reload()
    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
      self.updateWebViewTexture()
    }
  }
  
  @objc open func GoToUrl(_ url: String) {
    if let url = Foundation.URL(string: url) {
      let request = URLRequest(url: url)
      DispatchQueue.main.async {
        self._webView.load(request)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
          self.updateWebViewTexture()
        }
      }
    }
  }
  
  @objc open func GoHome() {
    if let url = Foundation.URL(string: _url) {
      let request = URLRequest(url: url)
      DispatchQueue.main.async {
        self._webView.load(request)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
          self.updateWebViewTexture()
        }
      }
    }
  }
}
