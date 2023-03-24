// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

class AIComponentKitTests: XCTestCase {
    
  override func setUp() {
    super.setUp()
    // Put setup code here. This method is called before the invocation of each test method in the class.
  }
  
  override func tearDown() {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    super.tearDown()
  }
  
  func testExample() {
    // This is an example of a functional test case.
    // Use XCTAssert and related functions to verify your tests produce the correct results.
  }

  func testEventDispatch() {
    let interpreter = try! getInterpreterForTesting()
    let form = ReplForm()
    form.formName = "Screen1"
    interpreter.setCurrentForm(form)
    interpreter.evalForm("(define test-success #f)");
    interpreter.evalForm("(clear-current-form)");
    interpreter.evalForm("(add-component Screen1 com.google.appinventor.components.runtime.Button Button1)")
    if let exception = interpreter.exception {
      XCTFail("Exception: \(exception.name.rawValue) (\(exception))");
      return;
    }
    interpreter.evalForm("(define-event Button1 Click()(set! test-success #t))")
    if let exception = interpreter.exception {
      XCTFail("Exception: \(exception.name.rawValue) (\(exception))");
      return;
    }
    XCTAssertEqual(1, form.components.count)
    form.Initialize()
    if let button = form.components[0] as? Button {
      button.Click()
      XCTAssertEqual("#t", interpreter.evalForm("test-success"))
    } else {
      XCTFail("No button in ReplForm")
    }
  }
    
  func testPerformanceExample() {
    // This is an example of a performance test case.
    self.measure {
      // Put the code you want to measure the time of here.
    }
  }
}
