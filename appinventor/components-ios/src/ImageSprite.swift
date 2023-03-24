// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit

open class ImageSprite: Sprite {
  fileprivate var _picturePath = ""
  fileprivate var _rotates = true
  fileprivate var _image = UIImage()
  fileprivate var _width = kLengthPreferred
  fileprivate var _height = kLengthPreferred

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    Width = kLengthPreferred
    Height = kLengthPreferred
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
        if _lastSetHeight == kLengthPreferred {
          updateHeight()
        }
        if _lastSetWidth == kLengthPreferred {
          updateWidth()
        }
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
      _lastSetWidth = width
      updateWidth()
    }
  }
  
  override open var Height: Int32 {
    get {
      return _height
    }
    set(height) {
      _lastSetHeight = height
      updateHeight()
    }
  }
  
  //MARK: Methods
  override func updateHeight() {
    if !_canvas.canvasView.Drawn {
      return
    }
    let canvasHeight = _canvas.canvasView.bounds.height
    if _lastSetHeight == kLengthPreferred {
      _height = Int32(min(_image.size.height, canvasHeight))
    } else if _height == kLengthFillParent {
      _height = Int32(canvasHeight)
    } else {
      // pixels
      _height = _lastSetHeight
    }
    updateDisplayLayer()
  }
  
  override func updateWidth() {
    if !_canvas.canvasView.Drawn {
      return
    }
    let canvasWidth = _canvas.canvasView.bounds.width
    if _lastSetWidth == kLengthPreferred {
      _width = Int32(min(_image.size.width, canvasWidth))
    } else if _lastSetWidth == kLengthFillParent {
      _width = Int32(canvasWidth)
    } else {
      _width = _lastSetWidth
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
