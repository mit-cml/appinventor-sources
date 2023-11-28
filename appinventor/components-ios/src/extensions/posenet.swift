// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import WebKit


open class PosenetExtension: NonvisibleComponent, WKScriptMessageHandler, WKUIDelegate {
  fileprivate final let _ERROR_WEBVIEWER_NOT_SET: String =
  "You must specify a WebViewer using the WebViewer designer property before you can call"
  fileprivate final let _ERROR_JSON_PARSE_FAILED: Int = 101
  fileprivate final let _MODEL_URL: String =
  "https://storage.googleapis.com/tfjs-models/savedmodel/posenet/mobilenet/quant2/050/"
  fileprivate final let _BACK_CAMERA: String = "Back"
  fileprivate final let _FRONT_CAMERA: String = "Front"
  
  fileprivate var _webview: WKWebView?
  fileprivate var _webviewer: WebViewer?
  private var _webviewerApiSource: String = ""
  private var _webviewerApi: WKUserScript?
  
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
  
  /**
      Creates a new Posenet Extension.
   */
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    // TODO: Hardware Acceleration?
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
    print("created posenet extension")
  }
  
  /**
     Configure the current webview. Add a WKScriptMessageHandler
     to handle the following Javascript calls necessary for
     Posenet Extension within userContentController(_ :didRecieve:):
            ready()
            reportImage(String dataURL)
            reportResult(String result)
            error(int errorCode, String errorMessage)
     Equivalent to the @JavascriptInterface in the Android Posenet Extension.
   */
  private func configureWebView(_ webview: WKWebView) {
    self._webview = webview
    let config = webview.configuration
    let controller = config.userContentController
    controller.add(self, name: "PosenetExtension")
  }
  
  
  /**
    Implements WKUIDelegate protocol to handle camera and video permissions.
  */
  @available(iOS 15.0, *)
  public func webView(_ webView: WKWebView,
              requestMediaCapturePermissionFor
              origin: WKSecurityOrigin,initiatedByFrame
              frame: WKFrameInfo,type: WKMediaCaptureType,
              decisionHandler: @escaping (WKPermissionDecision) -> Void) {
                decisionHandler(.grant)
  }
  
  /**
   Configures Javascript interface. TFJS app.js file has been
   edited such that it directly calls userContentController.
   Generic implementation yet to be built.
   */
  open func userContentController(
    _ userContentController: WKUserContentController,
    didReceive message: WKScriptMessage
  ) {
    print("recieving content")
    if message.name == "PosenetExtension" {
      guard let dict = message.body as? [String: Any],
            let functionCall = dict["functionCall"] as? String,
            let args = dict["args"] else {
        print("JSON Error message not recieved")
        return
      }
      if functionCall == "ready" {
        print("Model Ready")
        ModelReady()
        if _enabled {
          MinPartConfidence = _minPartConfidence
          MinPoseConfidence = _minPoseConfidence
          UseCamera = _cameraMode
        }
      }
      if functionCall == "reportResult" {
        print("Reporting Result")
        do {
          let result = try getYailObjectFromJson(args as? String, true)
          print(result)
        } catch {
          print("Error parsing JSON from web view")
          Error(_ERROR_JSON_PARSE_FAILED as AnyObject, "Error parsing JSON from web view" as AnyObject)
        }
      }
      if functionCall == "reportImage" {
        print("Reporting Image")
        // baackground image?
        VideoUpdated()
      }
      if functionCall == "error" {
        // TODO: Error function
        //        let (errorCode, errorMessage) = args
        print("Error function to be called")
        
      }
    }
  }
    
    
  // MARK: PoseNet Properties
  
  
  @objc open var WebViewer: WebViewer {
    get {
      return _webviewer!
    }
    set {
      configureWebView(newValue.view as! WKWebView)
      print("configurewebview called")
      if let url = Bundle(for: PosenetExtension.self).url(forResource: "assets/index", withExtension: "html") {
        let request = URLRequest(url: url)
        print(request)
        _webview?.load(request)
        print("request loaded")
      }
    }
  }
      
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
  
  @objc open var KeyPoints: [[String]] {
    get {
      var keyPoints: [[String]] = []
      for point in _keyPoints.values {
        if point.count == 2 {
          keyPoints.append(point)
        }
      }
      return keyPoints
    }
  }
  
  @objc open var Skeleton: [[String]] {
    var skeleton: [[String]] = []
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
    skeleton.append(lWrist!)
    skeleton.append(lElbow!)
    skeleton.append(lShoulder!)
    skeleton.append(rShoulder!)
    skeleton.append(rElbow!)
    skeleton.append(rWrist!)
    skeleton.append(lHip!)
    skeleton.append(lKnee!)
    skeleton.append(lAnkle!)
    skeleton.append(rHip!)
    skeleton.append(rKnee!)
    skeleton.append(rAnkle!)
    return skeleton
  }
  
  @objc open var Nose: [String] {
    return _keyPoints["nose"]!
  }
  
  @objc open var LeftEye: [String] {
    return _keyPoints["leftEye"]!
  }
  
  @objc open var RightEye: [String] {
    return _keyPoints["rightEye"]!
  }
  
  @objc open var LeftEar: [String] {
    return _keyPoints["leftEar"]!
  }
  
  @objc open var RightEar: [String] {
    return _keyPoints["rightEar"]!
  }
  
  @objc open var LeftShoulder: [String] {
    return _keyPoints["leftShoulder"]!
  }
  
  @objc open var RightShoulder: [String] {
    return _keyPoints["rightShoulder"]!
  }
  
  @objc open var LeftElbow: [String] {
    return _keyPoints["leftElbow"]!
  }
  
  @objc open var RightElbow: [String] {
    return _keyPoints["rightElbow"]!
  }
  
  @objc open var LeftWrist: [String] {
    return _keyPoints["leftWrist"]!
  }
  
  @objc open var RightWrist: [String] {
    return _keyPoints["rightWrist"]!
  }
  
  @objc open var LeftHip: [String] {
    return _keyPoints["leftHip"]!
  }
  
  @objc open var RightHip: [String] {
    return _keyPoints["rightHip"]!
  }
  
  @objc open var LeftKnee: [String] {
    return _keyPoints["leftKnee"]!
  }
  
  @objc open var RightKnee: [String] {
    return _keyPoints["rightKnee"]!
  }
  
  @objc open var LeftAnkle: [String] {
    return _keyPoints["leftAnkle"]!
  }
  
  @objc open var RightAnkle: [String] {
    return _keyPoints["rightAnkle"]!
  }
  
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
        // No Error.Extension_Error?
        _form?.dispatchErrorOccurredEvent(self, "UseCamera", 3300, "Invalid camera selection. Must be either 'Front' or 'Back'.")
      }
    }
  }
  
  
  // MARK: PoseNet Methods
  
  
  @objc open func Initialize() {
    if _webview != nil {
      _initialized = true
    }
  }
  
  @objc open func ModelReady() {
    EventDispatcher.dispatchEvent(of: self, called: "ModelReady")
    print("dispatched ModelReady")
  }
  
  @objc open func Error(_ errorCode: AnyObject, _ errorMessage: AnyObject) { //TODO: What to do about anyobject? Should be int and String
    EventDispatcher.dispatchEvent(of: self, called: "Error", arguments: errorCode, errorMessage)
    print("dispatched Error")
  }
  
  @objc open func PoseUpdated() {
    EventDispatcher.dispatchEvent(of: self, called: "PoseUpdated")
    print("dispatched PoseUpdated")
  }
  
  @objc open func VideoUpdated() {
    EventDispatcher.dispatchEvent(of: self, called: "VideoUpdated")
    print("dispatched VideoUpdated")
  }
  
  
  @objc open func onDelete() {
    if _initialized && _webview != nil {
      _webview?.evaluateJavaScript("teardown();", completionHandler: nil)
      _webview = nil
    }
  }
  
  @objc open func onPause() {
    if _initialized && _webview != nil {
      _webview?.evaluateJavaScript("stopVideo();", completionHandler: nil)
    }
  }
  
  @objc open func onResume() {
    if _initialized && _enabled && _webview != nil {
      _webview?.evaluateJavaScript("startVideo();", completionHandler: nil)
    }
  }
  
  @objc open func onStop() {
    if _initialized && _webview != nil {
      _webview?.evaluateJavaScript("teardown();", completionHandler: nil)
      _webview = nil
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
