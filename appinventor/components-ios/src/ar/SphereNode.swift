// Enhanced SphereNode.swift - Complete class with Surface Behaviors
// Your existing SphereNode with added wet/sticky/slippery behaviors

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
  public var GROUND_LEVEL = -0.8
  
  private var _currentDragMode: DragMode = .rolling
  private var _dragStartPosition: SIMD3<Float>?
  private var _dragStartTime: Date?
  private var _lastFingerPosition: CGPoint = .zero
  private var _fingerTrajectory: [CGPoint] = []
  private var _rollingPath: [SIMD3<Float>] = []
  private var _pickupStartHeight: Float = 0.0
  
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
    } else {
      // Fallback on earlier versions
    }  // Green for default
    restoreColorAfterCollision()
    
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
          mass = 0.8  // Heavy default
          dragSensitivity = 0.5  // Harder to drag
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

  // MARK: - Enhanced Dragging Methods
      
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
      
      // Show behavior-specific drag effect
      if #available(iOS 15.0, *) {
          showDragEffect()
      }
      
      print("🎾 Drag started - physics disabled")
  }
  
  // Add these methods to SphereNode to fix scaling issues

  override open func updateDrag(dragVector: CGPoint, velocity: CGPoint, worldDirection: SIMD3<Float>) {
      print("🎾 updateDrag called:")
      print("  dragVector: \(dragVector)")
      print("  velocity: \(velocity)")
      print("  worldDirection: \(worldDirection)")
      print("  DragSensitivity: \(DragSensitivity)")
      
      let userSensitivity = DragSensitivity
      let behaviorSensitivity = getBehaviorDragSensitivity()
      let finalSensitivity = userSensitivity * behaviorSensitivity
      
      print("  behaviorSensitivity: \(behaviorSensitivity)")
      print("  finalSensitivity: \(finalSensitivity)")
      
      let movement = worldDirection * finalSensitivity
      print("  calculated movement: \(movement)")
      
      let currentPos = _modelEntity.transform.translation
      print("  currentPos: \(currentPos)")
      
      let newPos = currentPos + movement
      print("  newPos (before constraint): \(newPos)")
      
      // ✅ Use scale-aware position validation
      let validPos = validatePositionAfterDrag(newPos)
      print("  validPos (after constraint): \(validPos)")
      
      // Check if position changed significantly
      let positionChange = simd_distance(currentPos, validPos)
      print("  position change distance: \(positionChange)")
      
      if positionChange < 0.001 {
          print("⚠️ WARNING: Position barely changed - might be stuck!")
      }
      
      _modelEntity.transform.translation = validPos
      
      applyDragBehaviors(movement: movement)
      
      if newPos.y != validPos.y {
          print("🚫 Sphere \(Name) Y constrained: \(newPos.y) → \(validPos.y)")
          print("   Ground level: \(GROUND_LEVEL), radius: \(_radius * Scale)")
      }
      
      print("🎾 updateDrag complete - new position: \(_modelEntity.transform.translation)")
  }
      // MARK: - Scale methods
      
      override open func ScaleBy(_ scalar: Float) {
          print("🔄 Scaling sphere \(Name) by \(scalar)")
          
          // Store the current state before scaling
          let currentPos = _modelEntity.transform.translation
          let oldScale = Scale
          let oldRadius = _radius * oldScale
          let groundLevel = Float(GROUND_LEVEL)
          
          
          // Temporarily disable physics during scaling to prevent phantom collisions
          let hadPhysics = _modelEntity.physicsBody != nil
          
          if hadPhysics {
              print("⚠️ Temporarily disabling physics for scaling")
              _modelEntity.physicsBody = nil
              _modelEntity.collision = nil
          }
          
          // Calculate new dimensions
          let newScale = oldScale * abs(scalar)
          let newRadius = _radius * abs(scalar)
          
          
          
          let newPosition = getValidScaledPosition(currentPos: currentPos, newRadius: newRadius)
          _modelEntity.transform.translation = newPosition
         
          
          print("🎾 ScaleBy: radius \(oldRadius) → \(newRadius), center Y \(currentPos.y) → \(newPosition.y)")
         
          // Re-enable physics with updated collision shape
          if hadPhysics {
              print("✅ Re-enabling physics after scaling")
              
              // Small delay to ensure visual scaling is complete
              DispatchQueue.main.asyncAfter(deadline: .now() + 0.01) {
                  self.EnablePhysics(true)
                  print("🎾 Physics restored after scaling to \(newScale)")
              }
          }
      }
      
      // MARK: - ScaleByPinch Method Override
      
      override open func scaleByPinch(scalar: Float) {
          print("🤏 Pinch scaling sphere \(Name) by \(scalar)")
          
          // Store the current state
          let currentPos = _modelEntity.transform.translation
          let oldScale = Scale
          let oldRadius = _radius * oldScale
          let groundLevel = Float(GROUND_LEVEL)
          

          // Calculate new dimensions
          let newScale = oldScale * scalar  // Don't use abs() for pinch - allow shrinking
          let newRadius = _radius * scalar
          
        
        
          // Prevent scaling too small or too large
          let minRadius: Float = 0.01  // 1cm minimum
          let maxRadius: Float = 1.0   // 1m maximum
          
          guard newRadius >= minRadius && newRadius <= maxRadius else {
              print("🚫 Pinch scale rejected - radius \(newRadius) out of bounds [\(minRadius), \(maxRadius)]")
              return
          }
          
          // Apply visual scaling immediately (for real-time feedback)
          _modelEntity.transform.scale = SIMD3<Float>(newScale, newScale, newScale)
          
          // Update internal radius to match new scale
          _radius = newRadius
          

          let newPosition = getValidScaledPosition(currentPos: currentPos, newRadius: newRadius)
          _modelEntity.transform.translation = newPosition
          
          // Update physics if enabled (real-time scaling with physics active)
          if _modelEntity.physicsBody != nil {
              // For pinch scaling, we keep physics active but update collision shape
              updatePhysicsCollisionShape()
          }
          
         
          print("🎾 Pinch: radius \(oldRadius) → \(newRadius), center Y \(currentPos.y) → \(newPosition.y)")
      }
      
      // MARK: - Helper Methods for Scaling
      
      private func updatePhysicsCollisionShape() {
          guard var physicsBody = _modelEntity.physicsBody else { return }
          
          // Create new collision shape with updated radius
          let sphereShape = ShapeResource.generateSphere(radius: _radius)
          
          // Update collision component with new shape
          _modelEntity.collision = CollisionComponent(
              shapes: [sphereShape],
              filter: _modelEntity.collision?.filter ?? CollisionFilter.default
          )
          
          // Update physics body with new mass based on volume
         let newMass = Mass * pow(Scale, 2.2)  //hybrd density for gameplay
          
          physicsBody.massProperties = PhysicsMassProperties(mass: newMass)
          
          
          print("🎾 Updated physics: radius=\(String(format: "%.3f", _radius)), mass=\(String(format: "%.3f", newMass))")
      }
      
      // MARK: - Scale Validation and Constraints (Updated with proper integration)
      
      private func getValidScaledPosition(currentPos: SIMD3<Float>, newRadius: Float) -> SIMD3<Float> {
          let groundLevel = Float(GROUND_LEVEL)
          let currentBottomY = currentPos.y - (_radius * Scale)
          
          // Calculate new center position to keep bottom at same level
          let newCenterY = max(
              currentBottomY + newRadius,           // Maintain bottom position
              groundLevel + newRadius + 0.01       // Ensure above ground
          )
          
          return SIMD3<Float>(currentPos.x, newCenterY, currentPos.z)
      }
      
      // MARK: - Updated constrainToValidPosition to use current scale
      
      public func constrainToValidPosition(_ position: SIMD3<Float>) -> SIMD3<Float> {
          let groundLevel = Float(GROUND_LEVEL)
          let currentRadius = _radius * Scale  // Use current scaled radius
          
          // Ball center must be at least one radius above ground
          let minY = groundLevel + currentRadius + 0.01  // Small buffer
          
          return SIMD3<Float>(
              position.x,
              max(position.y, minY),
              position.z
          )
      }
      
      // MARK: - Position validation for different scenarios
      
      private func validatePositionAfterDrag(_ position: SIMD3<Float>) -> SIMD3<Float> {
          // Called during and after dragging to ensure sphere stays above ground
          return constrainToValidPosition(position)
      }
      
      private func validatePositionAfterCollision(_ position: SIMD3<Float>) -> SIMD3<Float> {
          // Called after collision to prevent sphere from sinking into ground
          let groundLevel = Float(GROUND_LEVEL)
          let currentRadius = _radius * Scale
          
          // More aggressive constraint after collision (larger buffer)
          let minY = groundLevel + currentRadius + 0.02  // 2cm buffer after collision
          
          return SIMD3<Float>(
              position.x,
              max(position.y, minY),
              position.z
          )
      }
      
      private func validatePositionAfterPhysicsUpdate(_ position: SIMD3<Float>) -> SIMD3<Float> {
          // Called when physics might have moved the sphere below ground
          return constrainToValidPosition(position)
      }
      
      private func isValidScaleSize(_ newRadius: Float) -> Bool {
          let minRadius: Float = 0.005  // 5mm minimum
          let maxRadius: Float = 2.0    // 2m maximum
          return newRadius >= minRadius && newRadius <= maxRadius
      }
      
      // MARK: - Scale Behavior Integration
      
      private func applyScaleBehaviorEffects(_ oldRadius: Float, _ newRadius: Float) {
          // Apply behavior-specific effects when scaling
          
          if _behaviorFlags.contains(.bouncy) && newRadius > oldRadius {
              // Bouncy balls "pop" when they grow
              addBounceEffect()
          }
          
          if _behaviorFlags.contains(.heavy) {
              // Heavy balls have more mass when larger
              let massMultiplier = pow(newRadius / 0.05, 3) * 2.0  // Cubic scaling for volume
              updateMassForBehavior(massMultiplier)
          }
          
          if _behaviorFlags.contains(.light) {
              // Light balls maintain low mass regardless of size
              let lightMass = 0.1 * (newRadius / 0.05)  // Linear scaling only
              updateMassForBehavior(lightMass)
          }
      }
      
      private func addBounceEffect() {
          // Small upward impulse for bouncy behavior
        _modelEntity.addForce(SIMD3<Float>(0, 0.1, 0), relativeTo: nil as Entity?)
      }
      
      private func updateMassForBehavior(_ mass: Float) {
          _modelEntity.physicsBody?.massProperties = PhysicsMassProperties(mass: mass)
      }
      
      // MARK: - Physics Monitoring and Correction
      
     
      @objc open func debugScaleInfo() {
          let currentPos = _modelEntity.transform.translation
          let scale = Scale
          let radius = _radius
          let actualRadius = radius * scale
          let bottomY = currentPos.y - actualRadius
          let groundLevel = Float(GROUND_LEVEL)
          
          print("=== SPHERE SCALE DEBUG ===")
          print("Name: \(Name)")
          print("Internal radius: \(String(format: "%.4f", radius))m")
          print("Scale factor: \(String(format: "%.4f", scale))")
          print("Actual radius: \(String(format: "%.4f", actualRadius))m")
          print("Center position: (\(String(format: "%.4f", currentPos.x)), \(String(format: "%.4f", currentPos.y)), \(String(format: "%.4f", currentPos.z)))")
          print("Bottom Y: \(String(format: "%.4f", bottomY))m")
          print("Ground level: \(String(format: "%.4f", groundLevel))m")
          print("Distance above ground: \(String(format: "%.4f", bottomY - groundLevel))m")
          print("Has physics: \(_modelEntity.physicsBody != nil)")
          if let mass = _modelEntity.physicsBody?.massProperties.mass {
              print("Mass: \(String(format: "%.4f", mass))kg")
          }
          print("========================")
      }
  

  // Update sphere mesh to match new radius after scaling
  private func updateSphereMeshAfterScale() {
      // Generate new sphere mesh with current radius
      let mesh = MeshResource.generateSphere(radius: _radius)
      
      // Preserve existing materials when updating mesh
      let existingMaterials = _modelEntity.model?.materials ?? []
      _modelEntity.model = ModelComponent(
          mesh: mesh,
          materials: existingMaterials.isEmpty ? [SimpleMaterial()] : existingMaterials
      )
  }
  
  /* built in drag sensitivity for certain behaviors */
  private func getBehaviorDragSensitivity() -> Float {
      var sensitivity: Float = 1.0
      
      if _behaviorFlags.contains(.heavy) {
          sensitivity *= 0.7  // Heavy balls feel harder to move
      }
      
      if _behaviorFlags.contains(.light) {
          sensitivity *= 1.3  // Light balls move more easily
      }
      
      if _behaviorFlags.contains(.sticky) {
          sensitivity *= 0.5  // Sticky balls resist movement
      }
      
      if _behaviorFlags.contains(.slippery) {
          sensitivity *= 1.2  // Slippery balls move easily
      }
      
      if _behaviorFlags.contains(.floating) {
          sensitivity *= 1.4  // Floating objects move easily
      }
      
      return sensitivity
  }
  
  
  override open func endDrag(releaseVelocity: CGPoint, worldDirection: SIMD3<Float>) {
      let finalPosition = _modelEntity.transform.translation
      print("🎾 Ending drag of \(getBehaviorNames().joined(separator: "+")) sphere")
      
    // Calculate 3D velocity from 2D release velocity
    let flingSensitivity: Float = 2.0  // Adjust based on feel
    let velocityScale = getVelocityScaleForBehavior()
    
    let velocity3D = SIMD3<Float>(
        Float(releaseVelocity.x) * flingSensitivity * velocityScale,
        Float(releaseVelocity.y) * flingSensitivity * velocityScale * 0.7, // Reduce Y for more natural arc
        0  // Or map to Z if you want depth
    )
      // ✅ Restore physics with velocity reset
    restorePhysicsWithVelocityReset(at: finalPosition, velocity: velocity3D)
      
      // ✅ Apply behavior-specific end effects
      applyEndDragBehaviors()
      
      isBeingDragged = false
  }
  
  private func getVelocityScaleForBehavior() -> Float {
      var scale: Float = 1.0
      
      if _behaviorFlags.contains(.heavy) {
          scale *= 0.6  // Heavy balls need more force to fling
      }
      
      if _behaviorFlags.contains(.light) {
          scale *= 1.8  // Light balls fling easily
      }
      
      if _behaviorFlags.contains(.sticky) {
          scale *= 0.3  // Sticky balls resist being flung
      }
      
      if _behaviorFlags.contains(.slippery) {
          scale *= 1.4  // Slippery balls fling well
      }
      
      if _behaviorFlags.contains(.floating) {
          scale *= 1.6  // Floating objects move easily
      }
      
      return scale
  }

  private func restorePhysicsWithVelocityReset(at position: SIMD3<Float>, velocity: SIMD3<Float>) {
      // ✅ Enable physics
      EnablePhysics(true)
      _modelEntity.transform.translation = position
      
      // ✅ Reset velocity immediately (iOS 16 workaround)
      let currentMass = _modelEntity.physicsBody?.massProperties.mass ?? 1.0
      _modelEntity.physicsBody?.massProperties = PhysicsMassProperties(mass: 50.0)  // Heavy
      
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.05) {
          self._modelEntity.physicsBody?.massProperties = PhysicsMassProperties(mass: currentMass)
          self._modelEntity.physicsMotion?.linearVelocity = velocity
      }
      
      // ✅ Restore material
      if let original = OriginalMaterial {
          _modelEntity.model?.materials = [original]
          OriginalMaterial = nil
      }
      
      print("🎾 Physics restored with velocity reset")
  }
  
  // MARK: - Behavior-Aware Rolling Methods
  
  private func shouldRoll(currentHeight: Float, movement: SIMD3<Float>) -> Bool {
      // Sticky balls don't roll well
      if _behaviorFlags.contains(.sticky) {
          return false
      }
      
    let groundLevel: Float = Float(GROUND_LEVEL)
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
      
      // ✅ Modify rolling based on surface behavior
      let effectiveRollAngle: Float
      if _behaviorFlags.contains(.slippery) {
          effectiveRollAngle = rollAngle * 1.5  // Slippery balls roll more
      } else if _behaviorFlags.contains(.wet) {
          effectiveRollAngle = rollAngle * 0.6  // Wet balls roll less
      } else {
          effectiveRollAngle = rollAngle
      }
      
      // ✅ Roll axis perpendicular to movement
      let direction = simd_normalize(horizontalMovement)
      let rollAxis = SIMD3<Float>(direction.z, 0, -direction.x)
      
      // ✅ Apply rotation
      let rollRotation = simd_quatf(angle: effectiveRollAngle, axis: rollAxis)
      _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
      
      // ✅ Track for momentum
      _accumulatedRoll += effectiveRollAngle
      _rollDirection = direction
      
      print("🎾 Rolling: distance=\(distance), angle=\(effectiveRollAngle)")
  }
  
  private func isNearGround(height: Float) -> Bool {
    let groundLevel: Float = Float(GROUND_LEVEL)
      let ballRadius = _radius * _modelEntity.transform.scale.x
      return height <= (groundLevel + ballRadius + 0.1)
  }
  
  private func startGroundMomentum() {
      // ✅ Simple momentum - gradually slow down rolling
      guard _accumulatedRoll > 0.1 else { return }
      
      // Different momentum based on surface behavior
      let momentumMultiplier: Float
      let dampingRate: Float
      
      if _behaviorFlags.contains(.slippery) {
          momentumMultiplier = 0.8  // High momentum for slippery
          dampingRate = 0.98  // Very slow deceleration
      } else if _behaviorFlags.contains(.wet) {
          momentumMultiplier = 0.1  // Low momentum for wet
          dampingRate = 0.8   // Fast deceleration
      } else if _behaviorFlags.contains(.sticky) {
          return  // No momentum for sticky balls
      } else {
          momentumMultiplier = 0.3  // Normal momentum
          dampingRate = 0.9   // Normal deceleration
      }
      
      Task {
          var remainingRoll = _accumulatedRoll * momentumMultiplier
          
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
  
  // MARK: - Behavior-Specific Drag Effects
  
  private func applyDragBehaviors(movement: SIMD3<Float>) {
      // Rolling behavior during drag
      if _behaviorFlags.contains(.rolling) && shouldRoll(currentHeight: _modelEntity.transform.translation.y, movement: movement) {
          applySimpleRolling(movement: movement)
      }
      
      // Floating behavior during drag
      if _behaviorFlags.contains(.floating) {
          applyFloatingMotion()
      }
      
      // Slippery behavior during drag
      if _behaviorFlags.contains(.slippery) {
          applySlipperyMotion(movement: movement)
      }
      
      // Sticky behavior during drag - resistance
      if _behaviorFlags.contains(.sticky) {
          applyStickyResistance()
      }
  }
  
  private func applyEndDragBehaviors() {
      // Floating behavior after drag
      if _behaviorFlags.contains(.floating) {
          startFloatingBehavior()
      }
      
      // Rolling momentum after drag
      if _behaviorFlags.contains(.rolling) {
          startGroundMomentum()
      }
      
      // Sticky behavior after drag
      if _behaviorFlags.contains(.sticky) {
          startStickySettle()
      }
      
      // Slippery behavior after drag
      if _behaviorFlags.contains(.slippery) {
          startSlipperySlide()
      }
  }
  
  private func applyFloatingMotion() {
      // Add gentle floating oscillation
      let time = Float(Date().timeIntervalSince1970)
      let floatOffset = sin(time * 1.5) * 0.002  // 2mm gentle floating
      _modelEntity.transform.translation.y += floatOffset
  }
  
  private func applySlipperyMotion(movement: SIMD3<Float>) {
      // Slippery objects have momentum - they keep moving slightly
      let momentum = movement * 0.2
      let currentPos = _modelEntity.transform.translation
      _modelEntity.transform.translation = currentPos + momentum
  }
  
  private func applyStickyResistance() {
      // Sticky objects create subtle "stretching" effect when dragged
      let time = Float(Date().timeIntervalSince1970)
      let resistance = sin(time * 8.0) * 0.001  // Small resistance oscillation
      _modelEntity.transform.translation.y += resistance
  }
  
  private func startFloatingBehavior() {
      guard _behaviorFlags.contains(.floating) else { return }
      
      print("🎾 Starting continuous floating behavior")
      
      Task {
          while _behaviorFlags.contains(.floating) && !isBeingDragged {
              await MainActor.run {
                  guard !isBeingDragged else { return }
                  
                  let time = Float(Date().timeIntervalSince1970)
                  let floatOffset = sin(time * 1.2) * 0.004  // 4mm gentle floating
                  let currentPos = _modelEntity.transform.translation
                  let newY = currentPos.y + floatOffset
                  
                  // Keep floating above ground level
                let minFloatHeight: Float = Float(GROUND_LEVEL + 0.2)  // 20cm above ground
                  _modelEntity.transform.translation.y = max(newY, minFloatHeight)
              }
              
              try? await Task.sleep(nanoseconds: 50_000_000)  // ~20fps for gentle motion
          }
      }
  }
  
  private func startStickySettle() {
      guard _behaviorFlags.contains(.sticky) else { return }
      
      print("🎾 Sticky sphere settling in place")
      
      // Sticky objects don't move after being placed
      let settlePosition = _modelEntity.transform.translation
      
      Task {
          // Make small settling movements to simulate sticking
          for i in 0..<8 {
              await MainActor.run {
                  let microMovement = SIMD3<Float>(
                      Float.random(in: -0.0003...0.0003),
                      max(Float.random(in: -0.0003...0.0), 0),  // No upward movement
                      Float.random(in: -0.0003...0.0003)
                  )
                  _modelEntity.transform.translation = settlePosition + microMovement
              }
              
              try? await Task.sleep(nanoseconds: 60_000_000)
          }
          
          await MainActor.run {
              _modelEntity.transform.translation = settlePosition
              print("🎾 Sticky sphere stuck in place")
          }
      }
  }
  
  private func startSlipperySlide() {
      guard _behaviorFlags.contains(.slippery) && isNearGround(height: _modelEntity.transform.translation.y) else { return }
      
      print("🎾 Slippery sphere sliding with momentum")
      
      // Slippery spheres keep sliding after being released
      let slideDirection = _rollDirection
      var slideSpeed: Float = _accumulatedRoll * 0.6  // Good momentum
      
      Task {
          while slideSpeed > 0.005 && !isBeingDragged {
              await MainActor.run {
                  guard !isBeingDragged else { return }
                  
                  let slideMovement = slideDirection * slideSpeed * 0.03
                  let currentPos = _modelEntity.transform.translation
                  _modelEntity.transform.translation = currentPos + slideMovement
                  
                  // Keep rolling while sliding
                  let rollAngle = slideSpeed * 0.1
                  let rollRotation = simd_quatf(angle: rollAngle, axis: SIMD3<Float>(slideDirection.z, 0, -slideDirection.x))
                  _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
                  
                  slideSpeed *= 0.995  // Very slow deceleration for slippery
              }
              
              try? await Task.sleep(nanoseconds: 33_000_000)
          }
          
          print("🎾 Slippery slide stopped")
      }
  }

  @available(iOS 15.0, *)
  private func showDragEffect() {
      var dragMaterial = SimpleMaterial()
      
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
          dragMaterial.color = .init(tint: .green.withAlphaComponent(0.7))  // Green for default
      }
      
      _modelEntity.model?.materials = [dragMaterial]
    
      restoreMaterial()
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
}

// Enhanced SphereNode dragging implementation
// Add these methods to your existing SphereNode class

import Foundation
import RealityKit
import UIKit

@available(iOS 14.0, *)
extension SphereNode {
    
    // MARK: - Advanced Gesture Handling
    
    enum DragMode {
        case rolling      // Rolling ball along the floor
        case pickup       // Ball lifted off the floor
        case flinging     // Ball thrown with velocity
    }
    

    
  @objc open override func handleAdvancedGestureUpdate(
        fingerLocation: CGPoint,
        fingerVelocity: CGPoint,
        groundProjection: Any?,
        camera3DProjection: Any?,
        gesturePhase: UIGestureRecognizer.State
    ) {
        
        switch gesturePhase {
        case .began:
            startAdvancedDrag(fingerLocation: fingerLocation)
            
        case .changed:
            //updateDrag(dragVector: fingerLocation, velocity: fingerVelocity, worldDirection: .init(x: 0, y: 1, z: 0))
            updateAdvancedDrag(
                fingerLocation: fingerLocation,
                fingerVelocity: fingerVelocity,
                groundProjection: groundProjection as! SIMD3<Float>?,
                camera3DProjection: camera3DProjection as! SIMD3<Float>?
            )
            
        case .ended, .cancelled:
            endAdvancedDrag(
                fingerLocation: fingerLocation,
                fingerVelocity: fingerVelocity,
                groundProjection: groundProjection as! SIMD3<Float>?
            )
            
        default:
            break
        }
    }
    
    // MARK: - Drag Start Logic
    
    private func startAdvancedDrag(fingerLocation: CGPoint) {
        print("🎾 Starting advanced drag for \(getBehaviorNames().joined(separator: "+")) sphere")
        
        _dragStartPosition = _modelEntity.transform.translation
        _dragStartTime = Date()
        _lastFingerPosition = fingerLocation
        _fingerTrajectory = [fingerLocation]
        _rollingPath = []
        
        let currentHeight = _modelEntity.transform.translation.y
        let groundLevel = Float(GROUND_LEVEL)
        let ballRadius = _radius * Scale
        let isOnGround = currentHeight <= (groundLevel + ballRadius + 0.02)
        
        // Determine initial drag mode based on ball position and behavior
        if isOnGround && !_behaviorFlags.contains(.floating) {
            _currentDragMode = .rolling
            _pickupStartHeight = currentHeight
            print("🎾 Starting ROLLING mode - ball on ground")
        } else {
            _currentDragMode = .pickup
            _pickupStartHeight = currentHeight
            print("🎾 Starting PICKUP mode - ball in air")
        }
        
        // Don't disable physics immediately for rolling mode
        if _currentDragMode == .pickup {
            startDrag() // Use existing drag start logic for pickup
        } else {
            // For rolling, just mark as being dragged but keep physics
            isBeingDragged = true
            OriginalMaterial = _modelEntity.model?.materials.first
            if #available(iOS 15.0, *) {
                showRollingEffect()
            }
        }
    }
    
    // MARK: - Drag Update Logic
    
    private func updateAdvancedDrag(
        fingerLocation: CGPoint,
        fingerVelocity: CGPoint,
        groundProjection: SIMD3<Float>?,
        camera3DProjection: SIMD3<Float>?
    ) {
        
      print("🎾 updateDrag called:")
      print("  dragVector: \(fingerLocation)")
      print("  velocity: \(fingerVelocity)")
      print("  groundProjection: \(groundProjection)")
      print("  camera3DProjection: \(camera3DProjection)")
      print("  DragSensitivity: \(DragSensitivity)")
        // Track finger movement for gesture analysis
        _fingerTrajectory.append(fingerLocation)
        if _fingerTrajectory.count > 10 {
            _fingerTrajectory.removeFirst()
        }
        
        let fingerMovement = CGPoint(
            x: fingerLocation.x - _lastFingerPosition.x,
            y: fingerLocation.y - _lastFingerPosition.y
        )
        _lastFingerPosition = fingerLocation
        
        // Check for mode transitions
        checkForModeTransition(fingerMovement: fingerMovement, fingerVelocity: fingerVelocity)
        
        // Apply movement based on current mode
        switch _currentDragMode {
        case .rolling:
            updateRollingMode(
                fingerMovement: fingerMovement,
                groundProjection: groundProjection
            )
            
        case .pickup:
            updatePickupMode(
                fingerMovement: fingerMovement,
                camera3DProjection: camera3DProjection
            )
            
        case .flinging:
            updateFlingingMode(
                fingerMovement: fingerMovement,
                camera3DProjection: camera3DProjection
            )
        }
    }
    
    // MARK: - Mode Transition Logic
    
    private func checkForModeTransition(fingerMovement: CGPoint, fingerVelocity: CGPoint) {
        let verticalMovement = fingerMovement.y
        let speed = sqrt(fingerVelocity.x * fingerVelocity.x + fingerVelocity.y * fingerVelocity.y)
        
        switch _currentDragMode {
        case .rolling:
            // Transition to pickup if finger moves up significantly
            if verticalMovement > 20 { // Moving up on screen (negative Y)
                transitionToPickupMode()
            }
            // Transition to flinging if moving very fast
            else if speed > 800 {
                transitionToFlingingMode()
            }
            
        case .pickup:
            // Transition to flinging if moving very fast
            if speed > 600 {
                transitionToFlingingMode()
            }
            // Transition back to rolling if moving down toward ground
            else if verticalMovement < 20 && _modelEntity.transform.translation.y < _pickupStartHeight + 0.1 {
                transitionToRollingMode()
            }
            
        case .flinging:
            // Stay in flinging mode until gesture ends
            break
        }
    }
    
    private func transitionToPickupMode() {
        guard _currentDragMode != .pickup else { return }
        
        print("🎾 Transitioning to PICKUP mode")
        _currentDragMode = .pickup
        
        // Disable physics for pickup
        startDrag()
    }
    
    private func transitionToRollingMode() {
        guard _currentDragMode != .rolling else { return }
        
        print("🎾 Transitioning to ROLLING mode")
        _currentDragMode = .rolling
        
        // Re-enable physics for rolling
        EnablePhysics(true)
        
        if #available(iOS 15.0, *) {
            showRollingEffect()
        }
    }
    
    private func transitionToFlingingMode() {
        guard _currentDragMode != .flinging else { return }
        
        print("🎾 Transitioning to FLINGING mode")
        _currentDragMode = .flinging
        
        // Disable physics for flinging
        if _modelEntity.physicsBody != nil {
            startDrag()
        }
        
        if #available(iOS 15.0, *) {
            showFlingingEffect()
        }
    }
    
    // MARK: - Mode-Specific Updates
    
    private func updateRollingMode(
        fingerMovement: CGPoint,
        groundProjection: SIMD3<Float>?
    ) {
        guard let groundTarget = groundProjection else { return }
        
        let currentPos = _modelEntity.transform.translation
        let targetDirection = groundTarget - currentPos
        
        // Apply rolling movement with physics
        let rollForce = SIMD3<Float>(
            targetDirection.x * 2.0,
            0, // Don't apply upward force in rolling mode
            targetDirection.z * 2.0
        )
        
        // Apply behavior-specific rolling modifiers
        let behaviorMultiplier = getRollingBehaviorMultiplier()
        let finalForce = rollForce * behaviorMultiplier
        
        // Apply force through physics
        _modelEntity.addForce(
                   SIMD3<Float>(1.0, 0.0, 0.0),  // Force vector
                   relativeTo: nil as Entity?     // Reference entity (nil = world space)
        )
        
        // Apply visual rolling
        let movement = finalForce * 0.01 // Scale down for visual rolling
        if simd_length(movement) > 0.001 {
            applySimpleRolling(movement: movement)
        }
        
      
      // ✅ Use scale-aware position validation
        let validPos = validatePositionAfterDrag(currentPos)
        print("  validPos (after constraint): \(validPos)")
      
        // Track rolling path
        _rollingPath.append(currentPos)
        if _rollingPath.count > 20 {
            _rollingPath.removeFirst()
        }
      

        
        print("🎾 Rolling: force=\(simd_length(finalForce))")
    }
    
    private func updatePickupMode(
        fingerMovement: CGPoint,
        camera3DProjection: SIMD3<Float>?
    ) {
        guard let targetPos = camera3DProjection else { return }
        
        // Move ball to follow finger with pickup behavior
        let currentPos = _modelEntity.transform.translation
        let movement = targetPos - currentPos
        
        // Apply behavior-specific pickup sensitivity
        let sensitivity = getPickupSensitivity()
        let constrainedTarget = constrainPickupPosition(targetPos)
        
        _modelEntity.transform.translation = lerp(
            currentPos,
            constrainedTarget,
            0.8 * sensitivity
        )
        
        // Apply pickup-specific behaviors
        applyPickupBehaviors(movement: movement)
        
        print("🎾 Pickup: target=\(targetPos.y), current=\(currentPos.y)")
    }
    
    private func updateFlingingMode(
        fingerMovement: CGPoint,
        camera3DProjection: SIMD3<Float>?
    ) {
        // In flinging mode, follow finger but with more dramatic movement
        guard let targetPos = camera3DProjection else { return }
        
        let currentPos = _modelEntity.transform.translation
        let movement = targetPos - currentPos
        
        // More aggressive following for flinging
        let flingSensitivity = getFlingSensitivity()
        let flingTarget = currentPos + (movement * flingSensitivity)
        
        let validPos = constrainPickupPosition(targetPos)
        print("  validPos (after constraint): \(validPos)")
      
        _modelEntity.transform.translation = flingTarget
        
        // Apply flinging visual effects
        applyFlingingBehaviors(movement: movement)
        
        print("🎾 Flinging: movement magnitude=\(simd_length(movement))")
    }
    
    // MARK: - Drag End Logic
    
    private func endAdvancedDrag(
        fingerLocation: CGPoint,
        fingerVelocity: CGPoint,
        groundProjection: SIMD3<Float>?
    ) {
        print("🎾 Ending advanced drag in \(_currentDragMode) mode")
        
        let releaseSpeed = sqrt(fingerVelocity.x * fingerVelocity.x + fingerVelocity.y * fingerVelocity.y)
        
        switch _currentDragMode {
        case .rolling:
          endRollingMode(fingerVelocity: fingerVelocity, releaseSpeed: Float(releaseSpeed))
            
        case .pickup:
          endPickupMode(fingerVelocity: fingerVelocity, releaseSpeed: Float(releaseSpeed))
            
        case .flinging:
          endFlingingMode(fingerVelocity: fingerVelocity, releaseSpeed: Float(releaseSpeed))
        }
        
        // Clean up
        isBeingDragged = false
        _fingerTrajectory.removeAll()
        _rollingPath.removeAll()
        _currentDragMode = .rolling // Reset to default
        
        // Restore material
        if let original = OriginalMaterial {
            _modelEntity.model?.materials = [original]
            OriginalMaterial = nil
        }
    }
    
    private func endRollingMode(fingerVelocity: CGPoint, releaseSpeed: Float) {
        print("🎾 Ending rolling mode")
        
        // Continue rolling with momentum
        if releaseSpeed > 50 {
            let rollingVelocity = SIMD3<Float>(
                Float(fingerVelocity.x) * 0.002,
                0,
                Float(fingerVelocity.y) * 0.002
            )
            
            // Apply velocity through physics
            if let physicsBody = _modelEntity.physicsBody {
              _modelEntity.physicsMotion?.linearVelocity = rollingVelocity * getRollingBehaviorMultiplier()
            }
            
            // Start rolling momentum
            startGroundMomentum()
        }
    }
    
    private func endPickupMode(fingerVelocity: CGPoint, releaseSpeed: Float) {
        print("🎾 Ending pickup mode with speed: \(releaseSpeed)")
        
        // Calculate 3D release velocity
        let velocity3D = SIMD3<Float>(
            Float(fingerVelocity.x) * 0.003,
            Float(-fingerVelocity.y) * 0.002, // Upward flick
            0
        )
        
        // Use existing endDrag logic
        endDrag(releaseVelocity: fingerVelocity, worldDirection: velocity3D)
    }
    
    private func endFlingingMode(fingerVelocity: CGPoint, releaseSpeed: Float) {
        print("🎾 Ending flinging mode with speed: \(releaseSpeed)")
        
        // Calculate powerful 3D velocity for flinging
        let flingMultiplier: Float = 0.005
        let velocity3D = SIMD3<Float>(
            Float(fingerVelocity.x) * flingMultiplier,
            Float(-fingerVelocity.y) * flingMultiplier * 0.5, // Reduce Y for realistic arc
            Float(fingerVelocity.y) * flingMultiplier * 0.3  // Add some Z based on Y movement
        )
        
        // Apply behavior-specific fling velocity
        let behaviorVelocity = velocity3D * getVelocityScaleForBehavior()
        
        // Restore physics and apply velocity
        restorePhysicsWithVelocityReset(at: _modelEntity.transform.translation, velocity: behaviorVelocity)
        
        print("🎾 Fling velocity: \(behaviorVelocity)")
    }
    
    // MARK: - Behavior-Specific Helpers
    
    private func getRollingBehaviorMultiplier() -> Float {
        var multiplier: Float = 1.0
        
        if _behaviorFlags.contains(.slippery) {
            multiplier *= 1.5 // Slippery balls roll easier
        }
        if _behaviorFlags.contains(.wet) {
            multiplier *= 0.6 // Wet balls have more resistance
        }
        if _behaviorFlags.contains(.sticky) {
            multiplier *= 0.2 // Sticky balls resist rolling
        }
        if _behaviorFlags.contains(.heavy) {
            multiplier *= 0.8 // Heavy balls need more force
        }
        if _behaviorFlags.contains(.light) {
            multiplier *= 1.3 // Light balls roll easily
        }
        
        return multiplier
    }
    
    private func getPickupSensitivity() -> Float {
        var sensitivity: Float = 1.0
        
        if _behaviorFlags.contains(.sticky) {
            sensitivity *= 0.5 // Sticky balls resist pickup
        }
        if _behaviorFlags.contains(.floating) {
            sensitivity *= 1.4 // Floating balls move easily
        }
        if _behaviorFlags.contains(.heavy) {
            sensitivity *= 0.7 // Heavy balls harder to move
        }
        if _behaviorFlags.contains(.light) {
            sensitivity *= 1.3 // Light balls easy to move
        }
        
        return sensitivity
    }
    
    private func getFlingSensitivity() -> Float {
        var sensitivity: Float = 1.2 // Base fling sensitivity
        
        if _behaviorFlags.contains(.bouncy) {
            sensitivity *= 1.4 // Bouncy balls respond more to flinging
        }
        if _behaviorFlags.contains(.heavy) {
            sensitivity *= 0.6 // Heavy balls less responsive
        }
        if _behaviorFlags.contains(.light) {
            sensitivity *= 1.6 // Light balls very responsive
        }
        
        return sensitivity
    }
    
    private func constrainPickupPosition(_ position: SIMD3<Float>) -> SIMD3<Float> {
        let groundLevel = Float(GROUND_LEVEL)
        let ballRadius = _radius * Scale
        let minY = groundLevel + ballRadius + 0.02 // Keep ball above ground
        let maxY = groundLevel + 2.0 // Maximum pickup height
        
        return SIMD3<Float>(
            position.x,
            max(minY, min(position.y, maxY)),
            position.z
        )
    }
    
    private func applyPickupBehaviors(movement: SIMD3<Float>) {
        // Floating spheres have gentle oscillation during pickup
        if _behaviorFlags.contains(.floating) {
            let time = Float(Date().timeIntervalSince1970)
            let floatOffset = sin(time * 2.0) * 0.003
            _modelEntity.transform.translation.y += floatOffset
        }
        
        // Sticky spheres resist movement during pickup
        if _behaviorFlags.contains(.sticky) {
            let resistance = movement * -0.1
            _modelEntity.transform.translation += resistance
        }
    }
    
    private func applyFlingingBehaviors(movement: SIMD3<Float>) {
        // Add visual effects during flinging
        if _behaviorFlags.contains(.bouncy) {
            // Bouncy balls slightly expand during fling preparation
            let expansion = 1.0 + (simd_length(movement) * 0.02)
            _modelEntity.transform.scale = SIMD3<Float>(expansion, expansion, expansion)
        }
        
        // Light balls have more dramatic movement
        if _behaviorFlags.contains(.light) {
            let extraMovement = movement * 0.2
            _modelEntity.transform.translation += extraMovement
        }
    }
    
    // MARK: - Visual Effects
    
    @available(iOS 15.0, *)
    private func showRollingEffect() {
        var material = SimpleMaterial()
        material.color = .init(tint: .yellow.withAlphaComponent(0.6))
        _modelEntity.model?.materials = [material]
    }
    
    @available(iOS 15.0, *)
    private func showFlingingEffect() {
        var material = SimpleMaterial()
        material.color = .init(tint: .red.withAlphaComponent(0.7))
        _modelEntity.model?.materials = [material]
    }
    
    // MARK: - Utility Functions
    
    private func lerp(_ a: SIMD3<Float>, _ b: SIMD3<Float>, _ t: Float) -> SIMD3<Float> {
        return a + (b - a) * t
    }
}
