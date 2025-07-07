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
  
  private var _pinchToScale: Bool = false
  private var _panToMove: Bool = false
  private var _rotateWithGesture: Bool = false
  
  public var _anchorEntity: AnchorEntity?
  public var _followingMarker: ARImageMarker? = nil
  public var _fromPropertyPosition = "0.0,0.0,0.0"
  public var _objectModel: String = ""

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
  
  
  
  @objc open var Name: String {
    get {
      return _modelEntity.name ?? ""
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
  
  open var Model: ModelEntity {
    get {
      return _modelEntity
    }
    set(model) {
      os_log("setting model", log: .default, type: .info)
      _modelEntity = model
    }
  }
  
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
         
        do {
          yailDict["model"] = self.Model
          yailDict["texture"] = self.Texture
          yailDict["scale"] = self.Scale
          yailDict["pose"] = self._modelEntity.transform
          yailDict["type"] = self.Name
         
        
          os_log("exporting ARNode as Yail convert toYail %@", log: .default, type: .info, String(describing: yailDict))
          return yailDict
         } catch {
           os_log("failed to export as yail: %@", log: .default, type: .error, error.localizedDescription)
         }
         
         return [:]
     }
     
  
  @objc open func PoseToYailDictionary() -> YailDictionary? {
        os_log("anchor pose as YailDict", log: .default, type: .info)
        
      guard let p = self._anchorEntity else {
            os_log("pose is nil", log: .default, type: .info)
            return nil
        }
        
        os_log("pose is %@", log: .default, type: .info, String(describing: p))
        
        var translationDict: YailDictionary = [:]
        var rotationDict: YailDictionary = [:]
        var yailDictSave: YailDictionary = [:]
        
        // Translation components
        translationDict["x"] = p.position.x
        translationDict["y"] = p.position.y
        translationDict["z"] = p.position.z
        yailDictSave["t"] = translationDict
        
        // Rotation components (quaternion)
        rotationDict["x"] = p.transform.rotation.vector.x
        rotationDict["y"] = p.transform.rotation.vector.y
        rotationDict["z"] = p.transform.rotation.vector.z
        rotationDict["w"] = p.transform.rotation.vector.w
        yailDictSave["q"] = rotationDict
        
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
    
    if pose != nil{
      anchor = AnchorEntity(components: pose)
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


  
} // ← This closes the ARNodeBase class
