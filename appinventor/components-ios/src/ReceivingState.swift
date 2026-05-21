// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public class ReceivingState: NSObject, OptionList {
  @objc public static let Off = ReceivingState(1)
  @objc public static let Foreground = ReceivingState(2)
  @objc public static let Always = ReceivingState(3)

  private static let LOOKUP: [Int32:ReceivingState] = generateOptionsLookup(
    Off, Foreground, Always
  )

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc class func fromUnderlyingValue(_ value: Int32) -> ReceivingState? {
    return LOOKUP[value]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }
}
