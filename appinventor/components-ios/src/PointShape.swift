// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public class PointStyle: NSObject, OptionList{
  @objc public static let Circle = PointStyle(0)
  @objc public static let Square = PointStyle(1)
  @objc public static let Triangle = PointStyle(2)
  @objc public static let Cross = PointStyle(3)
  @objc public static let X = PointStyle(4)

  private static let LOOKUP: [Int32:PointStyle] = [
    0: PointStyle.Circle,
    1: PointStyle.Square,
    2: PointStyle.Triangle,
    3: PointStyle.Cross,
    4: PointStyle.X
  ]

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc class func fromUnderlyingValue(_ value: Int32) -> PointStyle? {
    return LOOKUP[value]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }
}
