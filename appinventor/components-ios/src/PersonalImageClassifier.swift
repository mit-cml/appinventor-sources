// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class PersonalImageClassifier: BaseAiComponent, AbstractMethodsForIA, LifecycleDelegate{

    public static let MODE_VIDEO = "Video"
    public static let MODE_IMAGE = "Image"
    private var _inputMode = PersonalImageClassifier.MODE_VIDEO
    private var _running = false
    private var _minClassTime: Int32 = 0

    @objc public override init(_ container: ComponentContainer) {
        super.init(container)
        super.setDelegate(self)
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

    // MARK: LifecycleDelegate

    @objc public func onPause() {
        if let webview = _webview, _inputMode == PersonalImageClassifier.MODE_VIDEO {
        webview.evaluateJavaScript("stopVideo();")
        }
    }

    @objc public func onResume() {
        if let webview = _webview, _inputMode == PersonalImageClassifier.MODE_VIDEO {
        webview.evaluateJavaScript("startVideo();")
        }
    }

    @objc public func onStop() {
        if let webview = _webview, _inputMode == PersonalImageClassifier.MODE_VIDEO {
        webview.evaluateJavaScript("stopVideo();")
        }
    }

    @objc public func onClear() {
        if let webview = _webview, _inputMode == PersonalImageClassifier.MODE_VIDEO {
        webview.evaluateJavaScript("stopVideo();")
        }
    }
}