// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class PosenetExtension: NonvisibleComponent {
  // fileprivate let LOG_TAG: String = PosenetExtension
  fileprivate final let _ERROR_WEBVIEWER_NOT_SET: String =
  "You must specify a WebViewer using the WebViewer designer property before you can call %1s"
  fileprivate final let _ERROR_JSON_PARSE_FAILED: Int = 101
  fileprivate final let _MODEL_URL: String =
  "https://storage.googleapis.com/tfjs-models/savedmodel/posenet/mobilenet/quant2/050/"
  fileprivate final let _BACK_CAMERA: String = "Back"
  fileprivate final let _FRONT_CAMERA: String = "Front"
  
  fileprivate var _webview: Webview?  // HELP: webview?
  fileprivate var _keyPoints = Dictionary<String, [String]>()
  fileprivate var _minPoseConfidence: Double = 0.1
  fileprivate var _minPartConfidence: Double = 0.5
  fileprivate lazy var _cameraMode: String = _FRONT_CAMERA
  fileprivate var _initialized: Bool = false
  fileprivate var _enabled: Bool = true
  fileprivate var _backgroundImage: String = ""
  
  
  public override init(_ parent: ComponentContainer) {
    // TODO
  }
  
  // MARK: PoseNet Properties
  
  @objc open var MinPoseConfidence: Double {
    get {
      return _minPoseConfidence
    }
    set {
      _minPoseConfidence = newValue
      if _initialized {
        // TODO
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
        // TODO
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
    
    // TODO
  }
  
  @objc open var BackgroundImage: String {
    get {
      return _backgroundImage
    }
  }
  
  @objc open var Enabled: Bool {
    get {
      return _enabled
    }
    set {
      _enabled = newValue
      if _initialized {
        // WEBVIEW TODO
      }
    }
  }
  
  // MARK: PoseNet Methods
  
  @objc open func Initialize() -> Void {
    if let unwrapped = _webview {
      _initialized = true
    }
  }
  
  // NO Mutability?
  @objc open func AddIfValid(
    _ point1: [String],
    _ point2: [String],
    _ skeleton: [[String]]
  ) {
    if point1.count == 2 && point2.count == 2 {
      var newPoint = [point1, point2].flatMap { $0 }
      skeleton.append(newPoint)
    }
  }
  
  
  
  
  
}

