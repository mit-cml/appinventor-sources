// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class TextAlignment: NSObject, OptionList {
  @objc public static let Left = TextAlignment(0)
  @objc public static let Center = TextAlignment(1)
  @objc public static let Right = TextAlignment(2)

  private static let LOOKUP: [Int32: TextAlignment] = generateOptionsLookup(Left, Center, Right)

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: Int32) -> TextAlignment? {
    return LOOKUP[value]
  }
}
