// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0

@objc public class ScreenAnimation: NSObject, OptionList {
  @objc public static let Default = ScreenAnimation("default")
  @objc public static let Fade = ScreenAnimation("fade")
  @objc public static let Zoom = ScreenAnimation("zoom")
  @objc public static let SlideHorizontal = ScreenAnimation("slidehorizontal")
  @objc public static let SlideVertical = ScreenAnimation("slidevertical")
  @objc public static let None = ScreenAnimation("none")

  private static let LOOKUP: [String: ScreenAnimation] = generateOptionsLookup(Default, Fade, Zoom, SlideHorizontal, SlideVertical, None)

  let value: String

  @objc private init(_ value: String) {
    self.value = value
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }

  @objc public static func fromUnderlyingValue(_ value: String) -> ScreenAnimation? {
    return LOOKUP[value]
  }
}
