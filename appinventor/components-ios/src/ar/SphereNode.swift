// Complete Enhanced SphereNode.swift with Fixed Rolling Behavior
// Includes all original functionality plus smooth rolling fixes

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class SphereNode: ARNodeBase, ARSphere {
  private var _radius: Float = 0.05 // stored in meters
  private var _storedPhysicsSettings: PhysicsSettings?
  private var _behaviorName: String = "default"
  
  private var _showShadow: Bool = true
  
  private var _currentDragMode: DragMode = .rolling
  private var _dragStartPosition: SIMD3<Float>?
  private var _dragStartTime: Date?
  private var _lastFingerPosition: SIMD3<Float>? = nil

  private var _dragStartDamping: Float = 0.1
  private var _dragStartAngularDamping: Float = 0.01
  
  private let NORMAL_ROLLING_SPEED: CGFloat = 1200      // Normal finger movement
  private let FAST_ROLLING_SPEED: CGFloat = 2500        // Fast but still rolling
  private let FLING_THRESHOLD_SPEED: CGFloat = 3500     // Definite fling intent
  private let PICKUP_FLING_SPEED: CGFloat = 2000        // Lower threshold when already picked up

  
  // MARK: - Smooth Tracking Properties
  private var _currentMomentum: SIMD3<Float> = SIMD3<Float>(0, 0, 0)
  private var _isRollingWithMomentum:Bool = false
  private var _lastUpdateTime: Date = Date()
  private var _isActivelyRolling: Bool = false
  
  private var _isInCollision: Bool = false
  private var _lastCollisionTime: Date?
  private var _collisionCount: Int = 0
  
  
  enum CollisionType {
      case wall
      case object
  }
  
  enum DragMode {
      case rolling      // Rolling ball along the floor
      case pickup       // Ball lifted off the floor
      case flinging     // Ball thrown with velocity
  }
  
   struct PhysicsSettings {
       let mass: Float
       let material: PhysicsMaterialResource
       let mode: PhysicsBodyMode
   }
  

  private var _collisionAnalyzer = CollisionAnalyzer()

  struct CollisionData {
      let preCollisionVelocity: SIMD3<Float>
      let postCollisionVelocity: SIMD3<Float>
      let collisionNormal: SIMD3<Float>
      let expectedReflection: SIMD3<Float>
      let actualReflection: SIMD3<Float>
      let angleError: Float
      let energyLoss: Float
      let timestamp: Date
  }

  class CollisionAnalyzer {
      private var collisionHistory: [CollisionData] = []
      
      func analyzeCollision(
          preVel: SIMD3<Float>,
          postVel: SIMD3<Float>,
          normal: SIMD3<Float>,
          restitution: Float
      ) -> CollisionData {
          
          // Calculate expected reflection using physics formula
          let expectedReflection = calculatePerfectReflection(
              incident: preVel,
              normal: normal,
              restitution: restitution
          )
          
          // Calculate angle error
          let expectedDirection = simd_normalize(expectedReflection)
          let actualDirection = simd_normalize(postVel)
          let angleError = acos(simd_dot(expectedDirection, actualDirection)) * 180.0 / Float.pi
          
          // Calculate energy loss
          let preEnergy = simd_length_squared(preVel)
          let postEnergy = simd_length_squared(postVel)
          let expectedEnergy = simd_length_squared(expectedReflection)
          let energyLoss = (preEnergy - postEnergy) / preEnergy
          
          let data = CollisionData(
              preCollisionVelocity: preVel,
              postCollisionVelocity: postVel,
              collisionNormal: normal,
              expectedReflection: expectedReflection,
              actualReflection: postVel,
              angleError: angleError,
              energyLoss: energyLoss,
              timestamp: Date()
          )
          
          collisionHistory.append(data)
          printAnalysis(data)
          
          return data
      }
      
      private func calculatePerfectReflection(
          incident: SIMD3<Float>,
          normal: SIMD3<Float>,
          restitution: Float
      ) -> SIMD3<Float> {
          // Physics formula: R = I - 2(I·N)N, scaled by restitution
          let dotProduct = simd_dot(incident, normal)
          return incident - 2.0 * dotProduct * normal * restitution
      }
      
      private func printAnalysis(_ data: CollisionData) {
          print("=== COLLISION ANALYSIS ===")
          print("Pre-collision velocity: \(data.preCollisionVelocity)")
          print("Expected reflection: \(data.expectedReflection)")
          print("Actual post-collision: \(data.postCollisionVelocity)")
          print("Angle error: \(String(format: "%.1f", data.angleError))°")
          print("Energy loss: \(String(format: "%.1f", data.energyLoss * 100))%")
          print("Surface normal: \(data.collisionNormal)")
          
          if data.angleError > 10.0 {
              print("⚠️ TRAJECTORY ERROR: Expected vs actual differs by \(data.angleError)°")
          }
          
          if data.energyLoss > 0.8 {
              print("⚠️ EXCESSIVE ENERGY LOSS: \(data.energyLoss * 100)% energy lost")
          }
          
          print("========================")
      }
  }
   
  // MARK: - Surface Behavior Flags
  public struct SurfaceBehaviorFlags: OptionSet {
      public let rawValue: Int
      public init(rawValue: Int) { self.rawValue = rawValue }
      
      static let rolling = SurfaceBehaviorFlags(rawValue: 1 << 0)    // Rolls when on ground
      static let bouncy = SurfaceBehaviorFlags(rawValue: 1 << 1)     // High bounce
      static let floating = SurfaceBehaviorFlags(rawValue: 1 << 2)   // Reduced gravity
      static let wet = SurfaceBehaviorFlags(rawValue: 1 << 3)        // High friction, low bounce
      static let sticky = SurfaceBehaviorFlags(rawValue: 1 << 4)     // Extreme adherence
      static let slippery = SurfaceBehaviorFlags(rawValue: 1 << 5)   // Low friction
      static let heavy = SurfaceBehaviorFlags(rawValue: 1 << 6)      // High mass
      static let light = SurfaceBehaviorFlags(rawValue: 1 << 7)      // Low mass
  }
  
  private var _behaviorFlags: SurfaceBehaviorFlags = [.rolling]  // Default rolling
  
  @objc init(_ container: ARNodeContainer) {
    // Create initial sphere mesh
    let mesh = MeshResource.generateSphere(radius: _radius)
    super.init(container: container, mesh: mesh)
    self.Name = "sphere"
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func updateCollisionShape() {
      let actualRadius = _radius * Scale
      let shape = ShapeResource.generateSphere(radius: actualRadius)
      _modelEntity.collision = CollisionComponent(shapes: [shape])
    
    
    debugVisualState()
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
    
    
    updateCollisionShape()
    
    
    if #available(iOS 15.0, *) {
        updateShadowSettings()
    }
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
  
  // MARK: - Surface Behavior Properties
  
  @objc open var IsRolling: Bool {
      get { return _behaviorFlags.contains(.rolling) }
      set {
          if newValue { _behaviorFlags.insert(.rolling) }
          else { _behaviorFlags.remove(.rolling) }
          updateBehaviorSettings()
      }
  }
  
  @objc open var IsBouncy: Bool {
      get { return _behaviorFlags.contains(.bouncy) }
      set {
          if newValue { _behaviorFlags.insert(.bouncy) }
          else { _behaviorFlags.remove(.bouncy) }
          updateBehaviorSettings()
      }
  }
  
  @objc open var IsFloating: Bool {
      get { return _behaviorFlags.contains(.floating) }
      set {
          if newValue { _behaviorFlags.insert(.floating) }
          else { _behaviorFlags.remove(.floating) }
          updateBehaviorSettings()
      }
  }
  
  @objc open var IsWet: Bool {
      get { return _behaviorFlags.contains(.wet) }
      set {
          if newValue { _behaviorFlags.insert(.wet) }
          else { _behaviorFlags.remove(.wet) }
          updateBehaviorSettings()
      }
  }
  
  @objc open var IsSticky: Bool {
      get { return _behaviorFlags.contains(.sticky) }
      set {
          if newValue { _behaviorFlags.insert(.sticky) }
          else { _behaviorFlags.remove(.sticky) }
          updateBehaviorSettings()
      }
  }
  
  @objc open var IsSlippery: Bool {
      get { return _behaviorFlags.contains(.slippery) }
      set {
          if newValue { _behaviorFlags.insert(.slippery) }
          else { _behaviorFlags.remove(.slippery) }
          updateBehaviorSettings()
      }
  }
  
  @objc open var IsHeavySphere: Bool {
      get { return _behaviorFlags.contains(.heavy) }
      set {
          if newValue {
              _behaviorFlags.insert(.heavy)
              _behaviorFlags.remove(.light)
          } else {
              _behaviorFlags.remove(.heavy)
          }
          updateBehaviorSettings()
      }
  }
  
  @objc open var IsLightSphere: Bool {
      get { return _behaviorFlags.contains(.light) }
      set {
          if newValue {
              _behaviorFlags.insert(.light)
              _behaviorFlags.remove(.heavy)
          } else {
              _behaviorFlags.remove(.light)
          }
          updateBehaviorSettings()
      }
  }
  
  // MARK: - Behavior Management
  
  @objc open var DefaultBehavior: String{
    get { return _behaviorName }
    set (behavior) {
      AddBehavior(behavior)
    }
  }
  
  @objc open func AddBehavior(_ behaviorName: String) {
      switch behaviorName.lowercased() {
      case "rolling": IsRolling = true
      case "bouncy": IsBouncy = true
      case "floating": IsFloating = true
      case "wet": IsWet = true
      case "sticky": IsSticky = true
      case "slippery": IsSlippery = true
      case "heavy": IsHeavySphere = true
      case "light": IsLightSphere = true
      default:
          IsRolling = true
      }
  }
  
  @objc open func RemoveBehavior(_ behaviorName: String) {
      switch behaviorName.lowercased() {
      case "rolling": IsRolling = false
      case "bouncy": IsBouncy = false
      case "floating": IsFloating = false
      case "wet": IsWet = false
      case "sticky": IsSticky = false
      case "slippery": IsSlippery = false
      case "heavy": IsHeavySphere = false
      case "light": IsLightSphere = false
      default:
          print("🎾 Unknown behavior: \(behaviorName)")
      }
  }
  
  @objc open func resetToDefaultSphere() {
      _behaviorFlags = [.rolling]  // Just rolling
      updateBehaviorSettings()
      print("🎾 Sphere reset to default behavior")
  }
  
  private func debugVisualState() {
      print("=== VISUAL STATE DEBUG ===")
      print("Model materials count: \(_modelEntity.model?.materials.count ?? 0)")
    if #available(iOS 18.0, *) {
      print("Entity opacity: \(_modelEntity.components[OpacityComponent.self]?.opacity ?? 1.0)")
    } else {
      // Fallback on earlier versions
    }
      print("Transform scale: \(_modelEntity.transform.scale)")
      print("==========================")
  }
  
  private func estimateVelocity3D() -> SIMD3<Float> {
      // For older iOS versions, estimate velocity from position changes
      // You'd need to track position over time to calculate this
      // For now, return zero as fallback
      return SIMD3<Float>(0, 0, 0)
  }
  
  private func getCurrentVelocity() -> SIMD3<Float> {
      if #available(iOS 18.0, *) {
          if let physicsMotion = _modelEntity.physicsMotion {
              return physicsMotion.linearVelocity
          }
      }
      
      // Fallback: estimate velocity
      return estimateVelocity3D()
  }
  
  override open func ObjectCollidedWithObject(_ otherNode: ARNodeBase) {
      // Capture pre-collision velocity
      let preVelocity = getCurrentVelocity()
      let preSpeed = simd_length(preVelocity)
      
      // Your existing collision handling
      let speed = getCollisionSpeed()
      let force = calculateCollisionForce(with: otherNode)
     // applyCollisionEffects(force: force, velocity: speed)
    if #available(iOS 15.0, *) {
      showCollisionFlash(intensity: speed, collisionType: .object)
    } else {
      // Fallback on earlier versions
    }
      
      // Analyze trajectory after collision response
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
          guard let self = self else { return }
          let postVelocity = self.getCurrentVelocity()
          
          self.analyzeCollisionTrajectory(
              preVel: preVelocity,
              postVel: postVelocity,
              otherNode: otherNode
          )
      }
  }

  private func analyzeCollisionTrajectory(preVel: SIMD3<Float>, postVel: SIMD3<Float>, otherNode: ARNodeBase) {
      let myPos = _modelEntity.transform.translation
      let otherPos = otherNode._modelEntity.transform.translation
      
      // Calculate collision normal (from other object to this one)
      let collisionNormal = simd_normalize(myPos - otherPos)
      
      // Calculate expected reflection
      let dotProduct = simd_dot(preVel, collisionNormal)
      let expectedReflection = preVel - 2.0 * dotProduct * collisionNormal * Restitution
      
      // Calculate angle error
      let expectedDirection = simd_normalize(expectedReflection)
      let actualDirection = simd_normalize(postVel)
      let angleError = acos(simd_dot(expectedDirection, actualDirection)) * 180.0 / Float.pi
      
      print("=== COLLISION TRAJECTORY ANALYSIS ===")
      print("Pre-collision: \(preVel) (speed: \(simd_length(preVel)))")
      print("Expected bounce: \(expectedReflection)")
      print("Actual result: \(postVel)")
      print("Angle error: \(String(format: "%.1f", angleError))°")
      print("Collision normal: \(collisionNormal)")
      
      if angleError > 15.0 {
          print("⚠️ TRAJECTORY PROBLEM: \(angleError)° off expected path")
      }
      
      print("=====================================")
  }

  

private func monitorPostCollisionState() {
    for i in 1...5 {
        DispatchQueue.main.asyncAfter(deadline: .now() + Double(i) * 0.2) { [weak self] in
            guard let self = self else { return }
            
            let pos = self._modelEntity.transform.translation
            let isEnabled = self._modelEntity.isEnabled
            
          print("   +\(Double(i) * 0.2)s: pos=\(pos), enabled=\(isEnabled)")
            
            // Check if ball suddenly teleported or got disabled
            if !isEnabled {
                print("🚨 BALL DISABLED after collision!")
            }
            
            if abs(pos.y) > 10 || abs(pos.x) > 50 || abs(pos.z) > 50 {
                print("🚨 BALL FLEW OUT OF REASONABLE BOUNDS!")
            }
        }
    }
    
    // Clear collision flag after a moment
    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
        self._isInCollision = false
    }
}
  
  private func calculateCollisionForce(with otherNode: ARNodeBase) -> Float {
      let speed = getCollisionSpeed()
      return Mass * speed // Simple F = ma
  }

  private func getCollisionSpeed() -> Float {
      // Get current speed from physics
      if #available(iOS 18.0, *) {
          if let motion = _modelEntity.physicsMotion {
              return simd_length(motion.linearVelocity)
          }
      }
      return 2.0 // Default fallback
  }

  private func applyBounceEffect(speed: Float) {
      guard speed > 0.5 else { return } // Only bounce if moving fast enough
      
      // Calculate bounce strength based on material
      var bounceForce: Float = speed * Restitution * 5.0
      
      // Adjust for different behaviors
      if _behaviorFlags.contains(.bouncy) {
          bounceForce *= 2.0
      } else if _behaviorFlags.contains(.wet) {
          bounceForce *= 0.3
      } else if _behaviorFlags.contains(.sticky) {
          bounceForce *= 0.1
      }
      
      // Apply upward bounce
      let bounce = SIMD3<Float>(0, bounceForce, 0)
      _modelEntity.addForce(bounce, relativeTo: nil as Entity?)
  }
  
  
  @available(iOS 15.0, *)
  func showCollisionFlash(intensity: Float, collisionType: CollisionType) {
    guard intensity > 1.0 else { return }
    
    var flashMaterial = SimpleMaterial()
    
    if OriginalMaterial == nil {
        OriginalMaterial = _modelEntity.model?.materials.first
    }

      
      // Different colors based on collision type
      switch collisionType {
      case .wall:
          // Wall collision - bright white/cyan flash
          flashMaterial.color = .init(tint: UIColor.cyan.withAlphaComponent(0.9))
          
      case .object:
          // Object collision - color based on behavior
          if _behaviorFlags.contains(.bouncy) {
              flashMaterial.color = .init(tint: .white.withAlphaComponent(0.8))
          } else if _behaviorFlags.contains(.wet) {
              flashMaterial.color = .init(tint: .blue.withAlphaComponent(0.8))
          } else if _behaviorFlags.contains(.heavy) {
              flashMaterial.color = .init(tint: .purple.withAlphaComponent(0.8))
          } else {
              flashMaterial.color = .init(tint: .red.withAlphaComponent(0.8))
          }
      }
      
      _modelEntity.model?.materials = [flashMaterial]
      
      // Flash duration - walls get longer flash
      let flashDuration = collisionType == .wall ? 0.3 : 0.2
      
      DispatchQueue.main.asyncAfter(deadline: .now() + flashDuration) { [weak self] in
          guard let self = self, !self.isBeingDragged else { return }
          
          if let original = self.OriginalMaterial {
              self._modelEntity.model?.materials = [original]
          }
      }
  }


  /* these are default setttings. they can be overridden by setting these values individually */
  private func updateBehaviorSettings() {
    
    
    var baseMass: Float = 0.2
    let massRatio = Mass / baseMass
    

    var staticFriction: Float = 0.5
    var dynamicFriction: Float = 0.3
    var restitution: Float = 0.6
    var gravityScale: Float = 1.0
    var dragSensitivity: Float = 1.0
    
    // MASS EFFECTS on both friction and damping
    let massEffect = calculateMassEffect(massRatio: massRatio)

      // ✅ Apply behavior-specific defaults (last behavior wins if multiple) CSB we don't currently support multiple
      if _behaviorFlags.contains(.heavy) {
        Mass = 1.0  // Heavy default
        dragSensitivity = 1.0  // Harder to drag
        staticFriction = 0.4
        restitution = 0.3
      }
      
      if _behaviorFlags.contains(.light) {
          Mass = 0.04  // Light default
          dragSensitivity = 1.6  // Easier to drag
          restitution = 0.7
      }
      
      if _behaviorFlags.contains(.bouncy) {
          restitution = 0.9  // Very bouncy default
          staticFriction = 0.2  // Low friction for bouncing
          dynamicFriction = 0.12
      }
      
      if _behaviorFlags.contains(.wet) {
          // Wet ball defaults - high friction, low bounce
          staticFriction = 0.8
          dynamicFriction = 0.55
          restitution = 0.15  // Wet balls absorb energy
          dragSensitivity = 0.7  // Harder to drag when wet
      }
      
      if _behaviorFlags.contains(.sticky) {
          // Sticky ball defaults - extreme friction, no bounce
          staticFriction = 0.95
          dynamicFriction = 0.85
          restitution = 0.05  // Almost no bounce
          dragSensitivity = 0.4  // Very hard to drag
      }
      
      if _behaviorFlags.contains(.slippery) {
          // Slippery ball defaults - minimal friction, bouncy
          staticFriction = 0.05
          dynamicFriction = 0.02
          restitution = 0.8  // Bounces well
          dragSensitivity = 1.4  // Easy to drag
      }
      
      if _behaviorFlags.contains(.floating) {
          // Floating defaults - light with reduced gravity
          Mass = 0.02  // Very light
          gravityScale = 0.05  // Almost no gravity
          staticFriction = 0.1  // Low friction for floating
          dynamicFriction = 0.06
          dragSensitivity = 1.8  // Very easy to move
      }
      
      // apply friction and damping
      staticFriction *= massEffect.friction
      dynamicFriction *= massEffect.friction
      _linearDamping *= massEffect.damping
      _angularDamping *= massEffect.damping
    
      // ✅ Set the defaults - user can override these afterward
      StaticFriction = staticFriction
      DynamicFriction = dynamicFriction
      Restitution = restitution
      GravityScale = gravityScale
      DragSensitivity = dragSensitivity
      
      let behaviorNames = getBehaviorNames()
      print("Applied \(behaviorNames.joined(separator: "+")) defaults - Mass: \(Mass), Friction: \(staticFriction), Restitution: \(restitution), DragSensitivity: \(dragSensitivity)")
  }
  
  
  private func getBehaviorNames() -> [String] {
      var names: [String] = []
      if _behaviorFlags.contains(.rolling) { names.append("rolling") }
      if _behaviorFlags.contains(.bouncy) { names.append("bouncy") }
      if _behaviorFlags.contains(.floating) { names.append("floating") }
      if _behaviorFlags.contains(.wet) { names.append("wet") }
      if _behaviorFlags.contains(.sticky) { names.append("sticky") }
      if _behaviorFlags.contains(.slippery) { names.append("slippery") }
      if _behaviorFlags.contains(.heavy) { names.append("heavy") }
      if _behaviorFlags.contains(.light) { names.append("light") }
      return names
  }
  
  private func calculateMassEffect(massRatio: Float) -> (friction: Float, damping: Float) {
      // Mass affects friction and damping differently
      
      // FRICTION: Heavier objects have more contact pressure
      let frictionEffect = sqrt(massRatio)  // Square root for realistic scaling
      
      // Lighter objects affected more by air resistance
      let dampingEffect = 1.0 / sqrt(massRatio)  // Inverse relationship
      
      return (frictionEffect, dampingEffect)
  }


  override open func ScaleBy(_ scalar: Float) {
      print("🔄 Scaling sphere \(Name) by \(scalar)")
      
      let oldScale = Scale
      let oldActualRadius = _radius * oldScale
          
      let hadPhysics = _modelEntity.physicsBody != nil
      
      let newScale = oldScale * abs(scalar)
      // ✅ Update physics immediately if it was enabled before we change the scale
      if hadPhysics {
        let previousSize = _radius * Scale
        _modelEntity.position.y = _modelEntity.position.y - (previousSize) + (_radius * newScale)
  
      }
    
      Scale = newScale
      
      print("🎾 Scale complete - bottom position maintained")
  }

  override open func scaleByPinch(scalar: Float) {
      print("🤏 Pinch scaling sphere \(Name) by \(scalar)")
      
      let oldScale = Scale
      let newScale = oldScale * abs(scalar) // however big it was before times the new scale change
      

      let newActualRadius = _radius * newScale
      let minRadius: Float = 0.01
      let maxRadius: Float = 4.0 // CSB maybe we don't want this?
      
      guard newActualRadius >= minRadius && newActualRadius <= maxRadius else {
          print("🚫 Pinch scale rejected - radius would be \(newActualRadius)m")
          return
      }


      // ✅ CRITICAL: Update collision shape BEFORE and AFTER scaling
      let hadPhysics = _modelEntity.physicsBody != nil
      
      if hadPhysics {
          // Temporarily disable physics to avoid conflicts
          let savedMass = Mass
          let savedFriction = StaticFriction
          let savedRestitution = Restitution
          
          _modelEntity.physicsBody = nil
          _modelEntity.collision = nil
          
          let previousSize = _radius * Scale
          _modelEntity.position.y = _modelEntity.position.y - (previousSize) + (_radius * newScale)
        
        
          // Apply visual scaling
          Scale = newScale

          // Restore physics properties
          Mass = savedMass
          StaticFriction = savedFriction
          Restitution = savedRestitution
          EnablePhysics(true)
          
          print("🎾 Physics recreated with correct collision shape")
      } else {
          // No physics - just scale visually
          _modelEntity.transform.scale = SIMD3<Float>(newScale, newScale, newScale)
      }
      
      print("🎾 Pinch scale complete: \(oldScale) → \(newScale), collision radius: \(newActualRadius)m")
      
      // Debug collision shape
      debugCollisionShape()
  }
  
  @objc open func debugCollisionShape() {
       let visualScale = _modelEntity.transform.scale.x
       let calculatedRadius = _radius * visualScale
       
       print("=== COLLISION SHAPE DEBUG ===")
       print("Internal radius: \(_radius)m")
       print("Visual scale: \(visualScale), and Scale is \(Scale)")
       print("Calculated collision radius: \(calculatedRadius)m")
       print("Has collision: \(_modelEntity.collision != nil)")
       print("Has physics: \(_modelEntity.physicsBody != nil)")
       
       if let collision = _modelEntity.collision {
           print("Collision shapes count: \(collision.shapes.count)")
           
           // Try to extract actual collision radius (this is tricky in RealityKit)
           if let shape = collision.shapes.first {
               print("Collision shape type: \(type(of: shape))")
               // Note: RealityKit doesn't easily expose collision shape dimensions
           }
       }
       
       // Visual bounds check
       let bounds = _modelEntity.visualBounds(relativeTo: nil)
       let visualBoundRadius = (bounds.max.x - bounds.min.x) / 2.0
       print("Visual bounds radius: \(visualBoundRadius)m")
       
       if abs(visualBoundRadius - calculatedRadius) > 0.01 {
           print("⚠️ WARNING: Visual and calculated radius mismatch!")
           print("  Visual: \(visualBoundRadius)m")
           print("  Calculated: \(calculatedRadius)m")
       }
       
       print("==========================")
     }
      
  

      

    
  @available(iOS 15.0, *)
  private func showDragEffect() {
      var dragMaterial = SimpleMaterial()
    
      OriginalMaterial = _modelEntity.model?.materials.first
      // Color based on behavior
      if _behaviorFlags.contains(.heavy) {
          dragMaterial.color = .init(tint: .blue.withAlphaComponent(0.7))  // Blue for wet
      } else if _behaviorFlags.contains(.light) {
          dragMaterial.color = .init(tint: .orange.withAlphaComponent(0.7))  // Orange for sticky
      } else if _behaviorFlags.contains(.bouncy) {
          dragMaterial.color = .init(tint: .white.withAlphaComponent(0.8))  // White for slippery
      } else if _behaviorFlags.contains(.floating) {
          dragMaterial.color = .init(tint: .cyan.withAlphaComponent(0.6))  // Cyan for floating
      } else {
          dragMaterial.color = .init(tint: .yellow.withAlphaComponent(0.7))  // Green for default
      }
    
      
      _modelEntity.model?.materials = [dragMaterial]
    
  }
    
  private func restoreMaterial() {
      if let original = OriginalMaterial {
          _modelEntity.model?.materials = [original]
      }
  }
  
  // MARK: - Preset Sphere Configurations
  
  @objc open func configureAsBeachBall() {
      resetToDefaultSphere()
      IsLightSphere = true
      IsBouncy = true
      IsFloating = true  // Beach balls are buoyant
      print("🏖️ Configured as beach ball")
  }
  
  @objc open func configureAsWetTennisBall() {
      resetToDefaultSphere()
      IsWet = true
      IsRolling = true
      IsBouncy = false  // Wet tennis balls don't bounce much
      print("🎾 Configured as wet tennis ball")
  }
  
  @objc open func configureAsStickyBall() {
      resetToDefaultSphere()
      IsSticky = true
      IsRolling = false  // Sticky balls don't roll
      IsBouncy = false
      print("🍯 Configured as sticky ball")
  }
  
  @objc open func configureAsIceBall() {
      resetToDefaultSphere()
      IsSlippery = true
      IsRolling = true
      IsBouncy = true
      IsHeavySphere = true  // Ice is dense
      print("🧊 Configured as ice ball")
  }
  
  @objc open func configureAsBubble() {
      resetToDefaultSphere()
      IsFloating = true
      IsLightSphere = true
      IsBouncy = false  // Bubbles pop, don't bounce
      IsSlippery = true
      print("🫧 Configured as bubble")
  }
  
  @objc open func configureAsBowlingBall() {
      resetToDefaultSphere()
      IsHeavySphere = true
      IsRolling = true
      IsBouncy = false  // Heavy, doesn't bounce much
      print("🎳 Configured as bowling ball")
  }
  // MARK: - iOS Version-Optimized Drag Methods for SphereNode
  // Optimized for iOS 16+ with iOS 18+ enhancements

  override open func startDrag() {
      _isBeingDragged = true
      
      // Switch to kinematic - position controlled manually
      if var physicsBody = _modelEntity.physicsBody {
          physicsBody.mode = .kinematic
          _modelEntity.physicsBody = physicsBody
      }
  }

  func updateDrag(fingerWorldPosition: SIMD3<Float>) {
      guard _isBeingDragged else { return }
      let constrainedPosition = SIMD3<Float>(
          fingerWorldPosition.x,
          max(fingerWorldPosition.y, Float(ARView3D.SHARED_GROUND_LEVEL) + _radius * Scale + 0.01),
          fingerWorldPosition.z
      )
      // Direct position control
      _modelEntity.transform.translation = constrainedPosition
      
      // Calculate rolling rotation based on movement
      if let lastPos = _lastFingerPosition {
          let movement = fingerWorldPosition - lastPos
          applyRealisticRolling(movement: movement)
      }
      
      _lastFingerPosition = fingerWorldPosition
  }
  
  override open func endDrag(releaseVelocity: CGPoint, worldDirection: SIMD3<Float>) {
      guard _isBeingDragged else { return }
      
      // Switch back to dynamic mode
      if var physicsBody = _modelEntity.physicsBody {
          physicsBody.mode = .dynamic
          _modelEntity.physicsBody = physicsBody
          
          // NOW apply release velocity (only once, at release)
        if #available(iOS 18.0, *) {
          applyReleaseVelocityIOS18(releaseVelocity: releaseVelocity)
        } else {
          applyReleaseForceIOS16(releaseVelocity: releaseVelocity)
        }
      }
      
      _isBeingDragged = false
  }

  private func applyRealisticRolling(movement: SIMD3<Float>) {
      let horizontalMovement = SIMD3<Float>(movement.x, 0, movement.z)
      let distance = simd_length(horizontalMovement)
      
      guard distance > 0.0001 else { return }
      
      // Physics-accurate rolling: distance = radius × angle
      let ballRadius = _radius * Scale
      let rollAngle = distance / ballRadius
      
      // Rotation axis perpendicular to movement
      let direction = simd_normalize(horizontalMovement)
      let rollAxis = SIMD3<Float>(direction.z, 0, -direction.x)
      
      // Apply incremental rotation
      let rollRotation = simd_quatf(angle: rollAngle, axis: rollAxis)
      _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
      print("🎾 Rolling: distance=\(String(format: "%.4f", distance)), angle=\(String(format: "%.4f", rollAngle))")
  }
  

  @available(iOS 18.0, *)
  private func applyReleaseVelocityIOS18(releaseVelocity: CGPoint) {
      let releaseSpeed = sqrt(releaseVelocity.x * releaseVelocity.x + releaseVelocity.y * releaseVelocity.y)
      
      if releaseSpeed > NORMAL_ROLLING_SPEED {
          // Direct velocity calculation instead of force-based
          let baseVelocityScale: Float = 0.002  // Much smaller base scale
          let massMultiplier = max(0.2, min(2.0, Mass))  // Clamp mass effect
          
          let targetVelocity = SIMD3<Float>(
              Float(releaseVelocity.x) * baseVelocityScale / massMultiplier,
              0,
              Float(releaseVelocity.y) * baseVelocityScale / massMultiplier
          )
          
          // Absolute maximum velocity regardless of calculation
          let maxSpeed: Float = 8.0  // 8 m/s maximum
          let currentSpeed = simd_length(targetVelocity)
          
          let finalVelocity: SIMD3<Float>
          if currentSpeed > maxSpeed {
              finalVelocity = simd_normalize(targetVelocity) * maxSpeed
          } else {
              finalVelocity = targetVelocity
          }
          
          if var physicsMotion = _modelEntity.physicsMotion {
              physicsMotion.linearVelocity = SIMD3<Float>(
                  finalVelocity.x,
                  physicsMotion.linearVelocity.y,
                  finalVelocity.z
              )
              _modelEntity.physicsMotion = physicsMotion
              
              print("Fixed velocity calculation: \(finalVelocity)")
          }
      }
  }

  // ✅ iOS 16-17 Release (with force)
  private func applyReleaseForceIOS16(releaseVelocity: CGPoint) {
      let releaseSpeed = sqrt(releaseVelocity.x * releaseVelocity.x + releaseVelocity.y * releaseVelocity.y)
      
      print("🎾 iOS 16-17 Release analysis: finger velocity (\(releaseVelocity.x), \(releaseVelocity.y)), speed: \(releaseSpeed)")
      
      if releaseSpeed > 100 {
          let behaviorMultiplier = getBehaviorMomentumMultiplier()
        let forceScale: Float = 2.0 * behaviorMultiplier // Increased for better momentum
          
          let releaseForce = SIMD3<Float>(
              Float(releaseVelocity.x) * forceScale,
              0,
              Float(releaseVelocity.y) * forceScale
          )
          
          _modelEntity.addForce(releaseForce, relativeTo: nil as Entity?)
          
          print("🎾 iOS 16-17: Applied release force \(releaseForce)")
      } else {
          print("🎾 iOS 16-17: Release speed too low (\(releaseSpeed)), no momentum applied")
      }
  }


  // ✅ Updated gesture handler
  override open func handleAdvancedGestureUpdate(
      fingerLocation: CGPoint,
      fingerMovement: CGPoint,
      fingerVelocity: CGPoint,
      groundProjection: Any?,
      camera3DProjection: Any?,
      gesturePhase: UIGestureRecognizer.State
  ) {
      var groundPos: SIMD3<Float>? = groundProjection as? SIMD3<Float>
      print("sphereNode, handling drag gesture \(groundPos)")
    
      switch gesturePhase {
      case .began:
          startDrag()
          
      case .changed:
          if let worldPos = groundPos {
              updateDrag(fingerWorldPosition: worldPos)
          } else {
              print("⚠️ No groundProjection available during drag")
          }
          
      case .ended, .cancelled:
        endDrag(releaseVelocity: fingerVelocity, worldDirection: groundPos ?? SIMD3<Float>(0, 0, 0))
          
      default:
          break
      }
  }

  // ✅ Keep existing helper methods
  private func getBehaviorMomentumMultiplier() -> Float {
      var multiplier: Float = 1.0
      
      if _behaviorFlags.contains(.heavy) {
          multiplier *= 0.7
      }
      if _behaviorFlags.contains(.light) {
          multiplier *= 1.0
      }
      if _behaviorFlags.contains(.sticky) {
          multiplier *= 0.2
      }
      if _behaviorFlags.contains(.slippery) {
          multiplier *= 1.5
      }
      if _behaviorFlags.contains(.wet) {
          multiplier *= 0.6
      }
      
      return multiplier
  }

  private func getResponsivenessForMass() -> Float {
      let baseMass: Float = 0.2
      let massRatio = Mass / baseMass
      let responsiveness = 1.0 / sqrt(massRatio)
      
      var finalResponsiveness = responsiveness
      
      if _behaviorFlags.contains(.heavy) {
          finalResponsiveness *= 0.6
      }
      if _behaviorFlags.contains(.light) {
          finalResponsiveness *= 1.4
      }
      if _behaviorFlags.contains(.sticky) {
          finalResponsiveness *= 0.4
      }
      if _behaviorFlags.contains(.floating) {
          finalResponsiveness *= 1.6
      }
      if _behaviorFlags.contains(.slippery) {
          finalResponsiveness *= 1.2
      }
      if _behaviorFlags.contains(.wet) {
          finalResponsiveness *= 0.8
      }
      
      return finalResponsiveness
  }



   
    @available(iOS 15.0, *)
    private func showModeEffect() {
        var material = SimpleMaterial()
        
        // Color based on current mode and behavior
        switch _currentDragMode {
        case .rolling:
            if _behaviorFlags.contains(.heavy) {
                material.color = .init(tint: .blue)
            } else if _behaviorFlags.contains(.light) {
                material.color = .init(tint: .orange)
            } else {
                material.color = .init(tint: .yellow)  // Rolling = yellow
            }
            
           
        case .flinging:
            material.color = .init(tint: .red.withAlphaComponent(0.7))    // Flinging = red
        
        default:
          material.color = .init(tint: .red.withAlphaComponent(0.7))    // Flinging = red
        }
        _modelEntity.model?.materials = [material]
    }
    




  // 5. ✅ TRUST PHYSICS: Let RealityKit handle everything
  override open func EnablePhysics(_ isDynamic: Bool = true) {
      if (!isDynamic) {
          _enablePhysics = false
          _modelEntity.collision = nil
          _modelEntity.physicsBody = nil
          return
      }
      
      // ✅ SPHERE COLLISION: Use precise sphere collision

      let shape = ShapeResource.generateSphere(radius: _radius)
      
      _enablePhysics = isDynamic
      _modelEntity.collision = CollisionComponent(shapes: [shape])

      // ✅ REALISTIC PHYSICS: Let RealityKit handle all collisions
      let massProperties = PhysicsMassProperties(mass: Mass)
      
      let material = PhysicsMaterialResource.generate(
          staticFriction: StaticFriction,
          dynamicFriction: DynamicFriction,
          restitution: Restitution
      )
    
    
    
    if _modelEntity.transform.translation.y < ARView3D.SHARED_GROUND_LEVEL{
      print("Warning, sphere is BELOW ground level \(_modelEntity.transform.translation.y) groundlevel: \(ARView3D.SHARED_GROUND_LEVEL)")
      
    }
      
      _modelEntity.physicsBody = PhysicsBodyComponent(
          massProperties: massProperties,
          material: material,
          mode: isDynamic ? .dynamic : .static
      )
   
      // Enable continuous collision detection for fast-moving objects
      if #available(iOS 16.0, *) {
                // Use swept collision detection
        _modelEntity.physicsBody?.isContinuousCollisionDetectionEnabled = true
      }
     
      _modelEntity.physicsMotion = PhysicsMotionComponent()
      
      if #available(iOS 15.0, *) {
          updateShadowSettings()
      }
    
      print("🎾 Physics enabled - RealityKit will handle all ground/floor collisions")
      debugPhysicsState()
      print("🎾 Sphere radius: \(_radius), mass: \(Mass)")
  }

  // 6. ✅ REMOVE ground level constraints entirely
  @objc open func debugPhysicsState() {
      let currentPos = _modelEntity.transform.translation
      
      print("=== PHYSICS STATE DEBUG ===")
      print("Position: \(currentPos)")
      print("Has physics: \(_modelEntity.physicsBody != nil)")
      print("enabled physics?: \(EnablePhysics)")
      
      if let physicsBody = _modelEntity.physicsBody {

          print("Mass: \(physicsBody.massProperties.mass)")
      }
      print("Ball radius: \(_radius)")
      print("Ball radius * scale: \(_radius * Scale)")
      print("Scale: \(Scale)")
      print("==========================")
  }






  // Add this computed property to SphereNode
  @objc open override var ShowShadow: Bool {
      get {
          return _showShadow
      }
      set(showShadow) {
          _showShadow = showShadow
          updateShadowSettings()
      }
  }

  // Add these methods to SphereNode class

  /// Updates shadow casting and receiving for the sphere
  private func updateShadowSettings() {
      guard #available(iOS 15.0, *) else {
          print("⚠️ Shadow control requires iOS 15.0+")
          return
      }
      
      // Enable/disable shadow casting and receiving
      if _showShadow {
          enableShadows()
      } else {
          disableShadows()
      }
  }

  @available(iOS 15.0, *)
  private func enableShadows() {
      // Enable shadow casting
    if #available(iOS 18.0, *) {
      _modelEntity.components.set(GroundingShadowComponent(castsShadow: true))
    } else {
      // TODO Fallback on earlier versions
    }
      
      // For more control over shadow properties, you can also modify materials
      if var material = _modelEntity.model?.materials.first as? SimpleMaterial {
          // Ensure the material can cast shadows
        
          _modelEntity.model?.materials = [material]
      }
      
      print("🌘 Shadows enabled for sphere \(Name)")
  }

  @available(iOS 15.0, *)
  private func disableShadows() {
      // Disable shadow casting
    if #available(iOS 18.0, *) {
      _modelEntity.components.set(GroundingShadowComponent(castsShadow: false))
    } else {
      // TODO Fallback on earlier versions
    }
      
      print("☀️ Shadows disabled for sphere \(Name)")
  }


  // Add convenience methods for shadow control
  @objc open func enableShadowCasting() {
      ShowShadow = true
  }

  @objc open func disableShadowCasting() {
      ShowShadow = false
  }

  // Add shadow-specific behavior methods
  @objc open func setShadowIntensity(_ intensity: Float) {
      guard #available(iOS 15.0, *) else { return }
      
      // Note: RealityKit doesn't have direct shadow intensity control per object
      // This would typically be controlled at the scene lighting level
      print("⚠️ Shadow intensity is controlled by scene lighting in RealityKit")
  }

  @objc open func setShadowColor(_ color: Int32) {
      guard #available(iOS 15.0, *) else { return }
      
      // Note: RealityKit doesn't have direct shadow color control per object
      // This would typically be controlled at the scene lighting level
      print("⚠️ Shadow color is controlled by scene lighting in RealityKit")
  }
  
}
