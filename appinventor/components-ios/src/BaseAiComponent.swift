// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import WebKit
import Zip 
import ZIPFoundation

fileprivate let MODEL_PATH_SUFFIX = ".mdl"
fileprivate var TRANSFER_MODEL_PREFIX: String? = nil
fileprivate var PERSONAL_MODEL_PREFIX: String? = nil

@objc open class BaseAiComponent: NonvisibleComponent,  WKScriptMessageHandler, WKURLSchemeHandler{

    public static let ERROR_WEBVEWER_REQUIRED = -7
    public static let ERROR_CLASSIFICATION_FAILED = -2;
    public static let ERROR_INVALID_MODEL_FILE = -8;

    private var _labels = [String]()
    private var _modelPath: String? = nil
    internal var _webview: WKWebView? = nil
    private var _webviewer: WebViewer?
    private var assetPath: String? = nil

    @objc public override init(_ container: ComponentContainer) {
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


    /*@objc open var WebViewer: WebViewer {
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
            print("enter request")
            _webview?.load(request)
            print("request loaded")
        }else{
          print("request not loaded")
        }
      }
    }*/
  
  @objc open var WebViewer: WebViewer {
    get {
      return _webviewer!
      }
      set {
      configureWebView(newValue.view as! WKWebView)
      print("configurewebview called")
      if self is PersonalImageClassifier{
          assetPath = "personal_image_classifier"
      } else {
          // implement checks for other AI components
      }
      print(assetPath)
      let bundle = Bundle(for: BaseAiComponent.self)
      print(bundle)
        
      if let bundlePath = bundle.resourcePath {
        do{
          let files = try FileManager.default.contentsOfDirectory(atPath: bundlePath)
          print(files)
        }catch{
          print(error)
        }
      }
        
      if let url = bundle.url(forResource: assetPath, withExtension: "html"){
        let request = URLRequest(url: url)
        print(request)
        _webview?.load(request)
        print("requestLoaded")
      }else{
        print("Request not lodaded")
      }
    }
  }
  
  
  
    open func ClassifierReady(){}
    open func GotClassification(_ result: AnyObject){}
    open func Error(_ errorCode: Int32){}

    // MARK: Private Implementation

    private func configureWebView(_ webview: WKWebView) {
        _webview = webview
      _webview!.configuration.preferences.javaScriptEnabled = true
          _webview!.configuration.allowsInlineMediaPlayback = true
          _webview!.configuration.mediaTypesRequiringUserActionForPlayback = []
        
        if self is PersonalImageClassifier{
            print("PersonalImageClassifier")
            _webview!.configuration.userContentController.add(self, name: "PersonalImageClassifier")
           TRANSFER_MODEL_PREFIX = "appinventor:personal-image-classifier/transfer/"
           PERSONAL_MODEL_PREFIX = "appinventor:personal-image-classifier/personal/"
        } else {
            // implement checks for other AI components
        }
        _webview!.configuration.setURLSchemeHandler(self, forURLScheme: "appinventor")
    }

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
    print("recieving content")
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
          let result = try getYailObjectFromJson(args as? String, true)
          print(result)
          _labels = try parseLabels(result as! String);
          ClassifierReady()
        } catch {
          print("Error parsing JSON from web view function ready")
        }
    }
    if functionCall == "reportResult" {
      print("report result")
        do {
          let result = try getYailObjectFromJson(args as? String, true)
          print(result)
          _labels = try parseLabels(result as! String);
          GotClassification(result)
        } catch {
          print("Error parsing JSON from web view function reportResult")
        }
    }
    if functionCall == "error" {
      print("error")
         do {
          let result = try getYailObjectFromJson(args as? String, true)
          print(result)
          let intValue = result as! Int32
          Error(intValue)
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
    if url.hasPrefix(TRANSFER_MODEL_PREFIX!) {
      print("transfer model")
      let fileName = url.replacingOccurrences(of: TRANSFER_MODEL_PREFIX!, with: "")
          if let assetURL = Bundle.main.url(forResource: fileName, withExtension: nil) {
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
    } else if url.hasPrefix(PERSONAL_MODEL_PREFIX!) {
        print("personal model")
        let fileName = url.replacingOccurrences(of: PERSONAL_MODEL_PREFIX!, with: "")
          guard let _modelPath = _modelPath, let zipURL = Bundle.main.url(forResource: _modelPath, withExtension: "zip") else {
              urlSchemeTask.didFailWithError(AIError.FileNotFound)
              return
          }
          do {
            guard let archive = Archive(url: zipURL, accessMode: .read) else {
                urlSchemeTask.didFailWithError(AIError.FileNotFound)
                return
            }

            var fileData: Data?

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
      } else {
          urlSchemeTask.didFailWithError(AIError.FileNotFound)
          return
      }
      if let fileData = fileData {
          let response = URLResponse(url: urlSchemeTask.request.url!,
                                    mimeType: "application/octet-stream",
                                    expectedContentLength: fileData.count,
                                    textEncodingName: nil)
          urlSchemeTask.didReceive(response)
          urlSchemeTask.didReceive(fileData)
          urlSchemeTask.didFinish()
      } else {
          urlSchemeTask.didFailWithError(AIError.FileNotFound)
      }
  }


  public func webView(_ webView: WKWebView, stop urlSchemeTask: WKURLSchemeTask) {
    // We deliver the payload in one go so it cannot be cancelled.
  }
  
  func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
      print("Started to load")
  }

  func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
      print("Finished loading")
  }

  func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
      print("Failed to load with error: \(error.localizedDescription)")
  }
  
  enum AIError: Error {
    case FileNotFound
    case webviewerNotSet
  }
}
