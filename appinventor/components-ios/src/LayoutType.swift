// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class LayoutType: NSObject, OptionList {
  @objc public static let MainText = LayoutType(0)
  @objc public static let MainText_DetailText_Vertical = LayoutType(1)
  @objc public static let MainText_DetailText_Horizontal = LayoutType(2)
  @objc public static let Image_MainText = LayoutType(3)
  @objc public static let Image_MainText_DetailText_Vertical = LayoutType(4)
  @objc public static let ImageTop_MainText_DetailText = LayoutType(5)

  private static let LOOKUP: [Int32: LayoutType] = generateOptionsLookup(MainText, MainText_DetailText_Vertical, MainText_DetailText_Horizontal, Image_MainText, Image_MainText_DetailText_Vertical, ImageTop_MainText_DetailText)

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: Int32) -> LayoutType? {
    return LOOKUP[value]
  }
}
