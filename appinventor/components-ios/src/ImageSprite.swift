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

  @objc open func setMarkOrigin(_ originString: String) {
    let parts = originString.split(",")
    if parts.count == 2 {
      guard let x = Double(parts[0]) else {
        return
      }
      guard let y = Double(parts[1]) else {
        return
      }
      U = x
      Y = y
    }
  }

  @objc open var OriginX: Double {
    get {
      U
    }
    set {
      U = newValue
    }
  }

  @objc open var OriginY: Double {
    get {
      V
    }
    set {
      V = newValue
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
    _yTop = yOriginToTop(yOrigin: yOrigin)
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
    _xLeft = xOriginToLeft(xOrigin: xOrigin)
    updateDisplayLayer()
  }

  override func updateDisplayLayer() {
    let d = DisplayLayer
    let xCenter = X + (0.5 - u) * Double(Width)
    let yCenter = Y + (0.5 - v) * Double(Height)
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

  override var centerVector: Vector2D {
    return rotate(vector: super.centerVector)
  }

  var extremityVectors: [Vector2D] {
    let deltas = [(0.0, 0.0), (1.0, 0.0), (1.0, 1.0), (0.0, 1.0)]
    return deltas.map { (dx, dy) in
      return self.rotate(vector: Vector2D(x: _xLeft + dx * Double(Width), y: _yTop + dy * Double(Height)))
    }
  }

  var normalAxes: [Vector2D] {
    var corners = extremityVectors
    let leftRightEdge = corners[0] - corners[1]
    let topDownEdge = corners[1] - corners[2]
    return [leftRightEdge.normalVector, topDownEdge.normalVector]
  }

  func getMinProjection(_ axis: Vector2D) -> Double {
    let corners = extremityVectors
    let starting: (vector: Vector2D?, d: Double) = (nil, Double.infinity)
    return corners.reduce(starting) { partialResult, v in
      let projMagnitude = Vector2D.dotProduct(axis, v)
      if projMagnitude < partialResult.d {
        return (vector: v, d: projMagnitude)
      } else {
        return partialResult
      }
    }.d
  }

  func getMaxProjection(_ axis: Vector2D) -> Double {
    let corners = extremityVectors
    let starting: (vector: Vector2D?, d: Double) = (nil, -Double.infinity)
    return corners.reduce(starting) { partialResult, v in
      let projMagnitude = Vector2D.dotProduct(axis, v)
      if projMagnitude > partialResult.d {
        return (vector: v, d: projMagnitude)
      } else {
        return partialResult
      }
    }.d
  }

  private func rotate(vector: Vector2D) -> Vector2D {
    if _rotates {
      let origin = Vector2D(x: xOrigin, y: yOrigin)
      var originToPoint = vector - origin
      originToPoint.rotate(radians: HeadingRadians)
      return origin + originToPoint
    } else {
      return vector
    }
  }
}
