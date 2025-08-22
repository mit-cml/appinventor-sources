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
        mass = 1.0  // Heavy default
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
         print("Visual scale: \(visualScale)")
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
         let visualRadius = (bounds.max.x - bounds.min.x) / 2.0
         print("Visual bounds radius: \(visualRadius)m")
         
         if abs(visualRadius - calculatedRadius) > 0.01 {
             print("⚠️ WARNING: Visual and calculated radius mismatch!")
             print("  Visual: \(visualRadius)m")
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
  
      // MARK: - Scale Validation and Constraints (Updated with proper integration)
      
      private func getValidScaledPosition(currentPos: SIMD3<Float>, newRadius: Float) -> SIMD3<Float> {
          let groundLevel = ARView3D.SHARED_GROUND_LEVEL
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
          let groundLevel = ARView3D.SHARED_GROUND_LEVEL
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
          let groundLevel = ARView3D.SHARED_GROUND_LEVEL
          let currentRadius = _radius * Scale
          
          // More aggressive constraint after collision (larger buffer)
          let minY = groundLevel + currentRadius //+ 0.02  // 2cm buffer after collision
          
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
        let groundLevel = ARView3D.SHARED_GROUND_LEVEL
          
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
          dragMaterial.color = .init(tint: .green.withAlphaComponent(0.7))  // Green for default
      }
      
      _modelEntity.model?.materials = [dragMaterial]
    
      //restoreMaterial()
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

  override open func handleAdvancedGestureUpdate(
          fingerLocation: CGPoint,
          fingerMovement: CGPoint,
          fingerVelocity: CGPoint,
          groundProjection: Any?,
          camera3DProjection: Any?,
          gesturePhase: UIGestureRecognizer.State
      ) {
          
          let groundPos = groundProjection as? SIMD3<Float>
          let cameraPos = camera3DProjection as? SIMD3<Float>
          
          switch gesturePhase {
          case .began:
              print("🎯 Starting simplified Angry Birds drag for \(Name)")
              startDrag()
              
          case .changed:
              // ✅ Use our simplified rolling logic from the extension
              updateAngryBirdsDrag(
                  fingerLocation: fingerLocation,
                  fingerMovement: fingerMovement,
                  fingerVelocity: fingerVelocity,
                  groundProjection: groundPos,
                  camera3DProjection: cameraPos
              )
              
          case .ended, .cancelled:
              print("🎯 Ending simplified drag for \(Name)")
              endDrag(releaseVelocity: fingerVelocity)
              
          default:
              break
          }
      }
      
      // ✅ SUPPORTING METHODS: Make sure these exist too
      
    open override func startDrag() {
          print("🎯 Starting Angry Birds drag for \(Name)")
          
          // Store physics settings if needed
          if let physicsBody = _modelEntity.physicsBody {
              _storedPhysicsSettings = PhysicsSettings(
                  mass: Mass,
                  material: physicsBody.material,
                  mode: physicsBody.mode
              )
          }
      _currentMomentum = SIMD3<Float>(0, 0, 0)
        print("=== DRAG DEBUG ===")
        print("initial position: \(_modelEntity.transform.translation)")
          
          // Disable physics for direct control during drag
          _modelEntity.physicsBody = nil
          _modelEntity.collision = nil
          
          isBeingDragged = true
          _lastUpdateTime = Date()
          
          // Visual feedback
          if #available(iOS 15.0, *) {
              showDragEffect()
          }
      }
      
      func endDrag(releaseVelocity: CGPoint) {
        print("🎯 Ending drag for \(Name)")
          
        let finalDragPosition = _modelEntity.transform.translation
        // ✅ Verify this is a reasonable world position
        if abs(finalDragPosition.x) > 10 || abs(finalDragPosition.z) > 10 {
            print("⚠️ Ball escaped to unrealistic position: \(finalDragPosition)")
            // Reset to last known good position
        }
        
        
        print("final position: \(finalDragPosition)")
        print("=== END DRAG DEBUG ===")
        EnablePhysics(true)
        _modelEntity.transform.translation = finalDragPosition
        
        // ✅ SIMPLE MOMENTUM: Just convert finger velocity to force
         let releaseSpeed = sqrt(releaseVelocity.x * releaseVelocity.x + releaseVelocity.y * releaseVelocity.y)
         
    
        if releaseSpeed > 100 {
             // ✅ Apply behavior multiplier
             let behaviorMultiplier = getBehaviorMomentumMultiplier()
             let momentumScale: Float = 0.002 * behaviorMultiplier
             
             let momentumX = Float(releaseVelocity.x) * momentumScale
             let momentumZ = Float(releaseVelocity.y) * momentumScale
             
             _currentMomentum = SIMD3<Float>(momentumX, 0, momentumZ)
             startSustainedRolling()
             
             print("🎾 released ball with rolling: momentum=\(_currentMomentum), behavior=\(behaviorMultiplier)")
         }
          // Cleanup
        isBeingDragged = false
        _storedPhysicsSettings = nil
          
          // Restore visual
        if let original = OriginalMaterial {
          
          print("🎾 restoring original material: \(original)")
            _modelEntity.model?.materials = [original]
            OriginalMaterial = nil
        }
      }
      

  private func startSustainedRolling() {
      guard !_isRollingWithMomentum else { return }  // Prevent multiple rolling tasks
      
      _isRollingWithMomentum = true
      
      Task {
          while simd_length(_currentMomentum) > 0.01 && !isBeingDragged {
              await MainActor.run {
                  // Apply current momentum as force
          
                // ✅ Apply initial impulse once, then let physics and friction handle deceleration
                if simd_length(_currentMomentum) > 0.01 {
                    let impulseScale: Float = 1.0  // Adjust this to control initial speed
                    let initialImpulse = _currentMomentum * impulseScale
                    _modelEntity.addForce(initialImpulse, relativeTo: nil as Entity?)
                    
                    print("🎾 Applied single impulse: \(initialImpulse)")
                }
                  // ✅ GRADUALLY REDUCE momentum (deceleration)
                  let dampingFactor: Float = 0.96  // 96% retention = 4% loss per frame
                _currentMomentum *= dampingFactor
                  
                  // ✅ VISUAL ROLLING: Rotate ball based on movement
                  applyRollingRotation()
              }
              
              // ✅ Run at ~30fps for smooth rolling
              try? await Task.sleep(nanoseconds: 33_000_000)
          }
          
          await MainActor.run {
              _isRollingWithMomentum = false
            _currentMomentum = SIMD3<Float>(0, 0, 0)
              print("🎾 Rolling momentum stopped")
          }
      }
  }

  // ✅ VISUAL ROLLING: Make ball rotate as it rolls

  private func applyRollingRotation() {
      let horizontalMomentum = SIMD3<Float>(_currentMomentum.x, 0, _currentMomentum.z)
      let speed = simd_length(horizontalMomentum)
      
      guard speed > 0.001 else { return }
      
      // Calculate rolling rotation
      let ballRadius = _radius * Scale
      let rotationAmount = speed * 0.001 / ballRadius  // Adjust multiplier for visual effect
      
      // Rotation axis perpendicular to movement direction
      let direction = simd_normalize(horizontalMomentum)
      let rollAxis = SIMD3<Float>(direction.z, 0, -direction.x)
      
      let rollRotation = simd_quatf(angle: rotationAmount, axis: rollAxis)
      _modelEntity.transform.rotation = rollRotation * _modelEntity.transform.rotation
  }
      
      // MARK: - Main Drag Update Method (Updated)
      
      func updateAngryBirdsDrag(
          fingerLocation: CGPoint,
          fingerMovement: CGPoint,
          fingerVelocity: CGPoint,
          groundProjection: SIMD3<Float>?,
          camera3DProjection: SIMD3<Float>?
      ) {
          let currentTime = Date()
          let deltaTime = Float(currentTime.timeIntervalSince(_lastUpdateTime))
          _lastUpdateTime = currentTime
          
          let fingerSpeed = sqrt(fingerVelocity.x * fingerVelocity.x + fingerVelocity.y * fingerVelocity.y)
          
          // ✅ SIMPLE: Default to rolling, only throw on clear intent
          let shouldThrow = checkThrowingIntent(fingerVelocity: fingerVelocity, fingerMovement: fingerMovement, fingerSpeed: fingerSpeed)
          
          if shouldThrow {
              print("🎯 THROWING MODE: Clear throwing intent detected")
              updateAngryBirdsThrowing(camera3DProjection: camera3DProjection, deltaTime: deltaTime)
          } else {
              print("🎯 ROLLING MODE: Default rolling behavior")
              updateAngryBirdsRollingTrace(groundProjection: groundProjection, deltaTime: deltaTime)
          }
          
          // Store momentum for release
          let movement = calculateMovementFromFingerVelocity(fingerVelocity)
          _currentMomentum = movement * getMassInertiaFactor()
      }
      
  private func getBehaviorMomentumMultiplier() -> Float {
      var multiplier: Float = 1.0
      
      if _behaviorFlags.contains(.heavy) {
          multiplier *= 0.7  // Heavy balls harder to get rolling, but roll further
      }
      
      if _behaviorFlags.contains(.light) {
          multiplier *= 1.3  // Light balls roll easily
      }
      
      if _behaviorFlags.contains(.sticky) {
          multiplier *= 0.2  // Sticky balls barely roll
      }
      
      if _behaviorFlags.contains(.slippery) {
          multiplier *= 1.5  // Slippery balls roll well
      }
      
      if _behaviorFlags.contains(.wet) {
          multiplier *= 0.6  // Wet balls have resistance
      }
      
      return multiplier
  }
      
  // ✅ MUCH more conservative pickup detection
  private func shouldTransitionToPickup(fingerVelocity: CGPoint, fingerMovement: CGPoint) -> Bool {
      let upwardThreshold: CGFloat = -400  // ✅ Much higher threshold (was -300)
      let ratioThreshold: Float = 3.0      // ✅ Much stricter ratio (was 1.5)
      let minimumDistance: CGFloat = 40    // ✅ Must move at least 40 pixels up
      
      // ✅ Must be fast upward movement
      guard fingerVelocity.y < upwardThreshold else { return false }
      
      // ✅ Must be primarily vertical (3x more vertical than horizontal)
     guard abs(fingerVelocity.y) > abs(fingerVelocity.x) * CGFloat(ratioThreshold) else { return false }
      
      // ✅ Must have actually moved upward a reasonable distance
      guard fingerMovement.y < -minimumDistance else { return false }
      
      print("🎯 Pickup intent: velocity=\(fingerVelocity.y), ratio=\(abs(fingerVelocity.y)/abs(fingerVelocity.x)), distance=\(fingerMovement.y)")
      return true
  }
  
      private func checkThrowingIntent(fingerVelocity: CGPoint, fingerMovement: CGPoint, fingerSpeed: CGFloat) -> Bool {
          // ✅ INTENT 1: Very fast finger movement (clear flick/throw gesture)
          if fingerSpeed > FLING_THRESHOLD_SPEED {
              print("🎯 Throw intent: Fast finger speed \(fingerSpeed) > \(FLING_THRESHOLD_SPEED)")
              return true
          }
          
          // ✅ INTENT 2: Strong upward finger movement (trying to lift ball)
    
          if shouldTransitionToPickup(fingerVelocity: fingerVelocity, fingerMovement: fingerMovement){
            return true
          }
          // ✅ DEFAULT: Always roll unless clear throwing intent
          return false
      }
      
      // MARK: - Rolling Mode (Simplified)
      
  private func updateAngryBirdsRollingTrace(groundProjection: SIMD3<Float>?, deltaTime: Float) {
      guard let targetPosition = groundProjection else {
          print("❌ ROLLING - No ground projection, staying in place")
          return
      }
      
      let currentPos = _modelEntity.transform.translation
      let movement = targetPosition - currentPos
      let distance = simd_length(movement)
      
      guard distance > 0.0001 else {
          print("🎯 ROLLING - Distance too small: \(distance)")
          return
      }
      print("updating position: \(currentPos) to \(targetPosition)")
     
      let yDifference = abs(targetPosition.y - currentPos.y)
      if yDifference > 0.5 {
          print("⚠️ ROLLING - Large Y difference: ball=\(currentPos.y), target=\(targetPosition.y), diff=\(yDifference)")
      }
      print("🎯 ROLLING: horizontal movement=\(movement)")
      
    
      _modelEntity.transform.translation = targetPosition
      applyVisualRolling(movement: movement)
  }
      
      // MARK: - Throwing Mode (Full 3D)
      
      private func updateAngryBirdsThrowing(camera3DProjection: SIMD3<Float>?, deltaTime: Float) {
          guard let targetPosition = camera3DProjection else {
              print("❌ THROWING - No camera projection")
              return
          }
          
          let currentPos = _modelEntity.transform.translation
          let movement = targetPosition - currentPos
          
          // More aggressive following for intentional throwing
          let throwResponsiveness = getThrowResponsivenessForMass() * DragSensitivity * 1.5
          let lerpSpeed = throwResponsiveness * deltaTime * 10.0
          
          let actualMovement = movement * min(lerpSpeed, 0.9)
          _modelEntity.transform.translation = currentPos + actualMovement
          
          print("🎯 THROWING: full 3D movement=\(actualMovement)")
      }
      
      // MARK: - Helper Methods
      
      private func calculateMovementFromFingerVelocity(_ fingerVelocity: CGPoint) -> SIMD3<Float> {
          return SIMD3<Float>(
              Float(fingerVelocity.x) * 0.001,
              Float(-fingerVelocity.y) * 0.001,
              0
          )
      }
      
      private func getResponsivenessForMass() -> Float {
          // Heavy objects respond slower to finger movement (more realistic)
          let baseMass: Float = 0.2  // Reference mass for calculations
          let massRatio = Mass / baseMass
          
          // Inverse relationship: heavier = less responsive
          let responsiveness = 1.0 / sqrt(massRatio)
          
          // Apply behavior modifiers
          var finalResponsiveness = responsiveness
          
          if _behaviorFlags.contains(.heavy) {
              finalResponsiveness *= 0.6  // Even slower for heavy behavior
          }
          if _behaviorFlags.contains(.light) {
              finalResponsiveness *= 1.4  // Faster for light behavior
          }
          if _behaviorFlags.contains(.sticky) {
              finalResponsiveness *= 0.4  // Very slow for sticky
          }
          if _behaviorFlags.contains(.floating) {
              finalResponsiveness *= 1.6  // Very responsive for floating
          }
          if _behaviorFlags.contains(.slippery) {
              finalResponsiveness *= 1.2  // Easier to move when slippery
          }
          if _behaviorFlags.contains(.wet) {
              finalResponsiveness *= 0.8  // Slight resistance when wet
          }
          
          return finalResponsiveness
      }
      
      private func getThrowResponsivenessForMass() -> Float {
          // Throwing is even more affected by mass than rolling
          return getResponsivenessForMass() * 0.7  // 30% penalty for throwing
      }
      
      private func getMassInertiaFactor() -> Float {
          // Heavier objects have more inertia (resist changes in motion)
          return Mass / 0.2  // Normalize to base mass of 0.2kg
      }
      

      
      @objc open func debugRollingMode(fingerVelocity: CGPoint) {
          let fingerSpeed = sqrt(fingerVelocity.x * fingerVelocity.x + fingerVelocity.y * fingerVelocity.y)
          //let wouldThrow = checkThrowingIntent(fingerVelocity: fingerVelocity, , fingerSpeed: fingerSpeed)
          
          print("=== ROLLING MODE DEBUG ===")
          print("Finger velocity: \(fingerVelocity)")
          print("Finger speed: \(fingerSpeed)")
          print("Fling threshold: \(FLING_THRESHOLD_SPEED)")
          //print("Would throw: \(wouldThrow)")
          //print("Selected mode: \(wouldThrow ? "THROWING" : "ROLLING")")
          print("Mass responsiveness: \(getResponsivenessForMass())")
          print("========================")
      }
      
      
      @objc open func dropToPhysics() {
          // Optional: Give ball a tiny downward impulse if it's floating
          guard let _ = _modelEntity.physicsBody else {
              print("No physics body to apply force to")
              return
          }
          
          let dropImpulse = SIMD3<Float>(0, -0.1, 0)  // Small downward impulse
          _modelEntity.addForce(dropImpulse, relativeTo: nil)
          print("🎾 Applied gentle drop impulse - physics will handle the rest")
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
        if var physicsBody = _modelEntity.physicsBody {
          physicsBody.linearVelocity = 0.0
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
        debugSmoothRolling()
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
      print("Ground level: \(ARView3D.SHARED_GROUND_LEVEL)")
        print("Ball radius: \(_radius * Scale)")
        print("Is being dragged: \(isBeingDragged)")
        print("Current momentum: \(_currentMomentum)")
        print("========================")
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
            material.color = .init(tint: .green.withAlphaComponent(0.7))  // Normal = green
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
