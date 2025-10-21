// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class ColorSensorMode: NSObject, OptionList {
  @objc public static let Reflected = ColorSensorMode("reflected", 0)
  @objc public static let Ambient = ColorSensorMode("ambient", 1)
  @objc public static let Color = ColorSensorMode("color", 2)

  private static let LOOKUP: [String: ColorSensorMode] = generateOptionsLookup(Reflected, Ambient, Color)

  let value: String
  let intValue: Int32

  @objc private init(_ value: String, _ intValue: Int32) {
    self.value = value
    self.intValue = intValue
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public func toInt() -> Int32 {
    return intValue
  }

  @objc public static func fromUnderlyingValue(_ value: String) -> ColorSensorMode? {
    return LOOKUP[value]
  }
}
