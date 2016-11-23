//
//  Web.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class Web: NonvisibleComponent {
  private var _url = ""
  private var _requestHeaders = YailList()
  private var _allowCookies = false
  private var _saveResponse = false
  private var _responseFileName = ""
  private var _cookieStorage = HTTPCookieStorage()

  public var Url: String {
    get {
      return _url
    }
    set(url) {
      _url = url
    }
  }
  
  public var RequestHeaders: YailList {
    get {
      return _requestHeaders
    }
    set(list) {
      if validateRequestHeaders(list: list) {
        _requestHeaders = list
      }
    }
  }

  public var AllowCookies: Bool {
    get {
      return _allowCookies
    }
    set(allowCookies) {
      _allowCookies = allowCookies
    }
  }

  private func validateRequestHeaders(list: YailList) -> Bool {
    _form?.dispatchErrorOccurredEvent(self, "RequestHeaders", ErrorMessages.ERROR_WEB_REQUEST_HEADER_NOT_LIST.code, ErrorMessages.ERROR_WEB_REQUEST_HEADER_NOT_LIST.message)
    return true
  }
}
