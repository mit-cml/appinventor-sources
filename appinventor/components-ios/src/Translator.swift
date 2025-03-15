// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import Base58Swift

public let TRANSLATOR_SERVICE_URL = "https://tr.appinventor.mit.edu/tr/v1"

@objc open class Translator: NonvisibleComponent, URLSessionDelegate {
  private static let _rootCerts = [readCert("comodo_root")!, readCert("dst_root_x3")!] as NSArray
  private var _urlSession: URLSession! = nil
  private var _apiKey = ""

  public override init(_ container: ComponentContainer) {
    super.init(container)
    _urlSession = URLSession(configuration: URLSessionConfiguration.default, delegate: self,
                             delegateQueue: nil)
  }

  // MARK: Properties

  @objc open var ApiKey: String {
    get {
      return _apiKey
    }
    set {
      _apiKey = newValue
    }
  }

  // MARK: Methods

  @objc open func RequestTranslation(_ languageToTranslateTo: String, _ textToTranslate: String) {
    guard let url = URL(string: TRANSLATOR_SERVICE_URL) else {
      showAlert(message: "Unable to parse translator service URL")
      return
    }
    guard let decodedTokenBytes = Base58.base58Decode(_apiKey) else {
      showAlert(message: "Unable to decode api key")
      return
    }
    let languages = languageToTranslateTo.split("-")
    guard languages.count <= 2 else {
      DispatchQueue.main.async {
        self.GotTranslation(400, "More than two languages provided")
      }
      return
    }
    var srcLanguage = "auto"
    var targetLanguage = languageToTranslateTo
    if languages.count == 2 {
      srcLanguage = languages[0]
      targetLanguage = languages[1]
    }
    let decodedToken = Data(decodedTokenBytes)
    do {
      let parsedToken = try Token(serializedData: decodedToken)
      let body = Request.with {
        $0.token = parsedToken
        $0.totranslate = textToTranslate
        $0.sourcelanguage = srcLanguage
        $0.targetlanguage = targetLanguage
      }
      var request = URLRequest(url: url)
      request.httpMethod = "POST"
      request.httpBody = try body.serializedData()
      _urlSession.dataTask(with: request) { data, response, error in
        DispatchQueue.main.async {
          if let response = response as? HTTPURLResponse, let data = data {
            var content: String? = nil
            if response.statusCode == 200 {
              let resp = try? Response(serializedData: data)
              content = resp?.translated
            } else {
              content = String(data: data, encoding: .utf8) ?? "Unable to decode error message"
            }
            self.GotTranslation(Int32(response.statusCode), content ?? "")
          } else if let error = error {
            print("Error in Translator API call \(error)")
          } else {
            print("Unknown path")
          }
        }
      }.resume()
    } catch {
      print("Error in translation: \(error)")
    }
  }

  // MARK: Events

  @objc open func GotTranslation(_ responseCode: Int32, _ translation: String) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "GotTranslation",
                                    arguments: responseCode as AnyObject, translation as NSString)
    }
  }

  // MARK: URLSessionDelegate Implementation

  public func urlSession(_ session: URLSession, didReceive challenge: URLAuthenticationChallenge,
                         completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
    guard challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust else {
      completionHandler(URLSession.AuthChallengeDisposition.performDefaultHandling, nil)
      return
    }
    guard challenge.protectionSpace.host == "tr.appinventor.mit.edu" else {
      completionHandler(URLSession.AuthChallengeDisposition.performDefaultHandling, nil)
      return
    }
    guard let trust = challenge.protectionSpace.serverTrust else {
      completionHandler(URLSession.AuthChallengeDisposition.performDefaultHandling, nil)
      return
    }
    SecTrustSetAnchorCertificates(trust, Translator._rootCerts)
    let credential = URLCredential(trust: trust)
    completionHandler(URLSession.AuthChallengeDisposition.useCredential, credential)
  }

}
