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
    get { return false }
    set(showShadow) { }
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
  
  @objc open var CollisionShape: String = "sphere" {
    didSet { updateCollisionShape() }
  }
  
  @objc open var IsFollowingImageMarker: Bool {
    get { return _followingMarker != nil }
  }
  
  // MARK: - Drag Properties
  
  @objc open var isBeingDragged: Bool {
      get { return _isBeingDragged }
      set(newValue) { _isBeingDragged = newValue }
  }
  
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
  
  @objc open func ScaleBy(_ scalar: Float) {
    _modelEntity.transform.scale *= abs(scalar)
    updateCollisionShape()
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
  
  open func scaleByPinch(scalar: Float) {
    if PinchToScale {
      ScaleBy(scalar)
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
  
  @objc open func EnablePhysics(_ isDynamic: Bool = true) {
    if (!isDynamic){
      _enablePhysics = false
      return
    }
    
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    let size = bounds.max - bounds.min
    
    // Ensure minimum size and make slightly larger for stability
    let safeSize = SIMD3<Float>(
        max(size.x, 0.05) * 1.001,
        max(size.y, 0.05) * 1.001,
        max(size.z, 0.05) * 1.001
    )
    
    // For round objects, use sphere collision (more stable)
    let shape: ShapeResource
    if abs(safeSize.x - safeSize.y) < 0.01 && abs(safeSize.y - safeSize.z) < 0.01 {
        // Nearly cubic - use sphere for stability
        let radius = (safeSize.x + safeSize.y + safeSize.z) / 6.0
        shape = ShapeResource.generateSphere(radius: radius)
    } else {
        shape = ShapeResource.generateBox(size: safeSize)
    }
    
    _enablePhysics = isDynamic
    _modelEntity.collision = CollisionComponent(shapes: [shape])
    
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
  }

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
          collidedMaterial.color = .init(tint: .red.withAlphaComponent(0.5))
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
            print("🎨 Collision effect restored")
        }
    }
     
  }
  

  
  // MARK: - Drag Methods
  
  @objc open func startDrag() {
      isBeingDragged = true
      _originalMaterial = _modelEntity.model?.materials.first
      print("🎯 \(Name) started being dragged")
  }
  
  @objc open func updateDrag(dragVector: CGPoint, velocity: CGPoint, worldDirection: SIMD3<Float>) {
      // Override in subclasses
      print("🎯 \(Name) drag update - override in subclass")
  }
  
  @objc open func endDrag(releaseVelocity: CGPoint, worldDirection: SIMD3<Float>) {
      isBeingDragged = false
      if let original = _originalMaterial {
          _modelEntity.model?.materials = [original]
          _originalMaterial = nil
      }
      print("🎯 \(Name) drag ended - override in subclass")
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
  
  private func updateCollisionShape() {
      let shape: ShapeResource
      let bounds = _modelEntity.visualBounds(relativeTo: nil)
      let autoSize = bounds.max - bounds.min
      let safeSize = SIMD3<Float>(
          max(autoSize.x, 0.05) + 0.001,
          max(autoSize.y, 0.05) + 0.001,
          max(autoSize.z, 0.05) + 0.001
      )
    
    let exactRadius = (bounds.max.x - bounds.min.x) / 2.0 * Scale
    shape = ShapeResource.generateSphere(radius: exactRadius)
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

  private func generateCollisionShape(size: SIMD3<Float>) -> ShapeResource {
      // Determine best collision shape based on object type and proportions
      let avgSize = (size.x + size.y + size.z) / 3.0
      let variance = max(abs(size.x - avgSize), abs(size.y - avgSize), abs(size.z - avgSize))
      
      if variance < 0.02 {
          let radius = avgSize / 2.0
          return ShapeResource.generateSphere(radius: radius)
      } else if abs(size.x - size.z) < 0.02 && size.y > size.x * 1.5 {
          let radius = min(size.x, size.z) / 2.0
          return ShapeResource.generateCapsule(height: size.y, radius: radius)
      } else {
          // Use box for everything else
          return ShapeResource.generateBox(size: size)
      }
  }

  @objc open func handleAdvancedGestureUpdate(
    fingerLocation: CGPoint,
    fingerMovement: CGPoint,
    fingerVelocity: CGPoint,
    groundProjection: Any?,
    camera3DProjection: Any?,
    gesturePhase: UIGestureRecognizer.State
  ) {}
  
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
}
