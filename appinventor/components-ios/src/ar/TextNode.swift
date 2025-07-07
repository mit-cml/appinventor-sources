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
    super.init(container: container, mesh: nil)
    
    // Generate text mesh
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
    // Create text as image first
    let textImage = createTextImage()
    
    // Create a plane mesh for the text
    let textBounds = calculateTextBounds()
    let mesh = MeshResource.generatePlane(width: textBounds.width, depth: textBounds.height)
    
    // Create material with text texture
    var material = SimpleMaterial()
    if let image = textImage {
      do {
        if #available(iOS 15.0, *) {
          let texture = try TextureResource.generate(from: image.cgImage!, options: .init(semantic: .color))
          material.baseColor = MaterialColorParameter.texture(texture)
        } else {
          material.baseColor = MaterialColorParameter.color(.green)
        }
        
      } catch {
        print("Failed to create text texture: \(error)")
        material.baseColor = MaterialColorParameter.color(.red)
      }
    } else {
      material.baseColor = MaterialColorParameter.color(.white)
    }
    
    // If depth > 0, we could extrude the text (simplified approach)
    // For now, we'll use a thin box to simulate depth
    let finalMesh: MeshResource
    if _depth > 0 {
      let depthInMeters = _depth * 0.01 // Convert cm to meters
      finalMesh = MeshResource.generateBox(width: textBounds.width, height: depthInMeters, depth: textBounds.height)
    } else {
      finalMesh = mesh
    }
    
    _modelEntity.model = ModelComponent(mesh: finalMesh, materials: [material])
    
    // Center the text (similar to updateTextCenter in original)
    updateTextCenter()
  }
  
  private func createTextImage() -> UIImage? {
    let font = UIFont.systemFont(ofSize: CGFloat(_fontSize))
    let textColor = UIColor.white
    
    // Configure paragraph style
    let paragraphStyle = NSMutableParagraphStyle()
    switch _textAlignment {
    case Alignment.normal.rawValue:
      paragraphStyle.alignment = .left
    case Alignment.center.rawValue:
      paragraphStyle.alignment = .center
    case Alignment.opposite.rawValue:
      paragraphStyle.alignment = .right
    default:
      paragraphStyle.alignment = .left
    }
    
    // Configure line break mode based on truncation
    switch _truncation {
    case AIComponentKit.Truncation.end.rawValue:
      paragraphStyle.lineBreakMode = .byTruncatingTail
    case AIComponentKit.Truncation.start.rawValue:
      paragraphStyle.lineBreakMode = .byTruncatingHead
    case AIComponentKit.Truncation.middle.rawValue:
      paragraphStyle.lineBreakMode = .byTruncatingMiddle
    default:
      paragraphStyle.lineBreakMode = _wrapText ? .byWordWrapping : .byClipping
    }
    
    let attributes: [NSAttributedString.Key: Any] = [
      .font: font,
      .foregroundColor: textColor,
      .paragraphStyle: paragraphStyle
    ]
    
    let attributedString = NSAttributedString(string: _text, attributes: attributes)
    let textBounds = calculateTextBounds()
    let size = CGSize(width: CGFloat(textBounds.width * 1000), height: CGFloat(textBounds.height * 1000)) // Scale for better resolution
    
    let renderer = UIGraphicsImageRenderer(size: size)
    return renderer.image { context in
      context.cgContext.setFillColor(UIColor.clear.cgColor)
      context.cgContext.fill(CGRect(origin: .zero, size: size))
      
      let rect = CGRect(origin: .zero, size: size)
      attributedString.draw(in: rect)
    }
  }
  
  private func calculateTextBounds() -> (width: Float, height: Float) {
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
  @objc open override var Scale: Float {
    get {
      return 1
    }
    set(scalar) {
      // Redirect to font size change instead
      let newFontSize = _fontSize * scalar
      FontSizeInCentimeters = newFontSize
    }
  }
  
  // The user should not be able to change the scale directly
  @objc open override func ScaleBy(_ scalar: Float) {
    let newFontSize = _fontSize * scalar
    FontSizeInCentimeters = newFontSize
  }
}
