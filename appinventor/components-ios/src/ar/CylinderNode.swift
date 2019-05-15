// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.3, *)
open class CylinderNode: ARNodeBase {
  private var _cylinderGeometry: SCNCylinder = SCNCylinder(radius: 0.02, height: 0.06)
  private var _cylinderNode: SCNNode
  
  @objc init(_ container: ARNodeContainer) {
    _cylinderNode = SCNNode(geometry: _cylinderGeometry)
    super.init(container: container, node: _cylinderNode)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc open var RadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_cylinderGeometry.radius)
    }
    set(radius) {
      _cylinderGeometry.radius = UnitHelper.centimetersToMeters(abs(radius))
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_cylinderGeometry.height)
    }
    set(height) {
      _cylinderGeometry.height = UnitHelper.centimetersToMeters(abs(height))
    }
  }
}
