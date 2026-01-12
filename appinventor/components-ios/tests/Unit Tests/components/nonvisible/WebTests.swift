// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class WebTests : AppInventorTestCase {

  var Web1: Web!

  override func setUp() {
    super.setUp()
    Web1 = Web(form)
  }

  func testJsonObjectEncode() {
    XCTAssertEqual("true", Web1.JsonObjectEncode(true as AnyObject))
    XCTAssertEqual("false", Web1.JsonObjectEncode(false as AnyObject))
    XCTAssertEqual("5", Web1.JsonObjectEncode(5 as AnyObject))
    XCTAssertEqual("\"test\"", Web1.JsonObjectEncode("test" as AnyObject))
    XCTAssertEqual("[]", Web1.JsonObjectEncode(YailList<SCMValueProtocol>()))
    XCTAssertEqual("{}", Web1.JsonObjectEncode(YailDictionary()))
    XCTAssertEqual("[true]", Web1.JsonObjectEncode([SCMValue.trueValue] as NSArray))
    let list = [SCMValue.trueValue, SCMValue.intValue(5), SCMString("test")] as YailList<SCMValueProtocol>
    XCTAssertEqual("[true,5,\"test\"]", Web1.JsonObjectEncode(list))
    let dict = ["bool": true, "number": 5, "string": "test",
        "list": ([1, 2, 3] as YailList<SCMValueProtocol>)] as YailDictionary
    try! JSONSerialization.data(withJSONObject: dict)
    XCTAssertEqual("{\"bool\":true,\"number\":5,\"string\":\"test\",\"list\":[1,2,3]}",
        Web1.JsonObjectEncode(dict))
  }

  func testJsonObjectEncodeThrows() {
    expectToReceiveEvent(on: form, named: "ErrorOccurred")
    XCTAssertEqual("", Web1.JsonObjectEncode(Web1))
    verify()
  }

  func testJsonSerializeDeserialize() {
    let original = ["bool": true, "number": 5, "string": "test",
        "list": [1, 2, 3] as YailList<SCMValueProtocol>] as YailDictionary
    let json = Web1.JsonObjectEncode(original)
    let copy = Web1.JsonTextDecodeWithDictionaries(json)
    XCTAssertEqual(original, (copy as? NSDictionary)?.yailDictionary(using: SCMInterpreter.shared))
  }

  func testJsonDecodeWithDictionariesThrows() {
    expectToReceiveEvent(on: form, named: "ErrorOccurred")
    XCTAssertEqual("", Web1.JsonTextDecodeWithDictionaries("this is not json") as? String)
    verify()
  }

  func testUriEncodeDecode() {
    let original = "Feliz compleaños"
    let encoded = Web1.UriEncode(original)
    XCTAssertEqual("Feliz%20complea%C3%B1os", encoded)
    XCTAssertEqual(original, Web1.UriDecode(encoded))
  }

  func testUriDecodeFailure() {
    let original = "Invalid%20UTF-8%80%90"
    XCTAssertEqual(original, Web1.UriDecode(original))
  }

  func testXmlParseToDictionary() {
    XCTAssertEqual(YailDictionary(), Web1.XMLTextDecodeAsDictionary(""))
    let response = Web1.XMLTextDecodeAsDictionary("<a xmlns:d=\"ns:\"><b attr=\"test\">foo<d:c>bar</d:c></b></a>")
    XCTAssertEqual("a", response["$tag"] as? String)
  }

  func testXmlParseToDictionaryThrows() {
    expectToReceiveEvent(on: form, named: "ErrorOccurred")
    let response = Web1.XMLTextDecodeAsDictionary("<malformed")
    XCTAssertEqual(YailDictionary(), response)
    verify()
  }
}
