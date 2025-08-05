// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import ARKit
import GLKit

import os.log


// MARK: - Extensions (properly placed outside the class)


// Remove any forward declaration and extend the class properly
@available(iOS 14.0, *)
open class ARNodeBase: NSObject, ARNode {

  weak var _container: ARNodeContainer?

  public var _modelEntity: ModelEntity
  
  private var _color: Int32 = Int32(bitPattern: AIComponentKit.Color.red.rawValue)
  
  public var hasTexture = false
  public var _texture: String = ""
  
  private var _enablePhysics: Bool = false
  private var _pinchToScale: Bool = false
  private var _panToMove: Bool = false
  private var _rotateWithGesture: Bool = false
  

  private var _isBeingDragged = false
  private var _dragStartLocation: CGPoint = .zero
  private var _lastDragLocation: CGPoint = .zero
  private var _originalMaterial: Material?
  
  private var _gravityScale = Float(0.0)
  private var _dragSensitivity = Float(0.0)
  private var _releaseForceMultiplier = Float(0.0)
  
  private var _currentVelocity: SIMD3<Float> = SIMD3<Float>(0, 0, 0)
  private var _momentumTask: Task<Void, Never>?
  private var _isCurrentlyColliding = false
  private var _collisionEffectTask: Task<Void, Never>?
  

  private var _linearDamping  = Float(0.0)
  private var _angularDamping  = Float(0.0)
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
  /**
   * CHANGED: Now takes optional MeshResource instead of SCNNode
   */
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
  
  /// This is necessary for creating nodes via the blocks
  open func syncInitialize() {
    self._container?.addNode(self)
  }
  
  /**
   * CHANGED: Simplified init for ModelNodes
   */
  @objc init(modelNodeContainer: ARNodeContainer) {
    _container = modelNodeContainer
    _modelEntity = ModelEntity() // Will be replaced when model loads
    super.init()
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  // MARK: - Anchor Management
  
  // Override the protocol extension to provide actual storage
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
      _geoAnchor = geoAnchor  // Much simpler!
  }

  func getGeoAnchor() -> ARGeoAnchor? {
      return _geoAnchor
  }
  
  @objc open var IsGeoAnchored: Bool {
      return getGeoAnchor() != nil
  }
  
  // Get GPS coordinates if geo anchored
  @objc open var GeoCoordinates: [Double] {
      guard let geoAnchor = getGeoAnchor() else {
          return []
      }
    return [geoAnchor.coordinate.latitude, geoAnchor.coordinate.longitude, Double(geoAnchor.altitude!)]
  }
  
  
  @objc open var Name: String {
    get {
      return _modelEntity.name
    }
    set(name) {
      _modelEntity.name = name
    }
  }
  
  @objc open var NodeType: String {
    get {
      return String(describing: type(of: self))
    }
  }
  
  @objc open var Model: String {
    get {
      return _objectModel
    }
    set(modelStr) {
      _objectModel = modelStr
    }
  }
  
  /*open var Model: ModelEntity {
    get {
      return _modelEntity
    }
    set(model) {
      _modelEntity = model
    }
  }*/
  
  // CHANGED: All position properties now work with _modelEntity.transform.translation
  @objc open var XPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(_modelEntity.transform.translation.x)
    }
    set(x) {
      _modelEntity.transform.translation.x = UnitHelper.centimetersToMeters(x)
    }
  }
  
  @objc open var YPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(_modelEntity.transform.translation.y)
    }
    set(y) {
      _modelEntity.transform.translation.y = UnitHelper.centimetersToMeters(y)
    }
  }
  
  @objc open var ZPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(_modelEntity.transform.translation.z)
    }
    set(z) {
      _modelEntity.transform.translation.z = UnitHelper.centimetersToMeters(z)
    }
  }
  
  // CHANGED: Rotation properties need manual quaternion/Euler conversion
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
    get {
      return _objectModel
    }
    set(model) {
      _objectModel = model
    }
  }
  
  @objc open var Scale: Float {
    get {
      return _modelEntity.transform.scale.x
    }
    set(scalar) {
      let scale = abs(scalar)
      _modelEntity.transform.scale = SIMD3<Float>(scale, scale, scale)
    }
  }
  
  @objc open var PoseFromPropertyPosition: String {
    get {
      return _fromPropertyPosition
    }
    set(pose) {
      _fromPropertyPosition = pose
    }
  }
  
  @objc open func ARNodeToYail() -> YailDictionary {
      os_log("going to try to export ARNode as yail", log: .default, type: .info)
         
      var yailDict: YailDictionary = [:]
      var transformDict: YailDictionary = PoseToYailDictionary() ?? [:]
      //var coordDict: YailDictionary = CoordinatesToYailDictionary() ?? [:]
        do {
          yailDict["model"] = self.ModelUrl // dont  need this unless it's a custom model
          //yailDict["creatorSessionStart"] = coordDict // dont' need, in transformDict
          yailDict["texture"] = self.Texture
          yailDict["scale"] = self.Scale
          yailDict["pose"] = transformDict
          yailDict["type"] = self.Name // this enables us to create the model mesh
         
        
          print("exporting ARNode as Yail convert toYail ")
          return yailDict
         }
         
     }
     
  //csb not currently using but may
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
        
        // Translation components
        translationDict["x"] = p.translation.x
        translationDict["y"] = p.translation.y
        translationDict["z"] = p.translation.z
        yailDictSave["t"] = translationDict
        
        // Rotation components (quaternion)
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
  

  // CHANGED: FillColor now uses RealityKit SimpleMaterial
  @objc open var FillColor: Int32 {
    get {
      return _color
    }
    set(color) {
      _color = color
      updateMaterial()
    }
  }
  
  @objc @available(iOS 14.0, *)
  open var FillColorOpacity: Int32 {
    get {
      if let material = _modelEntity.model?.materials.first as? SimpleMaterial {
        // CSB HOLD return Int32(_modelEntity.fill * 100)
      }
      return 100
    }
    set(opacity) {
      let alpha = Float(min(max(0, opacity), 100)) / 100.0
      if #available(iOS 15.0, *) {
        updateMaterialOpacity(alpha)
      } else {
        // Fallback on earlier versions
      }
    }
  }
  
  // CHANGED: Texture now uses RealityKit TextureResource
  @objc open var Texture: String {
    get {
      return _texture
    }
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
    get {
      return FillColorOpacity
    }
    set(opacity) {
      FillColorOpacity = opacity
    }
  }
  
  @objc open var Visible: Bool {
    get {
      return _modelEntity.isEnabled
    }
    set(visible) {
      _modelEntity.isEnabled = visible
    }
  }
  @objc open var ShowShadow: Bool {
    get {
      return false
    }
    set(showShadow) {
     // _node.castsShadow = showShadow
    }
  }
  // CHANGED: ShowShadow now uses GroundingShadowComponent
 /* @objc open var ShowShadow: Bool {
    get {
      return _modelEntity.components.shadow.map(\.castsShadow) ?? false
    }
    set(showShadow) {
      if showShadow {
        _modelEntity.components.set(GroundingShadowComponent(castsShadow: true))
      } else {
        _modelEntity.components.remove(GroundingShadowComponent.self)
      }
    }
  }*/
  
  @objc open var Opacity: Int32 {
    get {
      return FillColorOpacity
    }
    set(opacity) {
      FillColorOpacity = opacity
    }
  }
  
  @objc open var EnablePhysics: Bool {
    get {
      return _enablePhysics
    }
    set(enablePhysics){
      EnablePhysics(enablePhysics)
    }
  }
  
  @objc open var PinchToScale: Bool {
    get {
      return _pinchToScale
    }
    set(pinchToScale) {
      _pinchToScale = pinchToScale
    }
  }
  
  @objc open var PanToMove: Bool {
    get {
      return _panToMove
    }
    set(panToMove) {
      _panToMove = panToMove
    }
  }
  
  @objc open var RotateWithGesture: Bool {
    get {
      return _rotateWithGesture
    }
    set(rotate) {
      _rotateWithGesture = rotate
    }
  }
  
  @objc open var CollisionShape: String = "sphere" {
    didSet { updateCollisionShape()}
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
    
  
  
  @objc open var IsFollowingImageMarker: Bool {
    get {
      return _followingMarker != nil
    }
  }
  
  // MARK: - Component Protocol Implementation
  @objc open var Width: Int32 {
    get {
      return 0
    }
    set {}
  }
  
  @objc open var Height: Int32 {
    get {
      return 0
    }
    set {}
  }
  
  @objc open var dispatchDelegate: HandlesEventDispatching? {
    get {
      return _container?.form?.dispatchDelegate
    }
  }
  
  public func copy(with zone: NSZone? = nil) -> Any {
    return self // Return self for NSCopying protocol
  }
  
  public func setWidthPercent(_ toPercent: Int32) {}
  public func setHeightPercent(_ toPercent: Int32) {}
  
  // MARK: Methods - CHANGED to work with RealityKit transforms
  
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
  
  // CHANGED: Return SIMD3<Float> for pure RealityKit
  open func getPosition() -> SIMD3<Float> {
    return _modelEntity.transform.translation
  }
  
  open func setPosition(x: Float, y: Float, z: Float) {
    _modelEntity.transform.translation = SIMD3<Float>(x, y, z)
  }
  
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
    }
  }
  
  // CHANGED: RealityKit material management
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
  
  // MARK: - RealityKit Anchor Management
  
  func createAnchor() -> AnchorEntity {
    
    if let geoAnchor = getGeoAnchor() {
        if let existingAnchor = _anchorEntity {
            return existingAnchor
        }
        
        //create a placeholder if we don't have it, will update it after we start tracking
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
      // Default world anchor
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
    
    let anchor: AnchorEntity
    
    //if pose != nil{
      anchor = AnchorEntity(components: pose)
    /*} else {
      // Default world anchor
      let worldTransform = _modelEntity.transformMatrix(relativeTo: nil)
      anchor = AnchorEntity(world: worldTransform)
    }*/
    
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
  
  // MARK: - Quaternion/Euler Conversion (NEW - RealityKit needs this)
  
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
  
  @objc open func Click() {
    EventDispatcher.dispatchEvent(of: self, called: "Click")
  }
  
  @objc open func LongClick() {
    EventDispatcher.dispatchEvent(of: self, called: "LongClick")
  }
  
  
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



  @objc open func EnablePhysics(_ isDynamic: Bool = true) {
    
    if (!isDynamic){
      _enablePhysics = false
      return
    }
    let bounds = _modelEntity.visualBounds(relativeTo: nil)
    let size = bounds.max - bounds.min
    
    // Ensure minimum size and make slightly larger for stability
    let safeSize = SIMD3<Float>(
        max(size.x, 0.05) * 1.1,  // 10% larger for stability
        max(size.y, 0.05) * 1.1,
        max(size.z, 0.05) * 1.1
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
      staticFriction: StaticFriction,    // Higher friction prevents sliding
      dynamicFriction: DynamicFriction,   // Friction when moving
      restitution: Restitution       // Low restitution = less bouncy (0.0 to 1.0)
    )
    
    _enablePhysics = isDynamic
    _modelEntity.collision = CollisionComponent(shapes: [shape])
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
  
  @objc open func CollisionDetected() {}
  @objc open func ObjectCollidedWithScene(_ node: ARNodeBase) {
    EventDispatcher.dispatchEvent(of: self, called: "ObjectCollidedWithScene", arguments: node)
  }
  @objc open func ObjectCollidedWithObject(_ node: ARNodeBase, _ node2: ARNodeBase) {
    EventDispatcher.dispatchEvent(of: self, called: "ObjectCollidedWithObject", arguments: node, node2)
  }
  



} // end ARNodeBase base class

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
      
      private func updateDamping() {
          // Note: RealityKit might not expose damping directly in iOS 16
          // This is a placeholder for when it's available
          print("🎾 Damping updated: linear(\(_linearDamping)), angular(\(_angularDamping))")
          
          // For iOS 16, you might need to simulate damping manually
        if _linearDamping > 0 || _angularDamping > 0 {
              startDampingSimulation()
          }
      }

      private func startDampingSimulation() {
          _dampingTask.cancel()
          
          guard _linearDamping > 0 || _angularDamping > 0 else { return }
          
          _dampingTask = Task {
              while !Task.isCancelled {
                  await applyCustomDamping()
                  try? await Task.sleep(nanoseconds: 33_000_000) // ~30fps
              }
          }
      }
      
      private func applyCustomDamping() async {
          await MainActor.run {
              guard var physicsBody = _modelEntity.physicsBody else { return }
              
              // Since we can't directly access velocity in iOS 16,
              // we simulate damping by slightly adjusting physics properties
              
              // This is a simplified approach - real damping would need velocity access
           /* if !physicsBody.is {
                  // Gradually increase friction to simulate damping
                  let currentMaterial = physicsBody.material
                  
                  // This is a conceptual approach - actual implementation would depend
                  // on available RealityKit APIs in iOS 16
                  print("🎾 Applying custom damping...")
              }*/
          }
      }
      
      @objc func stopDamping() {
          _dampingTask.cancel()
        _dampingTask = Task{}
      }
  
  private func updateCollisionShape() {
      let shape: ShapeResource
      let bounds = _modelEntity.visualBounds(relativeTo: nil)
      let autoSize = bounds.max - bounds.min
      let safeSize = SIMD3<Float>(
          max(autoSize.x, 0.05) * 1.1,
          max(autoSize.y, 0.05) * 1.1,
          max(autoSize.z, 0.05) * 1.1
      )
      shape = generateCollisionShape(size: safeSize)
      _modelEntity.collision = CollisionComponent(shapes: [shape])
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
  
  
 
// Rolling/Movement Properties
    @objc open var RollingForce: Float {
        get { return _rollingForce }
        set { _rollingForce = newValue }
    }
    
    @objc open var ImpulseScale: Float {
        get { return _impulseScale }
        set { _impulseScale = newValue }
    }

      
      // Customizable physics parameters
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
      
 
      
      // Track drag state at the node level
      @objc open var isBeingDragged: Bool {
          get { return _isBeingDragged }
          set (newValue) { _isBeingDragged = newValue }
      }
      
      private var LastDragLocation: CGPoint {
          get { return _lastDragLocation}
          set (newValue){ _lastDragLocation = newValue}
      }
      
      public var OriginalMaterial: Material? {
          get { return _originalMaterial }
          set(newValue) { _originalMaterial = newValue}
      }
      

      
      // Base drag methods with state management
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
 
      // Handle collision with another AR node (we know it's a node)
      @objc open func handleNodeCollision(with otherNode: ARNodeBase, event: Any) {
          print("🔥 \(Name) collision with AR node: \(otherNode.Name)")
          
          // Dispatch existing event for backward compatibility
          EventDispatcher.dispatchEvent(of: self, called: "ObjectCollidedWithObject", arguments: self as AnyObject, otherNode as AnyObject)
          
          // Call overrideable method for custom node collision behavior
        respondToNodeCollision(with: otherNode, event: event as! CollisionEvents.Began)
      }
      
      // Handle collision with scene element (we know what type of scene element)
      @objc open func handleSceneCollision(sceneEntity: Any, sceneType: Any, event: Any) {
        print("🔥 \(Name) collision with \(sceneType): \(sceneEntity as! Entity) (\(String(describing: (sceneEntity as! Entity).name)).name)")
          
          // Dispatch existing event for backward compatibility
          EventDispatcher.dispatchEvent(of: self, called: "ObjectCollidedWithScene", arguments: self as AnyObject)
          
          // Call overrideable method for custom scene collision behavior
        respondToSceneCollision(sceneEntity: sceneEntity as! Entity, sceneType: sceneType as! ARView3D.SceneEntityType, event: event)
      }
      
      // MARK: - Overrideable Methods for Subclasses
      
      // Override this in subclasses for custom node collision behavior
      @objc open func respondToNodeCollision(with otherNode: ARNodeBase, event: Any) {
          print("🔥 \(Name) base node collision response with \(otherNode.Name) - override in subclass")
        if #available(iOS 15.0, *) {
          showCollisionEffect(type: .object)
        } else {
          // Fallback on earlier versions
        }
      }
      
      // Override this in subclasses for custom scene collision behavior
      @objc open func respondToSceneCollision(sceneEntity: Any, sceneType: Any, event: Any) {
          print("🔥 \(Name) base scene collision response with \(sceneType) - override in subclass")
          
          let collisionType: CollisionType
        switch sceneType as! ARView3D.SceneEntityType {
          case .floor:
              collisionType = .floor
          case .wall, .ceiling, .furniture:
              collisionType = .wall
          case .unknown:
              collisionType = .wall
          }
          
        if #available(iOS 15.0, *) {
          showCollisionEffect(type: collisionType)
        } else {
          // Fallback on earlier versions
        }
      }
      
      enum CollisionType {
          case floor
          case wall
          case object
          case none
      }
      
  @available(iOS 15.0, *)
  private func showCollisionEffect(type: CollisionType) {
          Task {
              await MainActor.run {
                  let originalMaterial = _modelEntity.model?.materials.first
                  
                  var collisionMaterial = SimpleMaterial()
                  switch type {
                  case .floor:
                      collisionMaterial.color = .init(tint: .brown.withAlphaComponent(0.6))
                  case .wall:
                      collisionMaterial.color = .init(tint: .red.withAlphaComponent(0.5))
                  case .object:
                      collisionMaterial.color = .init(tint: .orange.withAlphaComponent(0.5))
                  case .none:
                      return
                  }
                  
                  _modelEntity.model?.materials = [collisionMaterial]
                  
                  Task {
                      try? await Task.sleep(nanoseconds: 200_000_000)
                      await MainActor.run {
                          if let original = originalMaterial {
                              _modelEntity.model?.materials = [original]
                          }
                      }
                  }
              }
          }
    }
  
      
  @available(iOS 15.0, *)
  private func showCollisionEffect(type: ARView3D.SceneEntityType) {
          Task {
              await MainActor.run {
                  let originalMaterial = _modelEntity.model?.materials.first
                  
                  var collisionMaterial = SimpleMaterial()
                  switch type {
                  case .floor:
                      collisionMaterial.color = .init(tint: .brown.withAlphaComponent(0.6))
                  case .wall:
                      collisionMaterial.color = .init(tint: .red.withAlphaComponent(0.5))
                  case .furniture, .unknown:
                      collisionMaterial.color = .init(tint: .orange.withAlphaComponent(0.5))
                  default:
                      return
                  }
                  
                  _modelEntity.model?.materials = [collisionMaterial]
                  
                  Task {
                      try? await Task.sleep(nanoseconds: 200_000_000)
                      await MainActor.run {
                          if let original = originalMaterial {
                              _modelEntity.model?.materials = [original]
                          }
                      }
                  }
              }
          }
      }
  
}
