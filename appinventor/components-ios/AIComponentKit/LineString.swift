// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MapKit
import GEOSwift

@objc open class LineString: PolygonBaseWithPoints, MapLineString {
  @objc public init(_ container: MapFeatureContainer) {
    super.init(container: container)
    Type = MapFeatureType.TYPE_LINESTRING.rawValue
    StrokeWidth = 3
    StrokeColor = colorToArgb(UIColor.black)
  }

  // MARK: required inherited methods
  internal override func makeShape() {
    var geoString = ""
    if let points = points.array as? [[Double]] {
      guard points.count >= 2 else {
        return  // Need at least two points to make a line
      }
      geoString += "LINESTRING(" + parse(points: points) + ")"
      if let shape = Geometry.create(geoString), let mapShape = shape.mapShape() as? MKPolyline {
        _shape = shape
        overlay = MapLineOverlay(points: mapShape.points(), count: mapShape.pointCount)
      } else {
        parseError("PointsFromString")
      }
    } else if let points = points.array as? [[[Double]]] {
      geoString = "MULTILINESTRING("
      var first = true
      for line in points {
        if !first {
          geoString += ","
        }
        geoString += "(\(parse(points: line)))"
        first = false
      }
      if let shape = Geometry.create(geoString + ")"), let mapShape = shape.mapShape() as? MKShapesCollection {
        _shape = shape
        var shapes = [MapOverlayShape]()
        for shape in mapShape.shapes {
          if let line = shape as? MKPolyline {
            shapes.append(MapLineOverlay(points: line.points(), count: line.pointCount))
          }
        }
        overlay = MapShapeCollection(shapes: shapes)
      } else {
        parseError("PointsFromString")
      }
    }
  }

  internal override func parseError(_ method: String) {
    super.parseError(method)
    _container?.form?.dispatchErrorOccurredEvent(self, method,
        ErrorMessage.ERROR_LINESTRING_PARSE_ERROR.code,
        ErrorMessage.ERROR_LINESTRING_PARSE_ERROR.message, points)
  }

  open override func copy(container: MapFeatureContainer) {
    let line = LineString(container)
    line.copy(from: self)
    line.Points = Points
  }
}
