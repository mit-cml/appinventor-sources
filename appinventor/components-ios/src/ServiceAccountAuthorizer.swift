// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import GTMSessionFetcher

@objc class ServiceAccountAuthorizer : NSObject, GTMFetcherAuthorizationProtocol {

  let tokenProvider: ServiceAccountTokenProvider
  var request: NSMutableURLRequest? = nil
  var canceled = false

  public init(serviceAccountConfig: Data, scopes: [String]) {
    self.tokenProvider = ServiceAccountTokenProvider(credentialsData: serviceAccountConfig, scopes: scopes)!
    super.init()
    self.userEmail = self.tokenProvider.credentials.ClientEmail
  }

  func authorizeRequest(_ request: NSMutableURLRequest?, completionHandler handler: @escaping (Error?) -> Void) {
    do {
      self.request = request
      self.canceled = false
      let start = DispatchTime.now().uptimeNanoseconds
      try tokenProvider.withToken { token, error in
        print("Time for token = \(Double(DispatchTime.now().uptimeNanoseconds - start)/1000000.0)")
        if let token = token, let accessToken = token.AccessToken, !self.canceled {
          request?.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        }
        self.request = nil
        handler(error)
      }
    } catch {
      self.request = nil
      handler(error)
    }
  }

  func authorizeRequest(_ request: NSMutableURLRequest?, delegate: Any, didFinish sel: Selector) {
    authorizeRequest(request) { error in
      OAuth2CallbackHelper.doCallback(withDelegate: delegate, selector: sel, authorizer: self, request: request, error: error)
    }
  }

  func stopAuthorization() {
    canceled = true
  }

  func stopAuthorization(for request: URLRequest) {
    canceled = true
  }

  func isAuthorizingRequest(_ request: URLRequest) -> Bool {
    return self.request == request as NSURLRequest
  }

  func isAuthorizedRequest(_ request: URLRequest) -> Bool {
    return false
  }

  var userEmail: String?
}
