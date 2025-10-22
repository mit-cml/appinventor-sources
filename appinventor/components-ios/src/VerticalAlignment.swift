// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class VerticalAlignment: NSObject, OptionList {
  @objc public static let Top = VerticalAlignment(1)
  @objc public static let Center = VerticalAlignment(2)
  @objc public static let Bottom = VerticalAlignment(3)

  private static let LOOKUP: [Int32: VerticalAlignment] = generateOptionsLookup(Top, Center, Bottom)

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: Int32) -> VerticalAlignment? {
    return LOOKUP[value]
  }
}
