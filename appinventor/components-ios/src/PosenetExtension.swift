// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import WebKit

fileprivate let MODEL_URL =
"https://storage.googleapis.com/tfjs-models/savedmodel/posenet/mobilenet/quant2/050/"
fileprivate let BACK_CAMERA = "Back"
fileprivate let FRONT_CAMERA = "Front"

@objc open class PosenetExtension: BaseAiComponent {
  fileprivate final let _ERROR_JSON_PARSE_FAILED: Int = 101

  fileprivate var _keyPoints: [String: [Double]] = [:]
  fileprivate var _minPoseConfidence: Double = 0.1
  fileprivate var _minPartConfidence: Double = 0.5
  fileprivate lazy var _cameraMode: String = FRONT_CAMERA
  fileprivate var _enabled: Bool = true
  fileprivate var _backgroundImage: String = ""

  private enum IllegalStateError: Error {
    case webviewerNotSet
  }
  
  /**
      Creates a new Posenet Extension.
   */
  @objc public init(_ parent: ComponentContainer) {
    super.init(parent, "posenet", nil, nil)
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
    registerHandler(named: "reportImage", callback: self.reportImage(_:))
    print("created posenet extension")
  }

  // MARK: PoseNet Properties

  @objc open var MinPoseConfidence: Double {
    get {
      return _minPoseConfidence
    }
    set {
      _minPoseConfidence = newValue
      if isInitialized {
        do {
          try assertWebView("MinPoseConfidence")
        } catch {
          print(BaseAiComponent.ERROR_WEBVIEWER_NOT_SET, "MinPoseConfidence")
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
      if isInitialized {
        do {
          try assertWebView("MinPartConfidence")
        } catch {
          print(BaseAiComponent.ERROR_WEBVIEWER_NOT_SET, "MinPartConfidence")
        }
      }
    }
  }
  
  @objc open var KeyPoints: [[Double]] {
    get {
      var keyPoints: [[Double]] = []
      for point in _keyPoints.values {
        if point.count == 2 {
          keyPoints.append(point)
        }
      }
      return keyPoints
    }
  }

  private func addIfValid(_ point1: [Double]?, _ point2: [Double]?, _ skeleton: inout [[[Double]]]) {
    guard let point1 = point1, let point2 = point2 else {
      return
    }
    skeleton.append([point1, point2])
  }

  @objc open var Skeleton: [[[Double]]] {
    var skeleton: [[[Double]]] = []
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
    addIfValid(lWrist, lElbow, &skeleton)
    addIfValid(lElbow, lShoulder, &skeleton)
    addIfValid(lShoulder, rShoulder, &skeleton)
    addIfValid(rShoulder, rElbow, &skeleton)
    addIfValid(rElbow, rWrist, &skeleton)
    addIfValid(lShoulder, lHip, &skeleton)
    addIfValid(rShoulder, rHip, &skeleton)
    addIfValid(lHip, rHip, &skeleton)
    addIfValid(lHip, lKnee, &skeleton)
    addIfValid(lKnee, lAnkle, &skeleton)
    addIfValid(rHip, rKnee, &skeleton)
    addIfValid(rKnee, rAnkle, &skeleton)
    return skeleton
  }
  
  @objc open var Nose: [Double] {
    return _keyPoints["nose"] ?? []
  }
  
  @objc open var LeftEye: [Double] {
    return _keyPoints["leftEye"] ?? []
  }
  
  @objc open var RightEye: [Double] {
    return _keyPoints["rightEye"] ?? []
  }
  
  @objc open var LeftEar: [Double] {
    return _keyPoints["leftEar"] ?? []
  }
  
  @objc open var RightEar: [Double] {
    return _keyPoints["rightEar"] ?? []
  }
  
  @objc open var LeftShoulder: [Double] {
    return _keyPoints["leftShoulder"] ?? []
  }
  
  @objc open var RightShoulder: [Double] {
    return _keyPoints["rightShoulder"] ?? []
  }
  
  @objc open var LeftElbow: [Double] {
    return _keyPoints["leftElbow"] ?? []
  }
  
  @objc open var RightElbow: [Double] {
    return _keyPoints["rightElbow"] ?? []
  }
  
  @objc open var LeftWrist: [Double] {
    return _keyPoints["leftWrist"] ?? []
  }
  
  @objc open var RightWrist: [Double] {
    return _keyPoints["rightWrist"] ?? []
  }
  
  @objc open var LeftHip: [Double] {
    return _keyPoints["leftHip"] ?? []
  }
  
  @objc open var RightHip: [Double] {
    return _keyPoints["rightHip"] ?? []
  }
  
  @objc open var LeftKnee: [Double] {
    return _keyPoints["leftKnee"] ?? []
  }
  
  @objc open var RightKnee: [Double] {
    return _keyPoints["rightKnee"] ?? []
  }
  
  @objc open var LeftAnkle: [Double] {
    return _keyPoints["leftAnkle"] ?? []
  }
  
  @objc open var RightAnkle: [Double] {
    return _keyPoints["rightAnkle"] ?? []
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
      if isInitialized {
        do {
          try assertWebView("Enabled")
        } catch {
          print(BaseAiComponent.ERROR_WEBVIEWER_NOT_SET, "Enabled")
        }
      }
    }
  }
  
  @objc open var UseCamera: String {
    get {
      return _cameraMode
    }
    set {
      if newValue == BACK_CAMERA || newValue == FRONT_CAMERA {
        _cameraMode = newValue
        if isInitialized {
          do {
            let frontFacing = (newValue == FRONT_CAMERA)
            try assertWebView("UseCamera", frontFacing)
          } catch {
            print(BaseAiComponent.ERROR_WEBVIEWER_NOT_SET, "UseCamera")
          }
        }
      } else {
        // No Error.Extension_Error?
        _form?.dispatchErrorOccurredEvent(self, "UseCamera", 3300, "Invalid camera selection. Must be either 'Front' or 'Back'.")
      }
    }
  }
  
  
  // MARK: PoseNet Methods

  @objc open func Error(_ errorCode: AnyObject, _ errorMessage: AnyObject) { //TODO: What to do about anyobject? Should be int and String
    EventDispatcher.dispatchEvent(of: self, called: "Error", arguments: errorCode, errorMessage)
    print("dispatched Error")
  }
  
  @objc open func PoseUpdated() {
    EventDispatcher.dispatchEvent(of: self, called: "PoseUpdated")
    print("dispatched PoseUpdated")
  }

  @objc open override func ModelReady() {
    super.ModelReady()
    EventDispatcher.dispatchEvent(of: self, called: "ModelReady")
    if _enabled, let webview = _webview {
      webview.evaluateJavaScript("minPoseConfidence = \(_minPoseConfidence);")
      webview.evaluateJavaScript("minPartConfidence = \(_minPartConfidence);")
      let frontFacing = FRONT_CAMERA == _cameraMode
      webview.evaluateJavaScript("setCameraFacingMode(\(frontFacing));")
    }
  }

  @objc open override func GotResult(_ args: AnyObject) {
    guard let argList = args as? YailList<AnyObject> else {
      return
    }
    _keyPoints.removeAll(keepingCapacity: true)
    for i in 0..<argList.length {
      guard let keyPoint = argList[i+1] as? YailDictionary else {
        continue
      }
      guard let part = keyPoint["part"] as? String,
            let score = keyPoint["score"] as? Double,
            let position = keyPoint["position"] as? YailDictionary else {
        continue
      }
      guard let x = position["x"] as? Double,
            let y = position["y"] as? Double else {
        continue
      }
      let coord: [Double] = score < _minPartConfidence ? [] : [x, y]
      _keyPoints[part] = coord
    }
    PoseUpdated()
  }

  @objc open func reportImage(_ args: String) {
    guard let parsedArgs = try? JSONSerialization.jsonObject(with: Data(args.utf8), options: []) as? [String] else {
      debugPrint("Unable to parse image arguments")
      return
    }
    let dataUrl = parsedArgs[0]
    if !dataUrl.isEmpty {
      _backgroundImage = dataUrl
      DispatchQueue.main.async {
        self.VideoUpdated()
      }
    }
  }

  @objc open func VideoUpdated() {
    EventDispatcher.dispatchEvent(of: self, called: "VideoUpdated")
    print("dispatched VideoUpdated")
  }
}
