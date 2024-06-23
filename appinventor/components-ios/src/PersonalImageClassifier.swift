// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class PersonalImageClassifier: BaseAiComponent, AbstractMethodsForIA, LifecycleDelegate{

    public static let MODE_VIDEO = "Video"
    public static let MODE_IMAGE = "Image"
    public static let ERROR_INVALID_INPUT_MODE = -6;
    private var _inputMode = PersonalImageClassifier.MODE_VIDEO
    private var _running = false
    private var _minClassTime: Int32 = 0

    @objc public override init(_ container: ComponentContainer) {
        super.init(container)
        super.setDelegate(self)
    }

    //MARK: Methods

    @objc public func classifyImageData(_ image: String?) {
        assertWebView("ClassifyImageData")
    }

    @objc public func toggleCameraFacingMode() {
        assertWebView("ToggleCameraFacingMode")
        _webview.evaluateJavaScript("toggleCameraFacingMode();", completionHandler: nil)
    }

    @objc public func classifyVideoData() {
        assertWebView("ClassifyVideoData")
        _webview.evaluateJavaScript("classifyVideoData();", completionHandler: nil)
    }

    @objc public func startContinuousClassification() {
        if inputMode.caseInsensitiveCompare(MODE_VIDEO) == .orderedSame && !_running {
            assertWebView("StartVideoClassification")
            _webview.evaluateJavaScript("startVideoClassification();", completionHandler: nil)
            _running = true
        }
    }

    @objc public func stopContinuousClassification() {
        if inputMode.caseInsensitiveCompare(MODE_VIDEO) == .orderedSame && _running {
            assertWebView("StopVideoClassification")
            webview.evaluateJavaScript("stopVideoClassification();", completionHandler: nil)
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
                webview.evaluateJavaScript("setInputMode(\"video\");", completionHandler: nil)
                _inputMode = MODE_VIDEO
            } else if newValue.caseInsensitiveCompare(MODE_IMAGE) == .orderedSame {
                webview.evaluateJavaScript("setInputMode(\"image\");", completionHandler: nil)
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
            if let webview = _webview {
                webview.evaluateJavaScript("minClassTime = \(newValue);", completionHandler: nil)
            }
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