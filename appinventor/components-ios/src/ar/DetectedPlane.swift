// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import ARKit

@available(iOS 14.0, *)
open class DetectedPlane: NSObject, ARDetectedPlane {
  weak var _container: ARDetectedPlaneContainer?
  private var _anchorEntity: AnchorEntity
  private var _planeEntity: ModelEntity
  private var _alignment: ARPlaneAnchor.Alignment
  fileprivate var _name = "DetectedPlane"
  
  private var _color: Int32 = Int32(bitPattern: AIComponentKit.Color.default.rawValue)
  private var _texture: String = ""
  private var _width: Float = 0
  private var _height: Float = 0
  
  init(anchor: ARPlaneAnchor, container: ARDetectedPlaneContainer) {
    _container = container
    _alignment = anchor.alignment
    _width = anchor.extent.x
    _height = anchor.extent.z
    
    // Create anchor entity for the plane
    _anchorEntity = AnchorEntity(anchor: anchor)
    
    // Create plane entity
    let mesh = MeshResource.generatePlane(width: anchor.extent.x, depth: anchor.extent.z)
    _planeEntity = ModelEntity(mesh: mesh)
    
    super.init()
    
    setupPlaneEntity()
    _anchorEntity.addChild(_planeEntity)
    
    FillColor = Int32(bitPattern: AIComponentKit.Color.none.rawValue)
  }
  
  // Compatibility init for old SCNNode-based system (deprecated but kept for compatibility)
  init(anchor: ARPlaneAnchor, node: SCNNode, container: ARDetectedPlaneContainer) {
    _container = container
    _alignment = anchor.alignment
    _width = anchor.extent.x
    _height = anchor.extent.z
    
    // Create anchor entity for the plane
    _anchorEntity = AnchorEntity(anchor: anchor)
    
    // Create plane entity
    let mesh = MeshResource.generatePlane(width: anchor.extent.x, depth: anchor.extent.z)
    _planeEntity = ModelEntity(mesh: mesh)
    
    super.init()
    
    setupPlaneEntity()
    _anchorEntity.addChild(_planeEntity)
    
    FillColor = Int32(bitPattern: AIComponentKit.Color.none.rawValue)
  }
  
  private func setupPlaneEntity() {
    // Rotate to match plane orientation (planes in RealityKit face up by default)
    if _alignment == .horizontal {
      // No rotation needed for horizontal planes
    } else {
      // Rotate 90 degrees for vertical planes
      _planeEntity.transform.rotation = simd_quatf(angle: .pi/2, axis: [1, 0, 0])
    }
    
    // Set up physics
    let shape = ShapeResource.generateBox(width: _width, height: 0.001, depth: _height)
    _planeEntity.components.set(CollisionComponent(shapes: [shape]))
    
    // Set up material
    updateMaterial()
  }
  
  func getAnchorEntity() -> AnchorEntity {
    return _anchorEntity
  }
  
  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_width)
    }
    set(width) {
      // Width is read-only for detected planes
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_height)
    }
    set(height) {
      // Height is read-only for detected planes
    }
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
      updateMaterial()
    }
  }
  
  @objc open var FillColorOpacity: Int32 {
    get {
      if let material = _planeEntity.model?.materials.first as? SimpleMaterial {
        return Int32(1 * 100) //material.color.tint.alpha * 100) //CSB HOLD
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
  
  @objc public var Texture: String {
    get {
      return _texture
    }
    set(path) {
      if let image = AssetManager.shared.imageFromPath(path: path) {
        _texture = path
        updateTextureFromImage(image)
      } else {
        if !path.isEmpty {
          _container?.form?.dispatchErrorOccurredEvent(self, "Texture", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code)
        }
        _texture = ""
        updateMaterial()
      }
    }
  }
  
  @objc open var TextureOpacity: Int32 {
    get {
      return FillColorOpacity // Same as material opacity in RealityKit
    }
    set(opacity) {
      FillColorOpacity = opacity
    }
  }
  
  @objc open var Opacity: Int32 {
    get {
      return FillColorOpacity
    }
    set(opacity) {
      FillColorOpacity = opacity
    }
  }
  
  // FIXED: Return SIMD3<Float> instead of SCNVector3 for RealityKit compatibility
  open func getPosition() -> SIMD3<Float> {
    return _anchorEntity.transform.translation
  }
  
  open func updateFor(anchor: ARPlaneAnchor) {
    _width = anchor.extent.x
    _height = anchor.extent.z
    
    // Update the mesh with new dimensions
    let newMesh = MeshResource.generatePlane(width: anchor.extent.x, depth: anchor.extent.z)
    
    // Preserve existing materials
    let existingMaterials = _planeEntity.model?.materials ?? []
    _planeEntity.model = ModelComponent(mesh: newMesh, materials: existingMaterials)
    
    // Update collision shape
    let shape = ShapeResource.generateBox(width: _width, height: 0.001, depth: _height)
    _planeEntity.components.set(CollisionComponent(shapes: [shape]))
  }
  
  open func removed() {
    _container = nil
    _planeEntity.removeFromParent()
    _anchorEntity.removeFromParent()
  }
  
  private func updateMaterial() {
    var material = SimpleMaterial()
    
    if _color == AIComponentKit.Color.none.rawValue {
      // Make transparent for occlusion
      material.baseColor = MaterialColorParameter.color(.clear)
    } else {
      material.baseColor = MaterialColorParameter.color(argbToUIColor(_color))
    }
    
    _planeEntity.model?.materials = [material]
  }
  
  @available(iOS 15.0, *)
  private func updateMaterialOpacity(_ alpha: Float) {
    guard var material = _planeEntity.model?.materials.first as? SimpleMaterial else { return }
    
   material.baseColor = MaterialColorParameter.color(.blue)
    _planeEntity.model?.materials = [material]
  }
  
  private func updateTextureFromImage(_ image: UIImage) {
    do {
      var material = SimpleMaterial()
      
      if #available(iOS 15.0, *) {
        let texture = try TextureResource.generate(from: image.cgImage!, options: .init(semantic: .color))
        material.baseColor = MaterialColorParameter.texture(texture)
      } else {
        // CSB HOLD
      }
      

      _planeEntity.model?.materials = [material]
    } catch {
      print("Failed to create texture: \(error)")
    }
  }
  
  private func argbToUIColor(_ argb: Int32) -> UIColor {
    let alpha = CGFloat((argb >> 24) & 0xFF) / 255.0
    let red = CGFloat((argb >> 16) & 0xFF) / 255.0
    let green = CGFloat((argb >> 8) & 0xFF) / 255.0
    let blue = CGFloat(argb & 0xFF) / 255.0
    return UIColor(red: red, green: green, blue: blue, alpha: alpha)
  }
}

@available(iOS 14.0, *)
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
