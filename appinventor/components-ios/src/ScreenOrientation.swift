// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public class ScreenOrientation: NSObject, OptionList {
  @objc public static let Unspecified = ScreenOrientation("unspecified")
  @objc public static let Landscape = ScreenOrientation("landscape")
  @objc public static let Portrait = ScreenOrientation("portrait")
  @objc public static let Sensor = ScreenOrientation("sensor")
  @objc public static let User = ScreenOrientation("user")
  @objc public static let Behind = ScreenOrientation("behind")
  @objc public static let NoSensor = ScreenOrientation("nosensor")
  @objc public static let FullSensor = ScreenOrientation("fullSensor")
  @objc public static let ReverseLandscape = ScreenOrientation("reverseLandscape")
  @objc public static let ReversePortrait = ScreenOrientation("reversePortrait")
  @objc public static let SensorLandscape = ScreenOrientation("sensorLandscape")
  @objc public static let SensorPortrait = ScreenOrientation("sensorPortrait")

  private static let LOOKUP: [String:ScreenOrientation] = generateOptionsLookup(
    Unspecified, Landscape, Portrait, Sensor, User, Behind, NoSensor, FullSensor,
    ReverseLandscape, ReversePortrait, SensorLandscape, SensorPortrait
  )

  let value: NSString

  @objc private init(_ value: NSString) {
    self.value = value
  }

  @objc class func fromUnderlyingValue(_ value: String) -> ScreenOrientation? {
    return LOOKUP[value]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value
  }
}
