// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.3, *)
open class TextNode: ARNodeBase, ARText {
  private var _textGeometry: SCNText = SCNText(string: "TextNode", extrusionDepth: 1.0)
  private var _textNode: SCNNode
  
  @objc init(_ container: ARNodeContainer) {
    _textNode = SCNNode(geometry: _textGeometry)
    _textGeometry.flatness = 0
    
    /**
     * In order to have rounded text, we want to make the font-size large and scale it down.
     * If we directly set the fontsize to 0.06, this would have points at all of the curves.
     * Therefore, we do not allow the user to change the scale and have them provide sizing in cm.
     **/
    _textGeometry.font = _textGeometry.font.withSize(6.0)
    _textNode.scale = SCNVector3(0.01, 0.01, 0.01)
    
    super.init(container: container, node: _textNode)
    
    updateTextCenter()
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  /**
   * This sets the text's pivot point to be the center.
   * The default is the bottom left, which places the text at an unexpected location.
   */
  private func updateTextCenter() {
    let (min, max) = _textNode.boundingBox
    
    _textNode.pivot = SCNMatrix4MakeTranslation(
      min.x + (max.x - min.x)/2,
      min.y + (max.y - min.y)/2,
      min.z + (max.z - min.z)/2
    )
  }
  
  @objc open var Text: String {
    get {
      return _textGeometry.string as? String ?? ""
    }
    set(text) {
      _textGeometry.string = text
      updateTextCenter()
    }
  }
  
  @objc open var FontSizeInCentimeters: Float {
    get {
      return Float(_textGeometry.font?.pointSize ?? 6.0)
    }
    set(fontSize) {
      _textGeometry.font = _textGeometry.font.withSize(CGFloat(abs(fontSize)))
      updateTextCenter()
    }
  }
  
  /**
   * This does not need a conversion due to the scaling that takes place.
   * Check the note at the top of the file.
   */
  @objc open var DepthInCentimeters: Float {
    get {
      return Float(_textGeometry.extrusionDepth)
    }
    set(depth) {
      _textGeometry.extrusionDepth = CGFloat(max(depth, 0))
    }
  }

  /**
   * Alignment only makes a difference when a container frame is used.
   */
  @objc open var TextAlignment: Int32 {
    get {
      switch _textGeometry.alignmentMode {
      case CATextLayerAlignmentMode.left.rawValue:
        return Alignment.normal.rawValue
      case CATextLayerAlignmentMode.right.rawValue:
        return Alignment.opposite.rawValue
      case CATextLayerAlignmentMode.center.rawValue:
        return Alignment.center.rawValue
      default:
        return Alignment.normal.rawValue
      }
    }
    set(alignment) {
      switch alignment {
      case Alignment.normal.rawValue:
        _textGeometry.alignmentMode = CATextLayerAlignmentMode.left.rawValue
      case Alignment.opposite.rawValue:
        _textGeometry.alignmentMode = CATextLayerAlignmentMode.right.rawValue
      case Alignment.center.rawValue:
        _textGeometry.alignmentMode = CATextLayerAlignmentMode.center.rawValue
      default:
        _textGeometry.alignmentMode = CATextLayerAlignmentMode.left.rawValue
      }
    }
  }
  
  /**
   * These have no meaning until a container frame is set -- this container frames needs to be
   * adjustable by the user.
   */
  
  @objc open var WrapText: Bool {
    get {
      return _textGeometry.isWrapped
    }
    set(wrapped) {
      _textGeometry.isWrapped = wrapped
    }
  }
  
  @objc open var Truncation: Int32 {
    get {
      switch _textGeometry.truncationMode {
      case CATextLayerTruncationMode.end.rawValue:
        return AIComponentKit.Truncation.end.rawValue
      case CATextLayerTruncationMode.start.rawValue:
        return AIComponentKit.Truncation.start.rawValue
      case CATextLayerTruncationMode.middle.rawValue:
        return AIComponentKit.Truncation.middle.rawValue
      default:
        return AIComponentKit.Truncation.none.rawValue
      }
    }
    set(truncation) {
      switch truncation {
      case AIComponentKit.Truncation.end.rawValue:
        _textGeometry.truncationMode = CATextLayerTruncationMode.end.rawValue
      case AIComponentKit.Truncation.start.rawValue:
        _textGeometry.truncationMode = CATextLayerTruncationMode.start.rawValue
      case AIComponentKit.Truncation.middle.rawValue:
        _textGeometry.truncationMode = CATextLayerTruncationMode.middle.rawValue
      default:
        _textGeometry.truncationMode = CATextLayerTruncationMode.none.rawValue
      }
    }
  }
  
  
  /// Enable once values necessary to set are not tiny.
//  @objc open var CornerRadius: Float {
//    get {
//      return Float(_textGeometry.chamferRadius)
//    }
//    set(radius) {
//      _textGeometry.chamferRadius = CGFloat(radius)
//    }
//  }
  
  // The user should not be able to change the Scale
  @objc open override var Scale: Float {
    get {
      return 1
    }
    set(scalar) { }
  }
  
  // The user should not be able to change the scale directly
  @objc open override func ScaleBy(_ scalar: Float) {
    let fontSize = _textGeometry.font.pointSize * CGFloat(scalar)
    _textGeometry.font = _textGeometry.font.withSize(fontSize)
    updateTextCenter()
  }
}
