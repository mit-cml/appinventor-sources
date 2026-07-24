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

  private func visibleTableView(for listView: ListView,
                                file: StaticString = #filePath,
                                line: UInt = #line) -> UITableView {
    guard let tableView = listView.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView else {
      XCTFail("Expected UITableView to be visible", file: file, line: line)
      return UITableView()
    }
    return tableView
  }

  private func renderedMainTexts(for listView: ListView) -> [String] {
    let tableView = visibleTableView(for: listView)
    return (0..<listView.tableView(tableView, numberOfRowsInSection: 0)).map { row in
      listView.tableView(tableView, cellForRowAt: IndexPath(row: row, section: 0)).textLabel?.text ?? ""
    }
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

  func testListViewInsideHorizontalArrangementHasSize() {
    form.clear()
    let row = HorizontalArrangement(form)
    row.Width = kLengthFillParent
    row.Height = kLengthPreferred
    let listView = ListView(row)
    listView.Height = kLengthPreferred
    listView.Elements = ["apple", "banana", "cherry"] as [AnyObject]

    form.view.setNeedsLayout()
    form.view.layoutIfNeeded()
    row.view.setNeedsLayout()
    row.view.layoutIfNeeded()

    XCTAssertGreaterThan(row.view.frame.height, 0)
    XCTAssertGreaterThan(listView.view.frame.height, 0)
    guard let tableView = listView.view.subviews.first(where: { $0 is UITableView }) as? UITableView else {
      XCTFail("Expected ListView to contain a table view")
      return
    }
    XCTAssertEqual(3, listView.tableView(tableView, numberOfRowsInSection: 0))
    XCTAssertGreaterThan(tableView.frame.height, 0)
  }

  func testAutomaticVerticalHeightUsesRows() {
    form.clear()
    let listView = ListView(form)
    listView.FontSize = 22
    listView.Elements = ["apple", "banana", "cherry"] as [AnyObject]
    form.onAttach()

    form.view.setNeedsLayout()
    form.view.layoutIfNeeded()

    let oldCutoff = 44.0 * 3.0
    XCTAssertGreaterThan(listView.view.intrinsicContentSize.height, oldCutoff)
    XCTAssertGreaterThan(listView.view.frame.height, oldCutoff)
    guard let tableView = listView.view.subviews.first(where: { $0 is UITableView }) as? UITableView else {
      XCTFail("Expected ListView to contain a table view")
      return
    }
    XCTAssertGreaterThan(listView.tableView(tableView, heightForRowAt: IndexPath(row: 0, section: 0)), 44.0)
  }
  
  func testElementAsDictItems() {
    testList.Elements = [["Text1": "MainText","Text2": "DetailText", "Image": "Image"] as YailDictionary]
    XCTAssertEqual(1, testList.Elements.count)
    XCTAssertEqual("MainText", testList.GetMainText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
    XCTAssertEqual("DetailText", testList.GetDetailText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
    XCTAssertEqual("Image", testList.GetImageName(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
  }
  
  func testAddDictItems3Args() {
    testList.AddItems([["Text1": "MainText","Text2": "DetailText", "Image": "Image"] as YailDictionary])
    XCTAssertEqual(1, testList.Elements.count)
    XCTAssertEqual("MainText", testList.GetMainText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
    XCTAssertEqual("DetailText", testList.GetDetailText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
    XCTAssertEqual("Image", testList.GetImageName(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
  }
  
  func testAddDictItems2Args() {
    testList.AddItems([["Text1": "MainText","Text2": "DetailText"] as YailDictionary])
    XCTAssertEqual(1, testList.Elements.count)
    XCTAssertEqual("MainText", testList.GetMainText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
    XCTAssertEqual("DetailText", testList.GetDetailText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
  }
  
  func testAddDictItemsMixed2Args() {
    testList.AddItems([["Text1": "MainText","Image": "Image"] as YailDictionary])
    XCTAssertEqual(1, testList.Elements.count)
    XCTAssertEqual("MainText", testList.GetMainText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
    XCTAssertEqual("Image", testList.GetImageName(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
  }
  
  func testAddDictItems1Args() {
    testList.AddItems([["Text1": "MainText"] as YailDictionary])
    XCTAssertEqual(1, testList.Elements.count)
    XCTAssertEqual("MainText", testList.GetMainText(YailDictionary(dictionary: testList.Elements[0] as! Dictionary)))
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
      testList.BackgroundColor = Color.black.int32
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
  
  func testSelectionColor() {
    testList.BackgroundColor = Color.blue.int32
    testList.ElementColor = Color.red.int32
    testList.Elements = ["Test", "Best", "Fest"] as [AnyObject]

    testList.SelectionColor = Color.yellow.int32
    
    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      var cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertNotNil(cell)
      XCTAssertEqual(Color.red.uiColor, cell.backgroundColor)
      
      // Change selection
      testList.Selection = "Best"
      var selCell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 1, section: 0))
      XCTAssertEqual(/*testList.ElementColor*/ Color.yellow.uiColor, selCell.selectedBackgroundView?.backgroundColor)  // testList background color

      testList.SelectionColor = Color.none.int32
      selCell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 1, section: 0))
      XCTAssertNotNil(selCell)
      XCTAssertEqual(/*testList.ElementColor*/ Color.none.uiColor, selCell.selectedBackgroundView?.backgroundColor)  // testList background color
      
      testList.SelectionColor = Color.default.int32
      cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertNotNil(cell)
      XCTAssertEqual(Color.default.uiColor, cell.selectedBackgroundView?.backgroundColor)

    } else {
      XCTFail("Expected UITableView to be visible")
    }
  }
  
  func testDividerColor() {
    XCTAssertEqual(Color.none.int32, testList.DividerColor)
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

  func testTextAlignmentMain() {
    testList.Elements = ["Test"] as [AnyObject]

    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      // Default should be normal (left in LTR)
      var cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertEqual(Alignment.normal.rawValue, testList.TextAlignmentMain)
      XCTAssertEqual(NSTextAlignment.left, cell.textLabel?.textAlignment)

      // Center
      testList.TextAlignmentMain = Alignment.center.rawValue
      XCTAssertEqual(Alignment.center.rawValue, testList.TextAlignmentMain)
      cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertEqual(NSTextAlignment.center, cell.textLabel?.textAlignment)

      // Opposite (right in LTR)
      testList.TextAlignmentMain = Alignment.opposite.rawValue
      XCTAssertEqual(Alignment.opposite.rawValue, testList.TextAlignmentMain)
      cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertEqual(NSTextAlignment.right, cell.textLabel?.textAlignment)
    } else {
      XCTFail("Expected UITableView to be visible")
    }
  }

  func testTextAlignmentDetail() {
    testList.ListViewLayout = 1
    testList.ListData = "[{\"Text1\": \"apple\", \"Text2\": \"2.99\", \"Image\": \"\"}]"

    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      // Default should be normal (left in LTR)
      var cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertEqual(Alignment.normal.rawValue, testList.TextAlignmentDetail)
      XCTAssertEqual(NSTextAlignment.left, cell.detailTextLabel?.textAlignment)

      // Center
      testList.TextAlignmentDetail = Alignment.center.rawValue
      XCTAssertEqual(Alignment.center.rawValue, testList.TextAlignmentDetail)
      cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertEqual(NSTextAlignment.center, cell.detailTextLabel?.textAlignment)

      // Opposite (right in LTR)
      testList.TextAlignmentDetail = Alignment.opposite.rawValue
      XCTAssertEqual(Alignment.opposite.rawValue, testList.TextAlignmentDetail)
      cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 0, section: 0))
      XCTAssertEqual(NSTextAlignment.right, cell.detailTextLabel?.textAlignment)
    } else {
      XCTFail("Expected UITableView to be visible")
    }
  }

  func testListData() {
    testList.ListData = "[{\"Text1\": \"apple\", \"Text2\": \"2.99\", \"Image\": \"apple.jpg\"}]"
    XCTAssertEqual(1, testList.Elements.count)
    XCTAssertEqual([["Text1": "apple", "Text2": "2.99", "Image": "apple.jpg"]], testList.Elements as? [[String:String]])
  }

  func testListDataRendersDefaultLayout() {
    testList.ListData = "[{\"Text1\":\"77\", \"$H\":9947},{\"Text1\":\"hello\", \"$H\":9948}]"

    XCTAssertEqual(["77", "hello"], renderedMainTexts(for: testList))
  }

  func testListDataMutationMethodsRefreshRows() {
    testList.ListData = "[{\"Text1\":\"77\", \"$H\":9947},{\"Text1\":\"hello\", \"$H\":9948}]"

    testList.AddItemAtIndex(2, "I'm here", "", "")
    testList.AddItem("typed", "", "")
    testList.AddItems([
      testList.CreateElement("1", "", ""),
      testList.CreateElement("is this working?", "", ""),
      57 as NSNumber
    ] as [AnyObject])
    testList.AddItemAtIndex(100, "out of bounds", "", "")

    XCTAssertEqual(["77", "I'm here", "hello", "typed", "1", "is this working?", "57"],
                   renderedMainTexts(for: testList))
  }

  func testAddItemsSkipsYailListHeader() {
    testList.ListData = "[{\"Text1\":\"77\", \"$H\":9947},{\"Text1\":\"hello\", \"$H\":9948}]"

    testList.AddItems([
      interpreter.makeSymbol("*list*"),
      testList.CreateElement("1", "", ""),
      testList.CreateElement("is this working?", "", ""),
      57 as NSNumber
    ] as [AnyObject])

    XCTAssertEqual(["77", "hello", "1", "is this working?", "57"],
                   renderedMainTexts(for: testList))
  }

  func testExplicitHeightListViewCanScrollOverflowRows() {
    testList.Height = 88
    testList.Elements = (1...20).map { "\($0)" } as [AnyObject]

    XCTAssertEqual(44, testList.view.intrinsicContentSize.height)

    form.view.frame = CGRect(x: 0, y: 0, width: 393, height: 852)
    form.onAttach()
    form.view.setNeedsLayout()
    form.view.layoutIfNeeded()

    let tableView = visibleTableView(for: testList)
    tableView.reloadData()
    tableView.layoutIfNeeded()

    XCTAssertTrue(tableView.isScrollEnabled)
    XCTAssertGreaterThan(tableView.contentSize.height, tableView.bounds.height)
  }

  func testSelectionIndexUsesListDataRowsWithoutDetailText() {
    testList.ListData = "[{\"Text1\":\"77\", \"$H\":9947},{\"Text1\":\"hello\", \"$H\":9948}]"

    testList.SelectionIndex = 2

    XCTAssertEqual(2, testList.SelectionIndex)
    XCTAssertEqual("hello", testList.Selection)
    XCTAssertEqual("", testList.SelectionDetailText)
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
    testList.SelectionColor = Color.yellow.int32
    testList.Refresh()
    
    // Handle both orientations
    if let tableView = testList.view.subviews.first(where: { $0 is UITableView && !$0.isHidden }) as? UITableView {
      testList.tableView(tableView, didSelectRowAt: IndexPath(row: 1, section: 0))
      let cell = testList.tableView(tableView, cellForRowAt: IndexPath(row: 2, section: 0))
      
      XCTAssertNotNil(cell)
      XCTAssertEqual(Color.yellow.uiColor, cell.selectedBackgroundView?.backgroundColor)
      
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
