// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import XCTest
@testable import AIComponentKit

public func getInterpreterForTesting() throws -> SCMInterpreter {
  let interpreter = SCMInterpreter()
  if let runtimeUrl = Bundle(for: ReplForm.self).url(forResource: "runtime", withExtension: "scm") {
    do {
      let text = try String(contentsOf: runtimeUrl, encoding: String.Encoding.utf8)
      interpreter.evalForm(text)
      interpreter.evalForm("(set! *testing* #t)")
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

@objc class CoercionTestHelper: NSObject {

  var result: Any? = nil

  func callAsDouble(_ result: Double) {
    self.result = result
  }

  func callAsFloat(_ result: Float32) {
    self.result = result
  }

  func callAsUInt64(_ result: UInt64) {
    self.result = result
  }

  func callAsInt64(_ result: Int64) {
    self.result = result
  }
}

class RuntimeTests: XCTestCase {

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

  func testYailCoerceToString() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(string? (coerce-to-string 0.5))"))
  }

  func testYailCoerceToInstant() throws {
    let interpreter = try getInterpreterForTesting()
    interpreter.evalForm("(define *test-instant* (yail:make-instance NSDate))")
    XCTAssertEqual("#t", interpreter.evalForm("(yail:isa *test-instant* NSDate)"))
    XCTAssertEqual("#t", interpreter.evalForm("(eq? *non-coercible-value* (coerce-to-instant 0))"))
    XCTAssertEqual("#t", interpreter.evalForm("(eq? *test-instant* (coerce-to-instant *test-instant*))"))
  }

  func testYailGenerateRuntimeTypeError() throws {
    let interpreter = try getInterpreterForTesting()
    interpreter.evalForm("(generate-runtime-type-error 'testYailGenerateRuntimeTypeError '(\"Test error\"))")
    XCTAssertNotNil(interpreter.exception)
    XCTAssertEqual("RuntimeError", (interpreter.exception?.name)! as NSString)
    XCTAssertTrue((interpreter.exception?.reason?.contains("testYailGenerateRuntimeTypeError"))!)
  }

  func testYailError() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertNil(interpreter.exception)
    interpreter.evalForm("(yail:make-instance AIComponentKit.Button 1 2 3)")
    XCTAssertNotNil(interpreter.exception)
    XCTAssertFalse((interpreter.exception?.reason?.contains("%s"))!)
  }

  func testIsBase10() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(is-decimal? \"12345\")"))
    XCTAssertEqual("#f", interpreter.evalForm("(is-decimal? \"123abc\")"))
    XCTAssertEqual("#t", interpreter.evalForm("(is-decimal? \"101010\")"))
    XCTAssertEqual("#f", interpreter.evalForm("(is-decimal? \"foobar\")"))
  }

  func testIsBase16() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(is-hexadecimal? \"12345\")"))
    XCTAssertEqual("#t", interpreter.evalForm("(is-hexadecimal? \"123abc\")"))
    XCTAssertEqual("#t", interpreter.evalForm("(is-hexadecimal? \"101010\")"))
    XCTAssertEqual("#f", interpreter.evalForm("(is-hexadecimal? \"foobar\")"))
  }

  func testIsBase2() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#f", interpreter.evalForm("(is-binary? \"12345\")"))
    XCTAssertEqual("#f", interpreter.evalForm("(is-binary? \"123abc\")"))
    XCTAssertEqual("#t", interpreter.evalForm("(is-binary? \"101010\")"))
    XCTAssertEqual("#f", interpreter.evalForm("(is-binary? \"foobar\")"))
  }

  func testListsAsRetvals() throws {
    let interpreter = try getInterpreterForTesting()
    RetValManager.shared().fetch(false)  // clear any return values from other tests
    interpreter.evalForm("(send-to-block \"1\" (list \"OK\" (*list-for-runtime* (*list-for-runtime* 30.5 10.5) (*list-for-runtime* 31.5 11.5))))")
    if let exception = interpreter.exception {
      print(exception)
    }
    let result = RetValManager.shared().fetch(false)
    XCTAssertNotNil(result)
    XCTAssertEqual("{\"status\":\"OK\",\"values\":[{\"status\":\"OK\",\"value\":\"[[30.500000, 10.500000], [31.500000, 11.500000]]\",\"type\":\"return\",\"blockid\":\"1\"}]}", result)
  }

  func testYailNSDateCoerceToString() throws {
    let interpreter = try getInterpreterForTesting()
    if let gmt = TimeZone(abbreviation: "GMT") {
      interpreter.setTimeZone(gmt)
    }
    let date = NSDate(timeIntervalSince1970: 0)
    interpreter.setValue(date, forSymbol: "test-date")
    let result = interpreter.evalForm("(coerce-to-string test-date)")
    XCTAssertNotNil(result)
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("1970-01-01T00:00:00Z", result)
  }

  func testYailNSDateGetDisplayRepresentation() throws {
    let interpreter = try getInterpreterForTesting()
    if let gmt = TimeZone(abbreviation: "GMT") {
      interpreter.setTimeZone(gmt)
    }
    let date = NSDate(timeIntervalSince1970: 0)
    interpreter.setValue(date, forSymbol: "test-date")
    let result = interpreter.evalForm("(get-display-representation test-date)")
    XCTAssertNotNil(result)
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("1970-01-01T00:00:00Z", result)
  }
}
