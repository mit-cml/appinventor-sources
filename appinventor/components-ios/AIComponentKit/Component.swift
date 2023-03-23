// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

let kAssetDirectory = "component"

public enum Alignment: Int32 {
    case normal
    case center
    case opposite
}

public enum AccelerometerSensitivity: Int32 {
    case weak = 1
    case moderate = 2
    case strong = 3
}

public enum ButtonShape: Int32 {
    case normal
    case rounded
    case rect
    case oval
}

public enum Color: UInt32 {
    case none = 0x00FFFFFF
    case black = 0xFF000000
    case blue = 0xFF0000FF
    case cyan = 0xFF00FFFF
    case darkGray = 0xFF444444
    case gray = 0xFF888888
    case green = 0xFF00FF00
    case lightGray = 0xFFCCCCCC
    case magenta = 0xFFFF00FF
    case orange = 0xFFFFC800
    case pink = 0xFFFFAFAF
    case red = 0xFFFF0000
    case white = 0xFFFFFFFF
    case yellow = 0xFFFFFF00
    case `default` = 0x00000000
  
  public var int32: Int32 {
    return Int32(bitPattern: self.rawValue)
  }

  public var uiColor: UIColor {
    return argbToColor(self.int32)
  }
}

public enum DefaultValueColor: String {
  case none = "&H00FFFFFF"
  case black = "&HFF000000"
  case blue = "&HFF0000FF"
  case cyan = "&HFF00FFFF"
  case darkGray = "&HFF444444"
  case gray = "&HFF888888"
  case green = "&HFF00FF00"
  case lightGray = "&HFFCCCCCC"
  case magenta = "&HFFFF00FF"
  case orange = "&HFFFFC800"
  case pink = "&HFFFFAFAF"
  case red = "&HFFFF0000"
  case white = "&HFFFFFFFF"
  case yellow = "&HFFFFFF00"
  case DEFAULT = "&H00000000"
}

public let kFontSizeDefault: Float = 14.0
public let kLayoutOrientationHorizontal = HVOrientation.horizontal
public let kLayoutOrientationVertical = HVOrientation.vertical

public enum Scaling: Int32 {
    case proportionally
    case toFit
}

public enum Typeface: Int32 {
    case normal
    case sansSerif
    case serif
    case monospace
}

public let kLengthPreferred: Int32 = -1
public let kLengthFillParent: Int32 = -2
public let kLengthUnknown: Int32 = -3
public let kLengthPercentTag: Int32 = -1000

public enum ToastLength: Int32 {
  case short
  case long
}

@objc public enum Direction: Int32 {
  case north = 1
  case northeast = 2
  case east = 3
  case southeast = 4
  case south = -1
  case southwest = -2
  case west = -3
  case northwest = -4
  case none = 0
}

public let kSliderMinValue: Float32 = 10
public let kSliderMaxValue: Float32 = 50
public let kSliderThumbValue: Float32 = (kSliderMinValue + kSliderMaxValue) / 2.0

public let kDefaultValueTextToSpeechCountry = ""
public let kDefaultValueTextToSpeechLanguage = ""

@objc public protocol Component: NSCopying {
  var dispatchDelegate: HandlesEventDispatching? { get }
}
