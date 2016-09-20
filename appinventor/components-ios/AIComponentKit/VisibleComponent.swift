//
//  VisibleComponent.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/17/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol VisibleComponent: Component {
  func Width() -> Int32
  func Width(to: Int32)
  func WidthPercent(toPercent: Int32)
  func Height() -> Int32
  func Height(to: Int32)
  func HeightPercent(toPercent: Int32)
}
