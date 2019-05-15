// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.3, *)
open class SphereNode: ARNodeBase, ARSphere {
  private var _sphereGeometry: SCNSphere = SCNSphere(radius: 0.05)
  private var _sphereNode: SCNNode
  
  @objc init(_ container: ARNodeContainer) {
    _sphereNode = SCNNode(geometry: _sphereGeometry)
    super.init(container: container, node: _sphereNode)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc open var RadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_sphereGeometry.radius)
    }
    set(radius) {
      _sphereGeometry.radius = UnitHelper.centimetersToMeters(abs(radius))
    }
  }
}
