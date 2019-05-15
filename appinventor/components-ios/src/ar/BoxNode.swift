// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.3, *)
open class BoxNode: ARNodeBase, ARBox {
  private var _boxGeometry: SCNBox = SCNBox(width: 0.05, height: 0.05, length: 0.05, chamferRadius: 0)
  private var _boxNode: SCNNode

  @objc init(_ container: ARNodeContainer) {
    _boxNode = SCNNode(geometry: _boxGeometry)
    super.init(container: container, node: _boxNode)
  }

  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_boxGeometry.width)
    }
    set(width) {
      _boxGeometry.width = UnitHelper.centimetersToMeters(abs(width))
    }
  }

  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_boxGeometry.height)
    }
    set(height) {
      _boxGeometry.height = UnitHelper.centimetersToMeters(abs(height))
    }
  }

  @objc open var LengthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_boxGeometry.length)
    }
    set(length) {
      _boxGeometry.length = UnitHelper.centimetersToMeters(abs(length))
    }
  }

  @objc open var CornerRadius: Float {
    get {
      return UnitHelper.metersToCentimeters(_boxGeometry.chamferRadius)
    }
    set(radius) {
      
      _boxGeometry.chamferRadius = UnitHelper.centimetersToMeters(max(0, radius))
    }
  }
}
