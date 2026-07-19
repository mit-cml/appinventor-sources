// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// https://www.apache.org/licenses/LICENSE-2.0
// This file is auto-generated. Do not edit!

import Foundation

@objc public class OptionHelper: NSObject {
  private static let STRING_LOOKUP_TABLE: [String: [String: (String) -> AnyObject?]] = [
    "Button": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "CheckBox": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "ContactPicker": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "DataFile": [
      "DefaultScope": FileScope.fromUnderlyingValue(_:)
    ],
    "DatePicker": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "EmailPicker": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
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
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:),
      "MimeType": FileType.fromUnderlyingValue(_:)
    ],
    "Form": [
      "CloseScreenAnimation": ScreenAnimation.fromUnderlyingValue(_:),
      "DefaultFileScope": FileScope.fromUnderlyingValue(_:),
      "OpenScreenAnimation": ScreenAnimation.fromUnderlyingValue(_:),
      "ScreenOrientation": ScreenOrientation.fromUnderlyingValue(_:)
    ],
    "Image": [
      "Animation": ImageAnimation.fromUnderlyingValue(_:)
    ],
    "ImagePicker": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "Label": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "ListPicker": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "ListView": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:),
      "FontTypefaceDetail": FontTypeface.fromUnderlyingValue(_:)
    ],
    "Navigation": [
      "TransportationMethod": TransportMethod.fromUnderlyingValue(_:)
    ],
    "PasswordTextBox": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "PhoneNumberPicker": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "Spinner": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "Switch": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "TextBox": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "TimePicker": [
      "FontTypeface": FontTypeface.fromUnderlyingValue(_:)
    ],
    "Trendline": [
      "Model": BestFitModel.fromUnderlyingValue(_:)
    ]
  ]

  private static let INTEGER_LOOKUP_TABLE: [String: [String: (Int32) -> AnyObject?]] = [
    "AccelerometerSensor": [
      "Sensitivity": Sensitivity.fromUnderlyingValue(_:)
    ],
    "Button": [
      "Shape": Shape.fromUnderlyingValue(_:),
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "Canvas": [
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "Chart": [
      "Type": ChartType.fromUnderlyingValue(_:)
    ],
    "ChartData2D": [
      "LineType": LineType.fromUnderlyingValue(_:),
      "PointShape": PointStyle.fromUnderlyingValue(_:)
    ],
    "ContactPicker": [
      "Shape": Shape.fromUnderlyingValue(_:),
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "DatePicker": [
      "Shape": Shape.fromUnderlyingValue(_:),
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "EmailPicker": [
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "FilePicker": [
      "Shape": Shape.fromUnderlyingValue(_:),
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
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
    "ImagePicker": [
      "Shape": Shape.fromUnderlyingValue(_:),
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "Label": [
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "ListPicker": [
      "Shape": Shape.fromUnderlyingValue(_:),
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
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
    "Notifier": [
      "NotifierLength": NotifierLength.fromUnderlyingValue(_:)
    ],
    "PasswordTextBox": [
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "PhoneNumberPicker": [
      "Shape": Shape.fromUnderlyingValue(_:),
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "Spinner": [
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "TextBox": [
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
    ],
    "Texting": [
      "ReceivingEnabled": ReceivingState.fromUnderlyingValue(_:)
    ],
    "TimePicker": [
      "Shape": Shape.fromUnderlyingValue(_:),
      "TextAlignment": TextAlignment.fromUnderlyingValue(_:)
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
