// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
