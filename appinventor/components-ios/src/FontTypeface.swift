// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class FontTypeface: NSObject, OptionList {
  @objc public static let Default = FontTypeface("0")
  @objc public static let SansSerif = FontTypeface("1")
  @objc public static let Serif = FontTypeface("2")
  @objc public static let Monospace = FontTypeface("3")

  private static let LOOKUP: [String: FontTypeface] = generateOptionsLookup(Default, SansSerif,
      Serif, Monospace)

  let value: String

  @objc private init(_ value: String) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: String) -> FontTypeface? {
    return LOOKUP[value]
  }
}
