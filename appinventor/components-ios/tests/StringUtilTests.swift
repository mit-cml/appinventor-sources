// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class StringUtilTests: XCTestCase {

  func testJoinStrings() throws {
    XCTAssertEqual("a:b:c",
                   StringUtil.joinStrings(["a", "b", "c"], NSString(string: ":").description as NSString))
  }

  func testReverseString() {
    XCTAssertEqual("dlroW", StringUtil.reverseString("World"))
  }

  func testReplaceMappingsWithLongestString()  {
    XCTAssertEqual("peace for you and I and ourselves please",StringUtil.replaceAllMappingsLongestString("peace for me and you and yourselves please", ["you":"me", "me": "you","I": "you", "you": "I", "yourselves": "ourselves", "ourselves": "yourselves","myself": "yourself", "yourself": "myself", "mine": "yours", "ours": "yours", "yours": "mine" ]))// repl issue vs  "yours": "ours"])
  }

  func testReplaceMappingsWithDictionary()  {
    XCTAssertEqual("peace for you and I and Irselves please", StringUtil.replaceAllMappingsDictionary("peace for me and you and yourselves please", ["you":"me", "me": "you","I": "you", "you": "I", "yourselves": "ourselves", "ourselves": "yourselves","myself": "yourself", "yourself": "myself", "mine": "yours", "ours": "yours", "yours": "mine" ]))
  }

  func testSplit() {
    let result = StringUtil.split("10010", at: "")
    XCTAssertEqual(5, result.count)
    XCTAssertEqual(["1", "0", "0", "1", "0"], result as? [String])

    let dots = StringUtil.split("...", at: "")
    XCTAssertEqual(3, dots.count)
    XCTAssertEqual([".", ".", "."], dots as? [String])

    let ellipsis = StringUtil.split("…", at: "")
    XCTAssertEqual(1, ellipsis.count)
    XCTAssertEqual(["…"], ellipsis as? [String])

    let empty = StringUtil.split("", at: "")
    XCTAssertEqual(0, empty.count)

    let words = StringUtil.split("apple,banana,cantalope", at: ",")
    XCTAssertEqual(["apple", "banana", "cantalope"], words as? [String])

    let trailing = StringUtil.split("many,commas,,,,,", at: ",")
    XCTAssertEqual(["many", "commas"], trailing as? [String])

    let leading = StringUtil.split("strteststr", at: "str")
    XCTAssertEqual(["", "test"], leading as? [String])

    let emptyWithDelimiter = StringUtil.split("", at: ",")
    XCTAssertEqual(0, emptyWithDelimiter.count)

    let leadingAndTrailing = StringUtil.split(",a,b,", at: ",")
    XCTAssertEqual(["", "a", "b"], leadingAndTrailing as? [String])

    let emoji = StringUtil.split("😀😁😂", at: "")
    XCTAssertEqual(3, emoji.count)
    XCTAssertEqual(["😀", "😁", "😂"], emoji as? [String])

    let emojiDelimiter = StringUtil.split("hello😀world", at: "😀")
    XCTAssertEqual(["hello", "world"], emojiDelimiter as? [String])
  }
}
