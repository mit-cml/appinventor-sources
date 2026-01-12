// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class FormTests: AppInventorTestCase {
  func testErrorHandlerInvoked() {
    expectToReceiveEvent(on: form, named: "ErrorOccurred") { (_ arguments: [AnyObject]) in
      XCTAssertEqual(self.form, arguments[0] as? Form)
      XCTAssertEqual("Test", arguments[1] as! String)
      NSLog("Testing passed")
    }
    form.dispatchErrorOccurredEvent(form, "Test", Int32(ErrorMessage.ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED.rawValue))
    verify()
  }
}
