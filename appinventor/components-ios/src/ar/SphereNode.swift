// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class SphereNode: ARNodeBase, ARSphere {
  private var _radius: Float = 0.05 // stored in meters
    
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
        guard let physicsBody = _modelEntity.physicsBody else {
            print("⚠️ SphereNode needs physics enabled for dragging")
            return
        }
        
        // Disable gravity while dragging
        isBeingDragged = true
        
        // Store original material for visual feedback
        OriginalMaterial = self._modelEntity.model?.materials.first
    if #available(iOS 15.0, *) {
      showDragEffect()
    } else {
      // Fallback on earlier versions
    }
        
        print("🎾 SphereNode drag started - RealityKit physics paused")
    }
    
  override open func updateDrag(dragVector: CGPoint, velocity: CGPoint, worldDirection: SIMD3<Float>) {

        // Convert screen drag to world movement
        let dragMagnitude = sqrt(dragVector.x * dragVector.x + dragVector.y * dragVector.y)
        let movementScale = min(Float(dragMagnitude) * DragSensitivity, 0.01)
        
        // Move sphere directly while dragging (override physics temporarily)
        let currentPos = _modelEntity.transform.translation
        let movement = worldDirection * movementScale
        let newPos = currentPos + movement
        
        // Simple bounds check
        if newPos.y > -2.0 {  // Don't drag below reasonable floor
            _modelEntity.transform.translation = newPos
            
            // Add rolling rotation for visual feedback
            addVisualRolling(movement: movement)
        }
    }
    
    override open func endDrag(releaseVelocity: CGPoint, worldDirection: SIMD3<Float>) {
        guard let physicsBody = _modelEntity.physicsBody else { return }

        
        // Convert release velocity to physics impulse
        let velocityMagnitude = sqrt(releaseVelocity.x * releaseVelocity.x + releaseVelocity.y * releaseVelocity.y)
        let impulseScale = min(Float(velocityMagnitude) * ReleaseForceMultiplier, 0.03)
        
        let releaseImpulse = worldDirection * impulseScale
        
        // Re-enable RealityKit physics
        isBeingDragged = false
        
        // Apply custom gravity if different from default
        if GravityScale != 1.0 {
            startCustomGravity()
        }
        
        // Apply release impulse
        if #available(iOS 18.0, *) {
            // Use real impulse methods when available
            // physicsBody.applyLinearImpulse(releaseImpulse, relativeTo: nil)
        } else {
            // iOS 16 workaround
            applyReleaseImpulse(releaseImpulse)
        }
        
        restoreMaterial()
        
        print("🎾 SphereNode released - RealityKit physics resumed with custom gravity: \(GravityScale)")
    }
    
    private func applyReleaseImpulse(_ impulse: SIMD3<Float>) {
        // iOS 16 workaround - micro movement that physics responds to
        let currentPos = _modelEntity.transform.translation
        let impulseMovement = impulse * 0.002  // Small movement in impulse direction
        let newPos = currentPos + impulseMovement
        
        _modelEntity.transform.translation = newPos
      isBeingDragged = false
        
        // Physics will take over from this new position with momentum
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
        
        let rollRotation = simd_quatf(angle: rollAngle, axis: rollAxis)
        _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
    }
}
