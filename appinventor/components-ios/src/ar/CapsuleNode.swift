// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.3, *)
open class CapsuleNode: ARNodeBase, ARCapsule {
  private var _capsuleGeometry: SCNCapsule = SCNCapsule(capRadius: 0.02, height: 0.07)
  private var _capsuleNode: SCNNode

  
  @objc init(_ container: ARNodeContainer) {
    _capsuleNode = SCNNode(geometry: _capsuleGeometry)
    super.init(container: container, node: _capsuleNode)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc open var CapRadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_capsuleGeometry.capRadius)
    }
    set(radius) {
      _capsuleGeometry.capRadius = UnitHelper.centimetersToMeters(abs(radius))
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_capsuleGeometry.height)
    }
    set(height) {
      _capsuleGeometry.height = UnitHelper.centimetersToMeters(abs(height))
    }
  }
}
