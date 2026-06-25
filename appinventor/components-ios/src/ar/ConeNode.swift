// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 14.0, *)
open class ConeNode { //}: ARNodeBase, ARCone {
  /*private var _coneGeometry: SCNCone = SCNCone(topRadius: 0, bottomRadius: 0.03, height: 0.07)
  private var _coneNode: SCNNode
  
  @objc init(_ container: ARNodeContainer) {
    _coneNode = SCNNode(geometry: _coneGeometry)
    super.init(container: container, node: _coneNode)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc open var TopRadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_coneGeometry.topRadius)
    }
    set(radius) {
      _coneGeometry.topRadius = UnitHelper.centimetersToMeters(abs(radius))
    }
  }
  
  @objc open var BottomRadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_coneGeometry.bottomRadius)
    }
    set(radius) {
      _coneGeometry.bottomRadius = UnitHelper.centimetersToMeters(abs(radius))
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_coneGeometry.height)
    }
    set(height) {
      _coneGeometry.height = UnitHelper.centimetersToMeters(abs(height))
    }
  }*/
}
