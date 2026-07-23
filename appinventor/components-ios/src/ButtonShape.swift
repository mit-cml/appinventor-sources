// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class ButtonShape: NSObject, OptionList {
  @objc public static let Default = ButtonShape(0)
  @objc public static let Rounded = ButtonShape(1)
  @objc public static let Rectangular = ButtonShape(2)
  @objc public static let Oval = ButtonShape(3)

  private static let LOOKUP: [Int32: ButtonShape] = generateOptionsLookup(Default, Rounded,
      Rectangular, Oval)

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: Int32) -> ButtonShape? {
    return LOOKUP[value]
  }
}
