// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0
// This file is auto-generated. Do not edit!

import Foundation

@objc public class OptionHelper: NSObject {
  private static let STRING_LOOKUP_TABLE: [String: [String: (String) -> AnyObject?]] = [
    "DataFile": [
      "DefaultScope": FileScope.fromUnderlyingValue(_:)
    ],
    "Ev3ColorSensor": [
      "Mode": ColorSensorMode.fromUnderlyingValue(_:)
    ],
    "Ev3GyroSensor": [
      "Mode": GyroSensorMode.fromUnderlyingValue(_:)
    ],
    "Ev3UltrasonicSensor": [
      "Unit": UltrasonicSensorUnit.fromUnderlyingValue(_:)
    ],
    "File": [
      "DefaultScope": FileScope.fromUnderlyingValue(_:),
      "Scope": FileScope.fromUnderlyingValue(_:)
    ],
    "FilePicker": [
      "Action": FileAction.fromUnderlyingValue(_:),
      "MimeType": FileType.fromUnderlyingValue(_:)
    ],
    "Form": [
      "CloseScreenAnimation": ScreenAnimation.fromUnderlyingValue(_:),
      "DefaultFileScope": FileScope.fromUnderlyingValue(_:),
      "OpenScreenAnimation": ScreenAnimation.fromUnderlyingValue(_:),
      "ScreenOrientation": ScreenOrientation.fromUnderlyingValue(_:)
    ],
    "Navigation": [
      "TransportationMethod": TransportMethod.fromUnderlyingValue(_:)
    ],
    "Trendline": [
      "Model": BestFitModel.fromUnderlyingValue(_:)
    ]
  ]

  private static let INTEGER_LOOKUP_TABLE: [String: [String: (Int32) -> AnyObject?]] = [
    "AccelerometerSensor": [
      "Sensitivity": Sensitivity.fromUnderlyingValue(_:)
    ],
    "Chart": [
      "Type": ChartType.fromUnderlyingValue(_:)
    ],
    "ChartData2D": [
      "LineType": LineType.fromUnderlyingValue(_:),
      "PointShape": PointStyle.fromUnderlyingValue(_:)
    ],
    "Form": [
      "AlignHorizontal": HorizontalAlignment.fromUnderlyingValue(_:),
      "AlignVertical": VerticalAlignment.fromUnderlyingValue(_:)
    ],
    "HorizontalArrangement": [
      "AlignHorizontal": HorizontalAlignment.fromUnderlyingValue(_:),
      "AlignVertical": VerticalAlignment.fromUnderlyingValue(_:)
    ],
    "HorizontalScrollArrangement": [
      "AlignHorizontal": HorizontalAlignment.fromUnderlyingValue(_:),
      "AlignVertical": VerticalAlignment.fromUnderlyingValue(_:)
    ],
    "ListView": [
      "ListViewLayout": LayoutType.fromUnderlyingValue(_:),
      "Orientation": ListOrientation.fromUnderlyingValue(_:)
    ],
    "Map": [
      "MapType": MapType.fromUnderlyingValue(_:),
      "ScaleUnits": ScaleUnits.fromUnderlyingValue(_:)
    ],
    "Marker": [
      "AnchorHorizontal": HorizontalAlignment.fromUnderlyingValue(_:),
      "AnchorVertical": VerticalAlignment.fromUnderlyingValue(_:)
    ],
    "Texting": [
      "ReceivingEnabled": ReceivingState.fromUnderlyingValue(_:)
    ],
    "Trendline": [
      "StrokeStyle": StrokeStyle.fromUnderlyingValue(_:)
    ],
    "VerticalArrangement": [
      "AlignHorizontal": HorizontalAlignment.fromUnderlyingValue(_:),
      "AlignVertical": VerticalAlignment.fromUnderlyingValue(_:)
    ],
    "VerticalScrollArrangement": [
      "AlignHorizontal": HorizontalAlignment.fromUnderlyingValue(_:),
      "AlignVertical": VerticalAlignment.fromUnderlyingValue(_:)
    ]
  ]

  @objc public static func optionListFromValue(_ component: Component, _ functionName: String, _ value: AnyObject) -> AnyObject? {
    if let v = value as? Int32 {
      guard let lookupTable = INTEGER_LOOKUP_TABLE[String(describing: type(of: component))] else {
        return value
      }
      guard let lookupFunction = lookupTable[functionName] else {
        return value
      }
      return lookupFunction(v)
    } else if let v = value as? String {
      guard let lookupTable = STRING_LOOKUP_TABLE[String(describing: type(of: component))] else {
        return value
      }
      guard let lookupFunction = lookupTable[functionName] else {
        return value
      }
      return lookupFunction(v)
    } else {
      return value
    }
  }
}
