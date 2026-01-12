// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
    XCTAssertEqual("#t", interpreter.evalForm("(eq? *non-coercible-value* (coerce-to-instant #f))"))
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

  func testDegreesToRadians() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("0.0", interpreter.evalForm("(degrees->radians 0)"))
    XCTAssertEqual("-3.141592653589793", interpreter.evalForm("(degrees->radians 180)"))
    XCTAssertEqual("0.0", interpreter.evalForm("(degrees->radians 360)"))
  }

  func testRadiansToDegrees() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("0.0", interpreter.evalForm("(radians->degrees 0)"))
    XCTAssertEqual("180.0", interpreter.evalForm("(radians->degrees 3.141592653589793)"))
    XCTAssertEqual("0.0", interpreter.evalForm("(radians->degrees 6.283185307179586)"))
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
    XCTAssertEqual("{\"status\":\"OK\",\"values\":[{\"status\":\"OK\",\"value\":\"[[30.5, 10.5], [31.5, 11.5]]\",\"type\":\"return\",\"blockid\":\"1\"}]}", result)
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

  func testAnyComponentBlocks() throws {
    let interpreter = try getInterpreterForTesting()
    let form = Form()
    interpreter.setCurrentForm(form)
    XCTAssertEqual("Success", interpreter.evalForm("(begin (set-and-coerce-property-and-check! *this-form* 'com.google.appinventor.components.runtime.Form 'Title \"Success\" 'text) (get-property-and-check *this-form* 'com.google.appinventor.components.runtime.Form 'Title))"))
  }

  func testAppInventorNumberToString() throws {
    let interpreter = try getInterpreterForTesting()
    let form = Form()
    interpreter.setCurrentForm(form)
    XCTAssertEqual("4028354713", interpreter.evalForm("(appinventor-number->string 4028354713)"))
  }

  func testListMember() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(yail-list-member? \"touch\\u00e9\" (make-yail-list \"en garde\" \"touch\\u00e9\"))"))
  }

  func testListIndex() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("2", interpreter.evalForm("(yail-list-index \"touch\\u00e9\" (make-yail-list \"en garde\" \"touch\\u00e9\"))"))
  }

  func testSplit() throws {
    let interpreter = try getInterpreterForTesting()
    let form = Form()
    interpreter.setCurrentForm(form)
    XCTAssertEqual("[\"1\", \"0\", \"0\", \"1\", \"0\"]",
        interpreter.evalForm("(get-display-representation (string-split \"10010\" \"\"))"))
    XCTAssertEqual("[\"apple\", \"banana\", \"cantalope\"]",
        interpreter.evalForm("(get-display-representation (string-split \"apple,banana,cantalope\" \",\"))"))
    XCTAssertEqual("[\"\", \"test\"]",
        interpreter.evalForm("(get-display-representation (string-split \"strteststr\" \"str\"))"))
    XCTAssertEqual("[\"many\", \"commas\"]",
        interpreter.evalForm("(get-display-representation (string-split \"many,commas,,,,,\" \",\"))"))
  }

  func testCoerceToList() throws {
    let interpreter = try getInterpreterForTesting()
    let form = Form()
    interpreter.setCurrentForm(form)
    XCTAssertEqual("[]", interpreter.evalForm("(get-display-representation (coerce-to-yail-list (make-yail-list)))"))
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("[]", interpreter.evalForm("(get-display-representation (coerce-to-yail-list (make-yail-dictionary)))"))
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("[non-coercible]", interpreter.evalForm("(get-display-representation (coerce-to-yail-list 0))"))
  }

  func testJoinStrings() throws {
    let interpreter = try getInterpreterForTesting()
    let form = Form()
    interpreter.setCurrentForm(form)
    XCTAssertEqual("true, false, test, 5, \"test\"",
                   interpreter.evalForm("(call-yail-primitive yail-list-join-with-separator (*list-for-runtime* (call-yail-primitive make-yail-list (*list-for-runtime* #t #f \"test\" 5 \"\\\"test\\\"\" ) '(any any any any any ) \"make a list\") \", \") '(list text) \"join with separator\")"))
    XCTAssertNil(interpreter.exception)
  }

  func testReverseStrings() throws {
    let interpreter = try getInterpreterForTesting()
    let form = Form()
    interpreter.setCurrentForm(form)
    XCTAssertEqual("olleH", interpreter.evalForm("(string-reverse \"Hello\")"))
    XCTAssertEqual("🌎😀olleH", interpreter.evalForm("(string-reverse \"Hello😀🌎\")"))
    XCTAssertEqual("54321", interpreter.evalForm("(call-yail-primitive string-reverse (*list-for-runtime* 12345) '(text) \"reverse\")"))
  }

  func testGlobalSetGet() throws {
    let interpreter = try getInterpreterForTesting()
    let form = Form()
    interpreter.setCurrentForm(form)
    XCTAssertEqual("5", interpreter.evalForm("(begin (set-var! g$x 5) (get-var g$x))"))
    XCTAssertNotNil(form.environment["g$x"] as? NSNumber)
    XCTAssertEqual("42.25", interpreter.evalForm("(begin (set-var! g$y 42.25) (get-var g$y))"))
    XCTAssertNotNil(form.environment["g$y"] as? NSNumber)
  }
  
  func testSerialization() throws {
    let interpreter = try getInterpreterForTesting()
    let form = Form()
    let label = Label(form)
    form.environment["Label1"] = label
    interpreter.setCurrentForm(form)

    // Test booleans
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text #t 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("true", label.Text)
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text #f 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("false", label.Text)

    // Test numbers
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text 0.0 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("0", label.Text)
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text -42.25 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("-42.25", label.Text)
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text 98765432.1 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("9.8765E7", label.Text)
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text (/ 1.0 0) 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("+infinity", label.Text)
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text (/ -1.0 0) 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("-infinity", label.Text)

    // Test text
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text \"some text\" 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("some text", label.Text)
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text \"some text with \\\"quotes\\\"\" 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("some text with \"quotes\"", label.Text)

    // Test native list
    form.environment["g$object"] = NSArray()
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text (get-var g$object) 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("[]", label.Text)

    // Test YailList
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text (call-yail-primitive make-yail-list (*list-for-runtime* ) '() \"make a list\") 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("[]", label.Text)

    // Test native dictionary
    form.environment["g$object"] = NSDictionary()
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text (get-var g$object) 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("{}", label.Text)

    // Test YailDictionary
    interpreter.evalForm("(set-and-coerce-property! 'Label1 'Text (call-yail-primitive make-yail-dictionary (*list-for-runtime* ) '() \"make a dictionary\") 'text)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual("{}", label.Text)
  }

  func testEnum() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("#t", interpreter.evalForm("(enum? (static-field AIComponentKit.FileScope 'Shared))"))
    XCTAssertNil(interpreter.exception)
  }

  func testEnums() throws {
    let interpreter = try getInterpreterForTesting()
    let form = Form()
    form.environment["Screen1"] = form
    interpreter.setCurrentForm(form)
    interpreter.evalForm("(set-and-coerce-property! 'Screen1 'DefaultFileScope \"Shared\" 'com.google.appinventor.components.common.FileScopeEnum)")
    XCTAssertNil(interpreter.exception)
    XCTAssertEqual(FileScope.Shared, form.DefaultFileScope)
  }
}
