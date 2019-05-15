// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.3, *)
open class PyramidNode: ARNodeBase, ARPyramid {
  private var _pyramidGeometry: SCNPyramid = SCNPyramid(width: 0.04, height: 0.04, length: 0.04)
  private var _pyramidNode: SCNNode
  
  @objc init(_ container: ARNodeContainer) {
    _pyramidNode = SCNNode(geometry: _pyramidGeometry)
    super.init(container: container, node: _pyramidNode)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_pyramidGeometry.width)
    }
    set(width) {
      _pyramidGeometry.width = UnitHelper.centimetersToMeters(abs(width))
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_pyramidGeometry.height)
    }
    set(height) {
      _pyramidGeometry.height = UnitHelper.centimetersToMeters(abs(height))
    }
  }
  
  @objc open var LengthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_pyramidGeometry.length)
    }
    set(length) {
      _pyramidGeometry.length = UnitHelper.centimetersToMeters(abs(length))
    }
  }
}
