// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

class LinearViewTests: XCTestCase {

  var testForm = ReplForm()
  var testView: LinearView!

  override func setUp() {
    testView = LinearView()
    let window = UIWindow()
    window.rootViewController = testForm
    window.addSubview(testForm.view)
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

  func testAutomaticParent() {
    testForm.clear()
    let row1 = HorizontalArrangement(testForm)
    row1.view.accessibilityIdentifier = "row1"
    let row2 = HorizontalArrangement(testForm)
    row2.view.accessibilityIdentifier = "row2"

    row1.setHeightPercent(20)
    row2.Height = kLengthPreferred

    let Button1 = Button(row1)
    Button1.Text = "Text for Button1"
    let Button2 = Button(row1)
    Button2.Text = "Text for Button2"

    Button1.Height = kLengthFillParent
    Button1.setWidthPercent(50)
    Button2.Height = kLengthPreferred

    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    XCTAssertEqual(0.2 * testForm.view.frame.height, row1.view.frame.height, accuracy: 1)
    XCTAssertEqual(100.0, row2.view.frame.height)
    #if DEBUG
    testForm.printViewHierarchy()
    #endif
  }

  func testAutomaticParentswitch() {
    testForm.clear()
    let row1 = HorizontalArrangement(testForm)
    row1.view.accessibilityIdentifier = "row1"
    let row2 = HorizontalArrangement(testForm)
    row2.view.accessibilityIdentifier = "row2"

    row1.setHeightPercent(20)
    row2.Height = kLengthPreferred

    let Button1 = Button(row1)
    Button1.Text = "Text for Button1"
    let Button2 = Button(row1)
    Button2.Text = "Text for Button2"

    Button1.Height = kLengthFillParent
    Button1.setWidthPercent(50)
    Button2.Height = kLengthPreferred

    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    XCTAssertEqual(0.2 * testForm.view.frame.height, row1.view.frame.height, accuracy: 1)
    XCTAssertEqual(100.0, row2.view.frame.height)
    #if DEBUG
    testForm.printViewHierarchy()
    #endif

    testForm.clear()
    let Button3 = Button(testForm)
    Button3.Text = "Text for Button1"
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
    #if DEBUG
    testForm.printViewHierarchy()
    #endif

    // Switch to fill parent on both axes
    Button3.Height = kLengthFillParent
    Button3.Width = kLengthFillParent
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
    #if DEBUG
    testForm.printViewHierarchy()
    #endif
    XCTAssertEqual(0, Button3.view.frame.origin.x)
    XCTAssertEqual(0, Button3.view.frame.origin.y)
    XCTAssertEqual(testForm.view.frame.height, Button3.view.frame.height)
    XCTAssertEqual(testForm.view.frame.width, Button3.view.frame.width)

    testForm.clear()
    let row3 = HorizontalArrangement(testForm)
    row3.view.accessibilityIdentifier = "row3"
    let row4 = HorizontalArrangement(testForm)
    row4.view.accessibilityIdentifier = "row4"

    row3.setHeightPercent(20)
    row4.Height = kLengthPreferred

    let Button4 = Button(row3)
    Button4.Text = "Text for Button1"
    let Button5 = Button(row3)
    Button5.Text = "Text for Button2"

    Button4.Height = kLengthFillParent
    Button4.setWidthPercent(50)
    Button5.Height = kLengthPreferred

    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    XCTAssertEqual(0.2 * testForm.view.frame.height, row3.view.frame.height, accuracy: 1)
    XCTAssertEqual(100.0, row4.view.frame.height)
    #if DEBUG
    testForm.printViewHierarchy()
    #endif
  }

  func testFillParent() {
    testForm.clear()
    let Button1 = Button(testForm)
    Button1.Text = "Text for Button1"
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
    #if DEBUG
    testForm.printViewHierarchy()
    #endif
    let oldH = CGFloat(Button1.Height)
    let oldW = CGFloat(Button1.Width)

    // Switch to fill parent on both axes
    Button1.Height = kLengthFillParent
    Button1.Width = kLengthFillParent
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
    #if DEBUG
    testForm.printViewHierarchy()
    #endif
    XCTAssertEqual(0, Button1.view.frame.origin.x)
    XCTAssertEqual(0, Button1.view.frame.origin.y)
    XCTAssertEqual(testForm.view.frame.height, Button1.view.frame.height)
    XCTAssertEqual(testForm.view.frame.width, Button1.view.frame.width)

    // Switch back to automatic on both axes
    Button1.Height = kLengthPreferred
    Button1.Width = kLengthPreferred
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
    XCTAssertEqual(0, Button1.view.frame.origin.x)
    XCTAssertEqual(0, Button1.view.frame.origin.y)
    XCTAssertEqual(oldH, Button1.view.frame.size.height)
    XCTAssertEqual(oldW, Button1.view.frame.size.width)
    #if DEBUG
    testForm.printViewHierarchy()
    #endif
  }

  func testExampleQuadrant() {
    testForm.clear()
    let row1 = HorizontalArrangement(testForm)
    let row2 = HorizontalArrangement(testForm)
    let cell11 = VerticalArrangement(row1)
    let cell12 = VerticalArrangement(row1)
    let cell21 = VerticalArrangement(row2)
    let cell22 = VerticalArrangement(row2)

    // Make a 2x2 equal size grid filling the screen
    [row1, row2, cell11, cell12, cell21, cell22].forEach { (arrangement) in
      arrangement.Height = kLengthFillParent
      arrangement.Width = kLengthFillParent
    }
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
    XCTAssertEqual(testForm.view.frame.height, row1.view.frame.height + row2.view.frame.height)
    XCTAssertEqual(row1.view.frame.height, row2.view.frame.height)
    XCTAssertEqual(testForm.view.frame.width, row1.view.frame.width)
    XCTAssertEqual(testForm.view.frame.width, row2.view.frame.width)
    #if DEBUG
    testForm.printViewHierarchy()
    #endif

    // Now reset the views
    [row1, row2, cell11, cell12, cell21, cell22].forEach { (arrangement) in
      arrangement.Height = kLengthPreferred
      arrangement.Width = kLengthPreferred
    }
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
    XCTAssertEqual(100.0, cell11.view.frame.height)
    XCTAssertEqual(100.0, cell11.view.frame.width)
    XCTAssertEqual(100.0, cell12.view.frame.height)
    XCTAssertEqual(100.0, cell12.view.frame.width)
    XCTAssertEqual(100.0, cell21.view.frame.height)
    XCTAssertEqual(100.0, cell21.view.frame.width)
    XCTAssertEqual(100.0, cell22.view.frame.height)
    XCTAssertEqual(100.0, cell22.view.frame.width)
    XCTAssertEqual(100.0, row1.view.frame.height)
    XCTAssertEqual(200.0, row1.view.frame.width)
    XCTAssertEqual(100.0, row2.view.frame.height)
    XCTAssertEqual(200.0, row2.view.frame.width)
    #if DEBUG
    testForm.printViewHierarchy()
    #endif
  }

  func testExampleHorizontal() {
    testForm.clear()
    let row1 = HorizontalArrangement(testForm)
    row1.view.accessibilityIdentifier = "row1"
    let cell1 = VerticalArrangement(row1)
    cell1.view.accessibilityIdentifier = "cell1"
    let cell2 = VerticalArrangement(row1)
    cell2.view.accessibilityIdentifier = "cell2"

    testForm.view.layoutIfNeeded()

    #if DEBUG
    testForm.printViewHierarchy()
    #endif

    [row1, cell1, cell2].forEach { (arrangement) in
      arrangement.Width = kLengthFillParent
    }

    testForm.view.layoutIfNeeded()
    XCTAssertEqual(100.0, row1.view.frame.height)
    XCTAssertEqual(testForm.view.frame.width, row1.view.frame.width)
    XCTAssertEqual(cell1.view.frame.width, cell2.view.frame.width, accuracy: 1.0)
    XCTAssertEqual(row1.view.frame.width, cell1.view.frame.width + cell2.view.frame.width)

    [row1, cell1, cell2].forEach { (arrangement) in
      arrangement.Width = kLengthPreferred
    }

    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    XCTAssertEqual(100.0, row1.view.frame.height)
    XCTAssertEqual(200.0, row1.view.frame.width)
    #if DEBUG
    testForm.printViewHierarchy()
    #endif
  }
}
