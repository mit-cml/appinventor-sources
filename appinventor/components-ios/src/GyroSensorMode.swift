// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class GyroSensorMode: NSObject, OptionList {
  @objc public static let Angle = GyroSensorMode("angle", 0)
  @objc public static let Rate = GyroSensorMode("rate", 1)

  private static let LOOKUP: [String: GyroSensorMode] = generateOptionsLookup(Angle, Rate)

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

  @objc public static func fromUnderlyingValue(_ value: String) -> GyroSensorMode? {
    return LOOKUP[value]
  }
}
