// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class NotifierLength: NSObject, OptionList {
  @objc public static let Short = NotifierLength(0)
  @objc public static let Long = NotifierLength(1)

  private static let LOOKUP: [Int32: NotifierLength] = generateOptionsLookup(Short, Long)

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: Int32) -> NotifierLength? {
    return LOOKUP[value]
  }
}
