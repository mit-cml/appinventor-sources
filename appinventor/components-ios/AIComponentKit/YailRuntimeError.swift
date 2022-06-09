// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation

@objc open class YailRuntimeError : NSException, Error, @unchecked Sendable {
  @objc public init(_ message: String, _ errorType: String) {
    super.init(name: errorType as NSString as NSExceptionName, reason: message, userInfo: nil)
  }
  
  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}
