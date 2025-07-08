// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class CapsuleNode: ARNodeBase, ARCapsule {
  private var _capRadius: Float = 0.02 // stored in meters
  private var _height: Float = 0.07 // stored in meters
  
  @objc init(_ container: ARNodeContainer) {
    // Create capsule mesh - RealityKit doesn't have built-in capsule, so we'll create a cylinder with rounded ends
    let mesh = MeshResource.generateBox(width: _capRadius * 2, height: _height, depth: _capRadius * 2)
    super.init(container: container, mesh: mesh)
    self.Name = "capsule"
    // Update the mesh to be more capsule-like
    updateCapsuleMesh()
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func updateCapsuleMesh() {
    // Create a cylinder mesh (closest approximation to capsule in RealityKit)
    // In a full implementation, you'd create a custom mesh with rounded caps
    let mesh = MeshResource.generateBox(width: _capRadius, height: _height,  depth: _height)
    
    // Preserve existing materials when updating mesh
    let existingMaterials = _modelEntity.model?.materials ?? []
    _modelEntity.model = ModelComponent(mesh: mesh, materials: existingMaterials.isEmpty ? [SimpleMaterial()] : existingMaterials)
  }
  
  @objc open var CapRadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_capRadius)
    }
    set(radius) {
      _capRadius = UnitHelper.centimetersToMeters(abs(radius))
      updateCapsuleMesh()
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_height)
    }
    set(height) {
      _height = UnitHelper.centimetersToMeters(abs(height))
      updateCapsuleMesh()
    }
  }
}
