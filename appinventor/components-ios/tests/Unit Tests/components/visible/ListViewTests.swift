// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

class ListViewTests: AppInventorTestCase {

  var testList: ListView!

  override func setUp() {
    super.setUp()
    testList = ListView(form)
    XCTAssertTrue(addComponent(testList, named: "ListView1"))
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

  func testCreateElement() {
    testList.Elements = [testList.CreateElement("MainText", "DetailText", "Image")]
    XCTAssertEqual(1, testList.Elements.count)
    XCTAssertEqual("MainText", testList.GetMainText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
    XCTAssertEqual("DetailText", testList.GetDetailText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
    XCTAssertEqual("Image", testList.GetImageName(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
  }


  func testBackgroundColor() {
    testList.BackgroundColor = Color.blue.int32
    testList.Elements = ["Test"] as [AnyObject]
    let view = testList.view as! UITableView
    var cell = testList.tableView(view, cellForRowAt: IndexPath(row: 0, section: 0))
    XCTAssertNotNil(cell)
    XCTAssertEqual(Color.blue.uiColor, cell.backgroundColor)

    // Change back to the default (black)
    testList.BackgroundColor = Color.default.int32
    XCTAssertEqual(Color.black.int32, testList.BackgroundColor)

    cell = testList.tableView(view, cellForRowAt: IndexPath(row: 0, section: 0))
    XCTAssertNotNil(cell)
    XCTAssertEqual(Color.black.uiColor, cell.backgroundColor)
  }

  func testMixedElements() {
    testList.Elements = [testList.CreateElement("MainText", "", ""), "Plain String"] as [AnyObject]
    XCTAssertEqual(2, testList.Elements.count)
    // The second element should be promoted to a dictionary
    XCTAssertNotNil(testList.Elements[1] as? Dictionary<String, String>)
  }

  func testFontTypeface() {
    testList.FontTypeface = "2"
    testList.Elements = ["Test"] as [AnyObject]
    testList.Refresh()
    let view = testList.view as! UITableView
    let cell = testList.tableView(view, cellForRowAt: IndexPath(row: 0, section: 0))
    XCTAssertNotNil(cell)
    XCTAssertEqual("Times New Roman", cell.textLabel?.font.familyName)
  }

  func testListData() {
    testList.ListData = "[{\"Text1\": \"apple\", \"Text2\": \"2.99\", \"Image\": \"apple.jpg\"}]"
    XCTAssertEqual(1, testList.Elements.count)
    XCTAssertEqual([["Text1": "apple", "Text2": "2.99", "Image": "apple.jpg"]], testList.Elements as? [[String:String]])
  }

  func testBadListData() {
    testList.ListData = "this is not json"
    XCTAssertEqual(0, testList.Elements.count)
  }

  // MARK: Events

  func testAfterPicking() {
    expectToReceiveEvent(on: testList, named: "AfterPicking") { [self] arguments in
      XCTAssertEqual(2, testList.SelectionIndex)
      XCTAssertEqual("banana", testList.Selection)
    }
    testList.Elements = ["apple", "banana", "cantaloupe"] as [AnyObject]
    testList.Refresh()
    let view = testList.view as! UITableView
    testList.tableView(view, didSelectRowAt: IndexPath(row: 1, section: 0))
    verify()
  }
}
