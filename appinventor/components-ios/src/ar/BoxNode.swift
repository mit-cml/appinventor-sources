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
  
  private var _lastFingerPosition: SIMD3<Float>? = nil
  
  @objc init(_ container: ARNodeContainer) {
    // Create initial box mesh
    let mesh = MeshResource.generateBox(width: 0.05, height: 0.05, depth: 0.05)
    super.init(container: container, mesh: mesh)
    self.Name = "box"
  }

  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
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
    
    updateCollisionShape()
    
    if #available(iOS 15.0, *) {
        updateShadowSettings()
    }
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

  override open func startDrag() {
    let originalPosition = _modelEntity.transform.translation

    if var physicsBody = _modelEntity.physicsBody {
      physicsBody.mode = .kinematic
      _modelEntity.physicsBody = physicsBody
    }
    print("🎯 box Drag started at position: \(originalPosition)")
  }
  
  override open func endDrag(releaseVelocity: CGPoint, camera3DProjection: Any) {
    guard _isBeingDragged else { return }
    
    // Switch back to dynamic mode
    if var physicsBody = _modelEntity.physicsBody {
      physicsBody.mode = .dynamic
      _modelEntity.physicsBody = physicsBody
    }
    
    _isBeingDragged = false
    // Let base class handle surface placement
    super.endDrag(releaseVelocity: releaseVelocity, camera3DProjection: camera3DProjection)
  }
  
  private func updateCollisionShape() {
    let shape = ShapeResource.generateBox(width: _width * Scale,
                                        height: _height * Scale,
                                        depth: _length * Scale)
    _modelEntity.collision = CollisionComponent(shapes: [shape])
    
    debugVisualState()
  }
  
  override open func EnablePhysics(_ isDynamic: Bool = true) {
      let currentPos = _modelEntity.transform.translation
      let groundLevel = Float(ARView3D.SHARED_GROUND_LEVEL)
      
      let bounds = _modelEntity.visualBounds(relativeTo: nil)
      let sizeY = _height * Scale
      let halfHeight = sizeY / 2
      let bottomY = currentPos.y - halfHeight


    // don't scale the collision shape
    let shape: ShapeResource = ShapeResource.generateBox(width: _width, height: _height, depth: _length)
    _modelEntity.collision = CollisionComponent(shapes: [shape])

    _enablePhysics = isDynamic
    
    // Create mass properties separately
    let massProperties = PhysicsMassProperties(mass: Mass)
    
    // Create a custom physics material for gentle collisions
    let gentleMaterial = PhysicsMaterialResource.generate(
      staticFriction: StaticFriction,
      dynamicFriction: DynamicFriction,
      restitution: Restitution
    )
    
    _modelEntity.physicsBody = PhysicsBodyComponent(
      massProperties: massProperties,
      material: gentleMaterial,
      mode: isDynamic ? .dynamic : .static
    )
    
    _modelEntity.physicsMotion = PhysicsMotionComponent()
    
    if #available(iOS 15.0, *) {
        updateShadowSettings()
    }
  }
  
  override open func ScaleBy(_ scalar: Float) {
      let oldScale = Scale
      let hadPhysics = _modelEntity.physicsBody != nil
      
      let bounds = _modelEntity.visualBounds(relativeTo: nil)
      let halfHeight = (bounds.max.y - bounds.min.y) / 2.0  // Use Y for height
      let newScale = oldScale * abs(scalar)
      
      if hadPhysics {
          let previousSize = halfHeight * oldScale  // Use oldScale for clarity
          _modelEntity.position.y = _modelEntity.position.y - previousSize + (halfHeight * newScale)
      }
      
      Scale = newScale
  }

  override open func scaleByPinch(scalar: Float) {
      let oldScale = Scale
      let newScale = oldScale * abs(scalar)
      
      let hadPhysics = _modelEntity.physicsBody != nil
      
      if hadPhysics {
          let savedMass = Mass
          let savedFriction = StaticFriction
          let savedRestitution = Restitution
          
          _modelEntity.physicsBody = nil
          _modelEntity.collision = nil
          
        // Use internal _height like SphereNode uses _radius
        let halfHeight = _height / 2.0
        let previousSize = halfHeight * oldScale
        _modelEntity.position.y = _modelEntity.position.y - previousSize + (halfHeight * newScale)
        
          // Apply scale
          Scale = newScale
        

          
          // Restore physics
          Mass = savedMass
          StaticFriction = savedFriction
          Restitution = savedRestitution
        
          EnablePhysics(true)
      } else {
          Scale = newScale
      }
  }
}
