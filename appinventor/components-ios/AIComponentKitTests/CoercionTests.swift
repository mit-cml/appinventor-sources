// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class CoercionTests: AppInventorTestCase {
  func testToListOfStringCoercion() {
    let x = [1, 2, 3, 4]
    XCTAssertEqual(["1", "2", "3", "4"], x.toStringArray())
  }

  func testToListOfStringFromYail() {
    let ListView1 = ListView(form)
    let ListPicker1 = ListPicker(form)
    XCTAssertTrue(addComponent(ListView1, named: "ListView1"))
    XCTAssertTrue(addComponent(ListPicker1, named: "ListPicker1"))
    let yail = """
      (let ((x '(*list* 1 2 3 "a" "b" "c")))
        (set-and-coerce-property! ListView1 'Elements x 'list)
        (set-and-coerce-property! ListPicker1 'Elements x 'list))
    """
    interpreter.evalForm(yail)
    XCTAssertEqual(["1", "2", "3", "a", "b", "c"], ListView1.Elements as? [String])
    XCTAssertEqual(["1", "2", "3", "a", "b", "c"], ListPicker1.Elements as? [String])
  }
}
