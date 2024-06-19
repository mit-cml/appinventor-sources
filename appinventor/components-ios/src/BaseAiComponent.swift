// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import WebKit
import Zip 

fileprivate let MODEL_PATH_SUFFIX = ".mdl"
fileprivate let TRANSFER_MODEL_PREFIX = nil
fileprivate let PERSONAL_MODEL_PREFIX = nil

public protocol AbstractMethodsForIAComponents: AbstractMethodsForIAComponents {
  func classifierReady()
  func gotClassification(_ result: YailDictionary)
  func Error(_ errorCode: Int32)
}

@objc open class BaseAiComponent: NonVisibleComponent,  WKScriptMessageHandler, WKURLSchemeHandler{

    public static let ERROR_WEBVEWER_REQUIRED = -7
    public static let ERROR_CLASSIFICATION_FAILED = -2;

    private var _labels = [String]()
    private var _modelPath = ""
    private var _webview: WKWebView? = nil
    private var _webviewer: WebViewer?
    private var assetPath = nil;
    private var jsInterface = nil;

    @objc public override init(_ container: ComponentContainer) {
        super.init(container)
    }

    //MARK: Methods

    @objc public func Initialize() {
        guard let webview = _webview else {
        _form?.dispatchErrorOccurredEvent(self, "WebViewer", ErrorMessage.ERROR_EXTENSION_ERROR, BaseAiComponent.ERROR_WEBVEWER_REQUIRED)
        return
        }
        do {
            if self is PersonalImageClassifier{
                webview.load(try URLRequest(url: "appinventor:personal_image_classifier.html", method: .get))
            } else {
                // implement checks for other AI components
            }
        } catch {
        print("\(error)")
        }
    }

    @objc open var WebViewer: WebViewer {
    get {
        return _webviewer!
        }
        set {
        configureWebView(newValue.view as! WKWebView)
        print("configurewebview called")
        if self is PersonalImageClassifier{
            assetPath = "assets/personal_image_classifier"
        } else {
            // implement checks for other AI components
        }
        if let url = Bundle(for: BaseAiComponent.self).url(forResource: assetPath, withExtension: "html") {
            let request = URLRequest(url: url)
            print(request)
            _webview?.load(request)
            print("request loaded")
        }
    }

    // MARK: Private Implementation

    private func configureWebView(_ webview: WKWebView) {
        _webview = webview
        webview.configuration.preferences.javaScriptEnabled = true
        webview.configuration.allowsInlineMediaPlayback = true
        webview.configuration.mediaTypesRequiringUserActionForPlayback = []
        if self is PersonalImageClassifier{
            webview.configuration.userContentController.add(self, name: "PersonalImageClassifier")
        } else {
            // implement checks for other AI components
        }
        webview.configuration.setURLSchemeHandler(self, forURLScheme: "appinventor")
    }

    private func parseLabels(_ labels: String) throws -> [String] {
        var result = [String]()
        let data = Data(labels.utf8)
        do {
            if let arr = try JSONSerialization.jsonObject(with: data, options: []) as? [Any] {
                for item in arr {
                    if let label = item as? String {
                        result.append(label)
                    }
                }
            } else {
                throw YailRuntimeError("Got unparsable array from Javascript", "RuntimeError")
            }
        } catch {
            throw YailRuntimeError("Got unparsable array from Javascript", "RuntimeError")
        }
        return result
    }

  // MARK: WKScriptMessageHandler

  public func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
    guard let dict = message.body as? [String: Any],
    let functionCall = dict["functionCall"] as? String,
    let args = dict["args"] else {
        print("JSON Error message not recieved")
        return
    }
    if functionCall == "ready" {
        do {
          let result = try getYailObjectFromJson(args as? String, true)
          print(result)
          BaseAiComponent.self.labels = parseLabels(result);
          classifierReady(result as? String)
        } catch {
          print("Error parsing JSON from web view function ready")
        }
    }
    if functionCall = "reportResult" {
        do {
          let result = try getYailObjectFromJson(args as? String, true)
          print(result)
          BaseAiComponent.self.labels = parseLabels(result);
          gotClassification(result)
        } catch {
          print("Error parsing JSON from web view function reportResult")
        }
    }
    if functionCall = "error" {
         do {
          let result = try getYailObjectFromJson(args as? String, true)
          print(result)
          let intValue = try getInt32(from: result)
          Error(intValue)
        } catch {
          print("Error parsing JSON from web view function error")
        }
    }
    let eventName = body["eventName"]
  }

  // MARK: WKURLSchemeHandler

  public func webView(_ webView: WKWebView, start urlSchemeTask: WKURLSchemeTask) {
     if self is PersonalImageClassifier{
            TRANSFER_MODEL_PREFIX = "appinventor:personal-image-classifier/transfer/"
            PERSONAL_MODEL_PREFIX = "appinventor:personal-image-classifier/personal/"
        } else {
            // implement checks for other AI components
        }
    guard let url = urlSchemeTask.request.url?.absoluteString else {
      return
    }
    guard let fileName = urlSchemeTask.request.url?.lastPathComponent else {
      urlSchemeTask.didFailWithError(PICError.FileNotFound)
      return
    }
    if url.hasPrefix(TRANSFER_MODEL_PREFIX) {

    } else if url.hasPrefix(PERSONAL_MODEL_PREFIX) {

    } else {
      urlSchemeTask.didFailWithError(PICError.FileNotFound)
    }
  }

  public func webView(_ webView: WKWebView, stop urlSchemeTask: WKURLSchemeTask) {
    // We deliver the payload in one go so it cannot be cancelled.
  }

}
