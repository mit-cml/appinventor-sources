// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

import XCTest
@testable import AIComponentKit

class ImageTests: XCTestCase {

  var form: Form!
  var image: Image!

  override func setUp() {
    form = Form()
    form.Scrollable = false
    form.Sizing = "Responsive"
    image = Image(form)
  }

  // Tests the fix for appinventor-sources-ios#310
  func testVisible() {
    XCTAssert(image.Visible)
    XCTAssert(image.view.isDescendant(of: form.view))
    image.Visible = false
    XCTAssertFalse(image.Visible)
    XCTAssertFalse(image.view.isDescendant(of: form.view))
  }
}
