// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit

open class ImageSprite: Sprite {
  private let gifAnimationKey = "AppInventorGifAnimation"
  fileprivate var _picturePath = ""
  fileprivate var _rotates = true
  fileprivate var _image = UIImage()
  fileprivate var _gif: AnimatedGif? = nil
  fileprivate var _animated = true
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
      if let gif = AssetManager.shared.animatedGifFromPath(path: path), let image = gif.firstFrame {
        _gif = gif
        _image = image
        _picturePath = path
        self.DisplayLayer.contents = image.cgImage
        applyGifAnimation()
        if _lastSetHeight == kLengthPreferred {
          updateHeight()
        }
        if _lastSetWidth == kLengthPreferred {
          updateWidth()
        }
        registerChanges()
      } else if let image = AssetManager.shared.imageFromPath(path: path) {
        stopGifAnimation()
        _gif = nil
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

  @objc open var Animated: Bool {
    get {
      return _gif != nil && DisplayLayer.animation(forKey: gifAnimationKey) != nil && DisplayLayer.speed != 0
    }
    set(animated) {
      _animated = animated
      guard _gif != nil else {
        return
      }
      if animated {
        resumeGifAnimation()
      } else {
        pauseGifAnimation()
      }
    }
  }

  @objc open func ResetAnimation() {
    guard _gif != nil else {
      return
    }
    let wasAnimating = Animated
    stopGifAnimation()
    self.DisplayLayer.contents = _image.cgImage
    if wasAnimating {
      applyGifAnimation()
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
      if _width == kLengthPreferred || _width <= kLengthPercentTag {
        if _image.size.width > 0 {
          return Int32(_image.size.width)
        }
      }
      return _width
    }
    set(width) {
      _lastSetWidth = width
      updateWidth()
    }
  }

  override open var Height: Int32 {
    get {
      if _height == kLengthPreferred || _height <= kLengthPercentTag {
        if _image.size.height > 0 {
          return Int32(_image.size.height)
        }
      }
      return _height
    }
    set(height) {
      _lastSetHeight = height
      updateHeight()
    }
  }

  //MARK: Methods
  override func updateHeight() {
    let canvasHeight = _canvas.canvasView.bounds.height
    if _lastSetHeight == kLengthPreferred {
      if _image.size.height > 0 {
        _height = Int32(_image.size.height)
      } else if _canvas.canvasView.Drawn && canvasHeight > 0 {
        _height = Int32(canvasHeight)
      }
    } else if _lastSetHeight == kLengthFillParent {
      if _canvas.canvasView.Drawn && canvasHeight > 0 {
        _height = Int32(canvasHeight)
      }
    } else {
      // pixels
      _height = _lastSetHeight
    }
    _yTop = yOriginToTop(yOrigin: yOrigin)
    updateDisplayLayer()
  }

  override func updateWidth() {
    let canvasWidth = _canvas.canvasView.bounds.width
    if _lastSetWidth == kLengthPreferred {
      if _image.size.width > 0 {
        _width = Int32(_image.size.width)
      } else if _canvas.canvasView.Drawn && canvasWidth > 0 {
        _width = Int32(canvasWidth)
      }
    } else if _lastSetWidth == kLengthFillParent {
      if _canvas.canvasView.Drawn && canvasWidth > 0 {
        _width = Int32(canvasWidth)
      }
    } else {
      _width = _lastSetWidth
    }
    _xLeft = xOriginToLeft(xOrigin: xOrigin)
    updateDisplayLayer()
  }

  override func updateDisplayLayer() {
    let d = DisplayLayer
    let spriteWidth = max(0, CGFloat(Width))
    let spriteHeight = max(0, CGFloat(Height))
    let xCenter = X + (0.5 - u) * Double(spriteWidth)
    let yCenter = Y + (0.5 - v) * Double(spriteHeight)
    CATransaction.begin()
    CATransaction.setAnimationDuration(0.0)
    CATransaction.setDisableActions(true)
    if _rotates {
      DisplayLayer.transform = CATransform3DMakeRotation(HeadingRadians, 0, 0, 1.0)
    } else {
      DisplayLayer.transform = CATransform3DMakeRotation(0, 0, 0, 1.0)
    }
    d.position = CGPoint(x: xCenter, y: yCenter)
    d.bounds = CGRect(x: 0, y: 0, width: spriteWidth, height: spriteHeight)
    CATransaction.commit()
  }

  override var centerVector: Vector2D {
    return rotate(vector: super.centerVector)
  }

  var extremityVectors: [Vector2D] {
    let w = Double(max(0, Width))
    let h = Double(max(0, Height))
    let p0 = Vector2D(x: Double(_xLeft), y: Double(_yTop))
    let p1 = Vector2D(x: Double(_xLeft) + w, y: Double(_yTop))
    let p2 = Vector2D(x: Double(_xLeft) + w, y: Double(_yTop) + h)
    let p3 = Vector2D(x: Double(_xLeft), y: Double(_yTop) + h)

    if _rotates && HeadingRadians != 0 {
      let origin = Vector2D(x: xOrigin, y: yOrigin)
      let rad = HeadingRadians
      let cosVal = cos(rad)
      let sinVal = sin(rad)
      return [p0, p1, p2, p3].map { pt in
        let ox = pt.x - origin.x
        let oy = pt.y - origin.y
        return Vector2D(x: origin.x + ox * cosVal - oy * sinVal,
                        y: origin.y + ox * sinVal + oy * cosVal)
      }
    } else {
      return [p0, p1, p2, p3]
    }
  }

  var normalAxes: [Vector2D] {
    let corners = extremityVectors
    let leftRightEdge = corners[0] - corners[1]
    let topDownEdge = corners[1] - corners[2]
    return [leftRightEdge.normalVector, topDownEdge.normalVector]
  }

  func getMinProjection(_ axis: Vector2D) -> Double {
    return getMinProjection(axis, corners: extremityVectors)
  }

  func getMaxProjection(_ axis: Vector2D) -> Double {
    return getMaxProjection(axis, corners: extremityVectors)
  }

  func getMinProjection(_ axis: Vector2D, corners: [Vector2D]) -> Double {
    var minVal = Double.infinity
    for v in corners {
      let projMagnitude = Vector2D.dotProduct(axis, v)
      if projMagnitude < minVal {
        minVal = projMagnitude
      }
    }
    return minVal
  }

  func getMaxProjection(_ axis: Vector2D, corners: [Vector2D]) -> Double {
    var maxVal = -Double.infinity
    for v in corners {
      let projMagnitude = Vector2D.dotProduct(axis, v)
      if projMagnitude > maxVal {
        maxVal = projMagnitude
      }
    }
    return maxVal
  }

  private func rotate(vector: Vector2D) -> Vector2D {
    if _rotates && HeadingRadians != 0 {
      let origin = Vector2D(x: xOrigin, y: yOrigin)
      var originToPoint = vector - origin
      originToPoint.rotate(radians: HeadingRadians)
      return origin + originToPoint
    } else {
      return vector
    }
  }

  private func applyGifAnimation() {
    guard let gif = _gif else {
      return
    }
    stopGifAnimation()
    let animation = CAKeyframeAnimation(keyPath: "contents")
    animation.values = gif.cgImages
    animation.keyTimes = gif.keyTimes
    animation.duration = gif.duration
    animation.calculationMode = .discrete
    animation.repeatCount = gif.loopCount == 0 ? .infinity : Float(gif.loopCount)
    animation.isRemovedOnCompletion = false
    DisplayLayer.add(animation, forKey: gifAnimationKey)
    if !_animated {
      pauseGifAnimation()
    }
  }

  private func pauseGifAnimation() {
    guard DisplayLayer.animation(forKey: gifAnimationKey) != nil, DisplayLayer.speed != 0 else {
      return
    }
    let pausedTime = DisplayLayer.convertTime(CACurrentMediaTime(), from: nil)
    DisplayLayer.speed = 0
    DisplayLayer.timeOffset = pausedTime
  }

  private func resumeGifAnimation() {
    guard DisplayLayer.animation(forKey: gifAnimationKey) != nil else {
      applyGifAnimation()
      return
    }
    let pausedTime = DisplayLayer.timeOffset
    DisplayLayer.speed = 1
    DisplayLayer.timeOffset = 0
    DisplayLayer.beginTime = 0
    let timeSincePause = DisplayLayer.convertTime(CACurrentMediaTime(), from: nil) - pausedTime
    DisplayLayer.beginTime = timeSincePause
  }

  private func stopGifAnimation() {
    DisplayLayer.removeAnimation(forKey: gifAnimationKey)
    DisplayLayer.speed = 1
    DisplayLayer.timeOffset = 0
    DisplayLayer.beginTime = 0
  }
}
