// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class SpinnerTests: XCTestCase {
  var form: Form!
  var spinner: Spinner!

  override func setUp() {
    form = Form()
    spinner = Spinner(form)
  }

  func testNoElements() {
    XCTAssert(spinner.Elements.isEmpty)
    XCTAssertEqual(0, spinner.SelectionIndex)
    XCTAssertEqual("", spinner.Selection)
  }

  func testSetElementsFromString() {
    spinner.ElementsFromString = "alpha,beta,gamma"
    XCTAssertEqual(3, spinner.Elements.length)
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
    XCTAssertEqual(3, spinner.Elements.length)
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

  func testSpinnerFromYail() {
    let interpreter = try! getInterpreterForTesting()
    form.formName = "Screen1"
    interpreter.setCurrentForm(form!)
    interpreter.evalForm("(clear-current-form)")
    interpreter.evalForm("(add-component Screen1 AIComponentKit.Spinner Spinner1)")
    spinner = form?.environment["Spinner1"] as? Spinner
    interpreter.evalForm("""
      (set-and-coerce-property! 'Spinner1 'Elements
          (call-yail-primitive make-yail-list (*list-for-runtime* #t 5 "hello" )
              '(any any any ) "make a list")
          'list)
      """)
    XCTAssertEqual(["true", "5", "hello"], spinner.Elements)
  }
}
