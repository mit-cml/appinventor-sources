// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

fileprivate let MODEL_PATH_SUFFIX = ".tflite"

@objc open class FaceExtension: BaseAiComponent {
  fileprivate final let _ERROR_WEBVIEWER_NOT_SET: String =  "You must specify a WebViewer using the WebViewer designer property before you can call"
  private let _ERROR_JSON_PARSE_FAILED = 101
  private let _MODEL_URL = "https://tfhub.dev/mediapipe/tfjs-model/facemesh/1/default/1/"
  private let _BACK_CAMERA = "Back"
  private let _FRONT_CAMERA = "Front"
  
  private var assetPath: String? = nil

  private var _keyPoints: [String: [Double]] = [:]
  internal var _cameraMode = "Front"
  private var _enabled = true
  private var _showMesh: Bool = false
  private var _backgroundImage = ""
  private var width = 350
  private var height = 200
  
  private enum IllegalStateError: Error {
    case webviewerNotSet
  }
  
  /**
   * Creates a new FaceExtension extension.
   */
  
  @objc public init(_ parent: ComponentContainer) {
    super.init(parent, "facemesh", nil, nil)

    _keyPoints["forehead"] = []
    _keyPoints["leftCheek"] = []
    _keyPoints["rightCheek"] = []
    _keyPoints["leftEyebrow"] = []
    _keyPoints["rightEyebrow"] = []
    _keyPoints["chin"] = []
    _keyPoints["leftEyeInnerCorner"] = []
    _keyPoints["rightEyeInnerCorner"] = []
    _keyPoints["mouthTop"] = []
    _keyPoints["mouthBottom"] = []
    _keyPoints["leftEyeTop"] = []
    _keyPoints["leftEyeBottom"] = []
    _keyPoints["rightEyeTop"] = []
    _keyPoints["rightEyeBottom"] = []
    _keyPoints["rightEarStart"] = []
    _keyPoints["leftEarStart"] = []
    _keyPoints["noseBottom"] = []
    _keyPoints["rightNoseTop"] = []
    _keyPoints["leftNoseTop"] = []
    _keyPoints["allPoints"] = []
    registerHandler(named: "reportImage", callback: self.reportImage(_:))
    print("Created FaceExtension")
  }

  @objc open var FaceLandmarks: [[Double]] {
    get {
      var landmarks: [[Double]] = []
      for point in _keyPoints.values {
        if point.count == 2 {
          landmarks.append(point)
        }
      }
      return landmarks
    }
  }
  
  @objc open var BackgroundImage: String = ""
  
  @objc open var Forehead: [Double] {
    return _keyPoints["forehead"] ?? []
  }
  
  @objc open var NoseBottom: [Double] {
    return _keyPoints["noseBottom"] ?? []
  }
  
  @objc open var Chin: [Double] {
    return _keyPoints["chin"] ?? []
  }
  
  @objc open var LeftCheek: [Double] {
    return _keyPoints["leftCheek"] ?? []
  }
  
  @objc open var RightCheek: [Double] {
    return _keyPoints["rightCheek"] ?? []
  }
  
  @objc open var LeftEyebrow: [Double] {
    return _keyPoints["leftEyebrow"] ?? []
  }
  
  @objc open var RightEyebrow: [Double] {
    return _keyPoints["rightEyebrow"] ?? []
  }
  
  @objc open var LeftEyeInnerCorner: [Double] {
    return _keyPoints["leftEyeInnerCorner"] ?? []
  }
  
  @objc open var RightEyeInnerCorner: [Double] {
    return _keyPoints["rightEyeInnerCorner"] ?? []
  }
  
  @objc open var MouthTop: [Double] {
    return _keyPoints["mouthTop"] ?? []
  }
  
  @objc open var MouthBottom: [Double] {
    return _keyPoints["mouthBottom"] ?? []
  }
  
  @objc open var LeftEyeTop: [Double] {
    return _keyPoints["leftEyeTop"] ?? []
  }
  
  @objc open var LeftEyeBottom: [Double] {
    return _keyPoints["leftEyeBottom"] ?? []
  }
  
  @objc open var RightEyeTop: [Double] {
    return _keyPoints["rightEyeTop"] ?? []
  }
  
  @objc open var RightEyeBottom: [Double] {
    return _keyPoints["rightEyeBottom"] ?? []
  }
  
  @objc open var RightForehead: [Double] {
    return _keyPoints["rightEarStart"] ?? []
  }
  
  @objc open var LeftForehead: [Double] {
    return _keyPoints["leftEarStart"] ?? []
  }
  
  @objc open var RightNose: [Double] {
    return _keyPoints["rightNoseTop"] ?? []
  }
  
  @objc open var LeftNose: [Double] {
    return _keyPoints["leftNoseTop"] ?? []
  }
  
  @objc open var FaceWidth: Double {
    return (_keyPoints["rightCheek"]?.first ?? 0) - (_keyPoints["leftCheek"]?.first ?? 0)
  }
  
  @objc open var CheekToNoseDistance: Double {
    return (_keyPoints["leftNoseTop"]?.first ?? 0) - (_keyPoints["leftCheek"]?.first ?? 0)
  }
  
  @objc open var EyeToMouthHeight: Double {
    return (_keyPoints["mouthTop"]?.last ?? 0) - (_keyPoints["forehead"]?.last ?? 0)
  }
  
  @objc open var AllPoints: [Double] {
    return _keyPoints["allPoints"] ?? []
  }
  
  
  @objc open var Enabled: Bool {
    get {
      return _enabled
    }
    set {
      _enabled = newValue
      if isInitialized {
        do {
          try assertWebView("Enabled")
        } catch {
          print(_ERROR_WEBVIEWER_NOT_SET, "Enabled")
        }
      }
    }
  }
  
  @objc open var ShowMesh: Bool {
    get {
      return _showMesh
    }
    set {
      _showMesh = newValue
      if isInitialized {
        if _showMesh {
          _webview?.evaluateJavaScript("turnMeshOn();", completionHandler: nil)
        } else {
          _webview?.evaluateJavaScript("turnMeshOff();", completionHandler: nil)
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
        if isInitialized {
          do {
            let frontFacing = (newValue == _FRONT_CAMERA)
            try assertWebView("UseCamera", frontFacing)
          } catch {
            print(_ERROR_WEBVIEWER_NOT_SET, "UseCamera")
          }
        }
      } else {
        DispatchQueue.main.async {
          self.Error("InvalidCameraMode" as AnyObject, "Invalid camera selection. Must be either 'Front' or 'Back'." as AnyObject)
        }
        _form?.dispatchErrorOccurredEvent(self, "UseCamera", 3300, "Invalid camera selection. Must be either 'Front' or 'Back'.")
      }
    }
  }
  
  //faceMesh Methods

  @objc open var Width: Int {
    get {
      return width
    }
    set {
      width = newValue
    }
  }
  
  @objc open var Height: Int {
    get {
      return height
    }
    set {
      height = newValue
    }
  }

  // MARK: FaceExtension Events

  @objc open override func ModelReady() {
    super.ModelReady()
    EventDispatcher.dispatchEvent(of: self, called: "ModelReady")
    if _enabled {
      let frontFacing = _FRONT_CAMERA == _cameraMode
      _webview?.evaluateJavaScript("setCameraFacingMode(\(frontFacing))")
    }
  }

  @objc open func Error(_ errorCode: AnyObject, _ errorMessage: AnyObject) {
    EventDispatcher.dispatchEvent(of: self, called: "Error", arguments: errorCode, errorMessage)
    print("Dispatched Error")
  }

  @objc open func VideoUpdated() {
    EventDispatcher.dispatchEvent(of: self, called: "VideoUpdated")
    print("Dispatched VideoUpdated")
  }

  private func evaluateJavaScript(_ script: String) {
    _webview?.evaluateJavaScript(script, completionHandler: { (result, error) in
      if let error = error {
        print("JavaScript evaluation error: \(error)")
      } else {
        print("JavaScript evaluation result: \(String(describing: result))")
      }
    })
  }
  
  @objc open func onDelete() {
    if isInitialized && _webview != nil {
      evaluateJavaScript("teardown();")
      _webview = nil
    }
  }
  
  @objc open func onPause() {
    if isInitialized && _webview != nil {
      evaluateJavaScript("stopVideo();")
    }
  }
  
  @objc open func onResume() {
    if isInitialized && _enabled && _webview != nil {
      evaluateJavaScript("startVideo();")
    }
  }
  
  @objc open func onStop() {
    if isInitialized && _webview != nil {
      evaluateJavaScript("teardown();")
      _webview = nil
    }
  }
  
  private let y_offset: Int = -20
  private let y_multiplier: Double = 1.0 / 620.0
  private let x_multiplier: Double = 1.0 / 480.0
  private let x_offset: Double = 180.0
  private let x_range: Double = 480.0
  
  @objc open func reportImage(_ args: String) {
    guard let parsedArgs = try? JSONSerialization.jsonObject(with: Data(args.utf8), options: []) as? [String] else {
      print("Unable to parse image arguments")
      return
    }
    let dataUrl = parsedArgs[0]
    if !dataUrl.isEmpty {
      BackgroundImage = dataUrl
      DispatchQueue.main.async {
        self.VideoUpdated()
      }
    }
  }

  @objc open override func GotResult(_ args: AnyObject) {
    guard let res = args as? [String: Any] else {
      return
    }

    func parseKeyPoints(from obj: [String: Any], keys: [String]) -> [String: [Double]] {
      var points = [String: [Double]]()
      for key in keys {
        if let point = obj[key] as? [String: Double] {
          points[key] = [point["x"]! * Double(width) * x_multiplier, point["y"]! * Double(height) * y_multiplier, point["z"]!]
        }
      }
      return points
    }

    let keys = ["forehead", "chin", "leftCheek", "rightCheek", "leftEyebrow", "rightEyebrow", "leftEyeInnerCorner", "rightEyeInnerCorner", "mouthTop", "mouthBottom", "leftEyeTop", "leftEyeBottom", "rightEyeTop", "rightEyeBottom", "rightEarStart", "leftEarStart", "noseBottom", "rightNoseTop", "leftNoseTop"]
    _keyPoints = parseKeyPoints(from: res, keys: keys)

    if let allPoints = res["allPoints"] as? [String: [String: Double]] {
      var listOfObjects = [Double]()
      for i in 0..<450 {
        if let point = allPoints[String(i)],
           let x = point["x"], let y = point["y"] {
          let adjustedX = x * Double(width) * x_multiplier
          let adjustedY = y * Double(height) * y_multiplier
          listOfObjects.append(adjustedX)
          listOfObjects.append(adjustedY)
        }
      }

      _keyPoints["allPoints"] = listOfObjects
    }

    DispatchQueue.main.async {
      self.FaceUpdated()
    }
  }
  
  @objc open func FaceUpdated() {
    EventDispatcher.dispatchEvent(of: self, called: "FaceUpdated")
    print("Dispatched FaceUpdated")
  }
  
  
  @objc open func error(errorCode: Int, errorMessage: String) {
    DispatchQueue.main.async {
      self.Error(errorCode as AnyObject, errorMessage as AnyObject)
    }
  }
  
  private func assertWebView(_ method: String, _ args: Any...) throws {
    if _webview == nil {
      throw IllegalStateError.webviewerNotSet
    }
    switch method {
    case "UseCamera":
      _webview?.evaluateJavaScript("setCameraFacingMode(\(args[0] as! Bool));")
    default:
      print("Error: Not a valid method")
    }
  }

  enum AIError: Error {
    case FileNotFound
    case webviewerNotSet
  }
}

