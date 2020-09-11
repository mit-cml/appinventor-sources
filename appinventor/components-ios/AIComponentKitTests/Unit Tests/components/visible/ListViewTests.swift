// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

import XCTest
@testable import AIComponentKit

class ListViewTests: XCTestCase {

  var testForm: ReplForm!
  var testList: ListView!

  override func setUp() {
    testForm = ReplForm()
    testList = ListView(testForm)
  }

  func testSelectionIndex() {
    testList.Elements = ["apple", "banana", "cherry"] as [AnyObject]
    testList.Selection = "apple"
    XCTAssertEqual(1, testList.SelectionIndex)
    XCTAssertEqual("apple", testList.Selection)
    testList.Selection = "tomato"
    XCTAssertEqual(0, testList.SelectionIndex)
    XCTAssertEqual("", testList.Selection)
    testList.tableView(UITableView(), didSelectRowAt: IndexPath.init(row: 1, section: 0))
    XCTAssertEqual(2, testList.SelectionIndex)
    XCTAssertEqual("banana", testList.Selection)
  }

}
