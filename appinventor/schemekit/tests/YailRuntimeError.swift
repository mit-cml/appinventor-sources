// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@objc public class YailRuntimeError : NSException, Error, @unchecked Sendable {
  @objc public init(_ message: String, _ errorType: String) {
    super.init(name: NSExceptionName(rawValue: errorType), reason: message, userInfo: nil)
  }
  
  @objc required public init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
}
