// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import ARKit
import GLKit
import os.log

// MARK: - Supporting Types and Enums

enum SceneEntityType: String {
    case floor = "floor"
    case wall = "wall"
    case ceiling = "ceiling"
    case furniture = "furniture"
    case unknown = "unknown"
}

enum CollisionType {
    case floor
    case wall
    case object
    case none
}

// MARK: - ARNodeBase Class

@available(iOS 14.0, *)
open class ARNodeBase: NSObject, ARNode {

  weak var _container: ARNodeContainer?
  public var _modelEntity: ModelEntity
  
  private var _color: Int32 = Int32(bitPattern: AIComponentKit.Color.red.rawValue)
  
  public var hasTexture = false
  public var _texture: String = ""
  
  public var _enablePhysics: Bool = false
  private var _pinchToScale: Bool = false
  private var _panToMove: Bool = false
  private var _rotateWithGesture: Bool = false
  public var _showShadow: Bool = true
  
  public let DRAG_HEIGHT_OFFSET: Float = 0.001 // Hover above surfaces during drag
  public var _isBeingDragged = false
  private var _dragStartLocation: CGPoint = .zero
  private var _lastDragLocation: CGPoint = .zero
  private var _originalMaterial: Material?
  
  private var _gravityScale = Float(0.5)
  private var _dragSensitivity = Float(2.0)  // Better default for responsiveness
  private var _releaseForceMultiplier = Float(0.0)
  
  private var _currentVelocity: SIMD3<Float> = SIMD3<Float>(0, 0, 0)
  private var _momentumTask: Task<Void, Never>?
  private var _isCurrentlyColliding = false
  private var _collisionEffectTask: Task<Void, Never>?
  
  public var _linearDamping  = Float(0.0)
  public var _angularDamping  = Float(0.0)
  private var _rollingForce = Float(0.0)
  private var _impulseScale = Float(0.0)
  private var _dampingTask = Task {}

  private var _collisionWidth = Float(0.0)
  private var _collisionHeight = Float(0.0)
  private var _collisionDepth = Float(0.0)
  private var _collisionRadius = Float(0.0)

  public var _anchorEntity: AnchorEntity?
  public var _followingMarker: ARImageMarker? = nil
  public var _fromPropertyPosition = "0.0,0.0,0.0"
  public var _objectModel: String = ""
  public var _geoAnchor: ARGeoAnchor?
  public var _worldOffset: SIMD3<Float>?
  public var _creatorSessionStart: CLLocation?
  
  public var _previewPlacementSurface: SIMD3<Float>?
  public var _hasPreviewSurface: Bool = false
  


  // MARK: - Initialization
  
  init(container: ARNodeContainer, mesh: MeshResource? = nil) {
    _container = container
    _modelEntity = ModelEntity()
    
    super.init()
    setupInitialMaterial()
    XPosition = 0
    YPosition = 0
    ZPosition = -1
    
    if let mesh = mesh {
      _modelEntity.model = ModelComponent(mesh: mesh, materials: [])
    }
  }
  
  @objc open func Initialize() {
    self._container?.addNode(self)
  }
  
  open func syncInitialize() {
    self._container?.addNode(self)
  }
  
  @objc init(modelNodeContainer: ARNodeContainer) {
    _container = modelNodeContainer
    _modelEntity = ModelEntity()
    super.init()
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  
  
  // MARK: - Anchor Management
  
  open var Anchor: AnchorEntity? {
    get {
      if let anchor = _anchorEntity {
        return anchor
      }
      _anchorEntity = createAnchor()
      return _anchorEntity
    }
    set(a) {
      _anchorEntity = a
    }
  }
  
  func setGeoAnchor(_ geoAnchor: ARGeoAnchor) {
      _geoAnchor = geoAnchor
  }

  func getGeoAnchor() -> ARGeoAnchor? {
      return _geoAnchor
  }
  
  @objc open var IsGeoAnchored: Bool {
      return getGeoAnchor() != nil
  }
  
  @objc open var GeoCoordinates: [Double] {
      guard let geoAnchor = getGeoAnchor() else {
          return []
      }
    return [geoAnchor.coordinate.latitude, geoAnchor.coordinate.longitude, Double(geoAnchor.altitude!)]
  }
  
  // MARK: - Properties
  
  @objc open var Name: String {
    get { return _modelEntity.name }
    set(name) { _modelEntity.name = name }
  }
  
  @objc open var NodeType: String {
    get { return String(describing: type(of: self)) }
  }
  
  @objc open var Model: String {
    get { return _objectModel }
    set(modelStr) { _objectModel = modelStr }
  }
  
  @objc open var XPosition: Float {
    get { return UnitHelper.metersToCentimeters(_modelEntity.transform.translation.x) }
    set(x) { _modelEntity.transform.translation.x = UnitHelper.centimetersToMeters(x) }
  }
  
  @objc open var YPosition: Float {
    get { return UnitHelper.metersToCentimeters(_modelEntity.transform.translation.y) }
    set(y) { _modelEntity.transform.translation.y = UnitHelper.centimetersToMeters(y) }
  }
  
  @objc open var ZPosition: Float {
    get { return UnitHelper.metersToCentimeters(_modelEntity.transform.translation.z) }
    set(z) { _modelEntity.transform.translation.z = UnitHelper.centimetersToMeters(z) }
  }
  
  @objc open var XRotation: Float {
    get {
      let euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      return GLKMathRadiansToDegrees(euler.x)
    }
    set(degrees) {
      var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      euler.x = GLKMathDegreesToRadians(degrees)
      _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
    }
  }
  
  @objc open var YRotation: Float {
    get {
      let euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      return GLKMathRadiansToDegrees(euler.y)
    }
    set(degrees) {
      var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      euler.y = GLKMathDegreesToRadians(degrees)
      _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
    }
  }
  
  @objc open var ZRotation: Float {
    get {
      let euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      return GLKMathRadiansToDegrees(euler.z)
    }
    set(degrees) {
      var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      euler.z = GLKMathDegreesToRadians(degrees)
      _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
    }
  }
  
  @objc open var ModelUrl: String {
    get { return _objectModel }
    set(model) { _objectModel = model }
  }
  
  @objc open var Scale: Float {
    get { return _modelEntity.transform.scale.x }
    set(scalar) {
      let scale = abs(scalar)
      _modelEntity.transform.scale = SIMD3<Float>(scale, scale, scale)
    }
  }
  
  @objc open var PoseFromPropertyPosition: String {
    get { return _fromPropertyPosition }
    set(pose) { _fromPropertyPosition = pose }
  }
  
  @objc open var FillColor: Int32 {
    get { return _color }
    set(color) {
      _color = color
      updateMaterial()
    }
  }
  
  @objc @available(iOS 14.0, *)
  open var FillColorOpacity: Int32 {
    get { return 100 }
    set(opacity) {
      let alpha = Float(min(max(0, opacity), 100)) / 100.0
      if #available(iOS 15.0, *) {
        updateMaterialOpacity(alpha)
      }
    }
  }
  
  @objc open var Texture: String {
    get { return _texture }
    set(path) {
      if let image = AssetManager.shared.imageFromPath(path: path) {
        _texture = path
        if #available(iOS 15.0, *) {
          updateTextureFromImage(image)
        } else {
          updateMaterial()
        }
        hasTexture = true
      } else {
        if !path.isEmpty {
          _container?.form?.dispatchErrorOccurredEvent(self, "Texture", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code)
        }
        hasTexture = false
        _texture = ""
        updateMaterial()
      }
    }
  }
  
  @objc open var TextureOpacity: Int32 {
    get { return FillColorOpacity }
    set(opacity) { FillColorOpacity = opacity }
  }
  
  @objc open var Visible: Bool {
    get { return _modelEntity.isEnabled }
    set(visible) { _modelEntity.isEnabled = visible }
  }
  
  @objc open var ShowShadow: Bool {
    get { return _showShadow }
    set(showShadow) {
      _showShadow = showShadow
      updateShadowSettings()
    }
  }
  
  @objc open var Opacity: Int32 {
    get { return FillColorOpacity }
    set(opacity) { FillColorOpacity = opacity }
  }
  
  // MARK: - Physics Properties
  
  @objc open var EnablePhysics: Bool {
    get { return _enablePhysics }
    set(enablePhysics) { EnablePhysics(enablePhysics) }
  }
  
  @objc open var StaticFriction: Float = 0.6 {
      didSet { updatePhysicsMaterial() }
  }
  
  @objc open var DynamicFriction: Float = 0.6 {
      didSet { updatePhysicsMaterial() }
  }
  
  @objc open var Restitution: Float = 0.3 {
      didSet { updatePhysicsMaterial() }
  }
  
  @objc open var Mass: Float = 1.0 {
      didSet { updateMassProperties() }
  }
  
  @objc open var GravityScale: Float {
      get { return _gravityScale }
      set { _gravityScale = newValue }
  }
  
  @objc open var isBeingDragged: Bool {
      get { return _isBeingDragged }
      set {_isBeingDragged = newValue }
  }
  
  @objc open var DragSensitivity: Float {
      get { return _dragSensitivity }
      set { _dragSensitivity = newValue }
  }
  
  @objc open var ReleaseForceMultiplier: Float {
      get { return _releaseForceMultiplier }
      set { _releaseForceMultiplier = newValue }
  }
  
  // MARK: - Gesture Properties
  
  @objc open var PinchToScale: Bool {
    get { return _pinchToScale }
    set(pinchToScale) { _pinchToScale = pinchToScale }
  }
  
  @objc open var PanToMove: Bool {
    get { return _panToMove }
    set(panToMove) { _panToMove = panToMove }
  }
  
  @objc open var RotateWithGesture: Bool {
    get { return _rotateWithGesture }
    set(rotate) { _rotateWithGesture = rotate }
  }
  
  @objc open var CollisionShape: String = "box" {
    didSet { updateCollisionShape() }
  }
  
  @objc open var IsFollowingImageMarker: Bool {
    get { return _followingMarker != nil }
  }
  
  // MARK: - Drag Properties
  
  public var OriginalMaterial: Material? {
      get { return _originalMaterial }
      set(newValue) { _originalMaterial = newValue }
  }
  
  // MARK: - Component Protocol Implementation
  
  @objc open var Width: Int32 {
    get { return 0 }
    set { }
  }
  
  @objc open var Height: Int32 {
    get { return 0 }
    set { }
  }
  
  @objc open var dispatchDelegate: HandlesEventDispatching? {
    get { return _container?.form?.dispatchDelegate }
  }
  
  public func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  public func setWidthPercent(_ toPercent: Int32) {}
  public func setHeightPercent(_ toPercent: Int32) {}
  
  // MARK: - Movement Methods
  
  @objc open func RotateXBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
    euler.x += radians
    _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
  }
  
  @objc open func RotateYBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
    euler.y += radians
    _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
  }
  
  @objc open func RotateZBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
    euler.z += radians
    _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
  }

  
  @objc open func MoveBy(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    _modelEntity.transform.translation += SIMD3<Float>(xMeters, yMeters, zMeters)
  }
  
  @objc open func MoveTo(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    _modelEntity.transform.translation = SIMD3<Float>(xMeters, yMeters, zMeters)
  }
  
  // MARK: - Distance Methods
  
  @objc open func DistanceToNode(_ node: ARNode) -> Float {
    let myPos = _modelEntity.transform.translation
    let otherPos = node.getPosition()
    return UnitHelper.metersToCentimeters(simd_distance(myPos, otherPos))
  }
  
  @objc open func DistanceToSpotlight(_ light: ARSpotlight) -> Float {
    let myPos = _modelEntity.transform.translation
    let lightPos = light.getPosition()
    return UnitHelper.metersToCentimeters(simd_distance(myPos, lightPos))
  }
  
  @objc open func DistanceToPointLight(_ light: ARPointLight) -> Float {
    let myPos = _modelEntity.transform.translation
    let lightPos = light.getPosition()
    return UnitHelper.metersToCentimeters(simd_distance(myPos, lightPos))
  }
  
  @objc open func DistanceToDetectedPlane(_ detectedPlane: ARDetectedPlane) -> Float {
    let myPos = _modelEntity.transform.translation
    let planePos = detectedPlane.getPosition()
    return UnitHelper.metersToCentimeters(simd_distance(myPos, planePos))
  }
  
  // MARK: - Position Methods
  
  open func getPosition() -> SIMD3<Float> {
    return _modelEntity.transform.translation
  }
  
  open func setPosition(x: Float, y: Float, z: Float) {
    _modelEntity.transform.translation = SIMD3<Float>(x, y, z)
  }
  
  // MARK: - Gesture Response Methods
  
  
  open func ScaleBy(_ scalar: Float) {
    print("🔄 Scaling OBJECT \(Name) by \(scalar)")
    
    let oldScale = Scale

    let hadPhysics = _modelEntity.physicsBody != nil
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    let halfHeight = (bounds.max.x - bounds.min.x) / 2.0
    let newScale = oldScale * abs(scalar)
    // ✅ Update physics immediately if it was enabled before we change the scale
    if hadPhysics {
      let previousSize = halfHeight * Scale
      _modelEntity.position.y = _modelEntity.position.y - (previousSize) + (halfHeight * newScale)
    }
  
    Scale = newScale
    print("Scale complete - bottom position maintained")
  }
  
  open func scaleByPinch(scalar: Float) {
    let oldScale = Scale
    let newScale = oldScale * abs(scalar)
    
    let hadPhysics = _modelEntity.physicsBody != nil
    
    if hadPhysics {
      let savedMass = Mass
      let savedFriction = StaticFriction
      let savedRestitution = Restitution
      
      _modelEntity.physicsBody = nil
      _modelEntity.collision = nil
        
      //remember bounds already has scale. Behavior is inconsistent w/r to RealityKit scaling itself
      let bounds = _modelEntity.visualBounds(relativeTo: nil)
      let halfHeight = (bounds.max.x - bounds.min.x) / 2.0
      let previousSize = halfHeight * oldScale
      _modelEntity.position.y = _modelEntity.position.y - previousSize + (halfHeight * newScale)
      
      // Apply scale
      Scale = newScale

      // Restore physics
      Mass = savedMass
      StaticFriction = savedFriction
      Restitution = savedRestitution
    
      EnablePhysics(true)
    } else {
        Scale = newScale
    }
  }

 open func EnablePhysics(_ isDynamic: Bool = true) {
    let currentPos = _modelEntity.transform.translation
    let groundLevel = Float(ARView3D.SHARED_GROUND_LEVEL)
    
    print("🎾 EnablePhysics called for \(Name) with Mass \(Mass)")
    print("🎾 Current position: \(currentPos)")
    print("🎾 Ground level: \(groundLevel)")
    print("🎾 Distance from ground: \(currentPos.y - groundLevel)")
    
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    let sizeX = bounds.max.x - bounds.min.x
    let sizeY = bounds.max.y - bounds.min.y
    let sizeZ = bounds.max.z - bounds.min.z
    let halfHeight = sizeY / 2
    let bottomY = currentPos.y - halfHeight
    
    print("🎾 Box size: \(sizeY)")
    print("🎾 Half height: \(halfHeight)")
    print("🎾 Bottom Y: \(bottomY)")
    print("🎾 Bottom vs floor: \(bottomY - groundLevel)")

      // don't scale the collision shape
    let shape: ShapeResource = ShapeResource.generateBox(width: sizeX, height: sizeY, depth: sizeZ)
    _modelEntity.collision = CollisionComponent(shapes: [shape])

    _enablePhysics = isDynamic
    
    // Create mass properties separately
    let massProperties = PhysicsMassProperties(mass: Mass)
    
    // Create a custom physics material for gentle collisions
    let gentleMaterial = PhysicsMaterialResource.generate(
      staticFriction: StaticFriction,
      dynamicFriction: DynamicFriction,
      restitution: Restitution
    )
    
    _modelEntity.physicsBody = PhysicsBodyComponent(
      massProperties: massProperties,
      material: gentleMaterial,
      mode: isDynamic ? .dynamic : .static
    )
    
    _modelEntity.physicsMotion = PhysicsMotionComponent()
    
    if #available(iOS 15.0, *) {
        updateShadowSettings()
    }
  }
  
  @objc open func debugCollisionShape() {
     let visualScale = _modelEntity.transform.scale.x
     
     print("=== COLLISION SHAPE DEBUG ===")
     //print("Internal radius: \(_radius)m")
     print("Visual scale: \(visualScale), and Scale is \(Scale)")
     print("Has collision: \(_modelEntity.collision != nil)")
     print("Has physics: \(_modelEntity.physicsBody != nil)")

     // Visual bounds check
     let bounds = _modelEntity.visualBounds(relativeTo: nil)
     let visualBoundRadius = (bounds.max.x - bounds.min.x) / 2.0
     print("Visual bounds radius: \(visualBoundRadius)m")
     print("==========================")
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

  
  open func moveByPan(x: Float, y: Float) {
    if PanToMove {
      _modelEntity.transform.translation += SIMD3<Float>(x, y, 0.0)
    }
  }
  
  open func rotateByGesture(radians: Float) {
    if RotateWithGesture {
      var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      euler.y = radians
      _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
      print("node rotation \(radians)")
    }
  }
  
  // MARK: - Serialization Methods
  
  @objc open func ARNodeToYail() -> YailDictionary {
      os_log("going to try to export ARNode as yail", log: .default, type: .info)
         
      var yailDict: YailDictionary = [:]
      var transformDict: YailDictionary = PoseToYailDictionary() ?? [:]
      
      yailDict["model"] = self.ModelUrl
      yailDict["texture"] = self.Texture
      yailDict["scale"] = self.Scale
      yailDict["pose"] = transformDict
      yailDict["type"] = self.Name
      yailDict["physics"] = String(self._enablePhysics)
      yailDict["canMove"] = String(self._panToMove)
      yailDict["canScale"] = String(self._pinchToScale)
         
      print("exporting ARNode as Yail convert toYail ")
      return yailDict
  }
     
  open func CoordinatesToYailDictionary() -> YailDictionary? {
    let yailDictSave: YailDictionary = [:]
    yailDictSave["lat"] = self.getGeoAnchor()?.coordinate.latitude ?? 0.0
    yailDictSave["lng"] = self.getGeoAnchor()?.coordinate.longitude ?? 0.0
    yailDictSave["alt"] = self.getGeoAnchor()?.altitude ?? 0.0
    
    print("creator coordinates are \(yailDictSave)")
    return yailDictSave
  }
  
  @objc open func PoseToYailDictionary() -> YailDictionary? {
    os_log("anchor pose as YailDict", log: .default, type: .info)
    
    guard let p = self._modelEntity.transform as? Transform else {
        os_log("pose is nil", log: .default, type: .info)
        return nil
    }
    
    print("pose is \(p)")
    
    let translationDict: YailDictionary = [:]
    let rotationDict: YailDictionary = [:]
    let yailDictSave: YailDictionary = [:]
    
    translationDict["x"] = p.translation.x
    translationDict["y"] = p.translation.y
    translationDict["y_offset"] = p.translation.y - Float(ARView3D.SHARED_GROUND_LEVEL)
    translationDict["z"] = p.translation.z
    yailDictSave["t"] = translationDict
    
    rotationDict["x"] = p.rotation.vector.x
    rotationDict["y"] = p.rotation.vector.y
    rotationDict["z"] = p.rotation.vector.z
    rotationDict["w"] = p.rotation.vector.w
    yailDictSave["q"] = rotationDict

    yailDictSave["lat"] = self.getGeoAnchor()?.coordinate.latitude ?? 0.0
    yailDictSave["lng"] = self.getGeoAnchor()?.coordinate.longitude ?? 0.0
    yailDictSave["alt"] = self.getGeoAnchor()?.altitude ?? 0.0
    
    os_log("exporting pose as YailDict with %@", log: .default, type: .info, String(describing: yailDictSave))
    return yailDictSave
  }
  
  // MARK: - Material Management
  
  private func setupInitialMaterial() {
    updateMaterial()
  }
  
  private func updateMaterial() {
    guard _modelEntity.model != nil else { return }
    
    var material = SimpleMaterial()
    
    if _color == AIComponentKit.Color.none.rawValue {
      material.baseColor = MaterialColorParameter.color(.clear)
    } else {
      material.baseColor = MaterialColorParameter.color(argbToUIColor(_color))
    }
    
    _modelEntity.model?.materials = [material]
  }
  
  @available(iOS 15.0, *)
  private func updateMaterialOpacity(_ alpha: Float) {
    guard var material = _modelEntity.model?.materials.first as? SimpleMaterial else { return }
    
    let currentColor = material.color.tint
    let newColor = UIColor(
      red: currentColor.cgColor.components![0],
      green: currentColor.cgColor.components![1],
      blue: currentColor.cgColor.components![2],
      alpha: CGFloat(alpha)
    )
    material.baseColor = MaterialColorParameter.color(newColor)
    _modelEntity.model?.materials = [material]
  }
  
  @available(iOS 15.0, *)
  private func updateTextureFromImage(_ image: UIImage) {
    guard _modelEntity.model != nil else { return }
    
    var material = SimpleMaterial()
    
    do {
      let texture = try TextureResource.generate(from: image.cgImage!, options: .init(semantic: .color))
      material.baseColor = MaterialColorParameter.texture(texture)
    } catch {
      print("Failed to create texture: \(error)")
      return
    }
    
    _modelEntity.model?.materials = [material]
  }
  
  private func argbToUIColor(_ argb: Int32) -> UIColor {
    let alpha = CGFloat((argb >> 24) & 0xFF) / 255.0
    let red = CGFloat((argb >> 16) & 0xFF) / 255.0
    let green = CGFloat((argb >> 8) & 0xFF) / 255.0
    let blue = CGFloat(argb & 0xFF) / 255.0
    return UIColor(red: red, green: green, blue: blue, alpha: alpha)
  }
  
  // MARK: - Anchor Management
  
  func createAnchor() -> AnchorEntity {
    if let geoAnchor = getGeoAnchor() {
        if let existingAnchor = _anchorEntity {
            return existingAnchor
        }
        
        let placeholderAnchor = AnchorEntity(world: SIMD3<Float>(0, 0, 0))
        _anchorEntity = placeholderAnchor
        return placeholderAnchor
    }
    
    if let existingAnchor = _anchorEntity {
      return existingAnchor
    }
    
    let anchor: AnchorEntity
    
    if let followingMarker = _followingMarker {
      anchor = AnchorEntity(.image(group: "ARResources", name: followingMarker.Name))
    } else {
      let worldTransform = _modelEntity.transformMatrix(relativeTo: nil)
      anchor = AnchorEntity(world: worldTransform)
    }
    
    _anchorEntity = anchor
    if _anchorEntity != nil {
      anchor.addChild(_modelEntity)
    }
    return anchor
  }
  
  func createAnchorWithPose(pose: Transform) -> AnchorEntity {
    if let existingAnchor = _anchorEntity {
      return existingAnchor
    }
    
    let anchor = AnchorEntity(components: pose)
    _anchorEntity = anchor
    if _anchorEntity != nil {
      anchor.addChild(_modelEntity)
    }
    return anchor
  }
  
  func removeFromAnchor() {
    _modelEntity.removeFromParent()
    _anchorEntity = nil
  }
  
  // MARK: - Quaternion/Euler Conversion
  
  func quaternionToEulerAngles(_ q: simd_quatf) -> SIMD3<Float> {
    let w = q.vector.w
    let x = q.vector.x
    let y = q.vector.y
    let z = q.vector.z
    
    let sinr_cosp = 2 * (w * x + y * z)
    let cosr_cosp = 1 - 2 * (x * x + y * y)
    let roll = atan2(sinr_cosp, cosr_cosp)
    
    let sinp = 2 * (w * y - z * x)
    let pitch: Float
    if abs(sinp) >= 1 {
      pitch = copysign(Float.pi / 2, sinp)
    } else {
      pitch = asin(sinp)
    }
    
    let siny_cosp = 2 * (w * z + x * y)
    let cosy_cosp = 1 - 2 * (y * y + z * z)
    let yaw = atan2(siny_cosp, cosy_cosp)
    
    return SIMD3<Float>(roll, pitch, yaw)
  }
  
  func eulerAnglesToQuaternion(_ euler: SIMD3<Float>) -> simd_quatf {
    let cx = cos(euler.x * 0.5)
    let sx = sin(euler.x * 0.5)
    let cy = cos(euler.y * 0.5)
    let sy = sin(euler.y * 0.5)
    let cz = cos(euler.z * 0.5)
    let sz = sin(euler.z * 0.5)
    
    let w = cx * cy * cz + sx * sy * sz
    let x = sx * cy * cz - cx * sy * sz
    let y = cx * sy * cz + sx * cy * sz
    let z = cx * cy * sz - sx * sy * cz
    
    return simd_quatf(ix: x, iy: y, iz: z, r: w)
  }
  
  // MARK: - Event Methods
  
  @objc open func Click() {
    EventDispatcher.dispatchEvent(of: self, called: "Click")
  }
  
  @objc open func LongClick() {
    EventDispatcher.dispatchEvent(of: self, called: "LongClick")
  }
  
  // MARK: - Image Marker Following
  
  @objc open func Follow(_ imageMarker: ARImageMarker) {
    guard _followingMarker == nil else {
      _container?.form?.dispatchErrorOccurredEvent(self, "Follow", ErrorMessage.ERROR_ALREADY_FOLLOWING_IMAGEMARKER.code)
      return
    }
    
    _followingMarker = imageMarker
    setPosition(x: 0, y: 0, z: 0)
    imageMarker.attach(self)
  }
  
  @objc open func FollowWithOffset(_ imageMarker: ARImageMarker, _ x: Float, _ y: Float, _ z: Float) {
    guard _followingMarker == nil else {
      _container?.form?.dispatchErrorOccurredEvent(self, "FollowWithOffset", ErrorMessage.ERROR_ALREADY_FOLLOWING_IMAGEMARKER.code)
      return
    }
    
    _followingMarker = imageMarker
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    setPosition(x: xMeters, y: yMeters, z: zMeters)
    imageMarker.attach(self)
  }
  
  @objc open func StopFollowingImageMarker() {
    _followingMarker?.removeNode(self)
    stopFollowing()
  }
  
  open func stopFollowing() {
    let worldTransform = _modelEntity.transformMatrix(relativeTo: nil)
    
    _modelEntity.removeFromParent()
    _followingMarker?.removeNode(self)
    _followingMarker = nil
    
    _modelEntity.transform = Transform(matrix: worldTransform)
    _container?.addNode(self)
    
    StoppedFollowingMarker()
  }
  
  @objc open func StoppedFollowingMarker() {
    EventDispatcher.dispatchEvent(of: self, called: "StoppedFollowingMarker")
  }

  // MARK: - Lifecycle Methods
  
  @objc open func onResume() { }
  
  @objc open func onPause() { }
  
  @objc open func onDelete() {
    stopFollowing()
    _container?.removeNode(self)
    _container = nil
  }
  
  @objc open func onDestroy() {
    stopFollowing()
    _container?.removeNode(self)
    _container = nil
  }

  // MARK: - Physics Methods
  
  @objc open func DisablePhysics() {
      _modelEntity.collision = nil
      _modelEntity.physicsBody = nil
  }
  
  // MARK: - Collision Handling Methods
  
  @objc open func CollisionDetected() {}
  
  @objc open func ObjectCollidedWithScene(_ node: ARNodeBase) {
    EventDispatcher.dispatchEvent(of: self, called: "ObjectCollidedWithScene", arguments: node)
  }

  @objc open func ObjectCollidedWithObject(_ otherNode: ARNodeBase) {
    // Default collision behavior for all AR objects
    print("🔥 \(Name) collided with another node \(otherNode.Name) at y \(String(_modelEntity.transform.translation.y))")
    
    // Show collision effect if available
    if #available(iOS 15.0, *) {
        showCollisionEffect(type: .object)
    }
    
    // Dispatch event to app level
    EventDispatcher.dispatchEvent(of: self, called: "ObjectCollidedWithObject", arguments: otherNode)
  }

  
  // Handle collision with another AR node (we know it's a node)
  @objc open func handleNodeCollision(with otherNode: ARNodeBase, event: Any) {
      print("🔥 \(Name) collision with AR node: \(otherNode.Name)")
      
      // Call the main collision method that subclasses can override
      ObjectCollidedWithObject(otherNode)
  }
  
  // Handle collision with scene element (we know what type of scene element)
  @objc open func handleSceneCollision(sceneEntity: Any, sceneType: Any, event: Any) {
      guard let sceneTypeEnum = sceneType as? SceneEntityType else { return }
      
      print("🏠 \(Name) collided with scene: \(sceneTypeEnum.rawValue)")
      
      // Show different collision effect based on scene type
      if #available(iOS 15.0, *) {
          let collisionType: CollisionType = sceneTypeEnum == .floor ? .floor : .wall
          showCollisionEffect(type: collisionType)
      }
      
      // Dispatch scene collision event
      EventDispatcher.dispatchEvent(of: self, called: "ObjectCollidedWithScene", arguments: sceneTypeEnum.rawValue as AnyObject)
  }

  // Override this in subclasses for custom scene collision behavior
  @objc open func respondToSceneCollision(sceneEntity: Any, sceneType: Any, event: Any) {
      print("🔥 \(Name) base scene collision response with \(sceneType) - override in subclass")
      
      guard let sceneTypeEnum = sceneType as? SceneEntityType else { return }
      
      let collisionType: CollisionType
      switch sceneTypeEnum {
      case .floor:
          collisionType = .floor
      case .wall, .ceiling, .furniture:
          collisionType = .wall
      case .unknown:
          collisionType = .wall
      }
      
      if #available(iOS 15.0, *) {
          showCollisionEffect(type: collisionType)
      }
  }

  // Enhanced collision effect method
  @available(iOS 15.0, *)
  private func showCollisionEffect(type: CollisionType) {

    if OriginalMaterial == nil {
        OriginalMaterial = _modelEntity.model?.materials.first
    }
    _isCurrentlyColliding = true
    
    var collidedMaterial = SimpleMaterial()
    
    // Different colors for different collision types
    switch type {
    case .object:
        collidedMaterial.color = .init(tint: .red.withAlphaComponent(0.7))
    case .floor:
        collidedMaterial.color = .init(tint: .blue.withAlphaComponent(0.6))
    case .wall:
        collidedMaterial.color = .init(tint: .orange.withAlphaComponent(0.7))
    case .none:
        collidedMaterial.color = .init(tint: .white.withAlphaComponent(0.5))
    }
      
    // Apply collision color
    _modelEntity.model?.materials = [collidedMaterial]
    
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
      if let original = self?.OriginalMaterial {
          self?._modelEntity.model?.materials = [original]
      }
    }
  }
  
  // MARK: - Drag Methods
  
  @objc open func startDrag() {
    _isBeingDragged = true
    _originalMaterial = _modelEntity.model?.materials.first
    
    if #available(iOS 16.0, *) {
        _modelEntity.physicsBody?.isContinuousCollisionDetectionEnabled = false
    }
    
    if var physicsBody = _modelEntity.physicsBody {
      physicsBody.mode = .kinematic
      _modelEntity.physicsBody = physicsBody
    }
    
    print("🎯 \(Name) started being dragged")
  }
  
  @objc open func updateDrag(fingerWorldPosition: SIMD3<Float>) {
      guard _isBeingDragged else { return }
      let constrainedPosition = SIMD3<Float>(
        fingerWorldPosition.x,
        max(fingerWorldPosition.y + DRAG_HEIGHT_OFFSET, ARView3D.SHARED_GROUND_LEVEL + ARView3D.VERTICAL_OFFSET),
        fingerWorldPosition.z
      )
      // Direct position control
      _modelEntity.transform.translation = constrainedPosition
    }

  @objc open func endDrag(releaseVelocity: CGPoint, camera3DProjection: Any) {

    _isBeingDragged = false
    if let original = _originalMaterial {
      _modelEntity.model?.materials = [original]
      _originalMaterial = nil
    }
    
    if #available(iOS 15.0, *) {
      placeOnNearestSurface()
    } else {
      // will follow finger
    }
    
    print("🎯 \(Name) drag ended - override in subclass")
  }
  
  @available(iOS 15.0, *)
  private func placeOnNearestSurface() {
    print("Placing on nearest surface")
    let groundLevel = Float(ARView3D.SHARED_GROUND_LEVEL)
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    let halfHeight = (bounds.max.y - bounds.min.y) / 2
    var correctedY = groundLevel + halfHeight + ARView3D.VERTICAL_OFFSET
    
    if let cachedSurface = getPreviewPlacementSurface() {
        print("Using cached preview surface: \(cachedSurface)")
        
        let correctedSurface = SIMD3<Float>(
            cachedSurface.x,
            correctedY,  // Use corrected Y, not cached surface Y
            cachedSurface.z
        )
        
        print("📍 Corrected surface from \(cachedSurface.y) to \(correctedY)")
        
        _modelEntity.physicsBody = nil
        
        animateToPosition(correctedSurface) {
            self.finalizeModelPlacement()
            self.clearPreviewPlacementSurface()
        }
        return
    }
    
    // Fallback: find surface from current position
    let currentPos = _modelEntity.transform.translation
    if let placementPosition = findNearestHorizontalSurface(from: currentPos) {
      correctedY = placementPosition.y + halfHeight + ARView3D.VERTICAL_OFFSET
      let correctedSurface = SIMD3<Float>(
        placementPosition.x,
        correctedY,  // Use corrected Y, not cached surface Y
        placementPosition.z
      )
      print("Using horizontal surface: \(placementPosition)")
      animateToPosition(correctedSurface) {
          self.finalizeModelPlacement()
      }
    } else {
      // Final fallback to ground level
      let groundPosition = SIMD3<Float>(currentPos.x, correctedY, currentPos.z)
      print("Using groundPosition: \(groundPosition)")
      animateToPosition(groundPosition) {
          self.finalizeModelPlacement()
      }
    }
  }

  private func finalizeModelPlacement() {
    print("Finalizing node placement")

    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
        self?.EnablePhysics(true)
    }
    
    if let container = _container {
      container.hidePlacementPreview()
    }
    
    // Clear local preview data
    _previewPlacementSurface = nil
    
    print("node placement complete at: \(_modelEntity.transform.translation)")
  }
  

  /// Animates the node to a target position smoothly
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

  private func findNearestHorizontalSurface(from position: SIMD3<Float>) -> SIMD3<Float>? {
    guard let container = _container else { return nil }
    print("best surface for node dragged from \(position)")
    return container.getARView().findBestSurfaceForPlacement()
  }
  
  

} // end ARNodeBase class

// MARK: - ARNodeBase Extension

@available(iOS 14.0, *)
extension ARNodeBase {
  
  private func updatePhysicsMaterial() {
      guard var physicsBody = _modelEntity.physicsBody else { return }
      
      let newMaterial = PhysicsMaterialResource.generate(
          staticFriction: StaticFriction,
          dynamicFriction: DynamicFriction,
          restitution: Restitution
      )
      
      physicsBody.material = newMaterial
      print("🎾 Updated physics material: friction(\(StaticFriction), \(DynamicFriction)), bounce(\(Restitution))")
  }

  
  private func updateMassProperties() {
      guard var physicsBody = _modelEntity.physicsBody else { return }
      
      physicsBody.massProperties = PhysicsMassProperties(mass: Mass)
      print("🎾 Updated mass to: \(Mass)")
  }
  
  //assume sphere shape. can be overriden to be more accurate
  private func updateCollisionShape() {
      let shape: ShapeResource
      let bounds = _modelEntity.visualBounds(relativeTo: nil)
      let autoSize = bounds.max - bounds.min
      let safeSize = SIMD3<Float>(
          max(autoSize.x, 0.05) + 0.001,
          max(autoSize.y, 0.05) + 0.001,
          max(autoSize.z, 0.05) + 0.001
      )
    
    shape = ShapeResource.generateBox(size: safeSize)
    _modelEntity.collision = CollisionComponent(shapes: [shape])
      
    // Update physics body if it exists
    if _modelEntity.physicsBody != nil {
        // Update mass based on new volume (realistic)
        let volumeScale = Scale
        let newMass = Mass * volumeScale
        
        let material = PhysicsMaterialResource.generate(
            staticFriction: StaticFriction,
            dynamicFriction: DynamicFriction,
            restitution: Restitution
        )
        
        _modelEntity.physicsBody = PhysicsBodyComponent(
            massProperties: PhysicsMassProperties(mass: newMass),
            material: material,
            mode: .dynamic
        )
        
        print("🎾 Collision updated: mass=\(newMass)")
    }
  }


  
  public func debugVisualState() {
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
  

  @objc open func handleAdvancedGestureUpdate(
    fingerLocation: CGPoint,
    fingerMovement: CGPoint,
    fingerVelocity: CGPoint,
    groundProjection: Any?,
    camera3DProjection: Any?,
    gesturePhase: UIGestureRecognizer.State
  ) {
    var groundPos: SIMD3<Float>? = groundProjection as? SIMD3<Float>

    switch gesturePhase {
    case .began:
      startDrag()
        
    case .changed:
      if let worldPos = groundPos {
        updateDrag(fingerWorldPosition:worldPos)
      } else {
        print("⚠️ No groundProjection available during drag")
      }
        
    case .ended, .cancelled:
      endDrag(releaseVelocity: fingerVelocity, camera3DProjection: camera3DProjection!)
        
    default:
        break
    }
  }
  
  // Add these methods to ModelNode class
  @objc public func setPreviewPlacementSurface(_ surface: SIMD3<Float>) {
      _previewPlacementSurface = surface
      _hasPreviewSurface = true
  }

  @objc public func clearPreviewPlacementSurface() {
      _previewPlacementSurface = nil
      _hasPreviewSurface = false
  }

  public func getPreviewPlacementSurface() -> SIMD3<Float>? {
      return _previewPlacementSurface
  }

  public func hasPreviewSurface() -> Bool {
      return _hasPreviewSurface
  }
  
  /// Updates shadow casting and receiving for the sphere
  public func updateShadowSettings() {
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
  
  @available(iOS 15.0, *)
  private func enableShadows() {
      // Enable shadow casting
    if #available(iOS 18.0, *) {
      _modelEntity.components.set(GroundingShadowComponent(castsShadow: true))
    }
    print("🌘 Shadows enabled \(Name)")
  }

  @available(iOS 15.0, *)
  private func disableShadows() {
      // Disable shadow casting
    if #available(iOS 18.0, *) {
      _modelEntity.components.set(GroundingShadowComponent(castsShadow: false))
    }
    print("☀️ Shadows disabled\(Name)")
  }

}
