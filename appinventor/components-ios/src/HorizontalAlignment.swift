// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class HorizontalAlignment: NSObject, OptionList {
  @objc public static let Left = HorizontalAlignment(1)
  @objc public static let Center = HorizontalAlignment(3)
  @objc public static let Right = HorizontalAlignment(2)

  private static let LOOKUP: [Int32: HorizontalAlignment] = generateOptionsLookup(Left, Center, Right)

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: Int32) -> HorizontalAlignment? {
    return LOOKUP[value]
  }
}
