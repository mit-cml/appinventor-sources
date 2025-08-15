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
  public var GROUND_LEVEL = -0.8
  
  private var _currentDragMode: DragMode = .rolling
  private var _dragStartPosition: SIMD3<Float>?
  private var _dragStartTime: Date?
  private var _lastFingerPosition: CGPoint = .zero
  private var _fingerTrajectory: [CGPoint] = []
  private var _rollingPath: [SIMD3<Float>] = []
  private var _pickupStartHeight: Float = 0.0
  
  private let NORMAL_ROLLING_SPEED: CGFloat = 1200      // Normal finger movement
  private let FAST_ROLLING_SPEED: CGFloat = 2500        // Fast but still rolling
  private let FLING_THRESHOLD_SPEED: CGFloat = 3500     // Definite fling intent
  private let PICKUP_FLING_SPEED: CGFloat = 2000        // Lower threshold when already picked up

  
  // MARK: - Smooth Tracking Properties
  private var _currentMomentum: SIMD3<Float> = SIMD3<Float>(0, 0, 0)
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
        dragSensitivity = 1.0  // Harder to drag
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
      let newRadius = _radius * abs(scalar)
      
      // ✅ Keep physics enabled during scaling
      let hadPhysics = _modelEntity.physicsBody != nil
      
      // ✅ Update scale first
      _modelEntity.transform.scale = SIMD3<Float>(abs(scalar), abs(scalar), abs(scalar))
     
      
      // ✅ Calculate new position to keep bottom at same level
      let groundLevel = Float(GROUND_LEVEL)
      let ballRadius = newRadius
      let currentBottomY = currentPos.y - (oldScale * _radius)
      let newCenterY = currentBottomY + ballRadius
      
      // ✅ Apply position immediately
      let newPosition = SIMD3<Float>(currentPos.x, newCenterY, currentPos.z)
      _modelEntity.transform.translation = newPosition
      
      // ✅ Update physics immediately if it was enabled
      if hadPhysics {
          updatePhysicsCollisionShape() // Your existing method
      }
      
      print("🎾 Scale complete - no physics delay")
  }
      // MARK: - ScaleByPinch Method Override
      
  override open func scaleByPinch(scalar: Float) {
      print("🤏 Pinch scaling sphere \(Name) by \(scalar)")
      
      let currentPos = _modelEntity.transform.translation
      let oldScale = Scale
      let oldActualRadius = _radius * oldScale  // Current actual radius
      
      // ✅ Calculate new scale and actual radius
      let newScale = oldScale * scalar
      let newActualRadius = oldActualRadius * scalar  // Scale the actual size
      
      // ✅ Validate bounds using actual radius
      let minRadius: Float = 0.01
      let maxRadius: Float = 1.0
      guard newActualRadius >= minRadius && newActualRadius <= maxRadius else {
          print("🚫 Pinch scale rejected - radius would be \(newActualRadius)")
          return
      }
      
      // ✅ Apply scaling to transform
      _modelEntity.transform.scale = SIMD3<Float>(newScale, newScale, newScale)
      
      // ✅ DON'T change _radius - it stays constant, only scale changes
      // _radius = ... // ❌ Remove this line entirely
      
      // ✅ Adjust position to keep bottom at same level
      let groundLevel = Float(GROUND_LEVEL)
      let oldBottomY = currentPos.y - oldActualRadius
      let newCenterY = oldBottomY + newActualRadius
      
      _modelEntity.transform.translation = SIMD3<Float>(
          currentPos.x,
          newCenterY,
          currentPos.z
      )
      
      // ✅ Update physics collision immediately
      if _modelEntity.physicsBody != nil {
          updatePhysicsCollisionShape()
      }
      
      print("🎾 Scale complete: oldRadius=\(oldActualRadius), newRadius=\(newActualRadius), newScale=\(newScale)")
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
          
          //physicsBody.massProperties = PhysicsMassProperties(mass: newMass)
          // I don't think I should change the mass just based on scale? shouldn't it be a calculation? hmm
          
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
      print("🎾 Ending OLD drag method - using simple restore")
      
      // ✅ FIX 3: Use simple physics restore instead of problematic velocity reset
      EnablePhysics(true)
      _modelEntity.transform.translation = finalPosition
      
      // ✅ Simple force application without mass manipulation
      if simd_length(SIMD2<Float>(Float(releaseVelocity.x), Float(releaseVelocity.y))) > 50 {
          let velocity3D = SIMD3<Float>(
              Float(releaseVelocity.x) * 0.002,
              Float(releaseVelocity.y) * 0.001,
              0
          )
          _modelEntity.addForce(velocity3D, relativeTo: nil as Entity?)
      }
      
      // Restore material
      if let original = OriginalMaterial {
          _modelEntity.model?.materials = [original]
          OriginalMaterial = nil
      }
      
      isBeingDragged = false
      print("🎾 Simple drag end - no hop")
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

  // ✅ REMOVE: Delete the problematic physics reset method entirely
  // This method was causing the hop by manipulating mass
  // private func restorePhysicsWithVelocityReset(...) { ... } // DELETED
  
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

// MARK: - Fixed Advanced Gesture Handling Extension

@available(iOS 14.0, *)
extension SphereNode {
    
    // MARK: - Fixed Advanced Gesture Handler
    
    override open func handleAdvancedGestureUpdate(
        fingerLocation: CGPoint,
        fingerVelocity: CGPoint,
        groundProjection: Any?,
        camera3DProjection: Any?,
        gesturePhase: UIGestureRecognizer.State
    ) {
        
        // Convert Any? back to SIMD3<Float>? (your existing pattern)
        let groundPos = groundProjection as? SIMD3<Float>
        let cameraPos = camera3DProjection as? SIMD3<Float>
        
        switch gesturePhase {
        case .began:
            startSmoothRolling(fingerLocation: fingerLocation)
            
        case .changed:
            updateSmoothRolling(
                fingerLocation: fingerLocation,
                fingerVelocity: fingerVelocity,
                groundProjection: groundPos,
                camera3DProjection: cameraPos
            )
            
        case .ended, .cancelled:
            endSmoothRolling(
                fingerVelocity: fingerVelocity,
                groundProjection: groundPos
            )
            
        default:
            break
        }
    }
    
    // MARK: - Fixed updateSmoothRolling Method
    
    private func updateSmoothRolling(
        fingerLocation: CGPoint,
        fingerVelocity: CGPoint,
        groundProjection: SIMD3<Float>?,
        camera3DProjection: SIMD3<Float>?
    ) {
        
        print("🎾 Smooth update in \(_currentDragMode) mode:")
        print("  fingerLocation: \(fingerLocation)")
        print("  fingerVelocity: \(fingerVelocity)")
        print("  DragSensitivity: \(DragSensitivity)")
        
        // Track finger movement for mode transitions
        let fingerMovement = CGPoint(
            x: fingerLocation.x - _lastFingerPosition.x,
            y: fingerLocation.y - _lastFingerPosition.y
        )
        _lastFingerPosition = fingerLocation
        
        // Check for mode transitions with fixed thresholds
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
    
    // MARK: - Fixed endSmoothRolling Method
    
    private func endSmoothRolling(
        fingerVelocity: CGPoint,
        groundProjection: SIMD3<Float>?
    ) {
        print("🎾 Ending smooth interaction in \(_currentDragMode) mode")
        
        let releaseSpeed = sqrt(fingerVelocity.x * fingerVelocity.x + fingerVelocity.y * fingerVelocity.y)
        
        // Mode-specific ending behavior
        switch _currentDragMode {
        case .rolling:
            endRollingMode(fingerVelocity: fingerVelocity, releaseSpeed: Float(releaseSpeed))
            
        case .pickup:
            endPickupMode(fingerVelocity: fingerVelocity, releaseSpeed: Float(releaseSpeed))
            
        case .flinging:
            endFlingingMode(fingerVelocity: fingerVelocity, releaseSpeed: Float(releaseSpeed))
        }
        
        // Clean up
        _isActivelyRolling = false
        isBeingDragged = false
        _currentDragMode = .rolling  // Reset to default
        
        // Restore material
        if let original = OriginalMaterial {
            _modelEntity.model?.materials = [original]
            OriginalMaterial = nil
        }
        
        print("🎾 Smooth interaction ended with speed: \(releaseSpeed)")
    }

  // ✅ FIX 1: Keep physics ENABLED during rolling (like your working version)
  private func startSmoothRolling(fingerLocation: CGPoint) {
      print("🎾 Starting smooth interaction for \(getBehaviorNames().joined(separator: "+")) sphere")
      
      _lastUpdateTime = Date()
      _lastFingerPosition = fingerLocation
      isBeingDragged = true
      
      let currentHeight = _modelEntity.transform.translation.y
      let groundLevel = Float(GROUND_LEVEL)
      let ballRadius = _radius * Scale
      let isOnGround = currentHeight <= (groundLevel + ballRadius + 0.1)
      
      // Store original material
      if OriginalMaterial == nil {
          OriginalMaterial = _modelEntity.model?.materials.first
      }
      
      if !isOnGround && currentHeight > groundLevel + ballRadius + 0.3 {
          // Ball is clearly in the air - pickup mode (disable physics)
          _currentDragMode = .pickup
          _pickupStartHeight = currentHeight
          
          // Store and disable physics for pickup
          if let physicsBody = _modelEntity.physicsBody {
              _storedPhysicsSettings = PhysicsSettings(
                  mass: Mass,
                  material: physicsBody.material,
                  mode: physicsBody.mode
              )
          }
          _modelEntity.physicsBody = nil
          _modelEntity.collision = nil
        print("🎾 Starting PICKUP mode - physics DISABLED, mass is \(Mass) and \(_modelEntity.physicsBody?.massProperties.mass)")
          
      } else {
          // ✅ CORRECTED: Rolling mode keeps physics ENABLED
          _currentDragMode = .rolling
          _pickupStartHeight = currentHeight
          // DO NOT disable physics for rolling - keep it enabled!
        print("🎾 Starting ROLLING mode - physics ENABLED,  mass is \(Mass) and \(_modelEntity.physicsBody?.massProperties.mass)")
      }
      
      // Visual feedback
      if #available(iOS 15.0, *) {
          showModeEffect()
      }
      
      print("🎾 Smooth interaction started in \(_currentDragMode) mode")
  }

  // Replace updateRollingMode with this realistic physics version

  private func updateRollingMode(
      fingerMovement: CGPoint,
      groundProjection: SIMD3<Float>?
  ) {
      guard let targetPosition = groundProjection else {
          print("❌ No ground projection for rolling mode")
          return
      }
      
      let currentPos = _modelEntity.transform.translation
      let direction = targetPosition - currentPos
      let distance = simd_length(direction)
      
      print("🎾 Rolling: current=\(currentPos), target=\(targetPosition), distance=\(distance)")
      
      if let physicsBody = _modelEntity.physicsBody, distance > 0.01 {
          let normalizedDirection = direction / distance
          
          // ✅ REALISTIC PHYSICS: Same force for all balls (like pushing with same effort)
          let scaleFactor = Scale
          let ballMass = Mass //physicsBody.massProperties.mass
          
          // Apply same base force regardless of mass (realistic physics)
          let baseForce: Float = min(distance * 6.0, 3.0) //why these numbers
          
          // Let behavior multipliers modify the force (but not mass compensation)
          let behaviorMultiplier = calculateRollBehaviorMovementMultiplier()
          let finalForce = baseForce * behaviorMultiplier * DragSensitivity
          
          // Apply horizontal force (minimal Y for rolling)
          let force = SIMD3<Float>(
              normalizedDirection.x * finalForce,
              0,  // Keep on ground
              normalizedDirection.z * finalForce
          )
          
          _modelEntity.addForce(force, relativeTo: nil as Entity?)
          
          print("🎾 Applied rolling force: \(force), scale: \(scaleFactor), mass: \(ballMass)")
          print("🎾 Physics lesson: Heavy ball (mass \(ballMass)) will accelerate slower than light ball!")
      }
      
      // Store momentum for ending
      let movementDirection = simd_normalize(direction)
      _currentMomentum = movementDirection * min(distance, 0.5)
      
      print("🎾 Updated momentum: \(_currentMomentum)")
  }

  // Also update the momentum application to be realistic
  private func applySimpleMomentum(releaseSpeed: Float) {
      print("🎾 Applying momentum with speed: \(releaseSpeed)")
      
      if let physicsBody = _modelEntity.physicsBody {
          // ✅ REALISTIC PHYSICS: Don't compensate for mass in momentum either
          let behaviorScale = getVelocityScaleForBehavior()
          
          // Clean momentum (no Y for rolling)
          var cleanMomentum = _currentMomentum
          cleanMomentum.y = 0
          
          // Apply same impulse regardless of mass (realistic)
          let impulse = cleanMomentum * 2.0 * behaviorScale
          _modelEntity.addForce(impulse, relativeTo: nil as Entity?)
          
          let ballMass = physicsBody.massProperties.mass
          print("🎾 Applied momentum impulse: \(impulse), mass: \(ballMass)")
          print("🎾 Physics lesson: Heavy ball will decelerate slower but also accelerated slower!")
      }
  }
  // ✅ FIX 3: No physics manipulation needed in rolling end
  private func endRollingMode(fingerVelocity: CGPoint, releaseSpeed: Float) {
      print("🎾 Ending rolling mode")
      
      // ✅ Physics already enabled - just apply final momentum
      if releaseSpeed > 500 {
          applySimpleMomentum(releaseSpeed: releaseSpeed)
      } else {
          applySmoothSettle()
      }
  }

    // ✅ NEW: Smoother rolling calculation
    private func calculateSmoothRollingMovement(
        direction: SIMD3<Float>,
        distance: Float,
        deltaTime: Float
    ) -> SIMD3<Float> {
        
        guard distance > 0.001 else { return SIMD3<Float>(0, 0, 0) }
        
        // Adaptive speed based on distance (like Pokemon GO)
        var baseSpeed: Float = 4.0  // Base speed in m/s
        
        if distance < 0.1 {
            baseSpeed = 2.0  // Slow when close
        } else if distance > 0.5 {
            baseSpeed = min(8.0, distance * 8.0)  // Faster for far distances
        }
        
        // Apply behavior multipliers
        let behaviorMultiplier = calculateRollBehaviorMovementMultiplier()
        let finalSpeed = baseSpeed * behaviorMultiplier
        
        // Calculate movement for this frame
        let normalizedDirection = direction / distance
        let maxMovementThisFrame = finalSpeed * deltaTime
        let actualMovementDistance = min(distance * 0.7, maxMovementThisFrame)
        
        let movement = normalizedDirection * actualMovementDistance
        
        print("🎾 Movement calc: baseSpeed=\(baseSpeed), behaviorMult=\(behaviorMultiplier), finalSpeed=\(finalSpeed)")
        print("🎾 Frame movement: \(actualMovementDistance)m")
        
        return movement
    }
    
    private func calculateRollBehaviorMovementMultiplier() -> Float {
        var multiplier: Float = 1.0
        
        // Apply your existing behavior physics
        if _behaviorFlags.contains(.heavy) {
          multiplier *= 1.0  // Heavy balls move slower
        }
        if _behaviorFlags.contains(.light) {
          multiplier *= 2.5  // Light balls move faster
        }
        if _behaviorFlags.contains(.sticky) {
            multiplier *= 0.5  // Sticky balls resist movement
        }
        if _behaviorFlags.contains(.slippery) {
            multiplier *= 1.2  // Slippery balls move easier
        }
        if _behaviorFlags.contains(.wet) {
            multiplier *= 0.8  // Wet balls have resistance
        }
        if _behaviorFlags.contains(.floating) {
            multiplier *= 1.4  // Floating balls move easily
        }
        
        return multiplier
    }
    
    // MARK: - Smooth Rolling Rotation
    
    private func applySmoothRollingRotation(movement: SIMD3<Float>) {
        let horizontalMovement = SIMD3<Float>(movement.x, 0, movement.z)
        let movementDistance = simd_length(horizontalMovement)
        
        guard movementDistance > 0.0001 else { return }
        
        // Perfect rolling physics: distance = radius × angle
        let ballRadius = _radius * Scale
        let rollAngle = movementDistance / ballRadius
        
        // Apply behavior modifiers to rolling
        let effectiveRollAngle = rollAngle * getRollingBehaviorEffect()
        
        let maxAnglePerFrame: Float = 0.2  // ~11.5 degrees max per frame
        let clampedAngle = min(effectiveRollAngle, maxAnglePerFrame)
        
        // Calculate rotation axis perpendicular to movement
        let movementDirection = simd_normalize(horizontalMovement)
        let rollAxis = SIMD3<Float>(movementDirection.z, 0, -movementDirection.x)
        
        // Apply the rolling rotation
        let rollRotation = simd_quatf(angle: clampedAngle, axis: rollAxis)
        _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
        
        let degrees = effectiveRollAngle * 180.0 / Float.pi
        print("🎾 Rolling rotation: distance=\(movementDistance)m, angle=\(degrees)°")
    }
    
    private func getRollingBehaviorEffect() -> Float {
        var effect: Float = 1.0
        
        if _behaviorFlags.contains(.slippery) {
            effect *= 1.3  // Slippery balls roll more
        }
        if _behaviorFlags.contains(.wet) {
            effect *= 0.7  // Wet balls roll less
        }
        if _behaviorFlags.contains(.sticky) {
            effect *= 0.3  // Sticky balls barely roll
        }
        
        return effect
    }
      
    private func updateMomentumFromMovement(
        fingerVelocity: CGPoint,
        actualMovement: SIMD3<Float>
    ) {
        
        // Convert finger velocity to world momentum
        let fingerMomentum = SIMD3<Float>(
            Float(fingerVelocity.x) * 0.001,
            0,
            Float(fingerVelocity.y) * 0.001
        )
        
        let cleanMovement: SIMD3<Float>
        if _currentDragMode == .rolling {  // do not apply momentum on y axis
            cleanMovement = SIMD3<Float>(actualMovement.x, 0, actualMovement.z)
        } else {
            cleanMovement = actualMovement
        }
        
        // Blend both sources
        _currentMomentum = lerp(fingerMomentum, cleanMovement, 0.7)
        
        print("🎾 Updated momentum: \(_currentMomentum)")
    }
    
    private func continueMomentumRolling() {
        guard simd_length(_currentMomentum) > 0.01 else { return }
        
        Task {
            var remainingMomentum = _currentMomentum
            
            while simd_length(remainingMomentum) > 0.005 && !isBeingDragged {
                await MainActor.run {
                    // Apply rolling rotation during momentum
                    let momentumDistance = simd_length(remainingMomentum) * 0.016  // ~60fps
                    if momentumDistance > 0.0001 {
                        let ballRadius = self._radius * self.Scale
                        let rollAngle = momentumDistance / ballRadius
                        
                        let rollDirection = simd_normalize(remainingMomentum)
                        let rollAxis = SIMD3<Float>(rollDirection.z, 0, -rollDirection.x)
                        
                        let rollRotation = simd_quatf(angle: rollAngle, axis: rollAxis)
                        self._modelEntity.transform.rotation = rollRotation * self._modelEntity.transform.rotation
                    }
                    
                    // Apply behavior-specific momentum decay
                    let decayRate = self.getMomentumDecayRate()
                    remainingMomentum *= decayRate
                }
                
                try? await Task.sleep(nanoseconds: 16_000_000)  // ~60fps
            }
            
            await MainActor.run {
                self.applySmoothSettle()
            }
        }
    }
    
    private func getMomentumDecayRate() -> Float {
        if _behaviorFlags.contains(.slippery) {
            return 0.99  // Slippery balls maintain momentum longer
        } else if _behaviorFlags.contains(.sticky) {
            return 0.90  // Sticky balls lose momentum quickly
        } else if _behaviorFlags.contains(.wet) {
            return 0.92  // Wet balls have more resistance
        } else {
            return 0.96  // Normal decay
        }
    }
    
    private func applySmoothSettle() {
        print("🎾 Smooth settling")
        
        _currentMomentum = SIMD3<Float>(0, 0, 0)
        
        // Ensure proper ground positioning
        let currentPos = _modelEntity.transform.translation
        let settledPos = constrainToGroundPlane(currentPos)
        
        if simd_distance(currentPos, settledPos) > 0.001 {
            _modelEntity.transform.translation = settledPos
        }
    }
    
    // ✅ FIXED: Remove problematic physics reset method entirely
    private func endPickupMode(fingerVelocity: CGPoint, releaseSpeed: Float) {
        print("🎾 Ending pickup mode with speed: \(releaseSpeed)")
        
        // ✅ SIMPLE PHYSICS RESTORE without velocity reset
        restorePhysicsSimple()
        
        // Apply release velocity if fast enough
        if releaseSpeed > 300 {
            let velocity3D = SIMD3<Float>(
                Float(fingerVelocity.x) * 0.003,
                Float(-fingerVelocity.y) * 0.002,
                0
            )
            
            // ✅ Simple force application - no mass manipulation
            _modelEntity.addForce(velocity3D, relativeTo: nil as Entity?)
        }
    }
    
    private func endFlingingMode(fingerVelocity: CGPoint, releaseSpeed: Float) {
        print("🎾 Ending flinging mode with speed: \(releaseSpeed)")
        
        // ✅ SIMPLE PHYSICS RESTORE
        restorePhysicsSimple()
        
        // Calculate fling velocity
        let flingMultiplier: Float = 0.004
        let velocity3D = SIMD3<Float>(
            Float(fingerVelocity.x) * flingMultiplier,
            Float(-fingerVelocity.y) * flingMultiplier * 0.5,
            Float(fingerVelocity.y) * flingMultiplier * 0.3
        )
        
        let behaviorVelocity = velocity3D * getVelocityScaleForBehavior()
        
        // ✅ Simple force application
        _modelEntity.addForce(behaviorVelocity, relativeTo: nil as Entity?)
        
        print("🎾 Fling velocity: \(behaviorVelocity)")
    }
    
    // ✅ NEW: Simple physics restore without mass manipulation
    private func restorePhysicsSimple() {
        guard _storedPhysicsSettings != nil else { return }
        
        // Simply re-enable physics with stored settings
        EnablePhysics(true)
        
        // Clear stored settings
        _storedPhysicsSettings = nil
        
        print("🎾 Physics restored simply - no mass manipulation")
    }
    
    // ✅ FIXED: More conservative mode transitions - pickup only on clear upward swipe
    private func checkForModeTransition(fingerMovement: CGPoint, fingerVelocity: CGPoint) {
        let verticalMovement = fingerMovement.y
        let speed = sqrt(fingerVelocity.x * fingerVelocity.x + fingerVelocity.y * fingerVelocity.y)
        
        switch _currentDragMode {
        case .rolling:
            // ✅ VERY CONSERVATIVE: Only transition to pickup on strong upward swipe
            if verticalMovement < -60 && speed > 800 {  // Strong upward movement
                transitionToPickupMode()
            }
            // ✅ High threshold for flinging
            else if speed > 4000 {
                transitionToFlingingMode()
            }
            // Otherwise stay in rolling mode with physics enabled
            
        case .pickup:
            // Only transition to flinging on very fast movement
            if speed > 3000 {
                transitionToFlingingMode()
            }
            // Transition back to rolling when moving toward ground
            else if verticalMovement > 40 && _modelEntity.transform.translation.y < _pickupStartHeight + 0.2 {
                transitionToRollingMode()
            }
            
        case .flinging:
            // Stay in flinging mode until gesture ends
            break
        }
    }
    
    private func restoreOriginalScale() {
        let baseScale = Scale  // Your property that tracks intended scale
        _modelEntity.transform.scale = SIMD3<Float>(baseScale, baseScale, baseScale)
    }
    
    // ✅ FIXED: Smoother transitions
    private func transitionToPickupMode() {
        guard _currentDragMode != .pickup else { return }
        restoreOriginalScale()
        
        print("🎾 Transitioning to PICKUP mode")
        _currentDragMode = .pickup
        
        // Store physics settings and disable
        if let physicsBody = _modelEntity.physicsBody {
            _storedPhysicsSettings = PhysicsSettings(
                mass: Mass,
                material: physicsBody.material,
                mode: physicsBody.mode
            )
        }
        _modelEntity.physicsBody = nil
        _modelEntity.collision = nil
        
        if #available(iOS 15.0, *) {
            showModeEffect()
        }
    }
    
    private func transitionToRollingMode() {
        guard _currentDragMode != .rolling else { return }
        restoreOriginalScale()
        
        print("🎾 Transitioning to ROLLING mode")
        _currentDragMode = .rolling
        
        // ✅ Simple physics restore
        restorePhysicsSimple()
        
        if #available(iOS 15.0, *) {
            showModeEffect()
        }
    }
    
    private func transitionToFlingingMode() {
        guard _currentDragMode != .flinging else { return }
        restoreOriginalScale()
        
        print("🎾 Transitioning to FLINGING mode")
        _currentDragMode = .flinging
        
        // Store physics and disable for flinging
        if _modelEntity.physicsBody != nil {
            if let physicsBody = _modelEntity.physicsBody {
                _storedPhysicsSettings = PhysicsSettings(
                    mass: Mass,
                    material: physicsBody.material,
                    mode: physicsBody.mode
                )
            }
            _modelEntity.physicsBody = nil
            _modelEntity.collision = nil
        }
        
        if #available(iOS 15.0, *) {
            showModeEffect()
        }
    }
    
    private func updatePickupMode(
        fingerMovement: CGPoint,
        camera3DProjection: SIMD3<Float>?
    ) {
        guard let targetPos = camera3DProjection else {
            print("❌ No camera projection for pickup mode")
            return
        }
        
        let currentPos = _modelEntity.transform.translation
        let movement = targetPos - currentPos
        
        // Apply behavior-specific pickup sensitivity
        let sensitivity = getPickupSensitivity()
        let constrainedTarget = constrainPickupPosition(targetPos)
        
        // Smooth interpolation (Pokemon GO style)
        let lerpAmount = 0.8 * sensitivity
        let newPos = lerp(currentPos, constrainedTarget, lerpAmount)
        _modelEntity.transform.translation = newPos
        
        // Apply pickup-specific behaviors
        applyPickupBehaviors(movement: movement)
        
        print("🎾 Pickup: target=\(targetPos.y), current=\(currentPos.y), new=\(newPos.y)")
    }
    
    private func updateFlingingMode(
        fingerMovement: CGPoint,
        camera3DProjection: SIMD3<Float>?
    ) {
        guard let targetPos = camera3DProjection else {
            print("❌ No camera projection for flinging mode")
            return
        }
        
        let currentPos = _modelEntity.transform.translation
        let movement = targetPos - currentPos
        
        // More aggressive following for flinging (Pokemon GO style)
        let flingSensitivity = getFlingSensitivity()
        let flingTarget = currentPos + (movement * flingSensitivity)
        
        _modelEntity.transform.translation = flingTarget
        
        // Apply flinging visual effects
        applyFlingingBehaviors(movement: movement)
        
        print("🎾 Flinging: movement magnitude=\(simd_length(movement))")
    }
    
    // ✅ IMPROVED: More stable ground constraint
    private func constrainToGroundPlane(_ position: SIMD3<Float>) -> SIMD3<Float> {
        let groundLevel = Float(GROUND_LEVEL)
        let ballRadius = _radius * Scale
        
        // Use a consistent offset
        let minY = groundLevel + ballRadius + 0.01  // Small consistent buffer
        
        return SIMD3<Float>(
            position.x,
            max(position.y, minY),
            position.z
        )
    }
    
    // MARK: - Helper Functions for Different Modes
    
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
        if _behaviorFlags.contains(.bouncy) && _currentDragMode == .flinging {
            let expansion = 1.0 + (simd_length(movement) * 0.01)  // Reduced effect
            let targetScale = Scale * expansion  // Use proper base scale
            _modelEntity.transform.scale = SIMD3<Float>(targetScale, targetScale, targetScale)
        }
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
    
    @available(iOS 15.0, *)
    private func showModeEffect() {
        var material = SimpleMaterial()
        
        // Color based on current mode and behavior
        switch _currentDragMode {
        case .rolling:
            if _behaviorFlags.contains(.heavy) {
                material.color = .init(tint: .blue.withAlphaComponent(0.6))
            } else if _behaviorFlags.contains(.light) {
                material.color = .init(tint: .orange.withAlphaComponent(0.6))
            } else {
                material.color = .init(tint: .yellow.withAlphaComponent(0.6))  // Rolling = yellow
            }
            
        case .pickup:
            material.color = .init(tint: .green.withAlphaComponent(0.6))  // Pickup = green
            
        case .flinging:
            material.color = .init(tint: .red.withAlphaComponent(0.7))    // Flinging = red
        }
        
        _modelEntity.model?.materials = [material]
    }
    
    private func lerp(_ a: SIMD3<Float>, _ b: SIMD3<Float>, _ t: Float) -> SIMD3<Float> {
        return a + (b - a) * t
    }
    
    // MARK: - Configuration and Debug Methods
    
    @objc open func configureSmoothRolling() {
        // Optimize settings for smooth rolling experience
        DragSensitivity = 1.0
        StaticFriction = 0.6
        DynamicFriction = 0.4
        Restitution = 0.5
        
        // Ensure rolling behavior is enabled
        IsRolling = true
        
        print("🎾 Configured for smooth rolling")
    }
    
    @objc open func debugSmoothRolling() {
        print("=== SMOOTH ROLLING DEBUG ===")
        print("Is actively rolling: \(_isActivelyRolling)")
        print("Current momentum: \(_currentMomentum)")
        print("Current position: \(_modelEntity.transform.translation)")
        print("Behaviors: \(getBehaviorNames().joined(separator: ", "))")
        print("DragSensitivity: \(DragSensitivity)")
        print("==========================")
    }
    
    // ✅ DEBUGGING: Add method to check current state
    @objc open func debugCurrentState() {
        let pos = _modelEntity.transform.translation
        let hasPhysics = _modelEntity.physicsBody != nil
        
        print("=== SPHERE STATE DEBUG ===")
        print("Current mode: \(_currentDragMode)")
        print("Position: \(pos)")
        print("Has physics: \(hasPhysics)")
        print("Ground level: \(GROUND_LEVEL)")
        print("Ball radius: \(_radius * Scale)")
        print("Is being dragged: \(isBeingDragged)")
        print("Current momentum: \(_currentMomentum)")
        print("========================")
    }
}
