// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class UltrasonicSensorUnit: NSObject, OptionList {
  @objc public static let Centimeters = UltrasonicSensorUnit("cm", 0)
  @objc public static let Inches = UltrasonicSensorUnit("in", 1)

  private static let LOOKUP: [String: UltrasonicSensorUnit] = generateOptionsLookup(Centimeters, Inches)

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

  @objc public static func fromUnderlyingValue(_ value: String) -> UltrasonicSensorUnit? {
    return LOOKUP[value]
  }
}
