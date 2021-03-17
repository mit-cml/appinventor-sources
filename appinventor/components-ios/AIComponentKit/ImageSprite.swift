// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import UIKit

open class ImageSprite: Sprite {
  fileprivate var _picturePath = ""
  fileprivate var _rotates = true
  fileprivate var _image = UIImage()
  fileprivate var _width = kLengthPreferred
  fileprivate var _height = kLengthPreferred

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    Width = _width
    Height = _height
    DisplayLayer.transform = CATransform3DMakeRotation(HeadingRadians, 0, 0, 1.0)
  }
  
  // MARK: Properties
  @objc open var Picture: String {
    get {
      return _picturePath
    }
    set(path) {
      if let image = AssetManager.shared.imageFromPath(path: path) {
        _image = image
        _picturePath = path
        self.DisplayLayer.contents = image.cgImage
        registerChanges()
      }
    }
  }

  @objc open var Image: UIImage {
    get {
      return _image
    }
  }
  
  @objc open var Rotates: Bool {
    get {
      return _rotates
    }
    set(rotates) {
      if rotates != _rotates {
        _rotates = rotates
        updateDisplayLayer()
        registerChanges()
      }
    }
  }

  override open var Heading: CGFloat {
    get {
      return super.Heading
    }
    set(heading) {
      super.Heading = heading
      if _rotates {
        updateDisplayLayer()
        registerChanges()
      }
    }
  }

  override open var Width: Int32 {
    get {
      return _width
    }
    set(width) {
      _width = width
      updateWidth()
    }
  }
  
  override open var Height: Int32 {
    get {
      return _height
    }
    set(height) {
      _height = height
      updateHeight()
    }
  }
  
  //MARK: Methods
  override func updateHeight() {
    if !_canvas.canvasView.Drawn {
      return
    }
    let canvasHeight = _canvas.canvasView.bounds.height
    if _height == kLengthPreferred {
      Height = Int32(min(_image.size.height, canvasHeight))
    } else if _height == kLengthFillParent {
      Height = Int32(canvasHeight)
    }
    updateDisplayLayer()
  }
  
  override func updateWidth() {
    if !_canvas.canvasView.Drawn {
      return
    }
    let canvasWidth = _canvas.canvasView.bounds.width
    if _width == kLengthPreferred {
      Width = Int32(min(_image.size.width, canvasWidth))
    } else if _width == kLengthFillParent {
      Width = Int32(canvasWidth)
    }
    updateDisplayLayer()
  }
  
  override func updateDisplayLayer() {
    let d = DisplayLayer
    let xCenter = CGFloat(XCenter)
    let yCenter = CGFloat(YCenter)
    CATransaction.begin()
    CATransaction.setAnimationDuration(0.0)
    if _rotates {
      DisplayLayer.transform = CATransform3DMakeRotation(HeadingRadians, 0, 0, 1.0)
    } else {
      DisplayLayer.transform = CATransform3DMakeRotation(0, 0, 0, 1.0)
    }
    d.position = CGPoint(x: xCenter, y: yCenter)
    d.bounds = CGRect(x: xCenter, y: yCenter, width: CGFloat(Width), height: CGFloat(Height))
    CATransaction.commit()
  }
}
