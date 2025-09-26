// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import ARKit
import RealityKit
import os.log

@available(iOS 14.0, *)
open class ModelNode: ARNodeBase, ARModel {

  private var _addedEntity: Entity? {
    didSet {
      if _addedEntity != nil {
        
      }
    }
  }
  private var _nodeNames: [String] = []
  private var _rootNodeName: String = "model"
  private var _numberToUseForNode: String = "1"
  
  // Pokemon GO Style Dragging Variables
  private var _isDragging: Bool = false
  private var _isFlying: Bool = false
  private var _dragStartTime: Date?
  private var _fingerPositions: [SIMD3<Float>] = []
  private var _fingerTimestamps: [Date] = []
  private var _originalPosition: SIMD3<Float>?
  private var _throwVelocity: SIMD3<Float> = SIMD3<Float>(0, 0, 0)

  
  // Physics and trajectory constants
  private let VELOCITY_HISTORY_COUNT = 5
  private let MIN_THROW_SPEED: Float = 0.5  // m/s
  private let MAX_THROW_SPEED: Float = 4.0  // m/s
  private let VELOCITY_SCALE: Float = 1.0   // Amplify finger velocity
  private let GRAVITY: Float = -9.81        // Earth gravity
 
  private let PLACEMENT_RAYCAST_DISTANCE: Float = 50.0

  @objc init(_ container: ARNodeContainer) {
    super.init(container: container)
    self.Name="model"
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  override open var Model: String {
    get {
      return _objectModel
    }
    set(modelStr) {
      loadModel(modelStr)
    }
  }
  
  override open var ModelUrl: String {
    get {
      return _objectModel
    }
    set(modelStr) {
      loadModel(modelStr)
    }
  }
  
  @objc public var BoundingBox: [[Float]] {
    get {
      guard let entity = _addedEntity else { return [[], []] }
      let bounds = entity.visualBounds(relativeTo: nil)
      let min = [UnitHelper.metersToCentimeters(bounds.min.x), UnitHelper.metersToCentimeters(bounds.min.y), UnitHelper.metersToCentimeters(bounds.min.z)]
      let max = [UnitHelper.metersToCentimeters(bounds.max.x), UnitHelper.metersToCentimeters(bounds.max.y), UnitHelper.metersToCentimeters(bounds.max.z)]
      return [min, max]
    }
  }
  
  @objc public var RootNodeName: String {
    get {
      return _rootNodeName
    }
    set(name) {
      _rootNodeName = name
      setupEntity()
    }
  }
  
  @objc open var NamesOfNodes: [String] {
    get {
      if _nodeNames.isEmpty {
        _nodeNames = EntityNames()
      }
      return _nodeNames
    }
  }
  
  // FillColor is not user accessible
  @objc open override var FillColor: Int32 {
    get {
      return 0
    }
    set(color) {}
  }
  
  // FillColorOpacity is not user accessible
  @objc open override var FillColorOpacity: Int32 {
    get {
      return 1
    }
    set(color) {}
  }

  // TextureOpacity is not user accessible
  @objc open override var TextureOpacity: Int32 {
    get {
      return 1
    }
    set(opacity) {}
  }

  
  private func loadModel(_ modelStr: String) {
  
      let path = AssetManager.shared.pathForExistingFileAsset(modelStr)
      _objectModel = modelStr
      
      guard !path.isEmpty else { return }
      
      let url = URL(fileURLWithPath: path)
      
      // Load using RealityKit's Entity.load for USDZ and other supported formats
      do {
        let entity = try Entity.load(contentsOf: url)
        // If the loaded entity is a ModelEntity, use it directly
        if let modelEntity = entity as? ModelEntity {
          self._modelEntity = modelEntity
        } else {
          // If not, create a ModelEntity and add the loaded entity as a child
          let modelEntity = ModelEntity()
          modelEntity.addChild(entity)
          self._modelEntity = modelEntity
        }
        
        self.setupEntity()
        print("success loading model")
        PlayAnimationsForAllNodes()
      } catch {
        print("Failed to load model at \(url.path) \(error.localizedDescription)")
        NodeNotFound(path)
      }
    
  }
  
  private func setupEntity() {
    if _addedEntity != nil {
      DispatchQueue.main.async {
        self._container?.removeNode(self)
      }
    }
    
    let modelEntity = _modelEntity
    
    modelEntity.name = "model"
    
    if !_rootNodeName.isEmpty, let childEntity = findEntity(in: modelEntity, withName: _rootNodeName) {
      _addedEntity = childEntity
    } else {
      _addedEntity = modelEntity
    }
    
    if let entity = _addedEntity {
      self._modelEntity = modelEntity
      entity.name = entity.name.isEmpty ? Name : entity.name
    }
  }
  
  private func findEntity(in entity: Entity, withName name: String) -> Entity? {
    if entity.name == name {
      return entity
    }
    
    for child in entity.children {
      if let found = findEntity(in: child, withName: name) {
        return found
      }
    }
    
    return nil
  }
  
  open func NodeNotFound(_ name: String) {
    EventDispatcher.dispatchEvent(of: self, called: "NodeNotFound", arguments: name as NSString)
  }
  
  private func EntityNames() -> [String] {
    var names: [String] = []
    if let entity = _addedEntity {
      getNames(entity: entity, list: &names)
    }
    return names
  }
  
  private func getNames(entity: Entity, list: inout [String]) {
    if !entity.name.isEmpty {
      list.append(entity.name)
    } else {
      let name = "\(Name)-\(_numberToUseForNode)"
      entity.name = name
      list.append(name)
    }
    
    for child in entity.children {
      getNames(entity: child, list: &list)
    }
  }

  /// Starts the Pokémon GO style drag interaction
  override open func startDrag() {
    print("🎯 Starting Pokémon GO style drag for \(Name)")
    
    _isDragging = true
    _isFlying = false
    _dragStartTime = Date()
    _originalPosition = _modelEntity.transform.translation
    
    // Clear velocity tracking arrays
    _fingerPositions.removeAll()
    _fingerTimestamps.removeAll()
    
    // Disable physics during drag
    _modelEntity.physicsBody = nil
    _modelEntity.collision = nil
    
    // Visual feedback - slight glow effect
    showDragVisualFeedback()
    
    print("🎯 Drag started at position: \(_originalPosition!)")
  }

  /// Updates the model position during drag with Pokémon GO style behavior
  override open func updateDrag(fingerWorldPosition: SIMD3<Float>) {
    guard _isDragging && !_isFlying else { return }
    
    let currentTime = Date()
    
    // Store finger position for velocity calculation
    _fingerPositions.append(fingerWorldPosition)
    _fingerTimestamps.append(currentTime)
    
    // Keep only recent positions for velocity calculation
    if _fingerPositions.count > VELOCITY_HISTORY_COUNT {
      _fingerPositions.removeFirst()
      _fingerTimestamps.removeFirst()
    }

    // Update model position with slight upward offset during drag
    let dragPosition = SIMD3<Float>(
      fingerWorldPosition.x,
      //fingerWorldPosition.y,
      max(fingerWorldPosition.y + DRAG_HEIGHT_OFFSET, ARView3D.SHARED_GROUND_LEVEL + 0.005),
      fingerWorldPosition.z
    )
    
    _modelEntity.transform.translation = dragPosition
    
    print("🎯 Dragging to position: \(dragPosition)")
  }

  /// Ends the drag with Pokémon GO style throwing or placement

  override open func endDrag(releaseVelocity: CGPoint, camera3DProjection worldDirection: Any) {
    guard _isDragging else { return }
    
    print("🎯 Ending drag with release velocity: \(releaseVelocity)")
    
    _isDragging = false
    
    // Calculate 3D throw velocity from finger movement history
    let throwVel = calculateThrowVelocity()
    let throwSpeed = simd_length(throwVel)
    
    print("🎯 Calculated throw velocity: \(throwVel), speed: \(throwSpeed)")
    
    // Decide between throwing or gentle placement
    if throwSpeed >= MIN_THROW_SPEED {
      // THROW: Launch with trajectory physics
      if #available(iOS 15.0, *) {
        startTrajectoryThrow(velocity: throwVel)
      }
    } else {
      // GENTLE PLACEMENT: Find nearest surface
      if #available(iOS 15.0, *) {
        placeOnNearestSurface()
      }
    }
    
    // Clear tracking data
    _fingerPositions.removeAll()
    _fingerTimestamps.removeAll()
  }

  /// Calculates the 3D throw velocity from finger movement history
  private func calculateThrowVelocity() -> SIMD3<Float> {
    guard _fingerPositions.count >= 2, _fingerTimestamps.count >= 2 else {
      return SIMD3<Float>(0, 0, 0)
    }
    
    // Use the most recent positions for velocity calculation
    let recentPositions = Array(_fingerPositions.suffix(3))
    let recentTimestamps = Array(_fingerTimestamps.suffix(3))
    
    var totalVelocity = SIMD3<Float>(0, 0, 0)
    var validSamples = 0
    
    // Calculate velocity between consecutive points
    for i in 1..<recentPositions.count {
      let deltaPos = recentPositions[i] - recentPositions[i-1]
      let deltaTime = Float(recentTimestamps[i].timeIntervalSince(recentTimestamps[i-1]))
      
      if deltaTime > 0.001 { // Avoid division by very small numbers
        let velocity = deltaPos / deltaTime
        totalVelocity += velocity
        validSamples += 1
      }
    }
    
    if validSamples > 0 {
      let avgVelocity = totalVelocity / Float(validSamples)
      
      // Apply velocity scaling and clamping
      let scaledVelocity = avgVelocity * VELOCITY_SCALE
      let speed = simd_length(scaledVelocity)
      
      if speed > MAX_THROW_SPEED {
        // Clamp to max speed while preserving direction
        let clampedVelocity = simd_normalize(scaledVelocity) * MAX_THROW_SPEED
        return clampedVelocity
      }
      
      return scaledVelocity
    }
    
    return SIMD3<Float>(0, 0, 0)
  }

  /// Starts a physics-based trajectory throw
  @available(iOS 15.0, *)
  private func startTrajectoryThrow(velocity: SIMD3<Float>) {
    print("🚀 Starting trajectory throw with velocity: \(velocity)")
    
    _isFlying = true
    _throwVelocity = velocity
    
    // Enable physics for realistic trajectory
    setupTrajectoryPhysics()
    
    // Apply initial velocity
    if #available(iOS 18.0, *), var physicsMotion = _modelEntity.physicsMotion {
      physicsMotion.linearVelocity = velocity
      _modelEntity.physicsMotion = physicsMotion
    } else {
      // Fallback for older iOS versions
      _modelEntity.addForce(velocity * Mass * 10, relativeTo: nil as Entity?)
    }
    
    // Start monitoring trajectory for placement
    monitorTrajectoryForPlacement()
  }
  
  @available(iOS 15.0, *)
  private func placeOnNearestSurface() {
    print("Placing on nearest surface")
    
    // Use the cached preview surface if available
    if let cachedSurface = getPreviewPlacementSurface() {
      print("Using cached preview surface: \(cachedSurface)")
      
      // Disable physics completely during placement
      _modelEntity.physicsBody = nil
      
      animateToPosition(cachedSurface) {
          self.finalizeModelPlacement()
          self.clearPreviewPlacementSurface()
      }
      return
    }
    
    // Fallback: find surface from current position
    let currentPos = _modelEntity.transform.translation
    if let placementPosition = findNearestHorizontalSurface(from: currentPos) {
      animateToPosition(placementPosition) {
          self.finalizeModelPlacement()
      }
    } else {
      // Final fallback to ground level
      let groundPosition = SIMD3<Float>(currentPos.x, ARView3D.SHARED_GROUND_LEVEL, currentPos.z)
      animateToPosition(groundPosition) {
          self.finalizeModelPlacement()
      }
    }
  }
  
  //csb TODO move to parent
  private func findNearestHorizontalSurface(from position: SIMD3<Float>) -> SIMD3<Float>? {
    guard let container = _container else { return nil }
    //get the surface that the container has determined is the closest
    // or just the placement anchor, right?
    return container.getARView().findBestSurfaceForPlacement()
  }


  /// Sets up physics for trajectory throwing
  private func setupTrajectoryPhysics() {
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    let size = bounds.max - bounds.min
    let avgSize = (size.x + size.y + size.z) / 3.0
    
    // Create appropriate collision shape
    let collisionShape = ShapeResource.generateSphere(radius: max(avgSize / 2, 0.03))
    
    _modelEntity.collision = CollisionComponent(
      shapes: [collisionShape],
      filter: CollisionFilter(
        group: ARView3D.CollisionGroups.arObjects,
        mask: [ARView3D.CollisionGroups.arObjects, ARView3D.CollisionGroups.environment]
      )
    )
    
    // Create physics body with realistic properties
    let mass = max(Mass, 0.1) // Minimum mass for stability
    _modelEntity.physicsBody = PhysicsBodyComponent(
      massProperties: PhysicsMassProperties(mass: mass),
      mode: .dynamic
    )
    
    // Apply realistic physics material
    if var physicsBody = _modelEntity.physicsBody {
      physicsBody.material = PhysicsMaterialResource.generate(
        staticFriction: StaticFriction,
        dynamicFriction: DynamicFriction,
        restitution: Restitution
      )
      _modelEntity.physicsBody = physicsBody
    }
    
    print("🎾 Physics setup complete for trajectory - mass: \(mass)kg")
  }

  /// Monitors the trajectory and triggers placement when appropriate
  @available(iOS 15.0, *)
  private func monitorTrajectoryForPlacement() {
    // Use a timer to check trajectory status
    Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { timer in
      guard self._isFlying else {
        timer.invalidate()
        return
      }
      
      let currentPos = self._modelEntity.transform.translation
      let currentVel = self._modelEntity.physicsMotion?.linearVelocity ?? SIMD3<Float>(0, 0, 0)
      let speed = simd_length(currentVel)
      
      // Check if the model has slowed down significantly or is moving upward (bounced)
      if speed < 0.5 || (currentVel.y > 0 && speed < 2.0) {
        print("Trajectory complete - placing on surface")
        timer.invalidate()
        self._isFlying = false
        
        // Find surface from where the object currently is (after trajectory)
        if let placementPos = self.findNearestHorizontalSurface(from: currentPos) {
          self.animateToPosition(placementPos) {
            self.finalizeModelPlacement()
          }
        } else {
          self.finalizeModelPlacement()
        }
      }
      
      // Safety timeout - stop flying after 10 seconds
      if let startTime = self._dragStartTime,
         Date().timeIntervalSince(startTime) > 10.0 {
        print("⚠️ Trajectory timeout - finalizing placement")
        timer.invalidate()
        self._isFlying = false
        self.finalizeModelPlacement()
      }
    }
  }

  /// Animates the model to a target position smoothly
  @available(iOS 15.0, *)
  private func animateToPosition(_ targetPosition: SIMD3<Float>, completion: @escaping () -> Void) {
    print("📍 Animating to position: \(targetPosition)")
    
    // Disable physics during animation
    _modelEntity.physicsBody = nil
    
    // Create smooth animation to target position
    let currentTransform = _modelEntity.transform
    var targetTransform = currentTransform
    targetTransform.translation = targetPosition
    
    // Use RealityKit's animation system
    let animation = FromToByAnimation<Transform>(
      name: "placeAnimation",
      from: currentTransform,
      to: targetTransform,
      duration: 0.3,
      timing: .easeOut,
      bindTarget: .transform
    )
    
    if let animationResource = try? AnimationResource.generate(with: animation) {
      _modelEntity.playAnimation(animationResource, transitionDuration: 0.1, startsPaused: false)
      
      // Completion handler
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
        completion()
      }
    } else {
      // Fallback - direct position set
      _modelEntity.transform.translation = targetPosition
      completion()
    }
  }

  /// Finalizes the model placement with physics restoration
  private func finalizeModelPlacement() {
    print("Finalizing model placement")
    
    _isFlying = false
    
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
        self?.EnablePhysics(true)
    }

    if let container = _container {
      container.hidePlacementPreview()
    }
    
    // Clear local preview data
    _previewPlacementSurface = nil
    
    // Remove visual drag feedback
    removeDragVisualFeedback()
    
    print("Model placement complete at: \(_modelEntity.transform.translation)")
  }

  /// Shows visual feedback during dragging
  private func showDragVisualFeedback() {
    guard #available(iOS 15.0, *) else { return }
    
    // Store original material
    OriginalMaterial = _modelEntity.model?.materials.first
    
    // Apply glow effect
    var dragMaterial = SimpleMaterial()
    dragMaterial.color = .init(tint: UIColor.systemBlue.withAlphaComponent(0.8))
    dragMaterial.baseColor = MaterialColorParameter.color(UIColor.systemBlue.withAlphaComponent(0.8))
    
    _modelEntity.model?.materials = [dragMaterial]
  }

  /// Removes visual feedback after dragging
  private func removeDragVisualFeedback() {
    guard #available(iOS 15.0, *) else { return }
    
    // Restore original material
    if let original = OriginalMaterial {
      _modelEntity.model?.materials = [original]
      OriginalMaterial = nil
    }
  }

  // MARK: - Enhanced Gesture Handler

  override open func handleAdvancedGestureUpdate(
    fingerLocation: CGPoint,
    fingerMovement: CGPoint,
    fingerVelocity: CGPoint,
    groundProjection: Any?,
    camera3DProjection: Any?,
    gesturePhase: UIGestureRecognizer.State
  ) {
    let worldPos: SIMD3<Float>? = groundProjection as? SIMD3<Float>
    
    switch gesturePhase {
    case .began:
      startDrag()
      
    case .changed:
      if let worldPosition = worldPos {
        updateDrag(fingerWorldPosition: worldPosition)
      } else {
        print("⚠️ No world position available during drag update")
      }
      
    case .ended, .cancelled:
      endDrag(releaseVelocity: fingerVelocity, camera3DProjection: camera3DProjection) //worldPos ?? SIMD3<Float>(0, 0, 0))
      
    default:
      break
    }
  }

  // MARK: - Scaling Methods (Preserved from original)
  
  override open func ScaleBy(_ scalar: Float) {
    print("🔄 Scaling model \(Name) by \(scalar)")
    
    let currentPos = _modelEntity.transform.translation
    let oldScale = Scale
    
    // Calculate model bounds for bottom positioning
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    let modelHeight = (bounds.max.y - bounds.min.y) * oldScale
    let currentBottomY = currentPos.y - modelHeight / 2.0
    
    // Keep physics enabled during scaling
    let hadPhysics = _modelEntity.physicsBody != nil
    
    // Update scale
    let newScale = oldScale * abs(scalar)
    _modelEntity.transform.scale = SIMD3<Float>(newScale, newScale, newScale)
    
    // Calculate new position to keep bottom at same level
    let newModelHeight = (bounds.max.y - bounds.min.y) * newScale
    let newCenterY = currentBottomY + newModelHeight / 2.0
    
    // Apply new position
    let newPosition = SIMD3<Float>(currentPos.x, newCenterY, currentPos.z)
    _modelEntity.transform.translation = newPosition

    // Update physics collision shape if it was enabled
    if hadPhysics {
      updatePhysicsCollisionShape()
    }
  }

  override open func scaleByPinch(scalar: Float) {
    let oldScale = Scale
    let newScale = oldScale * scalar
    
    // Validate scale bounds
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    let currentSize = bounds.max - bounds.min
    let newSize = currentSize * newScale
    
    let minSize: Float = 0.01
    let maxSize: Float = 10.0
    
    let avgNewSize = (newSize.x + newSize.y + newSize.z) / 3.0
    guard avgNewSize >= minSize && avgNewSize <= maxSize else { return }
    
    _modelEntity.transform.scale = SIMD3<Float>(newScale, newScale, newScale)
    
    if _modelEntity.physicsBody != nil {
      updatePhysicsCollisionShape()
    }
  }

  private func updatePhysicsCollisionShape() {
    guard _modelEntity.physicsBody != nil else { return }
    
    let currentScale = _modelEntity.transform.scale.x
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    let scaledSize = (bounds.max - bounds.min) * currentScale
    
    let safeSize = SIMD3<Float>(
      max(scaledSize.x, 0.05) * 1.1,
      max(scaledSize.y, 0.05) * 1.1,
      max(scaledSize.z, 0.05) * 1.1
    )
    
    let newShape = ShapeResource.generateBox(size: safeSize)
    
    _modelEntity.collision = CollisionComponent(
      shapes: [newShape],
      filter: _modelEntity.collision?.filter ?? CollisionFilter(
        group: ARView3D.CollisionGroups.arObjects,
        mask: [ARView3D.CollisionGroups.arObjects, ARView3D.CollisionGroups.environment]
      )
    )
    
    if var physicsBody = _modelEntity.physicsBody {
      physicsBody.massProperties = PhysicsMassProperties(mass: Mass)
      _modelEntity.physicsBody = physicsBody
    }
  }
}

// MARK: - Extensions (Preserved from original)

// MARK: FillColor Functions
@available(iOS 14.0, *)
extension ModelNode {
  @objc open func SetFillColorForNode(_ name: String, _ color: Int32, _ opacity: Int32, _ shouldColorChildNodes: Bool) {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetFillColorForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let colorEntity = (entity.name == name) ? entity : findEntity(in: entity, withName: name) else {
      NodeNotFound(name)
      return
    }
    
    let floatOpacity = Float(min(max(0, opacity), 100)) / 100.0
    let fillColor = argbToColor(color)
    
    if let modelEntity = colorEntity as? ModelEntity {
      updateEntityColor(modelEntity, color: fillColor, opacity: floatOpacity)
    } else if !shouldColorChildNodes {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetFillColorForNode", ErrorMessage.ERROR_MODELNODE_CANNOT_COLOR_NODE.code, name)
      return
    }
    
    if shouldColorChildNodes {
      for child in entity.children {
        updateAllColors(entity: child, color: fillColor, opacity: floatOpacity)
      }
    }
  }
  
  @objc open func SetFillColorForAllNodes(_ color: Int32, _ opacity: Int32) {
    let floatOpacity = Float(min(max(0, opacity), 100)) / 100.0
    let fillColor = argbToColor(color)
    
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetFillColorForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    updateAllColors(entity: entity, color: fillColor, opacity: floatOpacity)
  }
  
  @objc open func RenameNode(_ oldName: String, _ newName: String) {
    guard let entity = (_addedEntity?.name == oldName) ? _addedEntity : findEntity(in: _addedEntity!, withName: oldName) else {
      NodeNotFound(oldName)
      return
    }
    
    entity.name = newName
    _nodeNames = EntityNames()
  }
  
  private func updateEntityColor(_ entity: ModelEntity, color: UIColor, opacity: Float) {
    var material = SimpleMaterial()
    material.baseColor = MaterialColorParameter.color(color.withAlphaComponent(CGFloat(opacity)))
    entity.model?.materials = [material]
  }
  
  private func updateAllColors(entity: Entity, color: UIColor, opacity: Float) {
    if let modelEntity = entity as? ModelEntity {
      updateEntityColor(modelEntity, color: color, opacity: opacity)
    }
    
    for child in entity.children {
      updateAllColors(entity: child, color: color, opacity: opacity)
    }
  }
}

// MARK: Texture Functions
@available(iOS 14.0, *)
extension ModelNode {
  @objc open func SetTextureForNode(_ name: String, _ texture: String, _ opacity: Int32, _ shouldTexturizeChildNodes: Bool) {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let textureImage = AssetManager.shared.imageFromPath(path: texture) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForNode", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code)
      return
    }
    
    guard let textureEntity = (entity.name == name) ? entity : findEntity(in: entity, withName: name) else {
      NodeNotFound(name)
      return
    }
    
    let floatOpacity = Float(min(max(0, opacity), 100)) / 100.0
    
    if let modelEntity = textureEntity as? ModelEntity {
      updateEntityTexture(modelEntity, texture: textureImage, opacity: floatOpacity)
    } else if !shouldTexturizeChildNodes {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForNode", ErrorMessage.ERROR_MODELNODE_CANNOT_TEXTURIZE_NODE.code, name)
      return
    }
    
    if shouldTexturizeChildNodes {
      for child in entity.children {
        updateAllTextures(entity: child, texture: textureImage, opacity: floatOpacity)
      }
    }
  }
  
  @objc open func SetTextureForAllNodes(_ texture: String, _ opacity: Int32) {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let textureImage = AssetManager.shared.imageFromPath(path: texture) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTextureForAllNodes", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code)
      return
    }
    
    let floatOpacity = Float(min(max(0, opacity), 100)) / 100.0
    updateAllTextures(entity: entity, texture: textureImage, opacity: floatOpacity)
  }
  
  @available(iOS 14.0, *)
  private func updateEntityTexture(_ entity: ModelEntity, texture: UIImage, opacity: Float) {
    do {
      var material = SimpleMaterial()
      
      if #available(iOS 15.0, *) {
        let textureResource = try TextureResource.generate(from: texture.cgImage!, options: TextureResource.CreateOptions(semantic: .color))
        material.baseColor = MaterialColorParameter.texture(textureResource)
      } else {
        material.baseColor = MaterialColorParameter.color(.blue)
      }
      
      entity.model?.materials = [material]
    } catch {
      os_log("Failed to create texture resource: %@", log: .default, type: .error, error.localizedDescription)
    }
  }
  
  private func updateAllTextures(entity: Entity, texture: UIImage, opacity: Float) {
    if let modelEntity = entity as? ModelEntity {
      updateEntityTexture(modelEntity, texture: texture, opacity: opacity)
    }
    
    for child in entity.children {
      updateAllTextures(entity: child, texture: texture, opacity: opacity)
    }
  }
}

// MARK: ShowShadow Functions
@available(iOS 14.0, *)
extension ModelNode {
  @objc open func SetShowShadowForNode(_ name: String, _ showShadow: Bool, _ shouldShadowChildNodes: Bool) {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetShowShadowForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let shadowEntity = (entity.name == name) ? entity : findEntity(in: entity, withName: name) else {
      NodeNotFound(name)
      return
    }
    
    updateEntityShadow(shadowEntity, showShadow: showShadow)
    
    if shouldShadowChildNodes {
      for child in entity.children {
        updateAllShadows(entity: child, showShadow: showShadow)
      }
    }
  }
  
  @objc open func SetShowShadowForAllNodes(_ showShadow: Bool) {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetShowShadowForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    updateAllShadows(entity: entity, showShadow: showShadow)
  }
  
  private func updateEntityShadow(_ entity: Entity, showShadow: Bool) {
    if let modelEntity = entity as? ModelEntity {
      // In RealityKit, shadow casting is typically controlled at the renderer/lighting level
      // For basic shadow control, we can modify material properties
      if let material = modelEntity.model?.materials.first as? SimpleMaterial {
        modelEntity.model?.materials = [material]
      }
    }
  }
  
  private func updateAllShadows(entity: Entity, showShadow: Bool) {
    updateEntityShadow(entity, showShadow: showShadow)
    
    for child in entity.children {
      updateAllShadows(entity: child, showShadow: showShadow)
    }
  }
}

// MARK: Animation Functions
@available(iOS 14.0, *)
extension ModelNode {
  @objc open func PlayAnimationsForNode(_ name: String, _ shouldPlayChildNodes: Bool) {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "PlayAnimationsForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let animationEntity = (entity.name == name) ? entity : findEntity(in: entity, withName: name) else {
      NodeNotFound(name)
      return
    }
    
    playEntityAnimations(entity: animationEntity)
    
    if shouldPlayChildNodes {
      playAnimationsForAllChildren(entity: animationEntity)
    }
  }
  
  @objc open func PlaySpecificAnimationForNode(_ name: String, _ animation: String) {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "PlayAnimationsForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let animationEntity = (entity.name == name) ? entity : findEntity(in: entity, withName: name) else {
      NodeNotFound(name)
      return
    }
    
    if #available(iOS 15.0, *) {
      playSpecificAnimation(entity: animationEntity, animationName: animation)
    }
    

  }
  
  @objc open func PlayAnimationsForAllNodes() {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "PlayAnimationsForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    playEntityAnimations(entity: entity)
    playAnimationsForAllChildren(entity: entity)
  }
  
  @objc open func StopAnimationsForNode(_ name: String, _ shouldStopChildNodes: Bool) {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "StopAnimationsForNode", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    guard let animationEntity = (entity.name == name) ? entity : findEntity(in: entity, withName: name) else {
      NodeNotFound(name)
      return
    }
    
    stopEntityAnimations(entity: animationEntity)
    
    if shouldStopChildNodes {
      stopAnimationsForAllChildren(entity: animationEntity)
    }
  }
  
  @objc open func StopAnimationsForAllNodes() {
    guard let entity = _addedEntity else {
      _container?.form?.dispatchErrorOccurredEvent(self, "StopAnimationsForAllNodes", ErrorMessage.ERROR_MODELNODE_NOT_LOADED.code)
      return
    }
    
    stopEntityAnimations(entity: entity)
    stopAnimationsForAllChildren(entity: entity)
  }
  
  @available(iOS 15.0, *)
   private func playSpecificAnimation(entity: Entity, animationName: String) -> Bool {
       guard let modelEntity = entity as? ModelEntity else { return false }
       
       // Find the animation by name
       let matchingAnimation = modelEntity.availableAnimations.first { animation in
           // Check if animation has a name property or identifier
           return animation.name == animationName ||
                  animation.definition.name == animationName
       }
       
       guard let animation = matchingAnimation else {
           print("⚠️ Animation '\(animationName)' not found in entity '\(entity.name)'")
           return false
       }
       
       // Play the specific animation
       let controller = modelEntity.playAnimation(animation.repeat(duration: .infinity))
       
       print("✅ Playing animation '\(animationName)' on entity '\(entity.name)'")
       return true
   }
  
  @available(iOS 14.0, *)
  private func playAnimationsForAllChildren(entity: Entity) {
    for child in entity.children {
      playEntityAnimations(entity: child)
      playAnimationsForAllChildren(entity: child)
    }
  }
  
  private func stopAnimationsForAllChildren(entity: Entity) {
    for child in entity.children {
      stopEntityAnimations(entity: child)
      stopAnimationsForAllChildren(entity: child)
    }
  }
  
  @available(iOS 14.0, *)
  private func playEntityAnimations(entity: Entity) {
    // In RealityKit, animations are handled differently
    // We need to access the available animations and play them
    if let modelEntity = entity as? ModelEntity,
       let animation = modelEntity.availableAnimations.first {
      if #available(iOS 15.0, *) {
        modelEntity.playAnimation(animation.repeat(duration: .infinity))
      } else {
        // Fallback on earlier versions
      }
    }
  }
  
  private func stopEntityAnimations(entity: Entity) {
    if let modelEntity = entity as? ModelEntity {
      modelEntity.stopAllAnimations()
    }
  }
}
