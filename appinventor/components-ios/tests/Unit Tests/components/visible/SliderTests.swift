// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class SliderTests: XCTestCase {
  var slider: Slider = Slider(Form())

  override func setUp() {
    slider = Slider(Form())
  }

  func testDefaultSliderPosition() {
    baseTest()
  }

  func testSettingAndGettingThumbPosition() {
    testPosition(kSliderMaxValue - 10)
    testPosition(kSliderMinValue + 10)
  }

  func testSettingAndGettingThumbPositionWithOverflow() {
    testPosition(kSliderMinValue - 10)
    testPosition(kSliderMaxValue + 10)
  }

  func testUpdateMaxValueAndThumbPosition() {
    baseTest(maxDiff:  10)
    baseTest(maxDiff: -10)
  }

  func testUpdateMinValueAndThumbPosition() {
    baseTest(minDiff: -10)
    baseTest(minDiff:  10)
  }

  func testUpdateMaxMinValueAndThumbPosition() {
    baseTest(minDiff: -10, maxDiff: 10)
    baseTest(minDiff: 10, maxDiff: 10)
    baseTest(minDiff: 10, maxDiff: -10)
    baseTest(minDiff: -10, maxDiff: -10)
  }

  func testMinValueOverflow() {
    slider.MinValue = kSliderMaxValue + 10
    XCTAssertEqual(slider.MaxValue, kSliderMaxValue + 10, "Slider does not update MaxValue when MinValue is larger than MaxValue")
  }

  func testMaxValueOverflow() {
    slider.MaxValue = kSliderMinValue - 10
    XCTAssertEqual(slider.MinValue, kSliderMinValue - 10, "Slider does not update MinValue when MinValue is larger than MinValue")
  }

  func testMinValue() {
    slider.MinValue = 0.1
    slider.MaxValue = 1.0
    slider.ThumbPosition = 0
    XCTAssertEqual(0.1, slider.ThumbPosition)
  }

  func testMaxValue() {
    slider.MinValue = 0.1
    slider.MaxValue = 1.0
    slider.ThumbPosition = 2
    XCTAssertEqual(1.0, slider.ThumbPosition)
  }

  func testPositionChanged() {
    slider.MinValue = 0.1
    slider.MaxValue = 1.0

    let view = slider.view as! UISlider
    view.value = 0.0
    slider.performSelector(onMainThread: #selector(Slider.positionChanged(sender:)), with: view, waitUntilDone: true)
    XCTAssertEqual(0.1, slider.ThumbPosition)

    view.value = 100.0
    slider.performSelector(onMainThread: #selector(Slider.positionChanged(sender:)), with: view, waitUntilDone: true)
    XCTAssertEqual(1.0, slider.ThumbPosition)
  }

  fileprivate func baseTest(minDiff: Float32? = 0, maxDiff: Float32? = 0) {
    let min = kSliderMinValue + (minDiff ?? 0)
    let max = kSliderMaxValue + (maxDiff ?? 0)
    slider.MinValue = min
    slider.MaxValue = max
    let expected = (max - min) / 2 + min

    XCTAssertEqual(slider.ThumbPosition, expected, "ThumbPosition \(slider.ThumbPosition) does not match the expected value \(expected)")
  }

  fileprivate func testPosition(_ position: Float32) {
    var expected = position
    if position > slider.MaxValue  {
      expected = slider.MaxValue
    } else if position < slider.MinValue {
      expected = slider.MinValue
    }
    slider.ThumbPosition = position
    XCTAssertEqual(slider.ThumbPosition, expected, "Slider ThumbPosition does not match expected value")

  }
}
