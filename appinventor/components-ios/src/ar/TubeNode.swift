// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.3, *)
open class TubeNode: ARNodeBase, ARTube {
  private var _tubeGeometry: SCNTube = SCNTube(innerRadius: 0.01, outerRadius: 0.03, height: 0.08)
  private var _tubeNode: SCNNode
  
  @objc init(_ container: ARNodeContainer) {
    _tubeNode = SCNNode(geometry: _tubeGeometry)
    super.init(container: container, node: _tubeNode)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc open var InnerRadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_tubeGeometry.innerRadius)
    }
    set(radius) {
      _tubeGeometry.innerRadius = UnitHelper.centimetersToMeters(abs(radius))
    }
  }
  
  @objc open var OuterRadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_tubeGeometry.outerRadius)
    }
    set(radius) {
      _tubeGeometry.outerRadius = UnitHelper.centimetersToMeters(abs(radius))
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_tubeGeometry.height)
    }
    set(height) {
      _tubeGeometry.height = UnitHelper.centimetersToMeters(abs(height))
    }
  }
}
