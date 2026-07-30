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
  private var _grabOffset: SIMD3<Float> = .zero
  private var _isFlying: Bool = false
  
  
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
      guard name != _rootNodeName else { return }
      _rootNodeName = name
      //setupEntity()
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
    print("⚠️ setupEntity called from: \(Thread.callStackSymbols[1])")
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
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    print("🔍 ModelNode bounds after setup: min=\(bounds.min) max=\(bounds.max)")
    print("🔍 Size: \(bounds.max - bounds.min)")
    
    if let entity = _addedEntity {
      self._modelEntity = modelEntity
      entity.name = entity.name.isEmpty ? Name : entity.name
    }
  }
  
  override open func EnablePhysics(_ isDynamic: Bool = true) {
    // relativeTo: _modelEntity gives bounds in local space
    // so the center offset is relative to entity origin, not world origin
    let bounds = _modelEntity.visualBounds(relativeTo: _modelEntity)
    let size = bounds.max - bounds.min
    let center = (bounds.max + bounds.min) / 2.0
    
    let safeSize = SIMD3<Float>(
      max(size.x, 0.05),
      max(size.y, 0.05),
      max(size.z, 0.05)
    )
    
    let shape = ShapeResource.generateBox(size: safeSize)
      .offsetBy(translation: center)
    
    _modelEntity.collision = CollisionComponent(
      shapes: [shape],
      filter: CollisionFilter(
        group: ARView3D.CollisionGroups.arObjects,
        mask: [ARView3D.CollisionGroups.arObjects, ARView3D.CollisionGroups.environment]
      )
    )
    
    _enablePhysics = isDynamic
    _modelEntity.physicsBody = PhysicsBodyComponent(
      massProperties: PhysicsMassProperties(mass: Mass),
      material: PhysicsMaterialResource.generate(
        staticFriction: StaticFriction,
        dynamicFriction: DynamicFriction,
        restitution: Restitution
      ),
      mode: isDynamic ? .dynamic : .static
    )
    _modelEntity.physicsMotion = PhysicsMotionComponent()
    
    if #available(iOS 15.0, *) {
      updateShadowSettings()
    }
    
    print("🎯 ModelNode collision center (local): \(center), size: \(safeSize)")
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
  
  override open func endDrag(releaseVelocity: CGPoint, camera3DProjection: Any) {
    // Let the base class handle placement preview and surface detection
    super.endDrag(releaseVelocity: releaseVelocity, camera3DProjection: camera3DProjection)
    
    // Calculate throw speed from release velocity
    let speed = sqrt(releaseVelocity.x * releaseVelocity.x +
                     releaseVelocity.y * releaseVelocity.y)
    
    guard Float(speed) >= MIN_THROW_SPEED,
          let cameraVectors = camera3DProjection as? ARView3D.CameraVectors else { return }
    
    // Apply velocity after finalizeModelPlacement restores physics (0.5s delay + buffer)
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) { [weak self] in
      guard let self = self, let physicsBody = self._modelEntity.physicsBody else { return }
      
      let right = cameraVectors.right
      let forward = cameraVectors.forward
      
      let screenX = Float(releaseVelocity.x) * 0.002
      let screenY = Float(-releaseVelocity.y) * 0.002
      let worldVelocity = (right * screenX) + (forward * screenY)
      
      let clampedSpeed = min(simd_length(worldVelocity), MAX_THROW_SPEED)
      let finalVelocity = simd_length(worldVelocity) > 0
      ? simd_normalize(worldVelocity) * clampedSpeed
      : worldVelocity
      
      if #available(iOS 18.0, *), var motion = self._modelEntity.physicsMotion {
        motion.linearVelocity = finalVelocity
        self._modelEntity.physicsMotion = motion
      } else {
        self._modelEntity.addForce(finalVelocity * self.Mass * 10, relativeTo: nil as Entity?)
      }
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
      EnablePhysics()
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
      EnablePhysics()
    }
  }
  private func updatePhysicsCollisionShape() {
    guard _modelEntity.physicsBody != nil else { return }
    EnablePhysics(_enablePhysics)
  }
  
  override open func RotateXBy(_ degrees: Float) {
      super.RotateXBy(degrees)
      updatePhysicsCollisionShape()
  }

  override open func RotateYBy(_ degrees: Float) {
      super.RotateYBy(degrees)
      updatePhysicsCollisionShape()
  }

  override open func RotateZBy(_ degrees: Float) {
      super.RotateZBy(degrees)
      updatePhysicsCollisionShape()
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
