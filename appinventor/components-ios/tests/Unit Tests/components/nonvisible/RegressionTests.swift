// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class RegressionTests: AppInventorTestCase {
  var Regression1: Regression!
  var xList: YailList<AnyObject>!
  var yList: YailList<AnyObject>!

  override func setUp() {
    super.setUp()
    Regression1 = Regression(form)
    xList = YailList(array: ["1", "2", "3", "4", "5", "6"])
    yList = YailList(array: ["1", "2", "3", "2", "2", "88"])
  }

  func testCalculateLineOfBestFitWrongInputSize() {
    let badList = YailList<AnyObject>(array: ["1", "2", "3"])
    do {
      _ = try Regression1.CalculateLineOfBestFitValue(badList, yList, "slope")
      XCTFail()
    } catch {
      // This is expected
    }
  }

  func testCalculateLineOfBestFitValueSlope() {
    let slope = try! Regression1.CalculateLineOfBestFitValue(xList, yList, "slope")
    if let slope = slope as? Double {
      XCTAssertEqual(12.4, slope, accuracy: 0.01)
    } else {
      XCTFail()
    }
  }

  func testCalculateLineOfBestFitValueCorrCoef() {
    let corrCoef = try! Regression1.CalculateLineOfBestFitValue(xList, yList, "correlation coefficient")
    if let corrCoef = corrCoef as? Double {
      XCTAssertEqual(0.66, corrCoef, accuracy: 0.01)
    } else {
      XCTFail()
    }
  }

  func testCalculateLineOfBestFitValueYintercept() {
    let yIntercept = try! Regression1.CalculateLineOfBestFitValue(xList, yList, "Yintercept")
    if let yIntercept = yIntercept as? Double {
      XCTAssertEqual(-27.07, yIntercept, accuracy: 0.01)
    } else {
      XCTFail()
    }
  }

  func testCalculateLineOfBestFitPredictions() {
    let predictions = try! Regression1.CalculateLineOfBestFitValue(xList, yList, "predictions") as! [Double]
    let result = [-14.6667, -2.2667, 10.1333, 22.5333, 34.9333, 47.3333]
    var i = 0
    for item in predictions {
      XCTAssertEqual(item, result[i], accuracy: 0.01)
      i += 1
    }
  }
}
