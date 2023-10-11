// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit
import MapKit

class MapTests: XCTestCase {

  var testForm: ReplForm!
  var testMap: Map!

  override func setUp() {
    testForm = ReplForm()
    testMap = Map(testForm)
  }

  /**
   * This tests that setting the `Features` property of the `Map` to an empty list will remove
   * any existing components from the `Map`.
   */
  func testClearFeatures() {
    let originalCount = (testMap.view as! MKMapView).annotations.count
    _ = Marker(testMap)
    XCTAssertGreaterThan((testMap.view as! MKMapView).annotations.count, originalCount)
    testMap.Features = YailList<MapFeature>()
    XCTAssertEqual((testMap.view as! MKMapView).annotations.count, originalCount)
    XCTAssertEqual(0, testMap.Features.length)
  }

}
