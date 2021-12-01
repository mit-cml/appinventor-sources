// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2021 Massachusetts Institute of Technology. All rights reserved.

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
