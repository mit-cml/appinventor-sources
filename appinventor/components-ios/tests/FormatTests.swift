// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

import XCTest
@testable import AIComponentKit

class FormatTests: XCTestCase {
  func testFormat() {
    let out = String(format: "%s", messageArgs: ["foo"])
    XCTAssertEqual("foo", out)
  }

  func testFormat2() {
    let out = String(format: "%s%s", messageArgs: ["fizz", "buzz"])
    XCTAssertEqual("fizzbuzz", out)
  }

  func testFormatNumber() {
    let out = String(format: "%d", messageArgs: [5])
    XCTAssertEqual("5", out)
  }

  func testFormatPercent() {
    let out = String(format: "%%", messageArgs: [])
    XCTAssertEqual("%", out)
  }

  func testNoExpansions() {
    let out = String(format: "foo", messageArgs: [])
    XCTAssertEqual("foo", out)
  }

  func testTrailing() {
    let out = String(format: "%sbar", messageArgs: ["foo"])
    XCTAssertEqual("foobar", out)
  }

  func testLeading() {
    let out = String(format: "foo%s", messageArgs: ["bar"])
    XCTAssertEqual("foobar", out)
  }
}
