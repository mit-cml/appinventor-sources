// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MapKit

/**
 * A class used to handle parsing and writing GeoJSON
 * Used primarily by Map and FeatureCollection
 */
open class GeoJSONUtil {
  // a handler for setting a property of a map
  typealias PropertyApplication = (MapFeature, Any) throws -> ()

  // MARK: error codes and messages for LoadError
  static let ERROR_CODE_MALFORMED_URL: Int32 = -1
  static let ERROR_CODE_IO_EXCEPTION: Int32 = -2
  static let ERROR_CODE_MALFORMED_GEOJSON: Int32 = -3
  static let ERROR_CODE_UNKNOWN_TYPE: Int32
    = -4
  static let ERROR_CODE_JSON_PARSE_ERROR: Int32 = -5

  static let ERROR_MALFORMED_URL: String = "The URL is malformed"
  static let ERROR_IO_EXCEPTION: String = "Unabled to download content from URL"
  static let ERROR_MALFORMED_GEOJSON: String = "Malformed GeoJSON response.  Expected FeatureCollection as root element."
  static let ERROR_UNKNOWN_TYPE: String = "Unrecognized/invalid type in JSON object"
  static let ERROR_JSON_PARSE_ERROR: String = "Unable to parse JSON from url."

  //MARK: expected GeoJSON property names
  fileprivate static let GEOJSON_FEATURECOLLECTION: String = "FeatureCollection"
  fileprivate static let GEOJSON_GEOMETRYCOLLECTION: String = "GeometryCollection"
  fileprivate static let GEOJSON_COORDINATES = "coordinates"
  fileprivate static let GEOJSON_FEATURE = "Feature"
  fileprivate static let GEOJSON_GEOMETRY = "geometry"
  fileprivate static let GEOJSON_PROPERTIES = "properties"
  fileprivate static let GEOJSON_TYPE = "type"

  // MARK: property names
  fileprivate static let PROPERTY_ANCHOR_HORIZONTAL = "anchorHorizontal"
  fileprivate static let PROPERTY_ANCHOR_VERTICAL = "anchorVertical"
  fileprivate static let PROPERTY_DESCRIPTION = "description"
  fileprivate static let PROPERTY_DRAGGABLE = "draggable"
  fileprivate static let PROPERTY_FILL = "fill"
  fileprivate static let PROPERTY_FILL_OPACITY = "fill-opacity"
  fileprivate static let PROPERTY_HEIGHT = "height"
  fileprivate static let PROPERTY_IMAGE = "image"
  fileprivate static let PROPERTY_INFOBOX = "infobox"
  fileprivate static let PROPERTY_STROKE = "stroke"
  fileprivate static let PROPERTY_STROKE_OPACITY = "stroke-opacity"
  fileprivate static let PROPERTY_STROKE_WIDTH = "stroke-width"
  fileprivate static let PROPERTY_TITLE = "title"
  fileprivate static let PROPERTY_WIDTH = "width"
  fileprivate static let PROPERTY_VISIBLE = "visible"

  /**
   * A dictionary of String: PropertyApplicaiton
   * Used to apply a property to a feature
   */
  fileprivate static let SUPPORTED_PROPERTIES: [String: PropertyApplication] = {
    var properties = [String: PropertyApplication]()
    properties[PROPERTY_ANCHOR_HORIZONTAL] = { feature, value in
      if var marker = feature as? Marker {
        marker.AnchorHorizontal = Int32(try parseIntegerOrString(value))
      }
    }
    properties[PROPERTY_ANCHOR_VERTICAL] = { feature, value in
      if var marker = feature as? Marker {
        marker.AnchorVertical = Int32(try parseIntegerOrString(value))
      }
    }
    properties[PROPERTY_DESCRIPTION] = { feature, value in
      feature.Description = String(describing: value)
    }
    properties[PROPERTY_DRAGGABLE] = { feature, value in
      feature.Draggable = try parseBoolOrString(value)
    }
    properties[PROPERTY_FILL] = { feature, value in
      if var fill = feature as? HasFill {
        if let number = value as? NSNumber {
          fill.FillColor = Int32(truncating: number)
        } else {
          fill.FillColor = try Int32(parseColor(String(describing: value)))
        }
      }
    }
    properties[PROPERTY_FILL_OPACITY] = { feature, value in
      if var fill = feature as? HasFill {
        fill.FillOpacity = try Float(parseIntegerOrString(value))
      }
    }
    properties[PROPERTY_HEIGHT] = { feature, value in
      if var marker = feature as? Marker {
        marker.Height = try Int32(parseIntegerOrString(value))
      }
    }
    properties[PROPERTY_IMAGE] = { feature, value in
      if let marker = feature as? Marker {
        marker.ImageAsset = String(describing: value)
      }
    }
    properties[PROPERTY_INFOBOX] = { feature, value in
      feature.EnableInfobox = try parseBoolOrString(value)
    }
    properties[PROPERTY_STROKE] = { feature, value in
      if var stroke = feature as? HasStroke {
        if let number = value as? NSNumber {
          stroke.StrokeColor = Int32(truncating: number)
        } else {
          stroke.StrokeColor = try Int32(parseColor(String(describing: value)))
        }
      }
    }
    properties[PROPERTY_STROKE_OPACITY] = { feature, value in
      if var stroke = feature as? HasStroke {
        stroke.StrokeOpacity = try Float(parseIntegerOrString(value))
      }
    }
    properties[PROPERTY_STROKE_WIDTH] = { feature, value in
      if var stroke = feature as? HasStroke {
        stroke.StrokeWidth = try Int32(parseIntegerOrString(value))
      }
    }
    properties[PROPERTY_TITLE] = { feature, value in
      (feature as MapFeature).Title = String(describing: value)
    }
    properties[PROPERTY_WIDTH] = { feature, value in
      if let marker = feature as? Marker {
        marker.StrokeWidth = try Int32(parseIntegerOrString(value))
      }
    }
    properties[PROPERTY_VISIBLE] = { feature, value in
      feature.Visible = try parseBoolOrString(value)
    }
    return properties
  }()

  fileprivate static let colors: [String: UInt32] = [
    "black": Color.black.rawValue,
    "blue": Color.blue.rawValue,
    "cyan": Color.cyan.rawValue,
    "darkgray": Color.darkGray.rawValue,
    "gray": Color.gray.rawValue,
    "green": Color.green.rawValue,
    "lightgray": Color.lightGray.rawValue,
    "magenta": Color.magenta.rawValue,
    "orange": Color.orange.rawValue,
    "pink": Color.pink.rawValue,
    "red": Color.red.rawValue,
    "white": Color.white.rawValue,
    "yellow": Color.yellow.rawValue,
  ]

  // saves a list of features to a file as GeoJSON
  public static func writeAsGeoJSON(features: [MapFeature], to path: String) throws {
    var output = "{\"type\": \"FeatureCollection\", \"features\":["
    for feature in features {
      output += visit(feature: feature)
    }
    let url = URL(fileURLWithPath: AssetManager.shared.transformPotentialAndroidPath(path: path))
    try output.write(to: url, atomically: true, encoding: .utf8)
  }

  // MARK: a lot of write helper methods
  fileprivate static func write(type: String) -> String {
    return "\"type\":\"\(type)\""
  }

  fileprivate static func write(property: String, _ objValue: Any) -> String {
    do {
      let result = try getJsonRepresentation(objValue as AnyObject)
      return ",\"\(property)\":\(result)"
    } catch {
      NSLog("Unable to serialize the value of \"\(property)\" as JSON")
      return ""
    }
  }

  fileprivate static func write(property: String, _ value: String?) -> String {
    guard let strVal = value, !strVal.isEmpty else {
      return ""
    }
    return write(property: property, strVal)
  }

  fileprivate static func write(color: Int32, _ property: String) -> String {
    return ",\"\(property)\":\"&H\(String(format: "%X", color))\""
  }

  fileprivate static func write(point: CLLocationCoordinate2D) -> String {
    return "\"geometry\":{\"type\":\"Point\",\"coordinates\":[\(point.longitude ),\(point.latitude)]}"
  }

  fileprivate static func write(header type: String) -> String {
    return ",\"properties\":{\"$Type\":\"\(type)\""
  }

  fileprivate static func write(feature: MapFeature) -> String {
    return write(property: PROPERTY_DESCRIPTION, feature.Description) +
      write(property: PROPERTY_DRAGGABLE, feature.Draggable) +
      write(property: PROPERTY_INFOBOX, feature.EnableInfobox) +
      write(property: PROPERTY_TITLE, feature.Title) +
      write(property: PROPERTY_VISIBLE, feature.Visible)
  }
  
  fileprivate static func write(stroke: HasStroke) -> String {
    return write(color: stroke.StrokeColor, PROPERTY_STROKE) +
      write(property: PROPERTY_STROKE_OPACITY, stroke.StrokeOpacity) +
      write(property: PROPERTY_STROKE_WIDTH, stroke.StrokeWidth)
  }

  fileprivate static func write(fill: HasFill) -> String {
    return write(color: fill.FillColor, PROPERTY_FILL) + write(property: PROPERTY_FILL_OPACITY, fill.FillOpacity)
  }

  fileprivate static func write(points: [[Double]]) -> String {
    var first = true
    var string = "["
    for point in points {
      if !first {
        string += ","
      }
      if point.count >= 2 {
        string += "[\(point[1]),\(point[0])"
      }
      if point.count >= 3 {
        string += ",\(point[2])"
      }
      string += "]"
      first = false
    }
    return string + "]"
  }

  fileprivate static func write(line: MapLineString) -> String {
    if let points = line.Points as? [[Double]] {
      return "\"geometry\":{\"type\":\"LineString\",\"coordinates\":" +
        write(points: points) + "}"
    } else if let points = line.Points as? [[[Double]]] {
      var string = "\"geometry\":{\"type\":\"MultiLineString\",\"coordinates\":["
      var first = true
      for line in points {
        if !first {
          string += ","
        }
        string += write(points: line)
        first = false
      }
      return string + "]}"
    } else {
      return ""
    }
  }

  fileprivate static func write(multiPolygon: MapPolygon) -> String {
    var string = ""
    if let points = multiPolygon.Points as? [[[Double]]] {
      string = "\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":["
      for (idx, shape) in points.enumerated() {
        //the first value is duplicated due to the way GEOSwift works.
        if idx == points.count - 1 {
          break
        }
        if idx > 0 {
          string += ","
        }
        string += write(points: shape)
        if idx < multiPolygon.HolePoints.count {
          if let hole = multiPolygon.HolePoints[idx] as? [[Double]] {
            string += "," + write(points: hole)
          } else if let multiHole = multiPolygon.HolePoints[idx] as? [[[Double]]] {
            for (jdx, hole) in multiHole.enumerated() {
              if jdx == multiHole.count - 1 {
                break
              }
              string += "," + write(points: hole)
            }
          }
        }
      }
    }
    return string
  }

  fileprivate static func write(single polygon: MapPolygon) -> String {
    var string = "\"geometry\":{\"type\":\"Polygon\",\"coordinates\":"
    if let points = polygon.Points as? [[Double]] {
      if polygon.HolePoints.count > 0 {
        string += "["
      }
      string += write(points: points)
      if polygon.HolePoints.count > 0 {
        if let hole = polygon.HolePoints as? [[Double]] {
          string += "," + write(points: hole) + "]"
        } else if let holes = polygon.HolePoints as? [[[Double]]] {
          for hole in holes {
            string += "," + write(points: hole)
          }
          string += "]"
        }
      }
    } else {
      return ""
    }
    return string
  }

  fileprivate static func write(polygon: Polygon) -> String {
    return polygon.points.depth == 2 ? write(single: polygon): write(multiPolygon: polygon)
  }

  /**
   * MARK: visit methods.
   * These are similar to that of Android, the only difference being that these return a string
   */
  fileprivate static func visit(_ marker: Marker) -> String {
    return "{\(GEOJSON_FEATURE)," +
      write(point: marker.Centroid) +
      write(header: "Marker") +
      write(feature: marker) +
      write(stroke: marker) +
      write(fill: marker) +
      write(property: PROPERTY_FILL_OPACITY, marker.FillOpacity) +
      write(property: PROPERTY_ANCHOR_HORIZONTAL, marker.AnchorHorizontal) +
      write(property: PROPERTY_ANCHOR_VERTICAL, marker.AnchorVertical) +
      write(property: PROPERTY_HEIGHT, marker.Height) +
      write(property: PROPERTY_IMAGE, marker.ImageAsset) +
      write(property: PROPERTY_WIDTH, marker.Width) + "}}"
  }

  fileprivate static func visit(_ line: LineString) -> String {
    return "{\(GEOJSON_FEATURE)," +
      write(line: line) +
      write(header: "LineString") +
      write(feature: line) +
      write(stroke: line) + "}}"
  }

  fileprivate static func visit(_ polygon: Polygon) -> String {
    return "{\(GEOJSON_FEATURE)," +
      write(polygon: polygon) +
      write(header: "Polygon") +
      write(feature: polygon) +
      write(stroke: polygon) +
      write(fill: polygon) + "}}"
  }

  fileprivate static func visit(_ circle: Circle) -> String {
    return "{\(GEOJSON_FEATURE)," +
      write(point: circle.Centroid) +
      write(header: "Circle") +
      write(feature: circle) +
      write(stroke: circle) +
      write(fill: circle) + "}}"
  }

  fileprivate static func visit(_ rectangle: Rectangle) -> String {
    return "{\(GEOJSON_FEATURE),\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[" +
      "[\(rectangle.WestLongitude),\(rectangle.NorthLatitude)]," +
      "[\(rectangle.WestLongitude),\(rectangle.SouthLatitude)]," +
      "[\(rectangle.EastLongitude),\(rectangle.SouthLatitude)]," +
      "[\(rectangle.EastLongitude),\(rectangle.NorthLatitude)]," +
      "[\(rectangle.WestLongitude),\(rectangle.NorthLatitude)]]}" +
      write(header: "Rectangle") +
      write(feature: rectangle) +
      write(stroke: rectangle) +
      write(fill: rectangle) +
      write(property: "NorthLatitude", rectangle.NorthLatitude) +
      write(property: "WestLongitude", rectangle.WestLongitude) +
      write(property: "SouthLatitude", rectangle.SouthLatitude) +
      write(property: "EastLongitude", rectangle.EastLongitude) + "}}"
  }

  fileprivate static func visit(feature: MapFeature) -> String {
    if let marker = feature as? Marker {
      return visit(marker)
    } else if let line = feature as? LineString {
      return visit(line)
    } else if let polygon = feature as? Polygon {
      return visit(polygon)
    } else if let circle = feature as? Circle {
      return visit(circle)
    } else if let rectangle = feature as? Rectangle {
      return visit(rectangle)
    } else {
      return ""
    }
  }

  /**
   * Attempts to parse features from JSON
   * Returns an array of MapFeatures if successful, else returns nil
   */
  public static func processGeoJsonUrl(for container: MapFeatureContainer, at url: String, with description: [String: Any]) -> [Any]? {
    guard let type = description["type"] as? String, type == GEOJSON_FEATURECOLLECTION || type == GEOJSON_GEOMETRYCOLLECTION else {
      container.LoadError(url, ERROR_CODE_MALFORMED_GEOJSON, ERROR_MALFORMED_GEOJSON)
      return nil
    }
    guard let items = description["features"] as? [Any] else {
      container.LoadError(url, ERROR_CODE_JSON_PARSE_ERROR, ERROR_JSON_PARSE_ERROR)
      return nil
    }
    var features = [Any]()
    do {
      for feature in items {
        if let json = feature as? [String: Any] {
          try features.append(jsonToPairArray(json))
        } else {
          throw YailRuntimeError("", "")
        }
      }
    } catch {
      container.LoadError(url, ERROR_CODE_UNKNOWN_TYPE, ERROR_UNKNOWN_TYPE)
      return nil
    }
    return features
  }

  /**
   * Attempts to create a MapFeature from a list of lists
   * Throws an erro if unable to parse properties
   */
  public static func processGeoJSONFeature(container: MapFeatureContainer, description: [Any]) throws -> MapFeature {
    var type: String? = nil
    var geometry: [Any]? = nil
    var properties: [Any]? = nil
    try processList(description) { key, value in
      if key == GEOJSON_TYPE {
        type = value as? String
      } else if key == GEOJSON_GEOMETRY {
        geometry = value as? [Any]
      } else if key == GEOJSON_PROPERTIES {
        properties = value as? [Any]
      } else {
        NSLog("Unexpected field \"\(key)\" in JSON format")
      }
    }
    if type == nil  {
      throw YailRuntimeError("Missing type", "IllegalArgument")
    } else if type! != GEOJSON_FEATURE {
      throw YailRuntimeError("Unknown type \"\(type!)\"", "IllegalArgument")
    }
    if let geom = geometry {
      let feature = try processGeometry(container: container, geometry: geom)
      if let props = properties {
        try processProperties(for: feature, with: props)
      }
      return feature
    } else {
      throw YailRuntimeError("No geometry defined for feature.", "IllegalArgument")
    }
  }

  // MARK: methods for converting json to arrays
  fileprivate static func jsonToPairArray(_ json: [String: Any]) throws -> [Any] {
    var pairs = [Any]()
    for (key, value) in json {
      if value is Bool || value is Int || value is Double || value is String {
        pairs.append([key, value])
      } else if let array = value as? [Any] {
        try pairs.append([key, arrayToPairArray(array)])
      } else if let json = value as? [String: Any] {
        try pairs.append([key, jsonToPairArray(json)])
      } else if (value as AnyObject?)?.isEqual(kCFNull) ?? false {
        continue
      } else {
        throw YailRuntimeError(ERROR_UNKNOWN_TYPE, "IllegalArgument")
      }
    }
    return pairs
  }

  fileprivate static func arrayToPairArray(_ array: [Any]) throws -> [Any] {
    var items = [Any]()
    for value in array {
      if value is Bool || value is Int || value is Double || value is String {
        items.append(value)
      } else if let arr = value as? [Any] {
        items.append(arr)
      } else if let json = value as? [String: Any] {
        try items.append(jsonToPairArray(json))
      } else if (value as AnyObject?)?.isEqual(kCFNull) ?? false {
        continue
      } else {
        throw YailRuntimeError(ERROR_UNKNOWN_TYPE, "IllegalArgument")
      }
    }
    return items
  }

  fileprivate static func parseBoolOrString(_ value: Any) throws -> Bool {
    if let bool = value as? Bool {
      return bool
    } else if let string = value as? String {
      return !(string == "false" || string.count == 0)
    } else {
      throw YailRuntimeError("Value is not a boolean", "IllegalArgument")
    }
  }

  fileprivate static func parseColor(_ value: String) throws -> UInt32 {
    let lcVal = value.lowercased()
    if let result = colors[lcVal] {
      return UInt32(result)
    } else if lcVal.starts(with: "#") {
      return try parseColorHex(String(lcVal.dropFirst()))
    } else if lcVal.starts(with: "&h") {
      return try parseColorHex(String(lcVal.dropFirst(2)))
    } else {
      return UInt32(Color.red.rawValue)
    }
  }

  fileprivate static func parseColorHex(_ value: String) throws -> UInt32 {
    var argb: UInt32 = 0
    if value.count == 3 {
      argb = 0xFF000000
      var hex: UInt32
      for (i, char) in value.unicodeScalars.enumerated() {
        hex = try charToHex(char)
        argb |= ((hex << 4) | hex) << UInt32(2 - i) * 8
      }
    } else if value.count == 6 {
      argb = 0xFF000000
      var hex: UInt32
      for i in 0..<3 {
        hex = try charToHex(charAt(2 * i, value)) << 4 | charToHex(charAt(2 * i + 1, value))
        argb |= hex << UInt32(2 - i) * 8
      }
    } else if value.count == 8 {
      var hex: UInt32
      for i in 0..<4 {
        hex = try charToHex(charAt(2 * i, value)) << 4 | charToHex(charAt(2 * i + 1, value))
        argb |= hex << UInt32(3 - i) * 8
      }
    } else {
      throw YailRuntimeError("Wrong number of bits", "IllegalArgument")
    }

    return argb
  }

  fileprivate static func charToHex(_ char: UnicodeScalar) throws -> UInt32 {
    switch char {
    case "0"..."9":
      return char.value - UnicodeScalar("0").value
    case "a"..."f":
      return (char.value - UnicodeScalar("a").value).advanced(by: 10)
    case "A"..."F":
      return (char.value - UnicodeScalar("A").value).advanced(by: 10)
    default:
      throw YailRuntimeError("Failed to parse hex value", "IllegalArgument")
    }
  }

  fileprivate static func charAt(_ index: Int, _ string: String) -> UnicodeScalar {
    let str = string.unicodeScalars
    return str[str.index(str.startIndex, offsetBy: index)]
  }

  fileprivate static func parseIntegerOrString(_ value: Any) throws -> Int {
    if let number = value as? NSNumber {
      return Int(truncating: number)
    } else if let number = (value as? NSString)?.integerValue {
      return number
    } else {
      throw YailRuntimeError("Value is not a number", "IllegalArgument")
    }
  }

  fileprivate static func processGeometry(container: MapFeatureContainer, geometry: [Any]) throws -> MapFeature {
    var type: String? = nil
    var coordinates: [Any]? = nil
    try processList(geometry) { key, value in
      if key == GEOJSON_TYPE {
        type = value as? String
      } else if key == GEOJSON_COORDINATES {
        coordinates = value as? [Any]
      } else {
        NSLog("Unsupported field \"\(key)\" in JSON format")
      }
    }
    if let coords = coordinates, let featureType = type {
      return try processCoordinates(container: container, for: featureType, with: coords)
    } else {
      throw YailRuntimeError("Missing coordinates and/or type for feature", "IllegalArgument")
    }
  }

  fileprivate static func processCoordinates(container: MapFeatureContainer, for type: String, with coordinates: [Any]) throws -> MapFeature {
    switch type {
    case MapFeatureType.TYPE_POINT.rawValue:
      return try markerFromGeoJSON(container, coordinates)
    case MapFeatureType.TYPE_LINESTRING.rawValue:
      return try lineStringFromGeoJSON(container, coordinates)
    case MapFeatureType.TYPE_MULTILINESTRING.rawValue:
      return try multiLineStringFromGeoJSON(container, coordinates)
    case MapFeatureType.TYPE_POLYGON.rawValue:
      return try polygonFromGeoJSON(container, coordinates)
    case MapFeatureType.TYPE_MULTIPOLYGON.rawValue:
      return try multiPolygonFromGeoJSON(container, coordinates)
    default:
      throw YailRuntimeError("Invalid type supplied in GeoJSON", "IllegalArgument")
    }
  }

  fileprivate static func markerFromGeoJSON(_ container: MapFeatureContainer, _ coordinates: [Any]) throws -> Marker {
    if let coords = coordinates as? [Double], coords.count > 1 {
      let marker = Marker(container)
      marker.SetLocation(coords[1], coords[0])
      return marker
    } else {
      throw YailRuntimeError("Invalid coordinates supplied in GeoJSON", "IllegalArgument")
    }
  }

  fileprivate static func lineStringFromGeoJSON(_ container: MapFeatureContainer, _ coordinates: [Any]) throws -> LineString {
    if let coords = coordinates as? [[Double]], coords.count >= 2 {
      let points: [[Double]] = try swapPoints(coords)
      let line = LineString(container)
      line.Points = points
      return line
    } else {
      throw YailRuntimeError("Invalid coordinates supplied in GeoJSON", "IllegalArgument")
    }
  }

  fileprivate static func multiLineStringFromGeoJSON(_ container: MapFeatureContainer, _ coordinates: [Any]) throws -> LineString {
    if let coords = coordinates as? [[[Double]]] {
      let points: [[[Double]]] = try coords.map { line in
        return try swapPoints(line)
      }
      let line = LineString(container)
      line.Points = points
      return line
    } else {
      throw YailRuntimeError("Invalid coordinates supplied in GeoJSON", "IllegalArgument")
    }
  }

  fileprivate static func polygonFromGeoJSON(_ container: MapFeatureContainer, _ coordinates: [Any]) throws -> Polygon {
    if let coords = coordinates as? [[[Double]]], coords.count > 0 {
      let points: [[Double]] = try swapPoints(coords[0])
      var holePoints: [[Double]]? = nil
      if coords.count > 1 {
        holePoints = try swapHolePoints(coords[1])
      }
      let polygon = Polygon(container)
      polygon.Points = points
      if let hole = holePoints {
        polygon.HolePoints = hole
      }
      return polygon
    } else {
      throw YailRuntimeError("Invalid coordinates supplied in GeoJSON", "IllegalArgument")
    }
  }

  fileprivate static func multiPolygonFromGeoJSON(_ container: MapFeatureContainer, _ coordinates: [Any]) throws -> Polygon {
    if let coords = coordinates as? [[[[Double]]]], coords.count > 0 {
      var points = [[[Double]]]()
      var holePoints = [[[Double]]]()
      for shape in coords {
        if shape.count == 0 {
          throw YailRuntimeError("Invalid coordinates supplied in GeoJSON", "IllegalArgument")
        }
        try points.append(swapPoints(shape[0]))
        if shape.count > 1 {
          try holePoints.append(swapHolePoints(shape[1]))
        }
      }
      let polygon = Polygon(container)
      polygon.Points = points
      polygon.HolePoints = holePoints
      return polygon
    } else {
      throw YailRuntimeError("Invalid coordinates supplied in GeoJSON", "IllegalArgument")
    }
  }

  fileprivate static func processProperties(for feature: MapFeature, with properties: [Any]) throws {
    try processList(properties) { key, value in
      if let handler = SUPPORTED_PROPERTIES[key] {
        try handler(feature, value)
      } else {
        NSLog("Ignoring GeoJSON property \"\(key)\"")
      }
    }
  }

  fileprivate static func swapPoints(_ points: [[Double]]) throws -> [[Double]] {
    return try points.map { point in
      if point.count < 2 {
        throw YailRuntimeError("Invalid coordinates supplied in GeoJSON", "IllegalArgument")
      }
      return try swapSinglePoint(point)
    }
  }

  fileprivate static func swapHolePoints(_ points: [[Double]]) throws -> [[Double]] {
    return try points.map { point in
      if point.count < 2 {
        return []
      } else {
        return try swapSinglePoint(point)
      }
    }
  }

  fileprivate static func swapSinglePoint(_ point: [Double]) throws -> [Double] {
    if !(-180.0...180 ~= point[1]) {
      throw YailRuntimeError("Invalid longitude \(point[1]) in geometry definition. Must be in range [-180, 180]", "IllegalArgument")
    }
    if !(-90.0...90 ~= point[0]) {
      throw YailRuntimeError("Invalid latitude \(point[0]) in geometry definition. Must be in range [-90, 90]", "IllegalArgument")
    }
    var point = [point[1], point[0]]
    if point.count > 2 {
      point.append(point[2])
    }
    return point
  }

  fileprivate static func processList(_ list: [Any], handler: (String, Any) throws -> ()) throws {
    for item in list {
      if let pair = item as? [Any], pair.count >= 2, let key = pair[0] as? String {
        let value = pair[1]
        try handler(key, value)
      } else {
        throw YailRuntimeError("Could not parse list from description", "IllegalArgument")
      }
    }
  }

  public static func swapCoordinates(_ original: YailList<AnyObject>) -> YailList<AnyObject> {
    for coord in original {
      if let coord = coord as? YailList<AnyObject> {
        let temp = coord[1]
        coord[1] = coord[2]
        coord[2] = temp
      }
    }
    return original
  }
}
