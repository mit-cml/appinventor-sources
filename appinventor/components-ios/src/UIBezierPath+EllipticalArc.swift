// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public extension UIBezierPath {

  convenience init(ellipseArcIn rect: CGRect, startAngle: CGFloat, endAngle: CGFloat, angleStep: CGFloat = CGFloat.pi/40.0, useCenter: Bool, closePath: Bool, clockwise: Bool = true) {
    let center = CGPoint(x: rect.midX, y: rect.midY)
    let ellipseSegment = UIBezierPath()

    /// We need to negate the angles to allow for clockwise drawing.
    let multiplier: CGFloat = clockwise ? -1 : 1

    let startingEllipsePoint = UIBezierPath.ellipsePointForAngle(center: center, angle: multiplier * startAngle, width: rect.width, height: rect.height)

    // If we're using the center, then the first point is the center point.
    // Otherwise, it's the first point of the ellipse.
    ellipseSegment.move(to: useCenter ? center : startingEllipsePoint)

    // If we're using the center, then the first angle is the starting angle.
    // Otherwise, we already put a point at the starting angle and want to push it forward a step.
    var currentAngle = useCenter ? startAngle : (startAngle + angleStep)

    while currentAngle < endAngle {
      let ellipsePoint = UIBezierPath.ellipsePointForAngle(center: center, angle: multiplier * currentAngle, width: rect.width, height: rect.height)
      ellipseSegment.addLine(to: ellipsePoint)
      currentAngle += angleStep
    }

    let endingPoint = UIBezierPath.ellipsePointForAngle(center: center, angle: multiplier * endAngle, width: rect.width, height: rect.height)
    ellipseSegment.addLine(to: endingPoint)

    if closePath {
      ellipseSegment.close()
    }

    self.init(cgPath: ellipseSegment.cgPath)
  }

  private static func ellipsePointForAngle(center: CGPoint, angle: CGFloat, width: CGFloat, height: CGFloat) -> CGPoint {
    let ePX = center.x + width/2.0 * cos(angle)
    let ePY = center.y - height/2.0 * sin(angle)
    return CGPoint(x: ePX, y: ePY)
  }
}
