import Foundation
import WebKit


class FaceExtension: NonvisibleComponent, WKScriptMessageHandler, WKUIDelegate {
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
    }
    
    fileprivate final let _ERROR_WEBVIEWER_NOT_SET: String =  "You must specify a WebViewer using the WebViewer designer property before you can call"
    private let _ERROR_JSON_PARSE_FAILED = 101
    private let _MODEL_URL = "https://tfhub.dev/mediapipe/tfjs-model/facemesh/1/default/1/"
    private let _BACK_CAMERA = "Back"
    private let _FRONT_CAMERA = "Front"

    private var _webview: WKWebView?
    private var _webviewer: WebViewer?
    private var _webviewerApiSource: String = ""
    private var _webviewerApi: WKUserScript?

    private var _faceLandmarks: [String: [Double]] = [:]
//    fileprivate var _minDetectionConfidence: Double = 0.5
//    fileprivate var _minTrackingConfidence: Double = 0.5
//    fileprivate var _cameraMode = "Front"
    internal var _minDetectionConfidence: Double = 0.5
    internal var _minTrackingConfidence: Double = 0.5
    internal var _cameraMode = "Front"
    internal var _initialized = false
    var _enabled = true
    private var _backgroundImage = ""
//    private var width = 350
//    private var height = 200

    private enum IllegalStateError: Error {
        case webviewerNotSet
    }

    public override init(_ parent: ComponentContainer) {
        super.init(parent)
        
        _faceLandmarks["forehead"] = []
        _faceLandmarks["leftCheek"] = []
        _faceLandmarks["rightCheek"] = []
        _faceLandmarks["leftEyebrow"] = []
        _faceLandmarks["rightEyebrow"] = []
        _faceLandmarks["chin"] = []
        _faceLandmarks["leftEyeInnerCorner"] = []
        _faceLandmarks["rightEyeInnerCorner"] = []
        _faceLandmarks["mouthTop"] = []
        _faceLandmarks["mouthBottom"] = []
        _faceLandmarks["leftEyeTop"] = []
        _faceLandmarks["leftEyeBottom"] = []
        _faceLandmarks["rightEyeTop"] = []
        _faceLandmarks["rightEyeBottom"] = []
        _faceLandmarks["rightEarStart"] = []
        _faceLandmarks["leftEarStart"] = []
        _faceLandmarks["noseBottom"] = []
        _faceLandmarks["rightNoseTop"] = []
        _faceLandmarks["leftNoseTop"] = []
        _faceLandmarks["allPoints"] = []
        print("Created FaceExtension")
    }

    private func configureWebView(_ webview: WKWebView) {
        self._webview = webview
        let config = webview.configuration
        let controller = config.userContentController
        controller.add(self, name: "FaceExtension")
    }

  @objc open var WebViewer: WebViewer {
        get {
            return _webviewer!
        }
        set {
            configureWebView(newValue.view as! WKWebView)
            print("configureWebView called")
            if let url = Bundle(for: FaceExtension.self).url(forResource: "assets/index", withExtension: "html") {
                let request = URLRequest(url: url)
                print(request)
                _webview?.load(request)
                print("Request loaded")
            }
        }
    }
  
  @available(iOS 15.0, *)
    public func webView(_ webView: WKWebView,
                requestMediaCapturePermissionFor
                origin: WKSecurityOrigin,initiatedByFrame
                frame: WKFrameInfo,type: WKMediaCaptureType,
                decisionHandler: @escaping (WKPermissionDecision) -> Void) {
                  decisionHandler(.grant)
    }

    @objc open var MinDetectionConfidence: Double {
        get {
            return _minDetectionConfidence
        }
        set {
            _minDetectionConfidence = newValue
            if _initialized {
                do {
                    try assertWebView("MinDetectionConfidence")
                } catch {
                    print(_ERROR_WEBVIEWER_NOT_SET, "MinDetectionConfidence")
                }
            }
        }
    }

    @objc open var MinTrackingConfidence: Double {
        get {
            return _minTrackingConfidence
        }
        set {
            _minTrackingConfidence = newValue
            if _initialized {
                do {
                    try assertWebView("MinTrackingConfidence")
                } catch {
                    print(_ERROR_WEBVIEWER_NOT_SET, "MinTrackingConfidence")
                }
            }
        }
    }

    @objc open var FaceLandmarks: [[Double]] {
        get {
            var landmarks: [[Double]] = []
            for point in _faceLandmarks.values {
                if point.count == 2 {
                    landmarks.append(point)
                }
            }
            return landmarks
        }
    }

    @objc open var BackgroundImage: String = ""

        @objc open var Forehead: [Double] {
            return _faceLandmarks["forehead"] ?? []
        }

        @objc open var NoseBottom: [Double] {
            return _faceLandmarks["noseBottom"] ?? []
        }

        @objc open var Chin: [Double] {
            return _faceLandmarks["chin"] ?? []
        }

        @objc open var LeftCheek: [Double] {
            return _faceLandmarks["leftCheek"] ?? []
        }

        @objc open var RightCheek: [Double] {
            return _faceLandmarks["rightCheek"] ?? []
        }

        @objc open var LeftEyebrow: [Double] {
            return _faceLandmarks["leftEyebrow"] ?? []
        }

        @objc open var RightEyebrow: [Double] {
            return _faceLandmarks["rightEyebrow"] ?? []
        }

        @objc open var LeftEyeInnerCorner: [Double] {
            return _faceLandmarks["leftEyeInnerCorner"] ?? []
        }

        @objc open var RightEyeInnerCorner: [Double] {
            return _faceLandmarks["rightEyeInnerCorner"] ?? []
        }

        @objc open var MouthTop: [Double] {
            return _faceLandmarks["mouthTop"] ?? []
        }

        @objc open var MouthBottom: [Double] {
            return _faceLandmarks["mouthBottom"] ?? []
        }

        @objc open var LeftEyeTop: [Double] {
            return _faceLandmarks["leftEyeTop"] ?? []
        }

        @objc open var LeftEyeBottom: [Double] {
            return _faceLandmarks["leftEyeBottom"] ?? []
        }

        @objc open var RightEyeTop: [Double] {
            return _faceLandmarks["rightEyeTop"] ?? []
        }

        @objc open var RightEyeBottom: [Double] {
            return _faceLandmarks["rightEyeBottom"] ?? []
        }

        @objc open var RightEarStart: [Double] {
            return _faceLandmarks["rightEarStart"] ?? []
        }

        @objc open var LeftEarStart: [Double] {
            return _faceLandmarks["leftEarStart"] ?? []
        }

        @objc open var RightNoseTop: [Double] {
            return _faceLandmarks["rightNoseTop"] ?? []
        }

        @objc open var LeftNoseTop: [Double] {
            return _faceLandmarks["leftNoseTop"] ?? []
        }

        @objc open var FaceWidth: Double {
            return (_faceLandmarks["rightCheek"]?.first ?? 0) - (_faceLandmarks["leftCheek"]?.first ?? 0)
        }

        @objc open var CheekToNoseDistance: Double {
            return (_faceLandmarks["leftNoseTop"]?.first ?? 0) - (_faceLandmarks["leftCheek"]?.first ?? 0)
        }

        @objc open var EyeToMouthHeight: Double {
            return (_faceLandmarks["mouthTop"]?.last ?? 0) - (_faceLandmarks["forehead"]?.last ?? 0)
        }

        @objc open var AllPoints: [Double] {
            return _faceLandmarks["allPoints"] ?? []
        }
    

    @objc open var Enabled: Bool {
        get {
            return _enabled
        }
        set {
            _enabled = newValue
            if _initialized {
                do {
                    try assertWebView("Enabled")
                } catch {
                    print(_ERROR_WEBVIEWER_NOT_SET, "Enabled")
                }
            }
        }
    }

    @objc open var UseCamera: String {
        get {
            return _cameraMode
        }
        set {
            if newValue == _BACK_CAMERA || newValue == _FRONT_CAMERA {
                _cameraMode = newValue
                if _initialized {
                    do {
                        let frontFacing = (newValue == _FRONT_CAMERA)
                        try assertWebView("UseCamera", frontFacing)
                    } catch {
                        print(_ERROR_WEBVIEWER_NOT_SET, "UseCamera")
                    }
                }
            } else {
             _form?.dispatchErrorOccurredEvent(self, "UseCamera", 3300, "Invalid camera selection. Must be either 'Front' or 'Back'.")
            }
        }
    }
    
    //faceMesh Methods

    @objc open func Initialize() {
        if _webview != nil {
            print("Initialized")
            _initialized = true
        }
    }

    @objc open func ModelReady() {
        EventDispatcher.dispatchEvent(of: self, called: "ModelReady")
        print("Dispatched ModelReady")
    }

    @objc open func Error(_ errorCode: AnyObject, _ errorMessage: AnyObject) {
        EventDispatcher.dispatchEvent(of: self, called: "Error", arguments: errorCode, errorMessage)
        print("Dispatched Error")
    }

    @objc open func VideoUpdated() {
        EventDispatcher.dispatchEvent(of: self, called: "VideoUpdated")
        print("Dispatched VideoUpdated")
    }

    private func assertWebView(_ method: String, _ args: Any...) throws {
        if _webview == nil {
            throw IllegalStateError.webviewerNotSet
        }
        switch method {
        case "MinDetectionConfidence":
            _webview?.evaluateJavaScript("setMinDetectionConfidence(\(MinDetectionConfidence));")
        case "MinTrackingConfidence":
            _webview?.evaluateJavaScript("setMinTrackingConfidence(\(MinTrackingConfidence));")
        case "UseCamera":
            _webview?.evaluateJavaScript("setCameraFacingMode(\(args[0] as! Bool));")
        default:
            print("Error: Not a valid method")
        }
    }
    func getYailObjectFromJson(_ json: String?, _ boolean: Bool) throws -> Any {
            guard let data = json?.data(using: .utf8) else {
                throw NSError(domain: "", code: 0, userInfo: [NSLocalizedDescriptionKey: "Invalid JSON"])
            }
            return try JSONSerialization.jsonObject(with: data, options: [])
        }
  
  @objc open func setMarkOrigin() {
      // Implementation of setMarkOrigin
      print("setMarkOrigin called")
      _webview?.evaluateJavaScript("setMarkOrigin();") { (result, error) in
          if let error = error {
              print("Error evaluating JavaScript for setMarkOrigin(): \(error)")
          } else {
              print("setMarkOrigin() called successfully")
          }
      }
  }

}

