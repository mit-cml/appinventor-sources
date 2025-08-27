// Complete Enhanced SphereNode.swift with Fixed Rolling Behavior
// Includes all original functionality plus smooth rolling fixes

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class SphereNode: ARNodeBase, ARSphere {
  private var _radius: Float = 0.05 // stored in meters
  private var _storedPhysicsSettings: PhysicsSettings?
  private var _accumulatedRoll: Float = 0.0
  private var _rollDirection: SIMD3<Float> = SIMD3<Float>(0, 0, 0)
  private var _totalRolling: SIMD3<Float> = SIMD3<Float>(0, 0, 0)  // Track total rotation
  private var _behaviorName: String = "default"
  
  private var _currentDragMode: DragMode = .rolling
  private var _dragStartPosition: SIMD3<Float>?
  private var _dragStartTime: Date?
  private var _lastFingerPosition: SIMD3<Float>? = nil
  private var _fingerTrajectory: [CGPoint] = []
  private var _rollingPath: [SIMD3<Float>] = []
  private var _pickupStartHeight: Float = 0.0

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
  
  private func updateSphereMesh() {
    // Generate new sphere mesh with current radius
    let mesh = MeshResource.generateSphere(radius: _radius * Scale)
    
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
  

  @objc open override func ObjectCollidedWithObject(_ otherNode: ARNodeBase) {
    var collidedMaterial = SimpleMaterial()
    if OriginalMaterial == nil {
        OriginalMaterial = _modelEntity.model?.materials.first
    }
    if #available(iOS 15.0, *) {
      collidedMaterial.color = .init(tint: .green.withAlphaComponent(0.7))
      self._modelEntity.model?.materials = [collidedMaterial]
      restoreColorAfterCollision()
    } else {
      // Fallback on earlier versions
    }  // Green for default
    
    
  }
  
  private func restoreColorAfterCollision() {
      // Cancel any existing restore timer
      // (You might want to add a property to track this if you need to cancel)
      
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) { // 200ms flash
          guard !self.isBeingDragged else {
              // Don't restore if currently being dragged (drag has its own color)
              return
          }
          
          if let original = self.OriginalMaterial {
              self._modelEntity.model?.materials = [original]
              print("🎾 Color restored after collision")
          }
      }
  }
  
  /* these are default setttings. they can be overridden by setting these values individually */
  private func updateBehaviorSettings() {
      // ✅ Start with base sphere defaults
      var mass: Float = 0.2
      var staticFriction: Float = 0.5
      var dynamicFriction: Float = 0.3
      var restitution: Float = 0.6
      var gravityScale: Float = 1.0
      var dragSensitivity: Float = 1.0
      
      // ✅ Apply behavior-specific defaults (last behavior wins if multiple)
      if _behaviorFlags.contains(.heavy) {
        mass = 1.0  // Heavy default
        dragSensitivity = 1.0  // Harder to drag
        staticFriction = 0.7
      }
      
      if _behaviorFlags.contains(.light) {
          mass = 0.04  // Light default
          dragSensitivity = 1.6  // Easier to drag
      }
      
      if _behaviorFlags.contains(.bouncy) {
          restitution = 0.9  // Very bouncy default
          staticFriction = 0.2  // Low friction for bouncing
          dynamicFriction = 0.12
      }
      
      if _behaviorFlags.contains(.wet) {
          // Wet ball defaults - high friction, low bounce
          staticFriction = 0.8
          dynamicFriction = 0.65
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
          mass = 0.02  // Very light
          gravityScale = 0.05  // Almost no gravity
          staticFriction = 0.1  // Low friction for floating
          dynamicFriction = 0.06
          dragSensitivity = 1.8  // Very easy to move
      }
      
      // ✅ Set the defaults - user can override these afterward
      Mass = mass
      StaticFriction = staticFriction
      DynamicFriction = dynamicFriction
      Restitution = restitution
      GravityScale = gravityScale
      DragSensitivity = dragSensitivity
      
      let behaviorNames = getBehaviorNames()
      print("Applied \(behaviorNames.joined(separator: "+")) defaults - Mass: \(mass), Friction: \(staticFriction), Restitution: \(restitution), DragSensitivity: \(dragSensitivity)")
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

  override open func ScaleBy(_ scalar: Float) {
      print("🔄 Scaling sphere \(Name) by \(scalar)")
      
      let currentPos = _modelEntity.transform.translation
      let oldScale = Scale
      let oldActualRadius = _radius * oldScale
      
      // ✅ Calculate where the bottom of the sphere currently is
      let currentBottomY = currentPos.y - oldActualRadius
      
      // ✅ Keep physics enabled during scaling
      let hadPhysics = _modelEntity.physicsBody != nil
      
      // ✅ Update scale
      let newScale = oldScale * abs(scalar)
      _modelEntity.transform.scale = SIMD3<Float>(newScale, newScale, newScale)
      
      // ✅ Calculate new radius and maintain bottom position
      let newActualRadius = _radius * newScale
      let newCenterY = currentBottomY + newActualRadius
      
      print("🔄 Scaling sphere center y from \(currentPos.y) to \(newCenterY)")
      print("🔄 Bottom Y stays at: \(currentBottomY), oldRadius: \(oldActualRadius), newRadius: \(newActualRadius)")
      
      // ✅ Apply new position
      let newPosition = SIMD3<Float>(currentPos.x, newCenterY, currentPos.z)
      _modelEntity.transform.translation = newPosition

      // ✅ Update physics immediately if it was enabled
      if hadPhysics {
          updatePhysicsCollisionShape()
      }
      
      print("🎾 Scale complete - bottom position maintained")
  }

  override open func scaleByPinch(scalar: Float) {
          print("🤏 Pinch scaling sphere \(Name) by \(scalar)")
          
          let oldScale = Scale
          let newScale = oldScale * scalar
          
          // Validate bounds
          let newActualRadius = _radius * newScale
          let minRadius: Float = 0.01
          let maxRadius: Float = 3.0
          
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
              
              // Apply visual scaling
              _modelEntity.transform.scale = SIMD3<Float>(newScale, newScale, newScale)
              
              // Recreate physics with NEW collision shape
              createFreshCollisionShape(newScale: newScale)
              
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
      
      // MARK: - Create Fresh Collision Shape (No Cached Issues)
      
      private func createFreshCollisionShape(newScale: Float) {
          // ✅ Calculate the EXACT collision radius we want
          let preciseRadius = _radius * newScale
          
          // ✅ Generate a completely new collision shape
          let freshCollisionShape = ShapeResource.generateSphere(radius: preciseRadius)
          
          // ✅ Create new collision component (don't update existing)
          _modelEntity.collision = CollisionComponent(
              shapes: [freshCollisionShape],
              filter: CollisionFilter(
                  group: ARView3D.CollisionGroups.arObjects,
                  mask: [ARView3D.CollisionGroups.arObjects, ARView3D.CollisionGroups.environment]
              )
          )
          
          print("🔄 Created fresh collision shape with radius: \(preciseRadius)m")
      }
      
      // MARK: - Enhanced updatePhysicsCollisionShape Method
      
      private func updatePhysicsCollisionShape() {
          guard _modelEntity.physicsBody != nil else {
              print("⚠️ No physics body to update collision shape for")
              return
          }
          
          // ✅ Get the current visual scale
          let currentScale = _modelEntity.transform.scale.x  // Uniform scaling
          let preciseRadius = _radius * currentScale
          
          print("🔍 Updating collision: visual scale=\(currentScale), calculated radius=\(preciseRadius)")
          
          // ✅ Force recreation of collision shape
          let newShape = ShapeResource.generateSphere(radius: preciseRadius)
          
          // ✅ Completely replace collision component
          _modelEntity.collision = CollisionComponent(
              shapes: [newShape],
              filter: _modelEntity.collision?.filter ?? CollisionFilter(
                  group: ARView3D.CollisionGroups.arObjects,
                  mask: [ARView3D.CollisionGroups.arObjects, ARView3D.CollisionGroups.environment]
              )
          )
          
          // ✅ Update mass properties to match
          if var physicsBody = _modelEntity.physicsBody {
              physicsBody.massProperties = PhysicsMassProperties(mass: Mass)
              _modelEntity.physicsBody = physicsBody
          }
          
          print("✅ Collision shape updated: radius=\(preciseRadius)m, mass=\(Mass)kg")
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
      print("🎾 Starting optimized drag for \(Name) (iOS \(ProcessInfo.processInfo.operatingSystemVersion.majorVersion))")
      
      _isBeingDragged = true
      _lastFingerPosition = nil
      
      // ✅ PHYSICS OPTIMIZATION: Use best available APIs per iOS version
      if var physicsBody = _modelEntity.physicsBody {
          if #available(iOS 18.0, *) {
              // ✅ iOS 18+: Full damping control for optimal responsiveness
              _dragStartDamping = physicsBody.linearDamping
              _dragStartAngularDamping = physicsBody.angularDamping
              
              physicsBody.linearDamping = 0.85    // Higher control for smooth dragging
              physicsBody.angularDamping = 0.9     // Reduce unwanted spinning during drag
              
              _modelEntity.physicsBody = physicsBody
              print("🎾 iOS 18+: Enhanced damping control enabled")
          } else {
              // ✅ iOS 16-17: No damping control available, use alternative approach
              print("🎾 iOS 16-17: Using alternative control method (no damping APIs)")
          }
      }
      
      isBeingDragged = true
      
      // Visual feedback
      if #available(iOS 15.0, *) {
          showDragEffect()
      }
  }

  func updateDrag(fingerWorldPosition: SIMD3<Float>) {
    guard _isBeingDragged else { return }
      
      let currentPos = _modelEntity.transform.translation
      
      if let lastPos = _lastFingerPosition {
          let movement = fingerWorldPosition - lastPos
          let distance = simd_length(movement)
          
          // ✅ MOVEMENT OPTIMIZATION: Choose best approach per iOS version
          if #available(iOS 18.0, *) {
              // ✅ iOS 18+: Hybrid approach with velocity control
              updateDragIOS18(currentPos: currentPos, targetPos: fingerWorldPosition, movement: movement)
          } else {
              // ✅ iOS 16-17: Direct position with force assistance
              updateDragIOS16(currentPos: currentPos, targetPos: fingerWorldPosition, movement: movement)
          }
          
          // ✅ VISUAL ROLLING: Apply rotation based on movement (all iOS versions)
          applyDragRotation(movement: movement)
          
          print("🎾 Applied movement: \(movement) (distance: \(String(format: "%.4f", distance)))")
          
      } else {
          print("🎾 DRAG START: Initial position \(fingerWorldPosition)")
      }
      
      _lastFingerPosition = fingerWorldPosition
  }

  // ✅ iOS 18+ Optimized Update (direct position + enhanced physics)
  @available(iOS 18.0, *)
  private func updateDragIOS18(currentPos: SIMD3<Float>, targetPos: SIMD3<Float>, movement: SIMD3<Float>) {
      // ✅ DIRECT POSITION for immediate visual feedback (same as iOS 16)
      let constrainedPos = SIMD3<Float>(
          targetPos.x,
          currentPos.y, // Maintain current Y
          targetPos.z
      )
      
      _modelEntity.transform.translation = constrainedPos
      
      // ✅ iOS 18+ ENHANCEMENT: Also set velocity for smoother physics integration
      if var physicsMotion = _modelEntity.physicsMotion {
          
          let responsiveness = getResponsivenessForMass() * DragSensitivity
          let velocityScale: Float = 1.5
          
          // Calculate target velocity from movement
          let targetVelocity = movement * responsiveness * velocityScale
          
          // Get current velocity and preserve Y component
          let currentVel = physicsMotion.linearVelocity
          
          // Set horizontal velocity while preserving Y physics
          physicsMotion.linearVelocity = SIMD3<Float>(
              targetVelocity.x,
              currentVel.y, // Keep physics-controlled Y
              targetVelocity.z
          )
          
          _modelEntity.physicsMotion = physicsMotion
          print("🎾 iOS 18+: Position \(constrainedPos) + velocity \(targetVelocity)")
      } else {
          print("🎾 iOS 18+: Position only \(constrainedPos)")
      }
  }

  // ✅ iOS 16-17 Optimized Update (direct position with force assistance)
  private func updateDragIOS16(currentPos: SIMD3<Float>, targetPos: SIMD3<Float>, movement: SIMD3<Float>) {
      // Direct position control for immediate feedback
      let constrainedPos = SIMD3<Float>(
          targetPos.x,
          currentPos.y, // Maintain current Y
          targetPos.z
      )
      
      _modelEntity.transform.translation = constrainedPos
      
      // Add subtle force to help with physics interactions
      if _modelEntity.physicsBody != nil {
          let responsiveness = getResponsivenessForMass() * DragSensitivity
          let forceScale: Float = 5.0 // Gentle assistance
          let assistForce = movement * responsiveness * forceScale
          
          _modelEntity.addForce(SIMD3<Float>(assistForce.x, 0, assistForce.z), relativeTo: nil as Entity?)
      }
      
      print("🎾 iOS 16-17: Direct position \(constrainedPos)")
  }

  open override func endDrag(releaseVelocity: CGPoint, worldDirection: SIMD3<Float>) {  //fingervelocity
    guard _isBeingDragged else { return }
      
      print("🎾 Ending optimized drag for \(Name)")
    _isBeingDragged = false
      
      // ✅ RESTORE PHYSICS: Use appropriate method per iOS version
      if var physicsBody = _modelEntity.physicsBody {
          if #available(iOS 18.0, *) {

              applyReleaseVelocityIOS18(physicsBody: &physicsBody, releaseVelocity: releaseVelocity)
            
            // Restore damping after a delay to allow momentum to take effect
              DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
                  guard let self = self else { return }
                  if var delayedPhysicsBody = self._modelEntity.physicsBody {
                      delayedPhysicsBody.linearDamping = self._dragStartDamping
                      delayedPhysicsBody.angularDamping = self._dragStartAngularDamping
                      self._modelEntity.physicsBody = delayedPhysicsBody
                  }
              }
          } else {
              // ✅ iOS 16-17: No damping to restore, just apply release force
              applyReleaseForceIOS16(releaseVelocity: releaseVelocity)
          }
      }
      
      // Restore visual appearance
      if let original = OriginalMaterial {
          _modelEntity.model?.materials = [original]
          OriginalMaterial = nil
      }
  }

  @available(iOS 18.0, *)
  private func applyReleaseVelocityIOS18(physicsBody: inout PhysicsBodyComponent, releaseVelocity: CGPoint) {
      let releaseSpeed = sqrt(releaseVelocity.x * releaseVelocity.x + releaseVelocity.y * releaseVelocity.y)
      
    if releaseSpeed > NORMAL_ROLLING_SPEED {
          // Calculate object properties
          let objectMass = Mass
        //bounds or scale??
          let bounds = _modelEntity.visualBounds(relativeTo: nil)
          let avgSize = ((bounds.max.x - bounds.min.x) + (bounds.max.y - bounds.min.y) + (bounds.max.z - bounds.min.z)) / 3.0
          let scaledSize = avgSize * Scale
          
          // Mass-based velocity scaling (lighter objects move faster for same finger motion)
          let massScale = 2.0 / max(objectMass, 0.1)  // Inverse relationship
          
          // Size-based air resistance simulation (larger objects have more drag)
          let sizeScale = 1.0 / max(scaledSize, 0.05)
          
          // Combined scaling
          let physicsScale = massScale * sizeScale * 0.005
          
          let fingerX = Float(releaseVelocity.x) * physicsScale
          let fingerY = Float(releaseVelocity.y) * physicsScale
          
          let releaseVel = SIMD3<Float>(fingerX, 0, fingerY)
          
          // Apply momentum considering mass
          if var physicsMotion = _modelEntity.physicsMotion {
              physicsMotion.linearVelocity = SIMD3<Float>(
                  physicsMotion.linearVelocity.x + releaseVel.x,
                  physicsMotion.linearVelocity.y,
                  physicsMotion.linearVelocity.z + releaseVel.z
              )
              _modelEntity.physicsMotion = physicsMotion
              
              print("Physics-accurate release: mass=\(objectMass)kg, size=\(scaledSize)m, velocity=\(releaseVel)")
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

  // ✅ Enhanced visual rolling (all iOS versions)
  private func applyDragRotation(movement: SIMD3<Float>) {
      let horizontalMovement = SIMD3<Float>(movement.x, 0, movement.z)
      let distance = simd_length(horizontalMovement)
      
      guard distance > 0.0001 else { return }
      
      // Realistic rolling physics
      let ballRadius = _radius * Scale
      let rollAngle = distance / ballRadius
      
      // Rotation axis perpendicular to movement
      let direction = simd_normalize(horizontalMovement)
      let rollAxis = SIMD3<Float>(direction.z, 0, -direction.x)
      
      let rollRotation = simd_quatf(angle: rollAngle, axis: rollAxis)
      _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
      
      print("🎾 Rolling: distance=\(String(format: "%.4f", distance)), angle=\(String(format: "%.4f", rollAngle))")
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
      let radius = _radius * Scale
      let shape = ShapeResource.generateSphere(radius: radius)
      
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
      print("Warning, sphere is below ground level \(_modelEntity.transform.translation.y) groundlevel: \(ARView3D.SHARED_GROUND_LEVEL)")
      
    }
      
      _modelEntity.physicsBody = PhysicsBodyComponent(
          massProperties: massProperties,
          material: material,
          mode: isDynamic ? .dynamic : .static
      )
    
      _modelEntity.physicsMotion = PhysicsMotionComponent()
      
      print("🎾 Physics enabled - RealityKit will handle all ground/floor collisions")
      debugPhysicsState()
      print("🎾 Sphere radius: \(radius), mass: \(Mass)")
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
      
      print("Ball radius: \(_radius * Scale)")
      print("Scale: \(Scale)")
      print("==========================")
  }



    
    private func applyVisualRolling(movement: SIMD3<Float>) {
        let horizontalMovement = SIMD3<Float>(movement.x, 0, movement.z)
        let distance = simd_length(horizontalMovement)
        
        guard distance > 0.0001 else { return }
        
        // Perfect rolling physics: distance = radius × angle
        let ballRadius = _radius * Scale
        let rollAngle = distance / ballRadius
        
        // Calculate rotation axis perpendicular to movement
        let direction = simd_normalize(horizontalMovement)
        let rollAxis = SIMD3<Float>(direction.z, 0, -direction.x)
        
        // Apply rolling rotation
        let rollRotation = simd_quatf(angle: rollAngle, axis: rollAxis)
        _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
    }
    

    
    // MARK: - Visual Effects
    
    @available(iOS 15.0, *)
    private func showAngryBirdsDragEffect() {
        var material = SimpleMaterial()
        
        // Color based on mass for visual feedback
        if Mass > 0.4 {
            material.color = .init(tint: .blue.withAlphaComponent(0.7))  // Heavy = blue
        } else if Mass < 0.1 {
            material.color = .init(tint: .orange.withAlphaComponent(0.7)) // Light = orange
        } else {
            material.color = .init(tint: .yellow.withAlphaComponent(0.7))  // Normal = green
        }
        
        // Store original for restoration
        if OriginalMaterial == nil {
            OriginalMaterial = _modelEntity.model?.materials.first
        }
        
        _modelEntity.model?.materials = [material]
    }
    
   
    
    // MARK: - Configuration for Angry Birds Demo
    
    @objc open func configureForAngryBirdsDemo() {
        // Set up physics for clear mass demonstration
        DragSensitivity = 1.0  // Neutral base sensitivity
        StaticFriction = 0.4
        DynamicFriction = 0.3
        Restitution = 0.5
        GravityScale = 1.0
        
        IsRolling = true
        
        print("🎯 Configured for Angry Birds style physics demo")
        print("🎯 Mass effects: Heavy balls = slower response, Light balls = quick response")
        print("🎯 Current mass: \(Mass)kg")
    }
}
