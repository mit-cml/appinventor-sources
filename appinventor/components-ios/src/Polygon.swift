// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MapKit
import GEOSwift

@objc open class Polygon: PolygonBaseWithPoints, MapPolygon {
  fileprivate var _holePoints = NestedArray()

  @objc public init(_ container: MapFeatureContainer) {
    super.init(container: container)
    Type = MapFeatureType.TYPE_POLYGON.rawValue
    FillColor = colorToArgb(UIColor.red)
    FillOpacity = 1
  }

  // MARK: properties
  @objc open var HolePoints: [Any] {
    get {
      return _holePoints.array
    }
    set(list) {
      do {
        try updateHolePoints(points: list)
      } catch {
        parseError("HolePoints")
      }
    }
  }

  @objc open var HolePointsFromString: String {
    get {
      return ""
    }
    set(points) {
      do {
        if points == "\"\"" {
          try updateHolePoints(points: [])
        } else {
          if let json = try getObjectFromJson(points) as? [Any] {
            try updateHolePoints(points: json)
          } else {
            parseError("HolePointsFromString")
          }
        }
      } catch {
        parseError("HolePointsFromString")
      }
    }
  }

  // MARK: helper methods
  open override func update(_ latitude: Double, _ longitude: Double) {
    _holePoints.update(latitude: latitude, longitude: longitude)
    super.update(latitude, longitude)
  }

  public func updateHolePoints(points: [Any]) throws {
    hasError = false
    let tempPoints = _holePoints
    if points.count == 0 {
      _holePoints = NestedArray()
    } else {
      _holePoints = try NestedArray(points)
    }
    if initialized {
      makeShape()
      // If an error is detected when trying to set holePoints, reverts to the previous version
      if hasError {
        _holePoints = tempPoints
      }
    }
  }

  override func makeShape() {
    var geoString = ""
    if points.count == 0 {
      return
    }

    /**
     * Here, we allow either an equal nesting of points, or one greater for holes
     * When the depth is the same, we assume that each shape has at most one hole.
     * When the depth is one greater, we assume that each shape has an arbitrary number of holes
     */
    if points.depth + 1 < _holePoints.depth || (_holePoints.depth != 0 && _holePoints.depth < points.depth) {
      parseError("Points")
      return
    }

    if points.depth == 3 {
      geoString += "MULTI"
    }
    geoString += "POLYGON("
    if _holePoints.count == 0 {
      if let multiPolygon = points.array as? [[[Double]]] {
        var first = true
        for shape in multiPolygon {
          if !first {
            geoString += ","
          }
          geoString += "((" + parse(points: shape) + "))"
          first = false
        }
      } else if let polygon = points.array as? [[Double]] {
        geoString += "(" + parse(points: polygon) + ")"
      }
    } else {
      if let multiPolygon = points.array as? [[[Double]]] {
        var first = true
        for (idx, shape) in multiPolygon.enumerated() {
          if !first {
            geoString += ","
          }
          if idx < _holePoints.count {
            geoString += "(" + parse(points: shape, holePoints: _holePoints.array[idx]) + ")"
          } else {
            parseError("HolePoints")
            return
          }
          first = false
        }
      } else if let shape = points.array as? [[Double]] {
        geoString += parse(points: shape, holePoints: _holePoints.array)
      }
    }
    geoString += ")"

    if points.depth == 2 {
      if let shape = Geometry.create(geoString), let mapShape = shape.mapShape() as? MKPolygon {
        _shape = shape
        overlay = MapPolygonOverlay(points: mapShape.points(), count: mapShape.pointCount, interiorPolygons: mapShape.interiorPolygons)
      } else {
        parseError("PointsFromString")
      }
    } else {
      if let shape = Geometry.create(geoString), let mapShape = shape.mapShape() as? MKShapesCollection {
        _shape = shape
        var shapes = [MapOverlayShape]()
        for shape in mapShape.shapes {
          if let polygon = shape as? MKPolygon {
            shapes.append(MapPolygonOverlay(points: polygon.points(), count: polygon.pointCount, interiorPolygons: polygon.interiorPolygons))
          }
        }
        overlay = MapShapeCollection(shapes: shapes)
      } else {
        parseError("PointsFromString")
      }
    }
  }

  override func parse(points: [[Double]]) -> String {
    return points.count > 0 ? super.parse(points: points) + ",\(points[0][1]) \(points[0][0])": ""
  }

  fileprivate func parse(points: [[Double]], holePoints: Any) -> String {
    var geoString = "(" + parse(points: points) + ")"
    if let hole = holePoints as? [[Double]], hole.count > 0 {
      geoString += ",(" + parse(points: hole) + ")"
    } else if let holes = holePoints as? [[[Double]]], holes.count > 0 {
      geoString += ","
      var first = true
      for hole in holes {
        if hole.count > 0 {
          if !first {
            geoString += ","
          }
          geoString += "(" + parse(points: hole) + ")"
          first = false
        }
      }
    }
    return geoString
  }

  internal override func parseError(_ method: String) {
    super.parseError(method)
    _container?.form?.dispatchErrorOccurredEvent(self, method,
        ErrorMessage.ERROR_POLYGON_PARSE_ERROR.code,
        ErrorMessage.ERROR_POLYGON_PARSE_ERROR.message, points)
  }

  open override func copy(container: MapFeatureContainer) {
    let polygon = Polygon(container)
    polygon.copy(from: self)
    polygon.FillColor = FillColor
    polygon.FillOpacity = FillOpacity
    polygon.HolePoints = HolePoints
    polygon.Points = Points
  }
}
