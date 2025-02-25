// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class AnomalyDetectionTests: AppInventorTestCase {
  var anomalyDetection: AnomalyDetection!
  var xList: YailList<AnyObject>!
  var yList: YailList<AnyObject>!

  override func setUp() {
    super.setUp()
    anomalyDetection = AnomalyDetection(form)
    xList = YailList(array: ["1", "2", "3", "4", "5", "6"])
    yList = YailList(array: ["1", "2", "3", "2", "2", "88"])
  }

  func testDetectAnomalies() {
    let anomalies = anomalyDetection.DetectAnomalies(yList, threshold: 2.0)
    let expectedAnomalies = YailList<AnyObject>(array: [
      YailList<AnyObject>(array: [6, 88.0])
    ])
    XCTAssertEqual(expectedAnomalies.count, anomalies.count)
    XCTAssertEqual(expectedAnomalies, anomalies)
  }

  func testDetectMultipleAnomalies() {
    let yList = YailList<AnyObject>(array: ["1", "2", "78", "2", "2", "88"])
    let anomalies = anomalyDetection.DetectAnomalies(yList, threshold: 1.0)
    let expectedAnomalies = YailList<AnyObject>(array: [
      YailList<AnyObject>(array: [3, 78.0]),
      YailList<AnyObject>(array: [6, 88.0])
    ])
    XCTAssertEqual(expectedAnomalies.count, anomalies.count)
    XCTAssertEqual(expectedAnomalies, anomalies)
  }

  func testGetAnomalyIndex() {
    let index = try! AnomalyDetection.getAnomalyIndex(YailList<AnyObject>(array: [6, 88.0]))
    XCTAssertEqual(6.0, index)
  }

  func testCleanData() {
    let cleanData = try! anomalyDetection.CleanData(YailList<AnyObject>(array: [6, 88.0]), xList, yList)
    let expected = YailList<AnyObject>(array: [
      YailList<AnyObject>(array: [1.0, 1.0]),
      YailList<AnyObject>(array: [2.0, 2.0]),
      YailList<AnyObject>(array: [3.0, 3.0]),
      YailList<AnyObject>(array: [4.0, 2.0]),
      YailList<AnyObject>(array: [5.0, 2.0])
    ])
    XCTAssertEqual(expected.count, cleanData.count)
    XCTAssertEqual(expected, cleanData)
  }
}
