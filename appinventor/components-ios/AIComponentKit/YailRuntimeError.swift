//
//  YailRuntimeError.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/9/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

@objc open class YailRuntimeError : NSException, Error {
  @objc public init(_ message: String, _ errorType: String) {
    super.init(name: errorType as NSString as NSExceptionName, reason: message, userInfo: nil)
  }
  
  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}
