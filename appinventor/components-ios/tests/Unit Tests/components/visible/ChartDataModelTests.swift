// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
import DGCharts
@testable import AIComponentKit

class ChartDataModelTests: AppInventorTestCase {

  var testChart: Chart!
  var testModel: ChartDataModel!
  var testData: LineChartData!
  var testView: AIComponentKit.LineChartView!

  override func setUp() {
    super.setUp()
    testChart = Chart(form)
    testData = LineChartData()
    testView = LineChartView(testChart)
    testModel = LineChartDataModel(data: testData, view: testView)
  }

  func populate() {
    testModel.addEntryFromTuple([0.0, 6.0] as YailList<AnyObject>)
    testModel.addEntryFromTuple([2.0, 5.0] as YailList<AnyObject>)
    testModel.addEntryFromTuple([4.0, 6.0] as YailList<AnyObject>)
    testModel.addEntryFromTuple([6.0, 0.0] as YailList<AnyObject>)
    testModel.addEntryFromTuple([8.0, 6.0] as YailList<AnyObject>)
    testModel.addEntryFromTuple([10.0, 5.0] as YailList<AnyObject>)
  }

  func testHighlightPoints() {
    populate()
    XCTAssertTrue(testModel.highlightPoints([[6.0, 0.0] as YailList<AnyObject>], Color.red.int32))
    XCTAssertEqual(Color.red.uiColor, testModel.highlights[3])
  }

  func testHighlightPointsAddInvariant() {
    testHighlightPoints()

    // If we insert a value before the highlighted point, the highlight index should be one greater
    // than it was previously
    testModel.addEntryFromTuple([1.0, 5.0] as YailList<AnyObject>)
    XCTAssertEqual(Color.red.uiColor, testModel.highlights[4])
  }

  func testHighlightPointsRemoveInvariant() {
    testHighlightPoints()

    // If we remove a value before the highlighted point, the highlight index should be one less
    // than it was previously
    testModel.removeEntryFromTuple([2.0, 5.0] as YailList<AnyObject>)
    XCTAssertEqual(Color.red.uiColor, testModel.highlights[2])
  }
}
