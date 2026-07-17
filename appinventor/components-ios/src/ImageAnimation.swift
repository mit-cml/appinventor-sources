// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class ImageAnimation: NSObject, OptionList {
  @objc public static let Stop = ImageAnimation("Stop")
  @objc public static let ScrollRightSlow = ImageAnimation("ScrollRightSlow")
  @objc public static let ScrollRight = ImageAnimation("ScrollRight")
  @objc public static let ScrollRightFast = ImageAnimation("ScrollRightFast")
  @objc public static let ScrollLeftSlow = ImageAnimation("ScrollLeftSlow")
  @objc public static let ScrollLeft = ImageAnimation("ScrollLeft")
  @objc public static let ScrollLeftFast = ImageAnimation("ScrollLeftFast")

  private static let LOOKUP: [String: ImageAnimation] = generateOptionsLookup(Stop,
      ScrollRightSlow, ScrollRight, ScrollRightFast, ScrollLeftSlow, ScrollLeft, ScrollLeftFast)

  let value: String

  @objc private init(_ value: String) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: String) -> ImageAnimation? {
    return LOOKUP[value]
  }
}
