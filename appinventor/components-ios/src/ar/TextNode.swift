// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import UIKit

@available(iOS 14.0, *)
open class TextNode: ARNodeBase, ARText {
  private var _text: String = "TextNode"
  private var _fontSize: Float = 6.0 // stored in points
  private var _depth: Float = 1.0 // stored in centimeters
  private var _textAlignment: Int32 = Alignment.normal.rawValue
  private var _wrapText: Bool = false
  private var _truncation: Int32 = AIComponentKit.Truncation.none.rawValue
  
  @objc init(_ container: ARNodeContainer) {
    // Create initial mesh from text
    let lineHeight: CGFloat = 0.05
    let font = MeshResource.Font.systemFont(ofSize: lineHeight)
    let textMesh = MeshResource.generateText(_text,extrusionDepth: UnitHelper.centimetersToMeters(_depth), font: font )
    super.init(container: container, mesh: textMesh)
    self.Name = "text"
    updateTextMesh()
    
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  /**
   * Creates a 3D text mesh using RealityKit's text generation capabilities
   * Note: RealityKit doesn't have built-in 3D text like SceneKit, so we create it from a 2D representation
   */
  private func updateTextMesh() {
      let lineHeight: CGFloat = 0.05
      let font = MeshResource.Font.systemFont(ofSize: lineHeight)
      let textMesh = MeshResource.generateText(
          _text,
          extrusionDepth: UnitHelper.centimetersToMeters(_depth),
          font: font
      )
      let textMaterial = SimpleMaterial(color: argbToColor(FillColor), isMetallic: true)

      // Update existing entity instead of creating new one
      _modelEntity.model = ModelComponent(mesh: textMesh, materials: [textMaterial])
  }
  
  
  public func calculateTextBounds() -> (width: Float, height: Float) {
    let font = UIFont.systemFont(ofSize: CGFloat(_fontSize))
    let textSize = _text.size(withAttributes: [.font: font])
    
    // Scale to appropriate AR size (similar to original 0.01 scale)
    let scaleFactor: Float = 0.01
    return (
      width: Float(textSize.width) * scaleFactor,
      height: Float(textSize.height) * scaleFactor
    )
  }
  
  /**
   * Centers the text entity similar to the original SCNNode pivot adjustment
   */
  private func updateTextCenter() {
    // RealityKit entities are centered by default, but we can adjust if needed
    // The transform.translation can be adjusted if we need different pivot behavior
  }
  
  @objc open var Text: String {
    get {
      return _text
    }
    set(text) {
      _text = text
      updateTextMesh()
    }
  }
  
  @objc open var FontSizeInCentimeters: Float {
    get {
      return _fontSize
    }
    set(fontSize) {
      _fontSize = abs(fontSize)
      updateTextMesh()
    }
  }
  
  @objc open var DepthInCentimeters: Float {
    get {
      return _depth
    }
    set(depth) {
      _depth = max(depth, 0)
      updateTextMesh()
    }
  }

  @objc open var TextAlignment: Int32 {
    get {
      return _textAlignment
    }
    set(alignment) {
      _textAlignment = alignment
      updateTextMesh()
    }
  }
  
  @objc open var WrapText: Bool {
    get {
      return _wrapText
    }
    set(wrapped) {
      _wrapText = wrapped
      updateTextMesh()
    }
  }
  
  @objc open var Truncation: Int32 {
    get {
      return _truncation
    }
    set(truncation) {
      _truncation = truncation
      updateTextMesh()
    }
  }
  
  // The user should not be able to change the Scale directly
 /* @objc open override var Scale: Float {
    get {
      return 1
    }
    set(scalar) {
      // Redirect to font size change instead
      let newFontSize = _fontSize * scalar
      FontSizeInCentimeters = newFontSize
    }
  }*/
  
  override open func ScaleBy(_ scalar: Float) {
    print("🔄 Scaling text \(Name) by \(scalar)")
    
    let oldScale = Scale
    let hadPhysics = _modelEntity.physicsBody != nil
    
    let newScale = oldScale * abs(scalar)
    // ✅ Update physics immediately if it was enabled before we change the scale
    if hadPhysics {
      let previousSize = _fontSize * Scale
      _modelEntity.position.y = _modelEntity.position.y - (previousSize) + (_fontSize * newScale)
    }
  
    Scale = newScale
    print("Scale complete - bottom position maintained")
  }
  
}
