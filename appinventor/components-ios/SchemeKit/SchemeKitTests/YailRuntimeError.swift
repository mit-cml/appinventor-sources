//
//  YailRuntimeError.swift
//  SchemeKit
//
//  Created by Evan Patton on 10/9/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

@objc public class YailRuntimeError : NSException {
  public init(_ message: String, _ errorType: String) {
    super.init(name: NSExceptionName(rawValue: errorType), reason: message, userInfo: nil)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
}
