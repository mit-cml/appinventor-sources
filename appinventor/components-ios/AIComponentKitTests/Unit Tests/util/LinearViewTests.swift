// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copright © 2020 Massachusetts Institute of Technology, All rights reserved.

import XCTest
@testable import AIComponentKit

class LinearViewTests: XCTestCase {

  var testForm = ReplForm()
  var testView = LinearView()

  override func setUp() {
    testView = LinearView()
  }

  func testAddItem() {
    let Button1 = Button(testForm)
    XCTAssertFalse(testView.contains(Button1.view))
    testView.addItem(LinearViewItem(Button1.view))
    XCTAssertTrue(testView.contains(Button1.view))
  }

  func testRemoveItem() {
    let Button1 = Button(testForm)
    XCTAssertFalse(testView.contains(Button1.view))
    testView.addItem(LinearViewItem(Button1.view))
    XCTAssertTrue(testView.contains(Button1.view))
    testView.removeItem(Button1.view)
    XCTAssertFalse(testView.contains(Button1.view))
  }

  func testVisibility() {
    let Button1 = Button(testForm)
    testView.addItem(LinearViewItem(Button1.view))
    XCTAssertTrue(testView.contains(Button1.view))
    testView.setVisibility(of: Button1.view, to: false)
    XCTAssertFalse(testView.contains(Button1.view))
  }

  func testVisibilityOrdering() {
    let Button1 = Button(testForm)
    let Button2 = Button(testForm)
    let Button3 = Button(testForm)
    testView.addItem(LinearViewItem(Button1.view))
    testView.addItem(LinearViewItem(Button2.view))
    testView.addItem(LinearViewItem(Button3.view))
    XCTAssertEqual(3, testView.arrangedSubviews.count)
    XCTAssertEqual([Button1.view, Button2.view, Button3.view], testView.arrangedSubviews)
    testView.setVisibility(of: Button2.view, to: false)
    testView.setVisibility(of: Button3.view, to: false)
    XCTAssertEqual(1, testView.arrangedSubviews.count)
    testView.setVisibility(of: Button3.view, to: true)
    XCTAssertEqual([Button1.view, Button3.view], testView.arrangedSubviews)
    testView.setVisibility(of: Button2.view, to: true)
    XCTAssertEqual([Button1.view, Button2.view, Button3.view], testView.arrangedSubviews)
  }

  func testInvisibleComponents() {
    testForm.clear()
    let HorizontalArrangement1 = HorizontalArrangement(testForm)
    HorizontalArrangement1.Width = kLengthFillParent
    HorizontalArrangement1.Visible = false
    let Label1 = Label(HorizontalArrangement1)
    Label1.Width = -1015
    let Label2 = Label(HorizontalArrangement1)
    Label2.Width = -1060
    let Button1 = Button(HorizontalArrangement1)
    Button1.Width = kLengthFillParent
  }
}
