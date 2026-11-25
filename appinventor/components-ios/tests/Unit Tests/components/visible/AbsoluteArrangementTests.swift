// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

class AbsoluteArrangementTests: XCTestCase {

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

  private func place(_ component: ViewComponent, left: Int, top: Int) {
    component.Left = left
    component.Top = top
  }

  private func layout() {
    testForm.onAttach()
    testForm.view.setNeedsLayout()
    testForm.view.layoutIfNeeded()
  }

  /// Origin of the child inside the arrangement.
  private func origin(of component: ViewComponent) -> CGPoint {
    return component.view.convert(CGPoint.zero, to: arrangement.view)
  }

  func testPositionIsUnsetByDefault() {
    let button = Button(arrangement)
    XCTAssertEqual(kDefaultXY, button.Left)
    XCTAssertEqual(kDefaultXY, button.Top)
  }

  func testPositivePosition() {
    let button = Button(arrangement)
    place(button, left: 20, top: 30)
    layout()
    XCTAssertEqual(CGPoint(x: 20, y: 30), origin(of: button))
  }

  func testNegativeLeftIsHonored() {
    let button = Button(arrangement)
    place(button, left: -40, top: 10)
    layout()
    XCTAssertEqual(CGPoint(x: -40, y: 10), origin(of: button))
  }

  func testNegativeTopKeepsLeft() {
    let button = Button(arrangement)
    place(button, left: 150, top: -15)
    layout()
    XCTAssertEqual(CGPoint(x: 150, y: -15), origin(of: button))
  }

  func testMinusOneIsARealPosition() {
    let button = Button(arrangement)
    place(button, left: -1, top: 120)
    layout()
    XCTAssertEqual(CGPoint(x: -1, y: 120), origin(of: button))
  }

  func testMinusOneDoesNotKeepRequeueing() {
    let button = Button(arrangement)
    place(button, left: -1, top: 120)
    // RelativeLayout.add queues the child on the main queue. With -1 treated as "unset"
    // it re-queued itself forever; now it is placed on the first pass.
    RunLoop.main.run(until: Date(timeIntervalSinceNow: 0.2))
    layout()
    XCTAssertEqual(arrangement.getView(), button.view.superview)
    XCTAssertEqual(CGPoint(x: -1, y: 120), origin(of: button))
  }

  func testMovingToNegativePosition() {
    let button = Button(arrangement)
    place(button, left: 20, top: 30)
    layout()
    place(button, left: -25, top: -5)
    layout()
    XCTAssertEqual(CGPoint(x: -25, y: -5), origin(of: button))
  }
}
