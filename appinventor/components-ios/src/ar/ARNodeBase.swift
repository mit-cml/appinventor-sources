// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit
import GLKit

@available(iOS 11.3, *)
open class ARNodeBase: NSObject, ARNode {
  weak var _container: ARNodeContainer?
  public var _node: SCNNode
  private var _color: Int32 = Int32(bitPattern: AIComponentKit.Color.red.rawValue)
  private var _colorMaterial: SCNMaterial = SCNMaterial()
  public var hasTexture = false
  private var _textureMaterial: SCNMaterial = SCNMaterial()
  private var _texture: String = ""
  private var _pinchToScale: Bool = false
  private var _panToMove: Bool = false
  private var _rotateWithGesture: Bool = false
  public var _followingMarker: ARImageMarker? = nil
  public var _fromPropertyPosition = "0.0,0.0,0.0";
  
  private var _modelString: String = ""
  private var _objectModel:String = ""
  /**
   * This init is used for all nodes except for ModelNodes.
   */
  @objc init(container: ARNodeContainer, node: SCNNode) {
    _container = container
    _node = node
    _textureMaterial.isDoubleSided = true
    _colorMaterial.isDoubleSided = true
    _colorMaterial.diffuse.contents = argbToColor(_color)
    super.init()
    setMaterials()
    XPosition = 0
    YPosition = 0
    ZPosition = -1
  }

  @objc open func Initialize() {
    self._container?.addNode(self)
  }

  /// This is necessary for creating nodes via the blocks
  open func syncInitialize() {
    self._container?.addNode(self)
  }
  
  /**
   * This init should only be used for a ModelNode.
   *
   * This init exists because when we make a ModelNode, we don't want to add the
   * dummy node that we use for init.  (We can't create the referenceNode until after
   * the ARNode has been initialized.  Similarly, FillColor and Texture should not be set
   * on the node.  So, this is essentially a simplified init.
   */
  @objc init(modelNodeContainer: ARNodeContainer) {
    _container = modelNodeContainer
    /// This is a dummy node that will be replaced when the ModelNode has actually loaded
    _node = SCNNode()
    super.init()
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc open var Name: String {
    get {
      return _node.name ?? ""
    }
    set(name) {
      _node.name = name
    }
  }
  
  @objc open var `Type`: String {
    get {
      return String(describing: type(of: self))
    }
  }
  
  @objc open var XPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(_node.simdPosition.x)
    }
    set(x) {
      _node.simdPosition.x = UnitHelper.centimetersToMeters(x)
    }
  }
  
  @objc open var YPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(_node.simdPosition.y)
    }
    set(y) {
      _node.simdPosition.y = UnitHelper.centimetersToMeters(y)
    }
  }
  
  @objc open var ZPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(_node.simdPosition.z)
    }
    set(z) {
      _node.simdPosition.z = UnitHelper.centimetersToMeters(z)
    }
  }
  
  @objc open var XRotation: Float {
    get {
      return GLKMathRadiansToDegrees(_node.simdEulerAngles.x)
    }
    set(degrees) {
      _node.simdEulerAngles.x = GLKMathDegreesToRadians(degrees)
    }
  }
  
  @objc open var YRotation: Float {
    get {
      return GLKMathRadiansToDegrees(_node.simdEulerAngles.y)
    }
    set(degrees) {
      _node.simdEulerAngles.y = GLKMathDegreesToRadians(degrees)
    }
  }
  
  @objc open var ZRotation: Float {
    get {
      return GLKMathRadiansToDegrees(_node.simdEulerAngles.z)
    }
    set(degrees) {
      _node.simdEulerAngles.z = GLKMathDegreesToRadians(degrees)
    }
  }
  
  @objc open var Model: String {
    get {
      return _objectModel
    }
    set(model) {
      _objectModel = model
    }
  }
  
  @objc open var Scale: Float {
    get {
      return _node.scale.x
    }
    set(scalar) {
      _node.simdScale = simd_float3(abs(scalar), abs(scalar), abs(scalar))
    }
  }
  
  
  @objc open var PoseFromPropertyPosition: String {
    get {
      return _fromPropertyPosition;
    }
    set(pose) {
      // turn pose into array and setX, y, z
      //let coordinateArray = pose.split(separator: ",");
      //var position = "1.0,0.0,0.0";
     /* for (index, item) in pose.enumerated() {
              if index < position.count {
                  position[index] = Float(String(item)) ?? 0.0
              }
      }*/
          
          // Store the coordinate array (converted to proper type)
      _fromPropertyPosition = pose
     
    }
  }
  /// NOTE: uncomment if we want to allow nonuniform scaling
//  @objc open var XScale: Float {
//    get {
//      return _node.scale.x
//    }
//  }
//
//  @objc open var YScale: Float {
//    get {
//      return _node.scale.y
//    }
//  }
//
//  @objc open var ZScale: Float {
//    get {
//      return _node.scale.z
//    }
//  }
  
  @objc open var FillColor: Int32 {
    get {
      return _color
    }
    set(color) {
      _color = color
      /**
       * When the color is set to none, we make the node occulde other items if they are behind it.
       * In order to do this, we just change the colorBufferMask but don't actually set the fill
       * color to none.
       */
      if color == AIComponentKit.Color.none.rawValue {
        _colorMaterial.colorBufferWriteMask = []
      } else {
        _colorMaterial.colorBufferWriteMask = .all
        _colorMaterial.diffuse.contents = argbToColor(color)
      }
    }
  }
  
  @objc open var FillColorOpacity: Int32 {
    get {
      return Int32(round(_colorMaterial.transparency * 100))
    }
    set(opacity) {
      let floatOpacity = Float(min(max(0, opacity), 100)) / 100.0
      _colorMaterial.transparency = CGFloat(floatOpacity)
    }
  }
  
  /*
   * A Texture is an image that is used to cover the node
   */
  @objc open var Texture: String {
    get {
      return _texture
    }
    set(path) {
      if let image = AssetManager.shared.imageFromPath(path: path) {
        _texture = path
        _textureMaterial.diffuse.contents = image
        hasTexture = true
        setMaterials()
      } else {
        if !path.isEmpty {
          _container?.form?.dispatchErrorOccurredEvent(self, "Texture", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code)
        }
        hasTexture = false
        _texture = ""
        setMaterials()
      }
    }
  }
  
  @objc open var TextureOpacity: Int32 {
    get {
      return Int32(round(_textureMaterial.transparency * 100))
    }
    set(opacity) {
      let floatOpacity = Float(min(max(0, opacity), 100)) / 100.0
      _textureMaterial.transparency = CGFloat(floatOpacity)
    }
  }
  
  @objc open var Visible: Bool {
    get {
      return !_node.isHidden
    }
    set(visible) {
      _node.isHidden = !visible
    }
  }
  
  @objc open var ShowShadow: Bool {
    get {
      return _node.castsShadow
    }
    set(showShadow) {
      _node.castsShadow = showShadow
    }
  }
  
  @objc open var Opacity: Int32 {
    get {
      return Int32(round(_node.opacity * 100))
    }
    set(opacity) {
      let floatOpacity = Float(min(max(0, opacity), 100)) / 100.0
      _node.opacity = CGFloat(floatOpacity)
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
  
  
  // MARK: Methods
  
  @objc open func RotateXBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    _node.simdEulerAngles.x += radians
  }
  
  @objc open func RotateYBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    _node.simdEulerAngles.y += radians
  }
  
  @objc open func RotateZBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    _node.simdEulerAngles.z += radians
  }
  
  @objc open func ScaleBy(_ scalar: Float) {
    _node.simdScale *= abs(scalar)
  }
  
  @objc open func MoveBy(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    _node.simdPosition += simd_float3(xMeters, yMeters, zMeters)
  }
  
  @objc open func MoveTo(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    _node.simdPosition = simd_float3(xMeters, yMeters, zMeters)
  }

  
  @objc open func DistanceToNode(_ node: ARNode) -> Float {
    return UnitHelper.metersToCentimeters(getPosition().distanceFromPos(pos: node.getPosition()))
  }
  
  @objc open func DistanceToSpotlight(_ light: ARSpotlight) -> Float {
    return UnitHelper.metersToCentimeters(getPosition().distanceFromPos(pos: light.getPosition()))
  }
  
  @objc open func DistanceToPointLight(_ light: ARPointLight) -> Float {
    return UnitHelper.metersToCentimeters(getPosition().distanceFromPos(pos: light.getPosition()))
  }
  
  @objc open func DistanceToDetectedPlane(_ detectedPlane: ARDetectedPlane) -> Float {
    return UnitHelper.metersToCentimeters(getPosition().distanceFromPos(pos: detectedPlane.getPosition()))
  }
  
  /**
   * Returns the position in meters
   */
  open func getPosition() -> SCNVector3 {
    return _node.position
  }
  
  /**
   * x, y, z should be provided as meters
   */
  open func setPosition(x: Float, y: Float, z: Float) {
    _node.simdPosition = simd_float3(x, y, z)
  }
  
  open func scaleByPinch(scalar: Float) {
    if PinchToScale {
      ScaleBy(scalar)
    }
  }
  
  open func moveByPan(x: Float, y: Float) {
    if PanToMove {
      _node.simdPosition += simd_float3(x, y, 0.0)
    }
  }
  
  open func rotateByGesture(radians: Float) {
    if RotateWithGesture {
      _node.simdEulerAngles.y = radians
    }
  }
  
  private func setMaterials() {
    _node.geometry?.materials = hasTexture ? [_textureMaterial] : [_colorMaterial]
  }
  
  @objc open func Click() {
    EventDispatcher.dispatchEvent(of: self, called: "Click")
    
  }
  
  @objc open func LongClick() {
    EventDispatcher.dispatchEvent(of: self, called: "LongClick")
  }
}

// MARK: FollowsMarker Protocol
@available(iOS 11.3, *)
extension ARNodeBase: FollowsMarker {
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
    let worldTransform = _node.worldTransform
    
    _node.removeFromParentNode()
    _followingMarker?.removeNode(self)
    _followingMarker = nil
    
    _node.transform = worldTransform
    _container?.addNode(self)
    
    StoppedFollowingMarker()
  }
  
  @objc open func StoppedFollowingMarker() {
    EventDispatcher.dispatchEvent(of: self, called: "StoppedFollowingMarker")
  }
}

@available(iOS 11.3, *)
extension ARNodeBase: CanLook {
  @objc open func LookAtNode(_ node: ARNode) {
    _node.look(at: node.getPosition())
  }
  
  @objc open func LookAtDetectedPlane(_ detectedPlane: ARDetectedPlane) {
    _node.look(at: detectedPlane.getPosition())
  }
  
  @objc open func LookAtSpotlight(_ light: ARSpotlight) {
    _node.look(at: light.getPosition())
  }
  
  @objc open func LookAtPointLight(_ light: ARPointLight) {
    _node.look(at: light.getPosition())
  }
  
  @objc open func LookAtPosition(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    _node.simdLook(at: simd_float3(xMeters, yMeters, zMeters))
  }
}

@available(iOS 11.3, *)
extension ARNodeBase: VisibleComponent {
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
      return _container!.form?.dispatchDelegate
    }
  }
  
  public func copy(with zone: NSZone? = nil) -> Any { return (Any).self }
  public func setWidthPercent(_ toPercent: Int32) {}
  public func setHeightPercent(_ toPercent: Int32) {}
}

@available(iOS 11.3, *)
extension ARNodeBase: LifecycleDelegate {
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
}
