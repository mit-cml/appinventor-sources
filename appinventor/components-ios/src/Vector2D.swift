// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public struct Vector2D: Comparable, AdditiveArithmetic {
  public static var zero: Vector2D = Vector2D(x: 0.0, y: 0.0)

  var x: Double
  var y: Double

  public var magnitude: Double {
    return sqrt(magnitudeSquared)
  }

  public var magnitudeSquared: Double {
    return x * x + y * y
  }

  public var normalVector: Vector2D {
    return Vector2D(x: y, y: -x)
  }

  public var unitVector: Vector2D {
    return Vector2D(x: x / magnitude, y: y / magnitude)
  }

  public mutating func rotate(radians angle: Double) {
    let newX = x * cos(angle) - y * sin(angle)
    let newY = y * sin(angle) + y * cos(angle)
    x = newX
    y = newY
  }

  public func closestVector(in vectors: [Vector2D]) -> Vector2D? {
    var result: (vector: Vector2D?, d: Double) = (nil, Double.infinity)
    result = vectors.reduce(result) { partialResult, v in
      let d = (self - v).magnitudeSquared
      if d < partialResult.d {
        return (vector: v, d: d)
      } else {
        return partialResult
      }
    }
    return result.vector
  }

  public static func < (lhs: Vector2D, rhs: Vector2D) -> Bool {
    return lhs.magnitudeSquared < rhs.magnitudeSquared
  }

  public static func - (lhs: Vector2D, rhs: Vector2D) -> Vector2D {
    return Vector2D(x: lhs.x - rhs.x, y: lhs.y - rhs.y)
  }

  public static func + (lhs: Vector2D, rhs: Vector2D) -> Vector2D {
    return Vector2D(x: lhs.x + rhs.x, y: lhs.y + rhs.y)
  }

  public static func dotProduct(_ lhs: Vector2D, _ rhs: Vector2D) -> Double {
    return lhs.x * rhs.x + lhs.y * rhs.y
  }
}
