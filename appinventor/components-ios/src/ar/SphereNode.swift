// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class SphereNode: ARNodeBase, ARSphere {
  private var _radius: Float = 0.05 // stored in meters
  private var _storedPhysicsSettings: PhysicsSettings?
  private var _totalRolling: SIMD3<Float> = SIMD3<Float>(0, 0, 0)  // Track total rotation

   struct PhysicsSettings {
       let mass: Float
       let material: PhysicsMaterialResource
       let mode: PhysicsBodyMode
   }
  
  @objc init(_ container: ARNodeContainer) {
    // Create initial sphere mesh
    let mesh = MeshResource.generateSphere(radius: 0.05)
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
  
  
      
      override open func startDrag() {
          print("🎾 Starting drag with nuclear physics removal")
          
          // ✅ Store physics settings if they exist
          if let physicsBody = _modelEntity.physicsBody {
              _storedPhysicsSettings = PhysicsSettings(
                  mass: physicsBody.massProperties.mass,
                  material: physicsBody.material,
                  mode: physicsBody.mode
              )
              
              // ✅ Remove physics completely - don't call EnablePhysics(false)
              _modelEntity.physicsBody = nil
              _modelEntity.collision = nil  // Also remove collision
          }
          
          isBeingDragged = true
          OriginalMaterial = self._modelEntity.model?.materials.first
          
          if #available(iOS 15.0, *) {
              showDragEffect()
          }
          
          print("🎾 Physics completely removed - ready for free dragging")
      }
      
      override open func updateDrag(dragVector: CGPoint, velocity: CGPoint, worldDirection: SIMD3<Float>) {
          // ✅ Debug all values
          print("🎾 === DRAG DEBUG ===")
          print("🎾 dragVector: \(dragVector)")
          print("🎾 worldDirection: \(worldDirection)")
          print("🎾 DragSensitivity: \(DragSensitivity)")
          
          // ✅ Much higher sensitivity and movement scale
          let dragMagnitude = sqrt(dragVector.x * dragVector.x + dragVector.y * dragVector.y)
          let movementScale = min(Float(dragMagnitude) * DragSensitivity, 0.05)  // Increased from 0.02 to 0.1
          
          print("🎾 dragMagnitude: \(dragMagnitude)")
          print("🎾 movementScale: \(movementScale)")
          
          let currentPos = _modelEntity.transform.translation
          let movement = worldDirection * movementScale
          let newPos = currentPos + movement
          
          print("🎾 currentPos: \(currentPos)")
          print("🎾 movement: \(movement)")
          print("🎾 newPos: \(newPos)")
          
          // ✅ No bounds checking - allow movement anywhere
          _modelEntity.transform.translation = newPos
          addVisualRolling(movement: movement)
          
          print("🎾 Moved to: \(newPos)")
          print("🎾 ==================")
      }
      
      override open func endDrag(releaseVelocity: CGPoint, worldDirection: SIMD3<Float>) {
          print("🎾 Ending drag - restoring physics")
          
          // ✅ Restore physics if it was enabled before
          if let storedSettings = _storedPhysicsSettings {
              // Recreate collision first
              let bounds = _modelEntity.visualBounds(relativeTo: nil)
              let size = bounds.max - bounds.min
              let radius = max(size.x, size.y, size.z) / 2
              let shape = ShapeResource.generateSphere(radius: max(radius, 0.03))
              _modelEntity.collision = CollisionComponent(shapes: [shape])
              
              // Recreate physics body
              _modelEntity.physicsBody = PhysicsBodyComponent(
                  massProperties: PhysicsMassProperties(mass: storedSettings.mass),
                  material: storedSettings.material,
                  mode: storedSettings.mode
              )
              
              print("🎾 Physics restored with mass: \(storedSettings.mass)")
          }
          
          // ✅ Apply release impulse after a short delay
          DispatchQueue.main.asyncAfter(deadline: .now() + 0.05) {
              let velocityMagnitude = sqrt(releaseVelocity.x * releaseVelocity.x + releaseVelocity.y * releaseVelocity.y)
              let impulseScale = min(Float(velocityMagnitude) * self.ReleaseForceMultiplier, 0.05)  // Increased
              let releaseImpulse = worldDirection * impulseScale
              
              print("🎾 Applying release impulse: \(releaseImpulse)")
              self.applyReleaseImpulse(releaseImpulse)
          }
          
          isBeingDragged = false
          restoreMaterial()
          _storedPhysicsSettings = nil
      }
      
      private func applyReleaseImpulse(_ impulse: SIMD3<Float>) {
          let currentPos = _modelEntity.transform.translation
          let impulseMovement = impulse * 0.005  // Increased from 0.002
          let newPos = currentPos + impulseMovement
          
          _modelEntity.transform.translation = newPos
          
          print("🎾 Applied impulse movement: \(impulseMovement)")
      }
  
    private func startCustomGravity() {
        guard GravityScale != 1.0 else { return }
        
        // Monitor and adjust gravity effect
        Task {
            while !Task.isCancelled {
                await adjustGravityEffect()
                try? await Task.sleep(nanoseconds: 33_000_000) // ~30fps
            }
        }
    }
    
    private func adjustGravityEffect() async {
        await MainActor.run {
            guard let physicsBody = _modelEntity.physicsBody else { return }
            
            let currentPos = _modelEntity.transform.translation
            
            // Apply additional downward force if gravity scale > 1.0
            if GravityScale > 1.0 && currentPos.y > -1.5 {
                let extraGravity = SIMD3<Float>(0, -0.001 * (GravityScale - 1.0), 0)
                let newPos = currentPos + extraGravity
                _modelEntity.transform.translation = newPos
              isBeingDragged = false
            }
            // For gravity scale < 1.0, we'd need to counteract default gravity
            // This is more complex and might require disabling physics gravity entirely
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

  

  private func addVisualRolling(movement: SIMD3<Float>) {
      let movementMagnitude = simd_length(movement)
      guard movementMagnitude > 0.0001 else { return }
      
      let normalizedMovement = simd_normalize(movement)
      let rollAxis = SIMD3<Float>(normalizedMovement.z, 0, -normalizedMovement.x)
      let rollAngle = movementMagnitude * 30.0
      
      // ✅ Accumulate total rolling instead of multiplying rotations
      _totalRolling += rollAxis * rollAngle
      
      // ✅ Apply fresh rotation from identity
      _modelEntity.transform.rotation = simd_quatf(
          angle: simd_length(_totalRolling),
          axis: simd_length(_totalRolling) > 0 ? simd_normalize(_totalRolling) : SIMD3<Float>(0, 1, 0)
      )
      
      print("🎾 Total rolling: \(_totalRolling)")
  }
}
