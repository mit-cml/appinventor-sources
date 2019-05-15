// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit
import ARKit
import GLKit

@available(iOS 11.0, *)
open class DetectedPlane: NSObject, ARDetectedPlane {
  weak var _container: ARDetectedPlaneContainer?
  private var _associatedNode: SCNNode
  private var _geometry: SCNPlane
  private var _planeNode: SCNNode
  private var _alignment: ARPlaneAnchor.Alignment
  fileprivate var _name = "DetectedPlane"
  
  private var _color: Int32 = Int32(bitPattern: AIComponentKit.Color.default.rawValue)
  private var _colorMaterial: SCNMaterial = SCNMaterial()
  public var hasTexture = false
  private var _textureMaterial: SCNMaterial = SCNMaterial()
  private var _texture: String = ""
  
  init(anchor: ARPlaneAnchor, node: SCNNode, container: ARDetectedPlaneContainer) {
    _container = container
    _alignment = anchor.alignment
    _associatedNode = node
    _associatedNode.name = _name
    _geometry = SCNPlane(width: CGFloat(anchor.extent.x), height: CGFloat(anchor.extent.z))
    _planeNode = SCNNode(geometry: _geometry)
    
    super.init()
    setupPlaneNode(anchor)
    FillColor = Int32(bitPattern: AIComponentKit.Color.none.rawValue)
    setMaterials()
  }
  
  private func setupPlaneNode(_ anchor: ARPlaneAnchor) {
    _planeNode.eulerAngles = SCNVector3(GLKMathDegreesToRadians(90), 0, 0)
    _planeNode.physicsBody = SCNPhysicsBody(type: .kinematic, shape: nil)
    _planeNode.renderingOrder = -1
    
    _textureMaterial.isDoubleSided = true
    _textureMaterial.lightingModel = .constant
    _textureMaterial.writesToDepthBuffer = true
    
    _colorMaterial.isDoubleSided = true
    _colorMaterial.lightingModel = .constant
    _colorMaterial.writesToDepthBuffer = true
    
    _associatedNode.addChildNode(_planeNode)
  }
  
  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_geometry.width)
    }
    set(width) {}
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_geometry.height)
    }
    set(height) {}
  }
  
  @objc open var IsHorizontal: Bool {
    get {
      return _alignment == .horizontal
    }
  }
  
  @objc public var FillColor: Int32 {
    get {
      return _color
    }
    set(color) {
      _color = color
      /**
       * When the color is set to none, we make the plane occulde other items if they are behind it.
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
  @objc public var Texture: String {
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
  
  @objc open var Opacity: Int32 {
    get {
      return Int32(round(_associatedNode.opacity * 100))
    }
    set(opacity) {
      let floatOpacity = Float(min(max(0, opacity), 100)) / 100.0
      _planeNode.opacity = CGFloat(floatOpacity)
    }
  }
  
  open func getPosition() -> SCNVector3 {
    return _associatedNode.position
  }
  
  open func updateFor(anchor: ARPlaneAnchor) {
    _geometry.width = CGFloat(anchor.extent.x)
    _geometry.height = CGFloat(anchor.extent.z)
  }
  
  open func removed() {
    _container = nil
    _planeNode.removeFromParentNode()
  }
  
  private func setMaterials() {
    _planeNode.geometry?.materials = hasTexture ? [_textureMaterial] : [_colorMaterial]
  }
  
}

@available(iOS 11.0, *)
extension DetectedPlane: Component {
  public final var Name: String {
    get {
      return _name
    }
    set(name) {
      _name = name
    }
  }
  
  public var Width: Int32 {
    get {
      return 0
    }
    set(width) {
      
    }
  }
  
  public var Height: Int32 {
    get {
      return 0
    }
    set(height) {
      
    }
  }
  
  public var dispatchDelegate: HandlesEventDispatching? {
    get {
      return _container!.form?.dispatchDelegate
    }
  }
  
  public func copy(with zone: NSZone? = nil) -> Any { return (Any).self }
  public func setWidthPercent(_ toPercent: Int32) {}
  public func setHeightPercent(_ toPercent: Int32) {}
}
