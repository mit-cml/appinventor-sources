// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

class ListPickerTests: XCTestCase {

  var testForm: ReplForm!
  var testListPicker: ListPicker!

  override func setUp() {
    testForm = ReplForm()
    testListPicker = ListPicker(testForm)
  }

  func testSelectionIndex() {
    testListPicker.Elements = ["apple", "banana", "cantalope"] as [AnyObject]

    // Setting the Selection to a valid item should yield the 1-based index of the selection
    testListPicker.Selection = "apple"
    XCTAssertEqual(1, testListPicker.SelectionIndex)
    XCTAssertEqual("apple", testListPicker.Selection)

    // Setting the SelectionIndex to a valid index should yield the same
    testListPicker.SelectionIndex = 2
    XCTAssertEqual(2, testListPicker.SelectionIndex)
    XCTAssertEqual("banana", testListPicker.Selection)

    // Setting the SelectionIndex to 0 should reset the Selection and SelectionIndex
    testListPicker.SelectionIndex = 0
    XCTAssertEqual(0, testListPicker.SelectionIndex)
    XCTAssertEqual("", testListPicker.Selection)

    // Setting the SelectionIndex out of range should reset the Selection and SelectionIndex
    testListPicker.SelectionIndex = 5
    XCTAssertEqual(0, testListPicker.SelectionIndex)
    XCTAssertEqual("", testListPicker.Selection)

    // Selecting an element in the list should update the Selection and SelectionIndex
    testListPicker.tableView(UITableView(), didSelectRowAt: IndexPath(row: 2, section: 0))
    XCTAssertEqual(3, testListPicker.SelectionIndex)
    XCTAssertEqual("cantalope", testListPicker.Selection)
  }

}
