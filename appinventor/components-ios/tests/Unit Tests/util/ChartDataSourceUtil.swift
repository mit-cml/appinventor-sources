// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

/**
 * This class adapted from the Java version, DataSourceUtilTest.
 */
class ChartDataSourceUtilTests: XCTestCase {
  func testDetermineMaximumListSizeEvenSizes() {
    let result = determineMaximumListSize(matrix: [
      ["X", "1", "2", "3", "4"],
      ["Y", "2", "3", "4", "5"],
      ["Z", "3", "4", "5", "6"]
    ] as [AnyObject])
    XCTAssertEqual(5, result)
  }

  func testDetermineMaximumListSizeUnevenSizes() {
    let result = determineMaximumListSize(matrix: [
      ["X", "1"],
      ["Y", "2", "3"],
      ["Z", "3"]
    ] as [AnyObject])
    XCTAssertEqual(3, result)
  }

  func testDetermineMaximumListSizeNonColumnEntry() {
    let result = determineMaximumListSize(matrix: [
      ["X", "1"],
      ["Y", "3", "5"],
      "random-entry"
    ] as [AnyObject])
    XCTAssertEqual(3, result)
  }

  func testDetermineMaximumListSizeEmpty() {
    let result = determineMaximumListSize(matrix: [[]] as [AnyObject])
    XCTAssertEqual(0, result)
  }

  func testGetTransposeEmpty() {
    let result = getTranspose(matrix: [[]] as [AnyObject])
    XCTAssertEqual([[]], result)
  }

  func testGetTransposeSingleEntry() {
    let result = getTranspose(matrix: [["1"]] as [AnyObject])
    XCTAssertEqual([["1"]], result)
  }

  func testGetTransposeMultipleColumns() {
    let matrix = [
      ["X", "Y", "Z"],
      ["1", "2", "3"],
      ["2", "3", "4"],
      ["3", "4", "5"]
    ] as NSArray
    let result = getTranspose(matrix: matrix as [AnyObject]) as! [[String]]
    XCTAssertEqual([["X", "1", "2", "3"], ["Y", "2", "3", "4"], ["Z", "3", "4", "5"]], result)
  }

  func testGetTransposeUnevenSizesDecreasing() {
    let matrix = [
      ["X", "Y", "Z"],
      ["1", "2", "3"],
      ["2", "3"],
      ["3"]
    ] as NSArray
    let result = getTranspose(matrix: matrix as [AnyObject]) as! [[String]]
    XCTAssertEqual([["X", "1", "2", "3"], ["Y", "2", "3", ""], ["Z", "3", "", ""]], result)
  }

  func testGetTransposeUnevenSizesIncreasing() {
    let matrix = [
      ["X"],
      ["Y", "2"],
      ["Z", "3", "4"]
    ] as NSArray
    let result = getTranspose(matrix: matrix as [AnyObject]) as! [[String]]
    XCTAssertEqual([["X", "Y", "Z"], ["", "2", "3"], ["", "", "4"]], result)
  }

  func testGetTransposeUnevenSizeMiddle() {
    let matrix = [
      ["X", "1", "2", "3"],
      ["Y", "2", "3", "4", "5"],
      ["Z", "3", "4"]
    ] as NSArray
    let result = getTranspose(matrix: matrix as [AnyObject]) as! [[String]]
    XCTAssertEqual([["X", "Y", "Z"], ["1", "2", "3"], ["2", "3", "4"], ["3", "4", ""], ["", "5", ""]], result)
  }

  func testTransposeProperty() {
    let matrix = [
      ["X", "Y", "Z"],
      ["5", "22", "73"],
      ["7", "64", "34"],
      ["9", "32", "51"],
      ["11", "32", "9"]
    ]
    let transpose = getTranspose(matrix: matrix as [AnyObject])
    XCTAssertNotEqual(matrix, transpose as! [[String]])
    let transpose2 = getTranspose(matrix: transpose as [AnyObject])
    XCTAssertEqual(matrix, transpose2 as! [[String]])
  }
}
