// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public enum HVOrientation: Int32 {
  case horizontal
  case vertical
}

public enum HorizontalGravity: Int32 {
  case left = 1
  case right = 2
  case center = 3
}

public enum VerticalGravity: Int32 {
  case top = 1
  case center = 2
  case bottom = 3
}

let kDefaultRowColumn: Int32 = -1
let kTextboxPreferredWidth: Int32 = 160
let kEmptyHVArrangementWidth: CGFloat = CGFloat(100)
let kEmptyHVArrangementHeight: CGFloat = CGFloat(100)
let kMapPreferredWidth: Int32 = 176
let kMapPreferredHeight: Int32 = 144
let kCanvasPreferredWidth = 32
let kCanvasPreferredHeight = 48
