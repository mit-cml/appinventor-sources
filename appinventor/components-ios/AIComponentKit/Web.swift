//
//  Web.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

open class Web: NonvisibleComponent {
  fileprivate var _url = ""
  fileprivate var _requestHeaders = YailList()
  fileprivate var _allowCookies = false
  fileprivate var _saveResponse = false
  fileprivate var _responseFileName = ""
  fileprivate var _cookieStorage = HTTPCookieStorage()

  open var Url: String {
    get {
      return _url
    }
    set(url) {
      _url = url
    }
  }
  
  open var RequestHeaders: YailList {
    get {
      return _requestHeaders
    }
    set(list) {
      if validateRequestHeaders(list) {
        _requestHeaders = list
      }
    }
  }

  open var AllowCookies: Bool {
    get {
      return _allowCookies
    }
    set(allowCookies) {
      _allowCookies = allowCookies
    }
  }

  fileprivate func validateRequestHeaders(_ list: YailList) -> Bool {
    _form?.dispatchErrorOccurredEvent(self, "RequestHeaders", ErrorMessages.ERROR_WEB_REQUEST_HEADER_NOT_LIST.code, ErrorMessages.ERROR_WEB_REQUEST_HEADER_NOT_LIST.message)
    return true
  }
}
