// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

private class IntrinsicTestView: UIView {
  private let size: CGSize

  init(width: CGFloat, height: CGFloat) {
    size = CGSize(width: width, height: height)
    super.init(frame: .zero)
    translatesAutoresizingMaskIntoConstraints = false
  }

  required init?(coder: NSCoder) {
    return nil
  }

  override var intrinsicContentSize: CGSize {
    return size
  }
}

class LinearViewTests: XCTestCase {

  var testForm = ReplForm()
  var testView: LinearView!

  override func setUp() {
    testView = LinearView()
    let window = UIWindow()
    window.rootViewController = testForm
    window.addSubview(testForm.view)
  }

  private func rootLayoutConstraintCount(for form: Form) -> Int {
    return form.view.constraints.filter { constraint in
      return isRootLayoutItem(constraint.firstItem) || isRootLayoutItem(constraint.secondItem)
    }.count
  }

  private func isRootLayoutItem(_ item: Any?) -> Bool {
    if item is ScaleFrameLayout {
      return true
    }
    return (item as? UIView)?.accessibilityIdentifier == "Form root view"
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

  func testAutomaticSizingIgnoresInvisibleItems() {
    let view1 = IntrinsicTestView(width: 30, height: 20)
    let view2 = IntrinsicTestView(width: 70, height: 40)
    testView.addItem(LinearViewItem(view1))
    testView.addItem(LinearViewItem(view2))

    XCTAssertEqual(CGSize(width: 70, height: 60), testView.intrinsicContentSize)

    testView.setVisibility(of: view1, to: false)
    testView.setVisibility(of: view2, to: false)

    XCTAssertEqual(CGSize(width: 100, height: 100), testView.intrinsicContentSize)
  }

  func testAutomaticSizingHonorsFixedChildSize() {
    let imageSizedView = IntrinsicTestView(width: 1790, height: 1228)
    testView.addItem(LinearViewItem(imageSizedView))
    testView.setWidth(of: imageSizedView, to: Length(pixels: 200))
    testView.setHeight(of: imageSizedView, to: Length(pixels: 200))

    XCTAssertEqual(CGSize(width: 200, height: 200), testView.intrinsicContentSize)
  }

  func testAutomaticHorizontalArrangementIsCenteredAfterLabelTextChanges() {
    testForm.clear()
    testForm.view.frame = CGRect(x: 0, y: 0, width: 393, height: 852)
    testForm.Sizing = "Responsive"

    let fillParentLine = HorizontalArrangement(testForm)
    fillParentLine.Width = kLengthFillParent
    fillParentLine.Height = 10

    let dataCleaningChart = Chart(testForm)
    dataCleaningChart.Width = kLengthFillParent
    dataCleaningChart.Height = kLengthFillParent

    let secondFillParentLine = HorizontalArrangement(testForm)
    secondFillParentLine.Width = kLengthFillParent
    secondFillParentLine.Height = 10

    let cleanedDataChart = Chart(testForm)
    cleanedDataChart.Width = kLengthFillParent
    cleanedDataChart.Height = kLengthFillParent

    let trendlineValuesRow = HorizontalArrangement(testForm)
    let slopeLabel = Label(trendlineValuesRow)
    slopeLabel.Text = "M ="
    let slopeValueLabel = Label(trendlineValuesRow)
    let yInterceptLabel = Label(trendlineValuesRow)
    yInterceptLabel.Text = "    B ="
    let yInterceptValueLabel = Label(trendlineValuesRow)
    let correlationCoefficientLabel = Label(trendlineValuesRow)
    correlationCoefficientLabel.Text = "    R ="
    let correlationCoefficientValueLabel = Label(trendlineValuesRow)
    let xInterceptLabel = Label(trendlineValuesRow)
    xInterceptLabel.Text = "    X-Int ="
    let xInterceptValueLabel = Label(trendlineValuesRow)

    testForm.AlignHorizontal = HorizontalGravity.center.rawValue
    testForm.onAttach()
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    let trendlineFrame = trendlineValuesRow.view.convert(trendlineValuesRow.view.bounds, to: testForm.view)
    XCTAssertLessThan(trendlineValuesRow.view.frame.width, testForm.view.frame.width)
    XCTAssertEqual(trendlineFrame.midX, testForm.view.frame.midX, accuracy: 0.5)

    slopeValueLabel.Text = "-0.123456789012345"
    yInterceptValueLabel.Text = "1234.56789012345"
    correlationCoefficientValueLabel.Text = "-0.987654321"
    xInterceptValueLabel.Text = "[-12345.6789012345]"

    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    let updatedTrendlineFrame = trendlineValuesRow.view.convert(trendlineValuesRow.view.bounds, to: testForm.view)
    XCTAssertEqual(updatedTrendlineFrame.midX, testForm.view.frame.midX, accuracy: 0.5)
  }

  func testScrollableFormDoesNotLeakIntoClearedCenteredForm() {
    testForm.clear()
    testForm.view.frame = CGRect(x: 0, y: 0, width: 393, height: 852)
    testForm.Sizing = "Responsive"
    testForm.Scrollable = true
    testForm.AlignHorizontal = HorizontalGravity.center.rawValue

    let scrollableLabel = Label(testForm)
    scrollableLabel.Width = kLengthFillParent
    scrollableLabel.Text = "Scrollable screen"

    testForm.onAttach()
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    testForm.clear()
    testForm.view.frame = CGRect(x: 0, y: 0, width: 393, height: 852)
    testForm.Sizing = "Responsive"
    testForm.AlignHorizontal = HorizontalGravity.center.rawValue
    testForm.AlignVertical = VerticalGravity.center.rawValue

    let Button1 = Button(testForm)
    Button1.Text = "Text for Button1"
    let Label1 = Label(testForm)
    Label1.Text = "Hi Pikachu!"

    testForm.onAttach()
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    let buttonFrame = Button1.view.convert(Button1.view.bounds, to: testForm.view)
    let labelFrame = Label1.view.convert(Label1.view.bounds, to: testForm.view)
    XCTAssertEqual(buttonFrame.midX, testForm.view.frame.midX, accuracy: 0.5)
    XCTAssertEqual(labelFrame.midX, testForm.view.frame.midX, accuracy: 0.5)
    XCTAssertGreaterThan(buttonFrame.minY, testForm.view.frame.height / 3)
  }

  func testFormRecomputeDoesNotAccumulateRootConstraints() {
    testForm.clear()
    testForm.view.frame = CGRect(x: 0, y: 0, width: 393, height: 852)
    testForm.Sizing = "Responsive"
    let initialConstraintCount = rootLayoutConstraintCount(for: testForm)

    for _ in 0..<3 {
      testForm.Scrollable = true
      testForm.Scrollable = false
      testForm.Sizing = "Fixed"
      testForm.Sizing = "Responsive"
    }

    XCTAssertEqual(initialConstraintCount, rootLayoutConstraintCount(for: testForm))
  }

  func testHiddenComponentCanReplayAutomaticSizing() {
    testForm.clear()
    let row = HorizontalArrangement(testForm)
    let Button1 = Button(row)

    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    Button1.Visible = false
    Button1.Width = kLengthPreferred
    Button1.Height = kLengthPreferred
    Button1.Visible = true

    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
    XCTAssertGreaterThan(Button1.view.frame.width, 0)
    XCTAssertGreaterThan(Button1.view.frame.height, 0)
  }

  func testAbsoluteArrangementAutomaticSizeUsesPositionedChildren() {
    testForm.clear()
    let arrangement = AbsoluteArrangement(testForm)
    let Button1 = Button(arrangement)

    Button1.Left = 10
    Button1.Top = 20
    Button1.Width = 50
    Button1.Height = 30

    RunLoop.main.run(until: Date(timeIntervalSinceNow: 0.01))
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()

    XCTAssertEqual(CGSize(width: 60, height: 50), arrangement.view.intrinsicContentSize)
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
