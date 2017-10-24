// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import XCTest
@testable import AIComponentKit

class RuntimeTests: XCTestCase {

  func getInterpreterForTesting() throws -> SCMInterpreter {
    let interpreter = SCMInterpreter()
    if let runtimeUrl = Bundle(for: ReplForm.self).url(forResource: "runtime", withExtension: "scm") {
      do {
        let text = try String(contentsOf: runtimeUrl, encoding: String.Encoding.utf8)
        interpreter.evalForm(text)
      } catch {
        XCTFail("Unable to load runtime.scm")
        throw TestFailure()
      }
      if let exception = interpreter.exception {
        XCTFail("Exception: \(exception.name.rawValue) (\(exception))")
        throw TestFailure()
      }
    }
    return interpreter
  }

  func testYailEqualBool() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? #t #t)"))
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? #f #f)"))
    XCTAssertEqual("#f", interpreter.evalForm("(yail-equal? #t #f)"))
  }

  func testYailEqualNumbers() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? 0 0)"))
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? 0 00)"))
    XCTAssertEqual("#f", interpreter.evalForm("(yail-equal? 0 01)"))
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? 0 0.0)"))
  }

  func testYailEqualStrings() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? \"hello\" \"hello\")"))
    XCTAssertEqual("#f", interpreter.evalForm("(yail-equal? \"hello\" \"world\")"))
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? \"hi\" (string-append \"h\" \"i\"))"))
  }

  func testYailEqualLists() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? (*list-for-runtime* #t 0.0 \"foo\") (*list-for-runtime* #t 0.0 \"foo\"))"))
    XCTAssertEqual("#f", interpreter.evalForm("(yail-equal? (*list-for-runtime*) (*list-for-runtime* \"foo\"))"))
    XCTAssertEqual("#f", interpreter.evalForm("(yail-equal? (*list-for-runtime* #t) #t)"))
  }

  func testYailEqualMixedTypes() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? 0 \"  0  \")"))
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? 1 \"  1.0  \")"))
    XCTAssertEqual("#f", interpreter.evalForm("(yail-equal? 3 \"pi\")"))
    XCTAssertEqual("#t", interpreter.evalForm("(yail-equal? (*list-for-runtime* 0.0) (*list-for-runtime* \"  0.0  \"))"))
  }

  func testYailStringContains() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(string-contains \"Hello\" \"ell\")"))  // inner substring
    XCTAssertEqual("#t", interpreter.evalForm("(string-contains \"Hello\" \"H\")"))  // starting substring
    XCTAssertEqual("#t", interpreter.evalForm("(string-contains \"Hello\" \"o\")"))  // ending substring
    XCTAssertEqual("#f", interpreter.evalForm("(string-contains \"Hello\" \"world\")"))  // mismatch of equal length
    XCTAssertEqual("#f", interpreter.evalForm("(string-contains \"Hello\" \"foo\")"))  // mismatch of lesser length
    XCTAssertEqual("#f", interpreter.evalForm("(string-contains \"Hello\" \"Hello world\")"))  // mismatch of greater length
  }
}
