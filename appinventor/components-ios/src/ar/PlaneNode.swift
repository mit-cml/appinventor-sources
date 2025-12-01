// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class PlaneNode: ARNodeBase, ARPlane {
  private var _width: Float = 0.06 // stored in meters
  private var _height: Float = 0.02 // stored in meters
  private var _cornerRadius: Float = 0.0 // stored in meters
  
  @objc init(_ container: ARNodeContainer) {
    // Create initial plane mesh
    let mesh = MeshResource.generatePlane(width: _width, depth: _height)
    super.init(container: container, mesh: mesh)
    _modelEntity.transform.rotation = simd_quatf(angle: .pi, axis: [0, 1, 0])
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func updatePlaneMesh() {
    // Generate new plane mesh with current dimensions
    let mesh = MeshResource.generatePlane(
      width: _width,
      depth: _height,
      cornerRadius: _cornerRadius
    )
    
    // Preserve existing materials when updating mesh
    let existingMaterials = _modelEntity.model?.materials ?? []
    var twoSidedMaterial: Material!
    if #available(iOS 15.0, *) {
      var mat = PhysicallyBasedMaterial()
      mat.faceCulling = .none
      twoSidedMaterial = mat
    } else {
      twoSidedMaterial = SimpleMaterial()
    }
    
    
    _modelEntity.model = ModelComponent(
      mesh: mesh,
      materials: existingMaterials.isEmpty ? [twoSidedMaterial] : existingMaterials
    )
    
   
    print("🔷 PlaneNode updatePlaneMesh ")
    print("   Current orientation: \(_modelEntity.orientation)")
  }
  
  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_width)
    }
    set(width) {
      _width = UnitHelper.centimetersToMeters(abs(width))
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
  
  @objc open var CornerRadius: Float {
    get {
      return UnitHelper.metersToCentimeters(_cornerRadius)
    }
    set(radius) {
      _cornerRadius = UnitHelper.centimetersToMeters(max(0, radius))
      updatePlaneMesh()
    }
  }
}
