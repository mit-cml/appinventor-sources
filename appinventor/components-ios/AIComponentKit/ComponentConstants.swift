//
//  ComponentConstants.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

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
let kEmptyHVArrangementWidth = 100
let kEmptyHVArrangementHeight = 100
let kCanvasPreferredWidth = 32
let kCanvasPreferredHeight = 48
let kMapPreferredWidth: Int32 = 176
let kMapPreferredHeight: Int32 = 144
