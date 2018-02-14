// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

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
