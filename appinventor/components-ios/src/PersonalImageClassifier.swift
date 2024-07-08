// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit

@objc open class PersonalImageClassifier: BaseAiComponent, AbstractMethodsForIA, LifecycleDelegate{

    public static let MODE_VIDEO = "Video"
    public static let MODE_IMAGE = "Image"
    public static let IMAGE_WIDTH = 500
    public static let ERROR_INVALID_INPUT_MODE = -6;
    private var _inputMode = PersonalImageClassifier.MODE_VIDEO
    private var _running = false
    private var _minClassTime: Int32 = 0

    @objc public override init(_ container: ComponentContainer) {
        super.init(container)
        super.setDelegate(self)
    }

    //MARK: Methods

    @objc public func classifyImageData(_ imagePath: String?) {
        assertWebView("ClassifyImageData")
        guard let imagePath = imagePath, !imagePath.isEmpty else{
            print("Image path is nil or empty.")
            return
        }
        print("Entered Classify")
        print(imagePath)
        guard let image = UIImage(contentsOfFile: imagePath) else{
            print("Unable to load \(imagePath)")
            return
        }
        let scaledImage = image.scaled(toWidth: IMAGE_WIDTH)
        if let imageData = scaledImage.pngData(){
            let imageEncodedBase64String = imageData.base64EncodedString().replacingOccurrences(of: "\n", with: "")
            print("imageEncodedBase64String: \(imageEncodedBase64String)")
<<<<<<< HEAD
            _webview.evaluateJavaScript("classifyImageData(\"\(imageEncodedBase64String)\");", completionHandler: nil)
=======
            _webview?.evaluateJavaScript("classifyImageData(\"\(imageEncodedBase64String)\");", completionHandler: nil)
>>>>>>> c949328f9 (fixed syntax errors in PersonalImageClassifier.swift)
        }
    }

    @objc public func toggleCameraFacingMode() {
        assertWebView("ToggleCameraFacingMode")
        _webview?.evaluateJavaScript("toggleCameraFacingMode();", completionHandler: nil)
    }

    @objc public func classifyVideoData() {
        assertWebView("ClassifyVideoData")
        _webview?.evaluateJavaScript("classifyVideoData();", completionHandler: nil)
    }

    @objc public func startContinuousClassification() {
        if inputMode.caseInsensitiveCompare(MODE_VIDEO) == .orderedSame && !_running {
            assertWebView("StartVideoClassification")
            _webview?.evaluateJavaScript("startVideoClassification();", completionHandler: nil)
            _running = true
        }
    }

    @objc public func stopContinuousClassification() {
        if inputMode.caseInsensitiveCompare(MODE_VIDEO) == .orderedSame && _running {
            assertWebView("StopVideoClassification")
<<<<<<< HEAD
            _webview.evaluateJavaScript("stopVideoClassification();", completionHandler: nil)
=======
            _webview?.evaluateJavaScript("stopVideoClassification();", completionHandler: nil)
>>>>>>> c949328f9 (fixed syntax errors in PersonalImageClassifier.swift)
            _running = false
        }
    }

    // MARK: Events

    @objc public func classifierReady() {
        DispatchQueue.main.async { [self] in
        InputMode = _inputMode
        MinimumInterval = _minClassTime
        EventDispatcher.dispatchEvent(of: self, called: "ClassifierReady")
        }
    }

    @objc public func gotClassification(_ result: AnyObject) {
        DispatchQueue.main.async { [self] in
        EventDispatcher.dispatchEvent(of: self, called: "GotClassification", arguments: result)
        }
    }

    @objc public func error(_ errorCode: Int32) {
        DispatchQueue.main.async {
        EventDispatcher.dispatchEvent(of: self, called: "Error", arguments: errorCode as AnyObject)
        }
    }

    // MARK: Properties

    @objc public var inputMode: String {
        get {
            return _inputMode
        }
        set {
            if newValue.caseInsensitiveCompare(MODE_VIDEO) == .orderedSame {
<<<<<<< HEAD
                _webview.evaluateJavaScript("setInputMode(\"video\");", completionHandler: nil)
                _inputMode = MODE_VIDEO
            } else if newValue.caseInsensitiveCompare(MODE_IMAGE) == .orderedSame {
                _webview.evaluateJavaScript("setInputMode(\"image\");", completionHandler: nil)
=======
                _webview?.evaluateJavaScript("setInputMode(\"video\");", completionHandler: nil)
                _inputMode = MODE_VIDEO
            } else if newValue.caseInsensitiveCompare(MODE_IMAGE) == .orderedSame {
                _webview?.evaluateJavaScript("setInputMode(\"image\");", completionHandler: nil)
>>>>>>> c949328f9 (fixed syntax errors in PersonalImageClassifier.swift)
                _inputMode = MODE_IMAGE
            } else {
                form.dispatchErrorOccurredEvent(self, "InputMode", ErrorMessages.ERROR_INPUT_MODE, ERROR_INVALID_INPUT_MODE, LOG_TAG, "Invalid input mode \(newValue)")
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