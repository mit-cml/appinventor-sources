// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit

fileprivate let DEFAULT_RADIUS: Int32 = 5
fileprivate let DEFAULT_PAINTCOLOR: Int32 = Int32(bitPattern: Color.black.rawValue)

open class Ball: Sprite {
  fileprivate var _radius = DEFAULT_RADIUS
  fileprivate var _paintColor = DEFAULT_PAINTCOLOR
  fileprivate var _image = UIImage()

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    DisplayLayer.fillColor = argbToColor(_paintColor).cgColor
    let point = UIBezierPath(arcCenter: CGPoint(x: 0, y: 0), radius: CGFloat(Radius),
                             startAngle: 0, endAngle:CGFloat(Double.pi * 2), clockwise: true)
    DisplayLayer.path = point.cgPath
    updateDisplayLayer()
  }

  // MARK: Properties
  @objc override open var OriginAtCenter : Bool {
    get {
      return super.OriginAtCenter
    }
    set(b){
      super.OriginAtCenter = b
    }
  }

  @objc open var PaintColor : Int32 {
    get {
      return _paintColor
    }
    set(color) {
      if color != _paintColor {
        _paintColor = color
        DisplayLayer.fillColor = argbToColor(_paintColor).cgColor
      }
    }
  }

  @objc open var Radius : Int32 {
    get {
      return _radius
    }
    set(radius) {
      if radius != _radius {
        _radius = radius
        let point = UIBezierPath(arcCenter: CGPoint(x: 0, y: 0), radius: CGFloat(Radius), startAngle: 0, endAngle:CGFloat(Double.pi * 2), clockwise: true)
        _xLeft = xOriginToLeft(xOrigin: xOrigin)
        _yTop = yOriginToTop(yOrigin: yOrigin)
        DisplayLayer.path = point.cgPath
      }
    }
  }

  @objc open var Image: UIImage {
    get {
      return _image
    }
  }

  @objc open override var XCenter: Double {
    return _xLeft + CGFloat(Radius)
  }

  @objc open override var YCenter: Double {
    return _yTop + CGFloat(Radius)
  }

  // Changes to width and height are only allowed via changes to radius.
  override open var Width: Int32 {
    get {
      return 2 * Radius
    }
    set(width) {}
  }

  override open func setWidthPercent(_ toPercent: Int32) {}

  override open var Height: Int32 {
    get {
      return 2 * Radius
    }
    set(height) {}
  }

  override open func setHeightPercent(_ toPercent: Int32) {}

  //MARK: Methods
  override func contains(_ point: CGPoint) -> Bool {
    let r = Double(Radius)
    let xCenter = XCenter
    let yCenter = YCenter
    let xDiffSquared = pow(Double(point.x) - xCenter, 2)
    let yDiffSquared = pow(Double(point.y) - yCenter, 2)
    let radiusSquared = pow(Double(r), 2)
    return xDiffSquared + yDiffSquared <= radiusSquared
  }

  override func updateDisplayLayer() {
    let xCenter = CGFloat(XCenter)
    let yCenter = CGFloat(YCenter)
    CATransaction.begin()
    CATransaction.setAnimationDuration(0.0)
    DisplayLayer.position = CGPoint(x: xCenter, y: yCenter)
    CATransaction.commit()
  }

  func getMinProjection(_ axis: Vector2D) -> Double {
    return Vector2D.dotProduct(centerVector, axis) - Double(Radius) * axis.magnitude
  }

  func getMaxProjection(_ axis: Vector2D) -> Double {
    return Vector2D.dotProduct(centerVector, axis) + Double(Radius) * axis.magnitude
  }
}
