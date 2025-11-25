// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class Sensitivity: NSObject, OptionList {
  @objc public static let Weak = Sensitivity(1)
  @objc public static let Moderate = Sensitivity(2)
  @objc public static let Strong = Sensitivity(3)

  private static let LOOKUP: [Int32: Sensitivity] = generateOptionsLookup(Weak, Moderate, Strong)

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: Int32) -> Sensitivity? {
    return LOOKUP[value]
  }
}
