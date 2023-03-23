// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
import Foundation
@testable import AIComponentKit

class JsonUtilTests: XCTestCase {

  // MARK: Serialization Tests

  func testSerializeNull() throws {
    XCTAssertEqual("null", try getJsonRepresentation(nil))
  }

  func testSerializeBools() throws {
    XCTAssertEqual("true", try getJsonRepresentation(true as AnyObject))
    XCTAssertEqual("false", try getJsonRepresentation(false as AnyObject))
  }

  func testSerializeNumbers() throws {
    XCTAssertEqual("1", try getJsonRepresentation(1 as NSNumber))
    XCTAssertEqual("-42.25", try getJsonRepresentation(-42.25 as NSNumber))
  }

  func testSerializeStrings() throws {
    XCTAssertEqual("\"\"", try getJsonRepresentation("" as AnyObject))
    XCTAssertEqual("\"test\"", try getJsonRepresentation("test" as AnyObject))
    XCTAssertEqual("\"test \\\"with\\\" quotes\"", try getJsonRepresentation("test \"with\" quotes" as AnyObject))
    XCTAssertEqual("\"test 🌎\"", try getJsonRepresentation("test 🌎" as AnyObject))
  }

  func testSerializeNativeTypes() throws {
    XCTAssertEqual("[]", try getJsonRepresentation(NSArray()))
    XCTAssertEqual("[1,2,3]", try getJsonRepresentation([1, 2, 3] as NSArray))
    XCTAssertEqual("{}", try getJsonRepresentation(NSDictionary()))
    XCTAssertEqual("{\"key\":\"value\"}", try getJsonRepresentation(["key": "value"] as NSDictionary))
  }

  func testSerializeYailTypes() throws {
    XCTAssertEqual("[]", try getJsonRepresentation(YailList<AnyObject>()))
    XCTAssertEqual("[1,2,3]", try getJsonRepresentation([1, 2, 3] as YailList<AnyObject>))
    XCTAssertEqual("{}", try getJsonRepresentation(YailDictionary()))
    XCTAssertEqual("{\"key\":\"value\"}", try getJsonRepresentation(["key": "value"] as YailDictionary))
  }

  func testSerializerThrows() throws {
    XCTAssertThrowsError(try getJsonRepresentation(NSObject()), "Expected bad object to throw")
  }

  // MARK: Parsing Tests

  func testParseEmpty() throws {
    XCTAssertEqual("", try getObjectFromJson(nil) as? String)
    XCTAssertEqual("", try getObjectFromJson("") as? String)
  }

  func testParseNull() throws {
    XCTAssertEqual(NSNull(), try getObjectFromJson("null") as! NSNull)
  }

  func testParseBoolean() throws {
    XCTAssertEqual(true, try getObjectFromJson("true") as? Bool)
    XCTAssertEqual(false, try getObjectFromJson("false") as? Bool)
  }

  func testParseNumbers() throws {
    XCTAssertEqual(1, try getObjectFromJson("1") as? NSNumber)
    XCTAssertEqual(-42.25, try getObjectFromJson("-42.25") as? NSNumber)
  }

  func testParseStrings() throws {
    XCTAssertEqual("test", try getObjectFromJson("\"test\"") as? String)
    XCTAssertEqual("test \"with\" quotes", try getObjectFromJson("\"test \\\"with\\\" quotes\"") as? String)
    XCTAssertEqual("test 🌎", try getObjectFromJson("\"test 🌎\"") as? String)
  }

  func testParseObjects() throws {
    XCTAssertEqual(NSArray(), try getObjectFromJson("[]") as? NSArray)
    XCTAssertEqual([1, 2, 3] as NSArray, try getObjectFromJson("[1,2,3]") as? NSArray)
    XCTAssertEqual(NSDictionary(), try getObjectFromJson("{}") as? NSDictionary)
    XCTAssertEqual(["key": "value"] as NSDictionary, try getObjectFromJson("{\"key\":\"value\"}") as? NSDictionary)
  }

  // MARK: Parsing to YAIL

  func testParseYailLists() throws {
    let result = try getYailObjectFromJson("[null, true, 1, -42.25, [\"test\"]]", true)
    XCTAssertNotNil(result)
    XCTAssertTrue(result is YailList<AnyObject>)
    let list = result as! YailList<AnyObject>
    XCTAssertEqual(5, list.length)
    XCTAssertTrue(list[1] is String)
    XCTAssertTrue(list[2] is Bool)
    XCTAssertTrue(list[3] is NSNumber)
    XCTAssertTrue(list[4] is NSNumber)
    XCTAssertTrue(list[5] is YailList<AnyObject>)
  }

  func testParseYailDicts() throws {
    let result = try getYailObjectFromJson("{\"bool\": true, \"num1\": 1, \"num2\": -42.25, \"str\": \"test\", \"list\": [1, 2, 3], \"dict\": {\"nested\": true}}", true)
    XCTAssertNotNil(result)
    XCTAssertTrue(result is YailDictionary)
    let dict = result as! YailDictionary
    XCTAssertEqual(6, dict.count)
    XCTAssertTrue(dict["bool"] as! Bool)
    XCTAssertEqual(1, dict["num1"] as! Int)
    XCTAssertEqual(-42.25, dict["num2"] as! Double)
    XCTAssertEqual([1, 2, 3], dict["list"] as! YailList<AnyObject>)
    XCTAssertEqual(["nested": true], dict["dict"] as! YailDictionary)
  }

  func testParseYailDictsAsLists() throws {
    let result = try getYailObjectFromJson("{\"bool\": true, \"num1\": 1, \"num2\": -42.25, \"str\": \"test\", \"list\": [1, 2, 3], \"dict\": {\"nested\": true}}", false)
    XCTAssertNotNil(result)
    XCTAssertTrue(result is YailList<AnyObject>)
    let list = result as! YailList<AnyObject>
    XCTAssertEqual(6, list.length)
    for pair in list {
      guard let elem = pair as? YailList<AnyObject> else {
        continue
      }
      if let key = elem[1] as? String, key == "dict" {
        guard let alist = elem[2] as? YailList<AnyObject> else {
          XCTFail()
          continue
        }
        XCTAssertEqual(["nested", true] as YailList<AnyObject>, alist[1] as? YailList<AnyObject>)
      }
    }
  }

  func testParseYailPrimitives() throws {
    XCTAssertEqual("null", try getYailObjectFromJson("null", true) as! String)
    XCTAssertTrue(try getYailObjectFromJson("true", true) as! Bool)
    XCTAssertFalse(try getYailObjectFromJson("false", true) as! Bool)
    XCTAssertEqual(1, try getYailObjectFromJson("1", true) as! NSNumber)
    XCTAssertEqual(-42.25, try getYailObjectFromJson("-42.25", true) as! NSNumber)
  }

  func testParseNestedLists() throws {
    let result = try getYailObjectFromJson("[[],[],[],[],[],[],[]]", true)
    guard let list = result as? YailList<AnyObject> else {
      XCTFail("Expected result to be a YailList")
      return
    }
    XCTAssertEqual(7, list.length)
  }
}
