// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class CsvUtilTests: XCTestCase {
  func testFromCsvRow() throws {
    XCTAssertEqual(["Entry", "5", "true", "[\"Sample List\"]"],
                   try CsvUtil.fromCsvRow("\"Entry\",\"5\",\"true\",\"[\"\"Sample List\"\"]\""))
  }

  func testFromCsvRowUnquoted() throws {
    XCTAssertEqual(["Hello", "World"],
                   try CsvUtil.fromCsvRow("Hello,World"))
  }

  func testFromCsvRowWithEmbeddedCommas() throws  {
    XCTAssertEqual(["quoted string,with comma"],
                   try CsvUtil.fromCsvRow("\"quoted string,with comma\""))
  }

  func testFromCsvRowWithEmbeddedCommasAndStrings() throws {
    XCTAssertEqual(["string \"with\" quotes, and commas!", "hello!"],
                   try CsvUtil.fromCsvRow("\"string \"\"with\"\" quotes, and commas!\",hello!"))
  }

  func testFromCsvRowWithQuotesNotStarting() throws {
    XCTAssertEqual(["test 14'6\" height", "good"],
                   try CsvUtil.fromCsvRow("test 14'6\" height,good"))
  }

  func testFromCsvTable() throws {
    let parsed = try CsvUtil.fromCsvTable("\"Header1\",\"Header2\",\"Header3\",\"Header4\"\r\n" +
      "\"Entry\",\"5\",\"true\",\"[\"\"Sample List\"\"]\"\r\n")
    let expected = [["Header1", "Header2", "Header3", "Header4"],
                    ["Entry", "5", "true", "[\"Sample List\"]"]]
    assertTablesEqual(expected, parsed)
  }

  func testFromCsvTableCR() throws {
    let parsed = try CsvUtil.fromCsvTable("\"foo\",\"bar\"\r\"1\",\"2\"\r")
    let expected = [["foo", "bar"], ["1", "2"]]
    assertTablesEqual(expected, parsed)
  }

  func testFromCsvTableLF() throws {
    let parsed = try CsvUtil.fromCsvTable("\"foo\",\"bar\"\n\"1\",\"2\"\n")
    let expected = [["foo", "bar"], ["1", "2"]]
    assertTablesEqual(expected, parsed)
  }

  func testFromCsvTableMixed() throws {
    let parsed = try CsvUtil.fromCsvTable("\"foo\",\"bar\"\r\n\"1\",\"2\"\r\"3\",\"4\"\n\"5\",\"6\"")
    let expected = [["foo", "bar"], ["1", "2"], ["3", "4"], ["5", "6"]]
    assertTablesEqual(expected, parsed)
  }

  func testFromCsvTableWithoutFinalLineEnding() throws {
    let parsed = try CsvUtil.fromCsvTable("\"foo\",\"bar\"\r\n\"1\",\"2\"")
    let expected = [["foo", "bar"], ["1", "2"]]
    assertTablesEqual(expected, parsed)
  }

  func testToCsvRow() {
    let result = CsvUtil.toCsvRow(["Entry", "5", "true", "[\"Sample List\"]"])
    XCTAssertEqual("\"Entry\",\"5\",\"true\",\"[\"\"Sample List\"\"]\"", result)
  }

  func testToCsvTable() {
    let result = CsvUtil.toCsvTable([
      ["Header1", "Header2", "Header3", "Header4"],
      ["Entry", "5", "true", "[\"Sample List\"]"]
      ])
    XCTAssertEqual("\"Header1\",\"Header2\",\"Header3\",\"Header4\"\r\n" +
      "\"Entry\",\"5\",\"true\",\"[\"\"Sample List\"\"]\"\r\n", result)
  }

  func assertTablesEqual(_ expected: [[String]], _ actual: YailList<YailList<NSString>>) {
    XCTAssertEqual(expected.count, actual.length)
    for i in 0..<expected.count {
      let expectedRow = YailList<NSString>(array: expected[i])
      let actualRow = actual[i + 1] as! YailList<NSString>
      XCTAssertEqual(expectedRow, actualRow)
    }
  }
}
