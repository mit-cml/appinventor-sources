//
//  AIComponentKitTests.swift
//  AIComponentKitTests
//
//  Created by Evan Patton on 9/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

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
    let form = ReplForm()
    form.formName = "Screen1"
    form.startHTTPD(false)
    let interpreter = form.interpreter
    interpreter.setCurrentForm(form)
    let runtimeUrl = Bundle(for: ReplForm.self).url(forResource: "runtime", withExtension: "scm")
    if (runtimeUrl != nil) {
      do {
        if interpreter.exception != nil {
          // FIXME: Happens due to SCMInterpreter not finding runtime.scm when testing
          interpreter.clearException()
        }
        let text = try String(contentsOf: runtimeUrl!, encoding: String.Encoding.utf8)
        interpreter.evalForm(text)
        interpreter.setCurrentForm(form)
      } catch {
        XCTFail("Unable to load runtime.scm")
      }
      if let exception = interpreter.exception {
        XCTFail("Exception: \(exception.name.rawValue) (\(exception))");
        return;
      }
    }
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
    let component = form.components[0]
    if component is Button {
      let button = component as! Button
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
