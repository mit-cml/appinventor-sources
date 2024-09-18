// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import WebKit
import Zip
import ZIPFoundation

fileprivate let MODEL_PATH_SUFFIX = ".mdl"

@objc open class BaseAiComponent: NonvisibleComponent,  WKScriptMessageHandler, WKURLSchemeHandler{

  public static let ERROR_WEBVEWER_REQUIRED = -7
  public static let ERROR_CLASSIFICATION_FAILED = -2;
  public static let ERROR_INVALID_MODEL_FILE = -8;

  private var _labels = [String]()
  private var _modelPath: String? = nil
  internal var _webview: WKWebView? = nil
  private var _webviewer: WebViewer?
  private var assetPath: String
  private var transferModelPrefix: String? = nil
  private var personalModelPrefix: String? = nil

  @objc public init(_ container: ComponentContainer, _ basePath: String) {
    assetPath = basePath
    super.init(container)
  }

  @objc public init(_ container: ComponentContainer, _ basePath: String, _ transferModelPrefix: String, _ personalModelPrefix: String) {
    assetPath = basePath
    self.transferModelPrefix = transferModelPrefix
    self.personalModelPrefix = personalModelPrefix
    super.init(container)
  }

  //MARK: Methods

  @objc public func Initialize() {
    guard let webview = _webview else {
      _form?.dispatchErrorOccurredEvent(self, "WebViewer", ErrorMessage.ERROR_WEBVIEW_AI, BaseAiComponent.ERROR_WEBVEWER_REQUIRED)
      return
    }
  }

  @objc public func setModel(_ path: String) {
    if path.hasSuffix(MODEL_PATH_SUFFIX) {
      _modelPath = path
    } else {
      _form?.dispatchErrorOccurredEvent(self, "Model", ErrorMessage.ERROR_MODEL_AI, "\(BaseAiComponent.ERROR_INVALID_MODEL_FILE): Invalid model file format. Files must be of format \(MODEL_PATH_SUFFIX)")
    }
  }

  @objc public var ModelLabels: [String] {
    return _labels
  }


  @objc open var WebViewer: WebViewer {
    get {
      return _webviewer!
    }
    set {
      newValue.aiSchemeHandler = self
      configureWebView(newValue.view as! WKWebView)
      print("configurewebview called")
      if let url = Bundle(for: BaseAiComponent.self).url(forResource: assetPath, withExtension: "html") {
        let readAccessURL = Bundle(for: BaseAiComponent.self).bundleURL
        let request = URLRequest(url: URL(string: "appinventor://localhost/\(assetPath).html")!)
        _webview?.load(request)
        print("request loaded")
      }else{
        print("request not loaded")
      }
    }
  }

  open func ModelReady() {}
  open func GotResult(_ result: AnyObject) {}
  open func Error(_ errorCode: Int32){}

  open func configureWebView(_ webview: WKWebView) {
    _webview = webview
  }

  // MARK: Private Implementation

  private func parseLabels(_ labels: String) throws -> [String] {
    var result = [String]()
    let data = Data(labels.utf8)
    do {
      if let arr = try JSONSerialization.jsonObject(with: data, options: []) as? [Any] {
        for item in arr {
          result.append(labels)
        }
      } else {
        throw YailRuntimeError("Got unparsable array from Javascript", "RuntimeError")
      }
    } catch {
      throw YailRuntimeError("Got unparsable array from Javascript", "RuntimeError")
    }
    return result
  }

  internal func assertWebView(_ method: String, _ frontFacing: Bool = true) throws {
    guard let _webview = _webview else {
      throw AIError.webviewerNotSet
    }
  }

  // MARK: WKScriptMessageHandler

  public func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
    guard let dict = message.body as? [String: Any],
          var functionCall = dict["functionCall"] as? String,
          let args = dict["args"] else {
      print("JSON Error message not recieved")
      return
    }
    print(message.body)
    if functionCall == "ready" {
      print("ready")
      do {
        guard let result = try getYailObjectFromJson(args as? String, true) as? YailList<AnyObject> else {
          print("Unable to parse result")
          return
        }
        print(result)
        var labelList: [String] = []
        for el in result {
          if let label = el as? String {
            labelList.append(label)
          }
        }
        _labels = labelList
        ModelReady()
      } catch {
        print("Error parsing JSON from web view function ready")
      }
    }
    if functionCall == "reportResult" {
      do {
        let result = try getYailObjectFromJson(args as? String, true)
        GotResult(result)
      } catch {
        print("Error parsing JSON from web view function reportResult")
      }
    }
    if functionCall == "error" {
      print("error")
      do {
        let result = try getYailObjectFromJson(args as? String, true)
        print(result)
        Error(args as! Int32)
      } catch {
        print("Error parsing JSON from web view function error")
      }
    }

  }

  // MARK: WKURLSchemeHandler

  public func webView(_ webView: WKWebView, start urlSchemeTask: WKURLSchemeTask) {
    print("WKURLSchemeHandler")
    var fileData: Data? = nil
    guard let url = urlSchemeTask.request.url?.absoluteString else {
      urlSchemeTask.didFailWithError(AIError.FileNotFound)
      return
    }
    guard let fileName = urlSchemeTask.request.url?.lastPathComponent else {
      urlSchemeTask.didFailWithError(AIError.FileNotFound)
      return
    }
    print(url)
    if let modelPrefix = transferModelPrefix, url.hasPrefix(modelPrefix) {
      print("transfer model")
      let fileName = url.replacingOccurrences(of: modelPrefix, with: "")
      if let assetURL = Bundle(for: BaseAiComponent.self).url(forResource: fileName, withExtension: nil) {
        do {
          fileData = try Data(contentsOf: assetURL)
        } catch {
          urlSchemeTask.didFailWithError(error)
          return
        }
      } else {
        urlSchemeTask.didFailWithError(AIError.FileNotFound)
        return
      }
    } else if let modelPrefix = personalModelPrefix, url.hasPrefix(modelPrefix) {
      print("personal model")
      let fileName = url.replacingOccurrences(of: modelPrefix, with: "")
      guard let _modelPath = _modelPath else {
        urlSchemeTask.didFailWithError(AIError.FileNotFound)
        return
      }
      let zipPath = AssetManager.shared.pathForExistingFileAsset(_modelPath)
      do {
        guard let zipURL = URL(string: "file://\(zipPath)"),
              let archive = Archive(url: zipURL, accessMode: .read) else {
          urlSchemeTask.didFailWithError(AIError.FileNotFound)
          return
        }

        // Cerca l'entry desiderata
        for entry in archive {
          if entry.path == fileName {
            // Leggi i dati dall'entry
            var entryData = Data()
            _ = try archive.extract(entry) { data in
              entryData.append(data)
            }
            fileData = entryData
            break
          }
        }
      } catch {
        urlSchemeTask.didFailWithError(error)
        return
      }
    } else if url.hasPrefix("appinventor://localhost/") {
      guard let assetUrl = URL(string: fileName, relativeTo: Bundle(for: BaseAiComponent.self).resourceURL) else {
        urlSchemeTask.didFailWithError(AIError.FileNotFound)
        return
      }
      do {
        fileData = try Data(contentsOf: assetUrl)
      } catch {
        print("Asset error: \(error)")
      }
    } else {
      urlSchemeTask.didFailWithError(AIError.FileNotFound)
      return
    }
    if let fileData = fileData {
      let headers = [
        "Access-Control-Allow-Origin": "*",
        "Content-Type": fileName.hasSuffix(".html") ? "text/html" : "application/octet-stream",
        "Content-Length": "\(fileData.count)"
      ]
      let response = HTTPURLResponse(url: urlSchemeTask.request.url!,
                                     statusCode: 200,
                                     httpVersion: "HTTP/1.1",
                                     headerFields: headers)
      urlSchemeTask.didReceive(response!)
      urlSchemeTask.didReceive(fileData)
      urlSchemeTask.didFinish()
      print("Did send data for \(fileName)")
    } else {
      urlSchemeTask.didFailWithError(AIError.FileNotFound)
      print("Failed to locate \(fileName)")
    }
  }


  public func webView(_ webView: WKWebView, stop urlSchemeTask: WKURLSchemeTask) {
    // We deliver the payload in one go so it cannot be cancelled.
  }


  enum AIError: Error {
    case FileNotFound
    case webviewerNotSet
  }
}
