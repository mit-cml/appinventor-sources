// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import SwiftProtobuf
import Base58Swift

public protocol HasToken {
  associatedtype T
  var token: T { get set }
  var apikey: String { get set }
}

public protocol HasResponse {
  associatedtype T
  var response: T { get }
}

struct ProxyError: Error {
  let message: String

  var localizedError: String {
    return message
  }
}

struct CertificateRoots {
  // Root CAs we use for certifcate trust validation
  static let cas = [readCert("comodo_root")!, readCert("dst_root_x3")!] as NSArray
}

open class ProxiedComponent<
    T: Message,
    Request: HasToken & Message,
    Response: HasResponse & Message
>: NonvisibleComponent, URLSessionDelegate, LifecycleDelegate where Request.T == T {

  private let url: URL
  private var _urlSession: URLSession! = nil

  public init(_ parent: ComponentContainer, _ url: URL) {
    self.url = url
    super.init(parent)
    _urlSession = URLSession(configuration: URLSessionConfiguration.default, delegate: self,
                             delegateQueue: nil)
  }

  @objc open var ApiKey: String = ""

  @objc open var Token: String = ""

  func doRequest(configuration populator: (inout Request) -> (), _ handler: @escaping (Int32, Response?, Error?) -> ()) throws {
    guard let decodedTokenBytes = Base58.base58Decode(Token.hasPrefix("%") ? String(Token.dropFirst()) : Token) else {
      showAlert(message: "Unable to decode api key")
      return
    }
    let decodedToken = Data(decodedTokenBytes)
    let parsedToken = try T(serializedData: decodedToken)
    let body = Request.with {
      $0.token = parsedToken
      if !ApiKey.isEmpty {
        $0.apikey = ApiKey
      }
      populator(&$0)
    }
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.httpBody = try body.serializedData()
    _urlSession.dataTask(with: request) { data, response, error in
      if let response = response as? HTTPURLResponse, let data = data {
        let statusCode = Int32(response.statusCode)
        if response.statusCode == 200 {
          let resp = try? Response(serializedData: data)
          handler(statusCode, resp, nil)
        } else {
          let content = String(data: data, encoding: .utf8) ?? "Unable to decode error message"
          handler(statusCode, nil, ProxyError(message: content))
        }
      } else if let error = error {
        handler(-1, nil, error)
      } else {
        handler(-2, nil, ProxyError(message: "Unknown path"))
      }
    }.resume()
  }

  // MARK: URLSessionDelegate Implementation

  // Handle trust validation
  public func urlSession(_ session: URLSession, didReceive challenge: URLAuthenticationChallenge,
                         completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
    guard challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust else {
      completionHandler(URLSession.AuthChallengeDisposition.performDefaultHandling, nil)
      return
    }
    guard challenge.protectionSpace.host == "chatbot.appinventor.mit.edu" else {
      completionHandler(URLSession.AuthChallengeDisposition.performDefaultHandling, nil)
      return
    }
    guard let trust = challenge.protectionSpace.serverTrust else {
      completionHandler(URLSession.AuthChallengeDisposition.performDefaultHandling, nil)
      return
    }
    SecTrustSetAnchorCertificates(trust, CertificateRoots.cas)
    let credential = URLCredential(trust: trust)
    completionHandler(URLSession.AuthChallengeDisposition.useCredential, credential)
  }

  // MARK: LifecycleDelegate Implementation

  // NB: We must call `_urlSession.invalidateAndCancel()` when we are going away,
  // otherwise the ChatBot and its _urlSession form a retain cycle.

  @objc public func onDelete() {
    _urlSession.invalidateAndCancel()
  }

  @objc public func onDestroy() {
    _urlSession.invalidateAndCancel()
  }
}
