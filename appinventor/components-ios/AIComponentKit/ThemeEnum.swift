// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

enum Theme : String {
  case Classic
  case DeviceDefault
  case BlackText
  case Dark

  static func fromString(_ value: String) -> Theme {
    switch (value) {
    case "Classic":
      return .Classic
    case "AppTheme.Light.DarkActionBar":
      return .DeviceDefault
    case "AppTheme.Light":
      return .BlackText
    case "AppTheme":
      return .Dark
    default:
      return .Classic
    }
  }
}
