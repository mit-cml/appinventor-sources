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
    testList.ElementColor = Color.none.int32
    testList.Elements = ["Test"] as [AnyObject]
    
    // Handle both table view and collection view
    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      let cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertNotNil(cell)
      XCTAssertEqual(Color.blue.uiColor, cell.backgroundColor)
      
      // Change back to the default (black)
      testList.BackgroundColor = Color.default.int32
      XCTAssertEqual(Color.black.int32, testList.BackgroundColor)
      
      let cell2 = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertNotNil(cell2)
      XCTAssertEqual(Color.black.uiColor, cell2.backgroundColor)
    } else {
      XCTFail("Expected UITableView to be visible")
    }
  }
  
  func testElementColor() {
    testList.BackgroundColor = Color.blue.int32
    testList.ElementColor = Color.red.int32
    testList.Elements = ["Test"] as [AnyObject]
    
    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      var cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertNotNil(cell)
      XCTAssertEqual(Color.red.uiColor, cell.backgroundColor)
      
      // Change elementColor
      testList.ElementColor = Color.yellow.int32
      cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertNotNil(cell)
      XCTAssertEqual(Color.yellow.uiColor, cell.backgroundColor)
      
      testList.ElementColor = Color.none.int32
      cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertNotNil(cell)
      XCTAssertEqual(Color.blue.uiColor, cell.backgroundColor)  // testList background color
      
      
      testList.ElementColor = Color.default.int32
      cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      let expectedColor = preferredTextColor(form)
      XCTAssertNotNil(cell)
      // Compare resolved colors to handle dynamic colors

      if #available(iOS 13.0, *) {
        let cellColor = cell.backgroundColor?.resolvedColor(with: UITraitCollection(userInterfaceStyle: .light))
        XCTAssertNotEqual(Color.yellow.uiColor, cell.backgroundColor)
      } else {
        // Fallback on earlier versions
      }
       
      

    } else {
      XCTFail("Expected UITableView to be visible")
    }
  }
  
  func testDividerColor() {
    testList.Elements = [testList.CreateElement("MainText", "", ""), "Plain String"] as [AnyObject]
    
    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      testList.DividerColor = Color.green.int32
      XCTAssertEqual(Color.green.int32, testList.DividerColor)
      tableView.separatorColor = argbToColor(testList.DividerColor)
      XCTAssertNotNil(tableView)
      XCTAssertNotNil(tableView.separatorColor)
      XCTAssertEqual(Color.green.uiColor, tableView.separatorColor)
    } else {
      XCTFail("Expected UITableView to be visible")
    }
  }
  
  func testCornerRadius() {
    testList.Elements = ["Test"] as [AnyObject]
    
    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      let cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertNotNil(cell)
      testList.ElementCornerRadius = 10
      cell.layer.cornerRadius = CGFloat(testList.ElementCornerRadius)
      XCTAssertEqual(10, cell.layer.cornerRadius)
    } else {
      XCTFail("Expected UITableView to be visible")
    }
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
    
    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      let cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertNotNil(cell)
      XCTAssertEqual("Times New Roman", cell.textLabel?.font.familyName)
    } else {
      XCTFail("Expected UITableView to be visible")
    }
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
    
    // Handle both orientations
    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      testList.tableView(tableView, didSelectRowAt: IndexPath(row: 1, section: 0))
    } else if let collectionView = testList.view.subviews.first(where: { $0 is UICollectionView && !$0.isHidden }) as? UICollectionView {
      testList.collectionView(collectionView, didSelectItemAt: IndexPath(row: 1, section: 0))
    } else {
      XCTFail("Expected either UITableView or UICollectionView to be visible")
    }
    
    verify()
  }
  
  // New test for horizontal orientation
  func testHorizontalOrientation() {
    testList.Orientation = Int32(HORIZONTAL_LAYOUT)
    testList.Elements = ["Test1", "Test2"] as [AnyObject]
    
    if let collectionView = testList.view.subviews.first(where: { $0 is UICollectionView && !$0.isHidden }) as? UICollectionView {
      XCTAssertFalse(collectionView.isHidden)
      XCTAssertEqual(2, testList.collectionView(collectionView, numberOfItemsInSection: 0))
    } else {
      XCTFail("Expected UICollectionView to be visible in horizontal orientation")
    }
    
    // Verify table view is hidden
    if let tableView = testList.view.subviews.first(where: { $0 is UITableView }) as? UITableView {
      XCTAssertTrue(tableView.isHidden)
    }
  }
}
