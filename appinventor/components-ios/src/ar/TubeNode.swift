// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class TubeNode: ARNodeBase, ARTube {
  private var _radius: Float = 0.05 // stored in meters
  private var _width: Float = 0.05 // stored in meters
  private var _height: Float = 0.05 // stored in meters
  @objc init(_ container: ARNodeContainer) {
    // Create initial sphere mesh
    let mesh = MeshResource.generatePlane(width: _radius, depth: _radius)
    super.init(container: container, mesh: mesh)
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func updatePlaneMesh() { //no tube mesh..
    // Generate new sphere mesh with current radius
    let mesh = MeshResource.generatePlane(width: _radius, depth: _radius)
    
    // Preserve existing materials when updating mesh
    let existingMaterials = _modelEntity.model?.materials ?? []
    _modelEntity.model = ModelComponent(
      mesh: mesh,
      materials: existingMaterials.isEmpty ? [SimpleMaterial()] : existingMaterials
    )
  }
  @objc open var OuterRadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_radius)
    }
    set(radius) {
      _radius = UnitHelper.centimetersToMeters(abs(radius))
      updatePlaneMesh()
    }
  }
    @objc open var InnerRadiusInCentimeters: Float {
      get {
        return UnitHelper.metersToCentimeters(_radius)
      }
      set(radius) {
        _radius = UnitHelper.centimetersToMeters(abs(radius))
        updatePlaneMesh()
      }
    }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_height)
    }
    set(height) {
      _height = UnitHelper.centimetersToMeters(abs(height))
      updatePlaneMesh()
    }
  }
  }
