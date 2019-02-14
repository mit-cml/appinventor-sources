// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

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
        DisplayLayer.path = point.cgPath
      }
    }
  }
  
  @objc open var Image: UIImage {
    get {
      return _image
    }
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
    let xCenter = X + r
    let yCenter = Y + r
    let xDiffSquared = pow(Double(point.x) - xCenter, 2)
    let yDiffSquared = pow(Double(point.y) - yCenter, 2)
    let radiusSquared = pow(Double(r), 2)
    return xDiffSquared + yDiffSquared <= radiusSquared
  }
  
  override func updateDisplayLayer() {
    let centerX = CGFloat(Double(Radius) + X)
    let centerY = CGFloat(Double(Radius) + Y)
    DisplayLayer.position = CGPoint(x: centerX, y: centerY)
  }
}
