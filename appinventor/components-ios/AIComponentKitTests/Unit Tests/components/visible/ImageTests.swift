// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

import XCTest
@testable import AIComponentKit

class ImageTests: XCTestCase {

  var image: Image!

  override func setUp() {
    image = Image(Form())
  }

  // Tests the fix for appinventor-sources-ios#310
  func testVisible() {
    XCTAssert(image.Visible)
    XCTAssertFalse(image.view.isHidden)
    image.Visible = false
    XCTAssertFalse(image.Visible)
    XCTAssert(image.view.isHidden)
  }
}
