// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * This struct is the result of frustation with dealing with nested arrays of arbitrary depth
 * It handles parsing of JSON to to arrays of points for LineString and Polygon
 */
public struct NestedArray {
  // A nested array of Doubles
  private var _array = [Any]()

  // The degree of nesting for the array
  private var _depth: UInt = 0

  init() {
    _array = []
  }

  static func unwrapYail(_ json: [Any]) -> [Any] {
    if json.first != nil && json.first is SCMSymbol {
      var result: [Any] = []
      for item in json {
        if item is SCMSymbol {
          continue
        } else if item is [Any] {
          result.append(unwrapYail(item as! [Any]))
        } else {
          result.append(item)
        }
      }
      return result
    } else {
      return json
    }
  }

  init(_ json: [Any]) throws {
    var data = json
    if json.first != nil && json.first is SCMSymbol {
      data = NestedArray.unwrapYail(json)
    }
    if let points = data as? [[Double]] {
      _array = try processList(points)
      _depth = 2
    } else if let singleNestedPoints = data as? [[[Double]]] {
      _array = try singleNestedPoints.map { points in
        return try processList(points)
      }
      _depth = 3
    } else if let doubleNestedPoints = data as? [[[[Double]]]] {
      _array = try doubleNestedPoints.map { singleNestedPoints in
        return try singleNestedPoints.map { points in
          return try processList(points)
        }
      }
      _depth = 4
    } else {
      _array = []
    }
  }

  /**
   * Converts list into an array of Points (an array of two to three Doubles)
   * If any index in the array lacks two points, throws an error
   */
  private func processList(_ list: [[Double]]) throws -> [[Double]] {
    return try list.map { point in
      if point.count < 2 {
        throw YailRuntimeError("Too few points", "IllegalArgument")
      }
      return Array(point[0..<min(point.count, 3)])
    }
  }

  // MARK: read-only properties
  public var array: [Any] {
    get {
      return _array
    }
  }

  public var count: Int {
    get {
      return _array.count
    }
  }

  public var depth: UInt {
    get {
      return _depth
    }
  }

  // Updates the array. Used when a shape is dragged
  public mutating func update(latitude: Double, longitude: Double) {
    switch depth {
    case 2:
      _array = update(points: _array as! [[Double]], latitude, longitude)
    case 3:
      _array = update(pointList: _array as! [[[Double]]], latitude, longitude)
    case 4:
      _array = update(pointNestedList: _array as! [[[[Double]]]], latitude, longitude)
    default:
      break
    }
  }

  // MARK: update handlers for different nesting levels
  private func update(points: [[Double]], _ latitude: Double, _ longitude: Double) -> [[Double]] {
    return points.map { point in
      var toReturn = [point[0] + latitude, point[1] + longitude]
      if point.count > 2 {
        toReturn.append(point[2])
      }
      return toReturn
    }
  }

  private func update(pointList: [[[Double]]], _ latitude: Double, _ longitude: Double) -> [[[Double]]] {
    return pointList.map { points in
      return update(points: points, latitude, longitude)
    }
  }

  private func update(pointNestedList: [[[[Double]]]], _ latitude: Double, _ longitude: Double) -> [[[[Double]]]] {
    return pointNestedList.map { list in
      return update(pointList: list, latitude, longitude)
    }
  }
}

/**
 * A class used to handle shapes with the Points property
 */
@objc open class PolygonBaseWithPoints: PolygonBase {
  fileprivate var _points = NestedArray()
  /**
   * Signals when there is an error in Points or HolePoints
   * Used to restore the previous value
   */
  internal var hasError = false

  open override func update(_ latitude: Double, _ longitude: Double) {
    _points.update(latitude: latitude, longitude: longitude)
    makeShape()
  }

  // MARK: properties
  @objc open var Points: [Any] {
    get {
      return _points.array
    }
    set(list) {
      do {
        try updatePoints(points: list)
      } catch {
        parseError("Points")
      }
    }
  }

  @objc open var PointsFromString: String {
    get {
      return ""
    }
    set(points) {
      do {
        if let json = try getObjectFromJson(points) as? [Any] {
          try updatePoints(points: json)
        } else {
          parseError("PointsFromString")
        }
      } catch {
        parseError("PointsFromString")
      }
    }
  }

  // the NestedArray used for making a shape
  open var points: NestedArray {
    return _points
  }

  // used for updating Points
  open func updatePoints(points: [Any]) throws {
    hasError = false
    let tempPoints = _points
    _points = try NestedArray(points)
    if _points.depth > 3 {
      throw YailRuntimeError("Unexpected depth of four", "IllegalAgument")
    }
    if initialized {
      makeShape()
      // If an error is detected when trying to set points, reverts to the previous version
      if hasError {
        _points = tempPoints
      }
    }
  }

  // Converts a list of Doubles to a stringe. Used in conjuction with GEOSwift
  func parse(points: [[Double]]) -> String {
    var geoString = ""
    var first = true
    for point in points {
      if !first {
        geoString += ","
      }
      geoString += "\(point[1]) \(point[0])"
      first = false
    }
    return geoString
  }

  // MARK: methods implemented by subclasses
  internal func parseError( _ method: String) {
    hasError = true
  }
}
