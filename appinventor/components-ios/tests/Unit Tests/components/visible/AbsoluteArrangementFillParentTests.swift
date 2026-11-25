// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

/// A child with Width or Height Fill parent fills the arrangement from its Left or Top to the
/// arrangement's far edge, like Android's MATCH_PARENT with a margin.
class AbsoluteArrangementFillParentTests: XCTestCase {

  var testForm = ReplForm()
  var arrangement: AbsoluteArrangement!

  override func setUp() {
    let window = UIWindow()
    window.rootViewController = testForm
    window.addSubview(testForm.view)
    testForm.clear()
    testForm.view.frame = CGRect(x: 0, y: 0, width: 393, height: 852)
    arrangement = AbsoluteArrangement(testForm)
    arrangement.Width = 300
    arrangement.Height = 200
  }

  /// Runs the pending main queue work (children are added asynchronously) and lays out the form.
  private func settle() {
    RunLoop.main.run(until: Date(timeIntervalSinceNow: 0.05))
    testForm.onAttach()
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
  }

  private func frame(of component: ViewComponent) -> CGRect {
    return component.view.convert(component.view.bounds, to: arrangement.view)
  }

  func testHeightFillParentSetBeforePosition() {
    let button = Button(arrangement)
    button.Height = kLengthFillParent
    button.Left = 10
    button.Top = 100
    settle()
    XCTAssertEqual(100, frame(of: button).minY)
    XCTAssertEqual(100, frame(of: button).height)
  }

  func testHeightFillParentSetAfterPosition() {
    let button = Button(arrangement)
    button.Left = 10
    button.Top = 100
    settle()
    button.Height = kLengthFillParent
    settle()
    XCTAssertEqual(100, frame(of: button).minY)
    XCTAssertEqual(100, frame(of: button).height)
  }

  func testWidthFillParentSetBeforePosition() {
    let button = Button(arrangement)
    button.Width = kLengthFillParent
    button.Left = 60
    button.Top = 20
    settle()
    XCTAssertEqual(60, frame(of: button).minX)
    XCTAssertEqual(240, frame(of: button).width)
  }

  func testWidthFillParentSetAfterPosition() {
    let button = Button(arrangement)
    button.Left = 60
    button.Top = 20
    settle()
    button.Width = kLengthFillParent
    settle()
    XCTAssertEqual(60, frame(of: button).minX)
    XCTAssertEqual(240, frame(of: button).width)
  }

  /// The companion sets designer properties alphabetically: Height, Left, Top, Width.
  func testCompanionPropertyOrder() {
    let button = Button(arrangement)
    button.Height = kLengthFillParent
    button.Left = 60
    button.Top = 100
    button.Width = kLengthFillParent
    settle()
    XCTAssertEqual(CGRect(x: 60, y: 100, width: 240, height: 100), frame(of: button))
  }

  func testFillParentInAutomaticArrangement() {
    arrangement.Width = kLengthPreferred
    arrangement.Height = kLengthPreferred
    let fixed = Button(arrangement)
    fixed.Width = 50
    fixed.Height = 30
    fixed.Left = 200
    fixed.Top = 0
    let fill = Button(arrangement)
    fill.Height = 20
    fill.Left = 10
    fill.Top = 50
    fill.Width = kLengthFillParent
    settle()
    XCTAssertEqual(250, arrangement.view.bounds.width)
    XCTAssertEqual(CGRect(x: 10, y: 50, width: 240, height: 20), frame(of: fill))
    fixed.Left = 100
    settle()
    XCTAssertEqual(150, arrangement.view.bounds.width)
    XCTAssertEqual(CGRect(x: 10, y: 50, width: 140, height: 20), frame(of: fill))
  }

  func testFillParentAtOriginFillsArrangement() {
    let button = Button(arrangement)
    button.Width = kLengthFillParent
    button.Height = kLengthFillParent
    button.Left = 0
    button.Top = 0
    settle()
    XCTAssertEqual(CGRect(x: 0, y: 0, width: 300, height: 200), frame(of: button))
  }

  func testFillParentFollowsMove() {
    let button = Button(arrangement)
    button.Width = kLengthFillParent
    button.Height = kLengthFillParent
    button.Left = 60
    button.Top = 100
    settle()
    button.Left = 100
    button.Top = 50
    settle()
    XCTAssertEqual(CGRect(x: 100, y: 50, width: 200, height: 150), frame(of: button))
  }

  func testFillParentFollowsArrangementResize() {
    let button = Button(arrangement)
    button.Height = kLengthFillParent
    button.Left = 10
    button.Top = 100
    settle()
    arrangement.Height = 250
    settle()
    XCTAssertEqual(150, frame(of: button).height)
  }

  func testFixedSizeIsUnchanged() {
    let button = Button(arrangement)
    button.Width = 80
    button.Height = 40
    button.Left = 60
    button.Top = 100
    settle()
    XCTAssertEqual(CGRect(x: 60, y: 100, width: 80, height: 40), frame(of: button))
  }
}
