// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import WebKit


open class PosenetExtension: NonvisibleComponent, WKScriptMessageHandler {
  fileprivate final let _ERROR_WEBVIEWER_NOT_SET: String =
  "You must specify a WebViewer using the WebViewer designer property before you can call"
  fileprivate final let _ERROR_JSON_PARSE_FAILED: Int = 101
  fileprivate final let _MODEL_URL: String =
  "https://storage.googleapis.com/tfjs-models/savedmodel/posenet/mobilenet/quant2/050/"
  fileprivate final let _BACK_CAMERA: String = "Back"
  fileprivate final let _FRONT_CAMERA: String = "Front"
  
  fileprivate var _webview: WKWebView?  // Double check ! syntax
  private var _webviewerApiSource: String = ""
  private var _webviewerApi: WKUserScript? // double check ! syntax
  
  fileprivate var _keyPoints: [String: [String]] = [:]
  fileprivate var _minPoseConfidence: Double = 0.1
  fileprivate var _minPartConfidence: Double = 0.5
  fileprivate lazy var _cameraMode: String = _FRONT_CAMERA
  fileprivate var _initialized: Bool = false
  fileprivate var _enabled: Bool = true
  fileprivate var _backgroundImage: String = ""
  
  private enum IllegalStateError: Error {
    case webviewerNotSet
  }
  
  
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    _keyPoints["nose"] = []
    _keyPoints["leftEye"] = []
    _keyPoints["rightEye"] = []
    _keyPoints["leftEar"] = []
    _keyPoints["rightEar"] = []
    _keyPoints["leftShoulder"] = []
    _keyPoints["rightShoulder"] = []
    _keyPoints["leftElbow"] = []
    _keyPoints["rightElbow"] = []
    _keyPoints["leftWrist"] = []
    _keyPoints["rightWrist"] = []
    _keyPoints["leftHip"] = []
    _keyPoints["rightHip"] = []
    _keyPoints["leftKnee"] = []
    _keyPoints["rightKnee"] = []
    _keyPoints["leftAnkle"] = []
    _keyPoints["rightAnkle"] = []
  }
  
  /**
   Configure the current webview. Add a WKScriptMessageHandler
   to handle the following Javascript calls necessary for
   Posenet Extension:
          ready()
          reportImage(String dataURL)
          reportResult(String result)
          error(int errorCode, String errorMessage)
   Equivalent to the @Javascript interface in the Android Posenet Extension.
   */
  private func configureWebView(_ webview: WKWebView) {
    self._webview = webview
    let config = webview.configuration
    // config.defaultWebpagePreferences.allowsContentJavaScript = true
    let controller = config.userContentController
    controller.add(self, name: "ready")
    controller.add(self, name: "reportImage")
    controller.add(self, name: "reportResult")
    controller.add(self, name: "error")
  }
  
  
  // MARK: PoseNet Properties
  
  
//  @objc open var WebViewer: WebViewer {
//    get {
//      return _webviewer
//    }
//    set {
//      if 
//    }
//  }
//  
  
  
  
  
  @objc open var MinPoseConfidence: Double {
    get {
      return _minPoseConfidence
    }
    set {
      _minPoseConfidence = newValue
      if _initialized {
        do {
          try assertWebView("MinPoseConfidence")
        } catch {
          print(_ERROR_WEBVIEWER_NOT_SET, "MinPoseConfidence")
        }
      }
    }
  }
  
  @objc open var MinPartConfidence: Double {
    get {
      return _minPartConfidence
    }
    set {
      _minPartConfidence = newValue
      if _initialized {
        do {
          try assertWebView("MinPartConfidence")
        } catch {
          print(_ERROR_WEBVIEWER_NOT_SET, "MinPartConfidence")
        }
      }
    }
  }
  
  @objc open var KeyPoints: [String] {
    get {
      var keyPoints: [[String]] = []
      for point in _keyPoints.values {
        if point.count == 2 {
          keyPoints.append(point)
        }
      }
      // TODO: build yaillist?
      // HELP: what is YailList.makelist returning?
      // returning flat array?
      return keyPoints.flatMap { $0 }
    }
  }
  
  @objc open var Skeleton: [String] {
    let lWrist = _keyPoints["leftWrist"]
    let lElbow = _keyPoints["leftElbow"]
    let lShoulder = _keyPoints["leftShoulder"]
    let rShoulder = _keyPoints["rightShoulder"]
    let rElbow = _keyPoints["rightElbow"]
    let rWrist = _keyPoints["rightWrist"]
    let lHip = _keyPoints["leftHip"]
    let lKnee = _keyPoints["leftKnee"]
    let lAnkle = _keyPoints["leftAnkle"]
    let rHip = _keyPoints["rightHip"]
    let rKnee = _keyPoints["rightKnee"]
    let rAnkle = _keyPoints["rightAnkle"]
    
    // TODO: generate correct return value
    return []
  }
  
  // TODO: add all body part properties and find YailList representation
  
  
  
  
  @objc open var BackgroundImage: String {
      return _backgroundImage
  }
  
  @objc open var Enabled: Bool {
    get {
      return _enabled
    }
    set {
      _enabled = newValue
      if _initialized {
        do {
          try assertWebView("Enabled")
        } catch {
          print(_ERROR_WEBVIEWER_NOT_SET, "Enabled")
        }
      }
    }
  }
  
  @objc open var UseCamera: String {
    get {
      return _cameraMode
    }
    set {
      if newValue == _BACK_CAMERA || newValue == _FRONT_CAMERA {
        _cameraMode = newValue
        if _initialized {
          do {
            let frontFacing = (newValue == _FRONT_CAMERA)
            try assertWebView("UseCamera", frontFacing)
          } catch {
            print(_ERROR_WEBVIEWER_NOT_SET, "UseCamera")
          }
        }
      } else {
          // TODO: figure out what form is
      }
    }
  }
  
  
  // MARK: PoseNet Methods
  
  
  open func userContentController(
    _ userContentController: WKUserContentController,
    didReceive message: WKScriptMessage
  ) {
    if message.name == "ready" {
      if _enabled {
        MinPartConfidence = _minPartConfidence
        MinPoseConfidence = _minPoseConfidence
        UseCamera = _cameraMode
      }
    }
    if message.name == "reportImage" {
      // TODO: Finish out the javascript interface here
    }
  }
  
  @objc open func initialize() {
    if _webview != nil {
      _initialized = true
    }
  }
  
  private func assertWebView(_ method: String, _ frontFacing: Bool = true) throws {
    guard let _webview = _webview else {
      throw IllegalStateError.webviewerNotSet
    }
    switch method {
    case "Enabled":
      _webview.evaluateJavaScript(_enabled ? "startVideo();" : "stopVideo();")
    case "MinPoseConfidence":
      _webview.evaluateJavaScript("minPoseConfidence = \(_minPoseConfidence) ;")
    case "MinPartConfidence":
      _webview.evaluateJavaScript("minPartConfidence = \(_minPartConfidence) ;")
    case "UseCamera":
      _webview.evaluateJavaScript("setCameraFacingMode( \(frontFacing) );")
    default:
      print("Error: Not a valid method")
    }
  }
  
  
  
  
  
  
}
