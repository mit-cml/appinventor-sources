// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class SphereNode: ARNodeBase, ARSphere {
  private var _radius: Float = 0.05 // stored in meters
  private var _storedPhysicsSettings: PhysicsSettings?
  private var _accumulatedRoll: Float = 0.0
  private var _rollDirection: SIMD3<Float> = SIMD3<Float>(0, 0, 0)
  private var _totalRolling: SIMD3<Float> = SIMD3<Float>(0, 0, 0)  // Track total rotation

  
  
   struct PhysicsSettings {
       let mass: Float
       let material: PhysicsMaterialResource
       let mode: PhysicsBodyMode
   }
  
  @objc init(_ container: ARNodeContainer) {
    // Create initial sphere mesh
    let mesh = MeshResource.generateSphere(radius: _radius)
    super.init(container: container, mesh: mesh)
    self.Name = "sphere" // vs type.. ?
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func updateSphereMesh() {
    // Generate new sphere mesh with current radius
    let mesh = MeshResource.generateSphere(radius: _radius)
    
    // Preserve existing materials when updating mesh
    let existingMaterials = _modelEntity.model?.materials ?? []
    _modelEntity.model = ModelComponent(
      mesh: mesh,
      materials: existingMaterials.isEmpty ? [SimpleMaterial()] : existingMaterials
    )
  }
  
  @objc open var RadiusInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_radius)
    }
    set(radius) {
      _radius = UnitHelper.centimetersToMeters(abs(radius))
      updateSphereMesh()
    }
  }
  

      
      // MARK: - Simple Drag Methods
      
      override open func startDrag() {
          print("🎾 Starting simple drag")
          
          // ✅ Remove physics during drag
          if let physicsBody = _modelEntity.physicsBody {
              _storedPhysicsSettings = PhysicsSettings(
                  mass: physicsBody.massProperties.mass,
                  material: physicsBody.material,
                  mode: physicsBody.mode
              )
          }
          
          _modelEntity.physicsBody = nil
          _modelEntity.collision = nil
          
          isBeingDragged = true
          
          // ✅ Store original material if you have custom materials
          OriginalMaterial = _modelEntity.model?.materials.first
          
          print("🎾 Drag started - physics disabled")
      }
      
      override open func updateDrag(dragVector: CGPoint, velocity: CGPoint, worldDirection: SIMD3<Float>) {
          // ✅ Simple movement - sphere follows finger
          let movement = worldDirection * DragSensitivity
          
          let currentPos = _modelEntity.transform.translation
          let newPos = currentPos + movement
          _modelEntity.transform.translation = newPos
          
          // ✅ Simple rolling decision
          if shouldRoll(currentHeight: currentPos.y, movement: movement) {
              applySimpleRolling(movement: movement)
          }
          
          print("🎾 Dragged to: \(newPos)")
      }
      
  override open func endDrag(releaseVelocity: CGPoint, worldDirection: SIMD3<Float>) {
      let finalPosition = _modelEntity.transform.translation
      print("🎾 Ending drag at: \(finalPosition)")
      
      // ✅ Restore physics with velocity reset
      restorePhysicsWithVelocityReset(at: finalPosition)
      
      // ✅ Apply end behavior
      let isOnGround = isNearGround(height: finalPosition.y)
      if isOnGround {
          print("🎾 On ground - will slow down and stop rolling")
          startGroundMomentum()
      } else {
          print("🎾 In air - will fall and bounce naturally")
      }
      
      isBeingDragged = false
  }

  private func restorePhysicsWithVelocityReset(at position: SIMD3<Float>) {
      // ✅ Enable physics
      EnablePhysics(true)
      _modelEntity.transform.translation = position
      
      // ✅ Reset velocity immediately
      /*if #available(iOS 18.0, *) {
          _modelEntity.physicsBody?.linearVelocity = SIMD3<Float>(0, 0, 0)
          _modelEntity.physicsBody?.angularVelocity = SIMD3<Float>(0, 0, 0)
      } else {*/
          // iOS 16 workaround
          let currentMass = _modelEntity.physicsBody?.massProperties.mass ?? 1.0
          _modelEntity.physicsBody?.massProperties = PhysicsMassProperties(mass: 50.0)  // Heavy
          
          DispatchQueue.main.asyncAfter(deadline: .now() + 0.05) {
              self._modelEntity.physicsBody?.massProperties = PhysicsMassProperties(mass: currentMass)
          //}
      }
      
      // ✅ Restore material
      if let original = OriginalMaterial {
          _modelEntity.model?.materials = [original]
          OriginalMaterial = nil
      }
      
      print("🎾 Physics restored with velocity reset")
  }

  
      
      // MARK: - Simple Helper Methods
      
      private func shouldRoll(currentHeight: Float, movement: SIMD3<Float>) -> Bool {
          let groundLevel: Float = -0.5  // Adjust for your floor
          let ballRadius = _radius * _modelEntity.transform.scale.x
          let isOnGround = currentHeight <= (groundLevel + ballRadius + 0.05)
          
          // ✅ Roll if on ground and moving horizontally
          let horizontalMovement = sqrt(movement.x * movement.x + movement.z * movement.z)
          let verticalMovement = abs(movement.y)
          
          return isOnGround && horizontalMovement > verticalMovement * 0.5
      }
      
      private func applySimpleRolling(movement: SIMD3<Float>) {
          let horizontalMovement = SIMD3<Float>(movement.x, 0, movement.z)
          let distance = simd_length(horizontalMovement)
          
          guard distance > 0.001 else { return }
          
          // ✅ Calculate roll angle: distance = radius × angle
          let ballRadius = _radius * _modelEntity.transform.scale.x
          let rollAngle = distance / ballRadius
          
          // ✅ Roll axis perpendicular to movement
          let direction = simd_normalize(horizontalMovement)
          let rollAxis = SIMD3<Float>(direction.z, 0, -direction.x)
          
          // ✅ Apply rotation
          let rollRotation = simd_quatf(angle: rollAngle, axis: rollAxis)
          _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
          
          // ✅ Track for momentum
          _accumulatedRoll += rollAngle
          _rollDirection = direction
          
          print("🎾 Rolling: distance=\(distance), angle=\(rollAngle)")
      }
      
      private func isNearGround(height: Float) -> Bool {
          let groundLevel: Float = -0.5
          let ballRadius = _radius * _modelEntity.transform.scale.x
          return height <= (groundLevel + ballRadius + 0.1)
      }
      
      private func restorePhysics(at position: SIMD3<Float>) {
          // ✅ Recreate physics
          EnablePhysics(true)
          _modelEntity.transform.translation = position
          
          // ✅ Restore original material
          if let original = OriginalMaterial {
              _modelEntity.model?.materials = [original]
              OriginalMaterial = nil
          }
          
          print("🎾 Physics restored")
      }
      
      private func startGroundMomentum() {
          // ✅ Simple momentum - gradually slow down rolling
          guard _accumulatedRoll > 0.1 else { return }
          
          Task {
              var remainingRoll = _accumulatedRoll * 0.3  // 30% momentum
              let dampingRate: Float = 0.9  // 10% slower each frame
              
              while remainingRoll > 0.01 && !isBeingDragged {
                  await MainActor.run {
                      let rollAngle = remainingRoll * 0.1  // Small increments
                      let rollRotation = simd_quatf(angle: rollAngle, axis: _rollDirection)
                      _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
                      
                      remainingRoll *= dampingRate
                  }
                  
                  try? await Task.sleep(nanoseconds: 33_000_000)  // ~30fps
              }
              
              print("🎾 Rolling momentum stopped")
          }
      }


  @available(iOS 15.0, *)
  private func showDragEffect() {
        var dragMaterial = SimpleMaterial()
        dragMaterial.color = .init(tint: .cyan.withAlphaComponent(0.7))
        _modelEntity.model?.materials = [dragMaterial]
    }
    
    private func restoreMaterial() {
        if let original = OriginalMaterial {
            _modelEntity.model?.materials = [original]
        }
    }
  

  
}
