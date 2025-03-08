// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

@objc public class LineType: NSObject, OptionList {

  @objc public static let Linear = LineType(0)
  @objc public static let Curved = LineType(1)
  @objc public static let Stepped = LineType(2)

  private static let LOOKUP: [Int32:LineType] = [
    0: LineType.Linear,
    1: LineType.Curved,
    2: LineType.Stepped
  ]

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc class func fromUnderlyingValue(_ value: Int32) -> LineType? {
    return LOOKUP[value]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }
}

