// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.3, *)
open class PlaneNode: ARNodeBase, ARPlane {
  private var _planeGeometry: SCNPlane = SCNPlane(width: 0.06, height: 0.02)
  private var _planeNode: SCNNode
  
  @objc init(_ container: ARNodeContainer) {
    _planeNode = SCNNode(geometry: _planeGeometry)
    super.init(container: container, node: _planeNode)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_planeGeometry.width)
    }
    set(width) {
      _planeGeometry.width = UnitHelper.centimetersToMeters(abs(width))
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_planeGeometry.height)
    }
    set(height) {
      _planeGeometry.height = UnitHelper.centimetersToMeters(abs(height))
    }
  }
  
  @objc open var CornerRadius: Float {
    get {
      return UnitHelper.metersToCentimeters(_planeGeometry.cornerRadius)
    }
    set(radius) {
      _planeGeometry.cornerRadius = UnitHelper.centimetersToMeters(max(0, radius))
    }
  }
}
