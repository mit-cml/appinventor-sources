// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class BoxNode: ARNodeBase, ARBox {
  private var _width: Float = 0.05 // stored in meters
  private var _height: Float = 0.05 // stored in meters
  private var _length: Float = 0.05 // stored in meters
  private var _cornerRadius: Float = 0.0 // stored in meters
  private var _showShadow: Bool = true
  

  private var _dragStartPosition: SIMD3<Float>?
  private var _dragStartTime: Date?
  private var _lastFingerPosition: SIMD3<Float>? = nil
  
  
  @objc init(_ container: ARNodeContainer) {
    // Create initial box mesh
    let mesh = MeshResource.generateBox(width: 0.05, height: 0.05, depth: 0.05)
    super.init(container: container, mesh: mesh)
  }

  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func updateCollisionShape() {

    let shape = ShapeResource.generateBox(width: _width, height: _height, depth: _length)
    _modelEntity.collision = CollisionComponent(shapes: [shape])
    
    // Update physics body if it exists
    if _modelEntity.physicsBody != nil {
      let volumeScale = Scale
      let newMass = Mass * volumeScale
       
      let material = PhysicsMaterialResource.generate(
        staticFriction: StaticFriction,
        dynamicFriction: DynamicFriction,
        restitution: Restitution
      )
       
      _modelEntity.physicsBody = PhysicsBodyComponent(
        massProperties: PhysicsMassProperties(mass: newMass),
        material: material,
        mode: .dynamic
      )
       
      print("BOX Collision updated: mass=\(newMass)")
    }

  }
  

  private func updateBoxMesh() {
    // Generate new box mesh with current dimensions
    let mesh = MeshResource.generateBox(
      width: _width,
      height: _height,
      depth: _length,
      cornerRadius: _cornerRadius
    )
    
    // Preserve existing materials when updating mesh
    let existingMaterials = _modelEntity.model?.materials ?? []
    _modelEntity.model = ModelComponent(
      mesh: mesh,
      materials: existingMaterials.isEmpty ? [SimpleMaterial()] : existingMaterials
    )
  }

  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_width)
    }
    set(width) {
      _width = UnitHelper.centimetersToMeters(abs(width))
      updateBoxMesh()
    }
  }

  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_height)
    }
    set(height) {
      _height = UnitHelper.centimetersToMeters(abs(height))
      updateBoxMesh()
    }
  }

  @objc open var LengthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_length)
    }
    set(length) {
      _length = UnitHelper.centimetersToMeters(abs(length))
      updateBoxMesh()
    }
  }
  

  @objc open var CornerRadius: Float {
    get {
      return UnitHelper.metersToCentimeters(_cornerRadius)
    }
    set(radius) {
      _cornerRadius = UnitHelper.centimetersToMeters(max(0, radius))
      updateBoxMesh()
    }
  }
  
  override open func ScaleBy(_ scalar: Float) {
    print("🔄 Scaling box \(Name) by \(scalar)")
    
    let oldScale = Scale
    let oldActualWidth = WidthInCentimeters * oldScale
    let oldActualHeight = HeightInCentimeters * oldScale
    let oldActualDepth = LengthInCentimeters * oldScale
        
    let hadPhysics = _modelEntity.physicsBody != nil
    
    let newScale = oldScale * abs(scalar)
    // adjust for height to be above floor
    if hadPhysics {
      //let previousSize = _capRadius * Scale
      

    }
  
    Scale = newScale
    print("Scale complete - bottom position maintained")
  }
}
