// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class ListOrientation: NSObject, OptionList {
  @objc public static let Vertical = ListOrientation(0)
  @objc public static let Horizontal = ListOrientation(1)

  private static let LOOKUP: [Int32: ListOrientation] = generateOptionsLookup(Vertical, Horizontal)

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: Int32) -> ListOrientation? {
    return LOOKUP[value]
  }
}
