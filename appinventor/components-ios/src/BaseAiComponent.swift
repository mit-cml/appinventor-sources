// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import WebKit
import Zip
import ZIPFoundation

fileprivate let MODEL_PATH_SUFFIX = ".mdl"

/**
 * The `BaseAiComponent` class serves as a starting point for any AI-enabled components that rely
 * on a `WebViewer` to execute their JavaScript based models.
 */
@objc open class BaseAiComponent: NonvisibleComponent,  WKScriptMessageHandler, WKURLSchemeHandler{
  public static let ERROR_WEBVEWER_REQUIRED = -7
  public static let ERROR_WEBVIEWER_NOT_SET =
      "You must specify a WebViewer using the WebViewer designer property before you can call"
  public static let ERROR_CLASSIFICATION_FAILED = -2;
  public static let ERROR_INVALID_MODEL_FILE = -8;

  private var _modelPath: String? = nil
  internal var _webview: WKWebView? = nil
  private var _webviewer: WebViewer?
  private var assetPath: String
  private var transferModelPrefix: String? = nil
  private var personalModelPrefix: String? = nil
  private var callbacks: [String:(String)->Void] = [:]
  private var _initialized = false

  /**
   * Constructs a new `BaseAiComponent` that loads the file named by `basePath` into the WebViewer
   * it is connected to.
   *
   * - Parameter container: The container for the component (i.e., `Form`).
   * - Parameter basePath: The base name of the HTML file to load with the component's logic
   */
  @objc public convenience init(_ container: ComponentContainer, _ basePath: String) {
    self.init(container, basePath, nil, nil)
  }

  /**
   * Constructs a new `BaseAiComponent` that loads the file named by `basePath` into the WebViewer
   * it is connected to. If specified, URLs matching the `transferModelPrefix` and
   * `personalModelPrefix` will be interecepted and mapped to local assets rather than being
   * loaded over the network.
   *
   * - Parameter container: The container for the component (i.e., `Form`).
   * - Parameter basePath: The base name of the HTML file to load with the component's logic
   * - Parameter transferModelPrefix: The optional prefix string to match the base model used in transfer learning
   * - Parameter personalModelPrefix: The optional prefix string to match the personalized layers created during transfer learning
   */
  @objc public init(_ container: ComponentContainer, _ basePath: String, _ transferModelPrefix: String?, _ personalModelPrefix: String?) {
    assetPath = basePath
    self.transferModelPrefix = transferModelPrefix
    self.personalModelPrefix = personalModelPrefix
    super.init(container)
    registerHandler(named: "ready", callback: self.onModelReady(_:))
    registerHandler(named: "reportResult", callback: self.onReportResult(_:))
    registerHandler(named: "error", callback: self.onError(_:))
  }

  // MARK: Properties

  /**
   * Sets the model file to use for lookups when the JavaScript code requests a model.
   *
   * - Parameter path: The name of the model file, ending in .mdl
   */
  @objc public func setModel(_ path: String) {
    if path.hasSuffix(MODEL_PATH_SUFFIX) {
      _modelPath = path
    } else {
      _form?.dispatchErrorOccurredEvent(self, "Model", ErrorMessage.ERROR_MODEL_AI, "\(BaseAiComponent.ERROR_INVALID_MODEL_FILE): Invalid model file format. Files must be of format \(MODEL_PATH_SUFFIX)")
    }
  }

  /**
   * Returns whether the component has been successfully initialized or not.
   */
  @objc public var isInitialized: Bool {
    _initialized
  }

  /**
   * The WebViewer to use for loading the model JavaScript and other logic.
   */
  @objc open var WebViewer: WebViewer {
    get {
      return _webviewer!
    }
    set {
      _webviewer = newValue
      newValue.aiSchemeHandler = self
      configureWebView(newValue.view as! WKWebView)
    }
  }

  // MARK: Methods

  /**
   * Initialize the component. This is called by the App Inventor runtime.
   */
  @objc public func Initialize() {
    guard let webview = _webview else {
      _form?.dispatchErrorOccurredEvent(self, "WebViewer", ErrorMessage.ERROR_WEBVIEW_AI, BaseAiComponent.ERROR_WEBVEWER_REQUIRED)
      return
    }
    if let view = _form?.view, !webview.isDescendant(of: view), let viewer = _webviewer {
      // NB: For iOS, the WKWebView must be attached to the view hierarchy to run Javascript.
      // To accomodate this, we set the width/height to be zero and then set the view to be visible
      viewer.Height = 0
      viewer.Width = 0
      viewer._container?.setVisible(component: viewer, to: true)
      // Wait for the view to get attached to the UI and then try again...
      DispatchQueue.main.async {
        self.Initialize()
      }
      return
    }
    _initialized = true
    if Bundle(for: BaseAiComponent.self).url(forResource: assetPath, withExtension: "html") != nil {
      let request = URLRequest(url: URL(string: "appinventor://localhost/\(assetPath).html")!)
      webview.load(request)
      print("request loaded")
    } else {
      print("Starting resource \(assetPath) not loaded")
    }

  }

  // MARK: Events

  /**
   * `ModelReady` is called when the model has been loaded and is ready to run.
   */
  open func ModelReady() {}

  /**
   * `GotResult(_:)` is called when the model has computed some results.
   */
  open func GotResult(_ result: AnyObject) {}

  /**
   * `Error(_:)` is called when the model encounters an error.
   */
  open func Error(_ errorCode: Int32){}

  // MARK: Private Implementation

  open func configureWebView(_ webview: WKWebView) {
    debugPrint("configurewebview called")
    _webview = webview
    let fqcn = "\(type(of: self))"
    let name = fqcn.contains("\\.") ? fqcn.split(".").last ?? fqcn : fqcn
    webview.configuration.userContentController.add(self, name: name)
    var source = "const \(fqcn) = {\n"
    for entry in callbacks {
      let key = entry.key
      source += """
      \(key): function() {
        window.webkit.messageHandlers.\(name).postMessage({
          functionCall: '\(key)',
          args: JSON.stringify(Array.prototype.slice.call(arguments))
        })
      },
      """
    }
    source += "}\n"
    debugPrint(source)
    let userScript = WKUserScript(source: source, injectionTime: .atDocumentStart, forMainFrameOnly: true)
    webview.configuration.userContentController.addUserScript(userScript)
  }

  internal func assertWebView(_ method: String, _ frontFacing: Bool = true) throws {
    if _webview == nil {
      throw AIError.webviewerNotSet
    }
  }

  /**
   * Register a handler relationship between the JavaScript code and the Swift code. By default,
   * `BaseAiComponent` will register handlers for `ready`, `reportResult`, and `error`. Handlers
   * must be registered prior to the `WebViewer` property being set, ideally in the `init` function.
   *
   * The handler should take a single argument that is a `String` containing the JSON-serialized
   * arguments. For example:
   *
   *    ```
   *    func doFoo(_ argdata: String) {
   *      do {
   *        // Parse args into an array
   *        guard let args = try getYailObjectFromJson(args, true) as? YailList<AnyObject> else {
   *          return
   *        }
   *      } catch {
   *      }
   *    }
   *
   *    registerHandler("foo": self.doFoo(_:))
   *    ```
   *
   * - Parameter name: The name of the handler. This must be a valid JavaScript identifier.
   * - Parameter callback: The callback function to invoke when a message is posted to the handler
   *                       from JavaScript.
   */
  func registerHandler(named name: String, callback: @escaping (String)->Void) {
    if callbacks.first(where: { (key: String, value: (String) -> Void) in
      key == name
    }) != nil {
      print("Overwriting existing AI handler for \(name)")
    }
    callbacks[name] = callback
  }

  /**
   * Unregister a handler with the given name.
   *
   * - Parameter name: The name of the handler to remove.
   * - Returns: true if the handler was previously registered, otherwise false.
   */
  func unregisterHandler(named name: String) -> Bool {
    return callbacks.removeValue(forKey: name) == nil
  }

  // MARK: WKScriptMessageHandler

  public func userContentController(_ userContentController: WKUserContentController,
                                    didReceive message: WKScriptMessage) {
    guard let dict = message.body as? [String: Any],
          let functionCall = dict["functionCall"] as? String,
          let args = dict["args"] else {
      print("JSON Error message not recieved")
      return
    }
    debugPrint(message.body)
    guard let handler = callbacks[functionCall] else {
      print("No handler registered for \(functionCall)")
      return
    }
    handler(args as? String ?? "[]")
  }

  /**
   * `onModelReady` is called when the model reports that it is ready. The arguments from
   * JavaScript are passed via `args`. Subclasses can override this method to process the
   * arguments.
   */
  open func onModelReady(_ args: String) {
    ModelReady()
  }

  /**
   * `onReportResult` is called when the model reports results. By default, it will process the
   * arguments and pass the first argument as a `String` to the `GotResult(_:)` event. Subclasses
   * can override either this method or `GotResult` to adjust how the arguments are (further)
   * processed.
   */
  open func onReportResult(_ args: String) {
    do {
      guard let arguments = try getYailObjectFromJson(args, true) as? YailList<AnyObject> else {
        return
      }
      let result = try getYailObjectFromJson(arguments[1] as? String, true)
      GotResult(result)
    } catch {
      print("Error parsing JSON from web view function reportResult")
    }
  }

  /**
   * `onError` is called when an error ocurrs. By default, it extracts the first argument, which
   * should be an error code and passes it to the `Error(_:)` event handler.
   */
  open func onError(_ args: String) {
    print("error")
    do {
      guard let result = try getYailObjectFromJson(args, true) as? YailList<AnyObject> else {
        print("Unable to parse error: \(args)")
        return
      }
      debugPrint(result)
      Error(result[0] as? Int32 ?? -999)
    } catch {
      print("Error parsing JSON from web view function error")
    }
  }

  // MARK: WKURLSchemeHandler

  public func webView(_ webView: WKWebView, start urlSchemeTask: WKURLSchemeTask) {
    debugPrint("WKURLSchemeHandler")
    var fileData: Data? = nil
    guard let url = urlSchemeTask.request.url?.absoluteString else {
      urlSchemeTask.didFailWithError(AIError.FileNotFound)
      return
    }
    guard let fileName = urlSchemeTask.request.url?.lastPathComponent else {
      urlSchemeTask.didFailWithError(AIError.FileNotFound)
      return
    }
    debugPrint(url)
    if let modelPrefix = transferModelPrefix, url.hasPrefix(modelPrefix) {
      debugPrint("transfer model")
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
      debugPrint("personal model")
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
      debugPrint("Did send data for \(fileName)")
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
