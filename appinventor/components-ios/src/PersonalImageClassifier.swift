// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit

fileprivate let TRANSFER_MODEL_PREFIX = "appinventor://personal-image-classifier/transfer/"
fileprivate let PERSONAL_MODEL_PREFIX = "appinventor://personal-image-classifier/personal/"

@objc open class PersonalImageClassifier: BaseClassifier, LifecycleDelegate {
  public static let MODE_VIDEO = "Video"
  public static let MODE_IMAGE = "Image"
  public static let IMAGE_WIDTH = 500
  public static let ERROR_INVALID_INPUT_MODE = -6;

  private var _inputMode = PersonalImageClassifier.MODE_VIDEO
  private var _minClassTime: Int32 = 0
  private var _running = false

  @objc public init(_ container: ComponentContainer) {
    super.init(container, "personal_image_classifier", TRANSFER_MODEL_PREFIX, PERSONAL_MODEL_PREFIX)
  }

  // MARK: Properties

  @objc public var InputMode: String {
    get {
      return _inputMode
    }
    set {
      if newValue.caseInsensitiveCompare(PersonalImageClassifier.MODE_VIDEO) == .orderedSame {
        _webview?.evaluateJavaScript("setInputMode(\"video\");", completionHandler: nil)
        _inputMode = PersonalImageClassifier.MODE_VIDEO
      } else if newValue.caseInsensitiveCompare(PersonalImageClassifier.MODE_IMAGE) == .orderedSame {
        _webview?.evaluateJavaScript("setInputMode(\"image\");", completionHandler: nil)
        _inputMode = PersonalImageClassifier.MODE_IMAGE
      } else {
        _form?.dispatchErrorOccurredEvent(self, "InputMode", ErrorMessage.ERROR_INPUT_MODE, PersonalImageClassifier.ERROR_INVALID_INPUT_MODE, "Invalid input mode \(newValue)")
      }
    }
  }

  @objc public var MinimumInterval: Int32 {
    get {
      return _minClassTime
    }
    set {
      _minClassTime = newValue
      _webview?.evaluateJavaScript("minClassTime = \(newValue);", completionHandler: nil)
    }
  }

  @objc public var Running: Bool {
    return _running
  }

  // MARK: Methods

  @objc public func ClassifyImageData(_ imagePath: String?) {
    do  {
      try assertWebView("ClassifyImageData")
    } catch {
      print("Error webViewer not set during ClassifyImageData")
    }
    guard let imagePath = imagePath, !imagePath.isEmpty else{
      print("Image path is nil or empty.")
      return
    }
    print("Entered Classify")
    print(imagePath)
    var scaledImageBitmap: UIImage? = nil
    if let image = UIImage(named: imagePath) {
      let width = PersonalImageClassifier.IMAGE_WIDTH
      let height = Int(image.size.height * CGFloat(PersonalImageClassifier.IMAGE_WIDTH) / image.size.width)

      UIGraphicsBeginImageContext(CGSize(width: width, height: height))
      image.draw(in: CGRect(x: 0, y: 0, width: width, height: height))
      scaledImageBitmap = UIGraphicsGetImageFromCurrentImageContext()
      UIGraphicsEndImageContext()
    } else {
      print("Unable to load \(imagePath)")
    }

    if let immagex = scaledImageBitmap {
      if let imageData = immagex.pngData() {
        let imageEncodedBase64String = imageData.base64EncodedString(options: .lineLength64Characters).replacingOccurrences(of: "\n", with: "")
        print("imageEncodedBase64String: \(imageEncodedBase64String)")
        _webview?.evaluateJavaScript("classifyImageData(\"\(imageEncodedBase64String)\");", completionHandler: nil)
      }
    }
  }

  @objc public func ToggleCameraFacingMode() {
    do {
      try assertWebView("ToggleCameraFacingMode")
    } catch {
      print("Error webViewer not set during ToggleCameraFacingMode")
    }
    _webview?.evaluateJavaScript("toggleCameraFacingMode();", completionHandler: nil)
  }

  @objc public func ClassifyVideoData() {
    do {
      try assertWebView("ClassifyVideoData")
    } catch {
      print("Error webViewer not set during ClassifyVideoData" )
    }
    print("ClassifyVideoData")
    _webview?.evaluateJavaScript("classifyVideoData();", completionHandler: nil)
  }

  @objc public func StartContinuousClassification() {
    if InputMode.caseInsensitiveCompare(PersonalImageClassifier.MODE_VIDEO) == .orderedSame && !_running {
      do {
        try assertWebView("StartVideoClassification")
      } catch {
        print("Error webViwer not set during StartVideoClassification")
      }
      _webview?.evaluateJavaScript("startVideoClassification();", completionHandler: nil)
      _running = true
    }
  }

  @objc public func StopContinuousClassification() {
    if InputMode.caseInsensitiveCompare(PersonalImageClassifier.MODE_VIDEO) == .orderedSame && _running {
      do {
        try assertWebView("StopVideoClassification")
      } catch {
        print("Error webViewer not set during StopVideoClassification")
      }
      _webview?.evaluateJavaScript("stopVideoClassification();", completionHandler: nil)
      _running = false
    }
  }

  // MARK: Events

  @objc public override func ClassifierReady() {
    InputMode = _inputMode
    MinimumInterval = _minClassTime
    super.ClassifierReady()
  }

  // MARK: LifecycleDelegate

  @objc public func onPause() {
    if let webview = _webview, _inputMode == PersonalImageClassifier.MODE_VIDEO {
      webview.evaluateJavaScript("stopVideo();", completionHandler: nil)
    }
  }

  @objc public func onResume() {
    if let webview = _webview, _inputMode == PersonalImageClassifier.MODE_VIDEO {
      webview.evaluateJavaScript("startVideo();", completionHandler: nil)
    }
  }

  @objc public func onClear() {
    if let webview = _webview, _inputMode == PersonalImageClassifier.MODE_VIDEO {
      webview.evaluateJavaScript("stopVideo();", completionHandler: nil)
    }
  }
}
