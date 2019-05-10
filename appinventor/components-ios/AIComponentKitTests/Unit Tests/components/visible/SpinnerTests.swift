// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import XCTest
@testable import AIComponentKit

class SpinnerTests: XCTestCase {
  var spinner: Spinner!

  override func setUp() {
    spinner = Spinner(Form())
  }

  func testNoElements() {
    XCTAssert(spinner.Elements.isEmpty)
    XCTAssertEqual(0, spinner.SelectionIndex)
    XCTAssertEqual("", spinner.Selection)
  }

  func testSetElementsFromString() {
    spinner.ElementsFromString = "alpha,beta,gamma"
    XCTAssertEqual(3, spinner.Elements.count)
    XCTAssertEqual(1, spinner.SelectionIndex)
    XCTAssertEqual("alpha", spinner.Selection)
    XCTAssertEqual("alpha", spinner.Text)
  }

  func testSetElementsFromStringWithSpaces() {
    spinner.ElementsFromString = "   alpha,  beta  ,gamma "
    XCTAssertEqual(["   alpha", "beta", "gamma "], spinner.Elements)
  }

  func testSetElements() {
    spinner.Elements = ["alpha", "beta", "gamma"]
    XCTAssertEqual(3, spinner.Elements.count)
    XCTAssertEqual(1, spinner.SelectionIndex)
    XCTAssertEqual("alpha", spinner.Selection)
    XCTAssertEqual("alpha", spinner.Text)
  }

  func testClearElements() {
    spinner.ElementsFromString = "alpha, beta, gamma"
    spinner.Elements = []
    XCTAssert(spinner.Elements.isEmpty)
    XCTAssertEqual(0, spinner.SelectionIndex)
    XCTAssertEqual("", spinner.Selection)
    XCTAssertEqual("", spinner.Text)
  }

  func testReplaceElements() {
    spinner.ElementsFromString = "alpha, beta, gamma"
    spinner.Elements = ["gamma", "beta", "alpha"]
    XCTAssertEqual("gamma", spinner.Selection)
  }

  func testSelectElementByIndex() {
    spinner.ElementsFromString = "alpha, beta, gamma, delta, epsilon"
    spinner.SelectionIndex = 5
    XCTAssertEqual("epsilon", spinner.Selection)
  }

  func testShortenElements() {
    spinner.ElementsFromString = "alpha, beta, gamma, delta, epsilon"
    spinner.SelectionIndex = 5
    spinner.Elements = ["alpha", "beta", "gamma"]
    XCTAssertEqual(3, spinner.SelectionIndex)
    XCTAssertEqual("gamma", spinner.Selection)
  }
}
