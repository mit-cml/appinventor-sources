//
//  VisibleComponent.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/17/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

@objc public protocol VisibleComponent: Component {
  var Width: Int32 { get set }
  func WidthPercent(_ toPercent: Int32)
  var Height: Int32 { get set }
  func HeightPercent(_ toPercent: Int32)
}
