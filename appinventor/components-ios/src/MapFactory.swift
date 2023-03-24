// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreLocation
import MapKit
import GEOSwift

//MapEventListener

@objc public protocol MapFeature: Component, NSObjectProtocol {
  var `Type`: String { get }
  var Visible: Bool { get set }
  var Draggable: Bool { get set }
  var Description: String { get set }
  var EnableInfobox: Bool { get set }
  var Title: String? { get set }
  var Centroid: CLLocationCoordinate2D { get }
  var geometry: Geometry? { get }
  var index: Int32 { get }
  var annotation: MapFeatureAnnotation { get }

  func ShowInfobox()
  func HideInfobox()
  func Click()
  func LongClick()
  func StartDrag()
  func Drag()
  func StopDrag()
  func removeFromMap()
  func copy(container: MapFeatureContainer)
}

@objc public protocol MapFeatureContainer: ComponentContainer {
  var Features: YailList<MapFeature> { get set }

  func FeatureFromDescription(_ description: [Any]) -> Any
  func FeatureClick(_ feature: MapFeature)
  func FeatureLongClick(_ feature: MapFeature)
  func FeatureStartDrag(_ feature: MapFeature)
  func FeatureDrag(_ feature: MapFeature)
  func FeatureStopDrag(_ feature: MapFeature)
  func LoadError(_ url: String, _ responseCode: Int32, _ message: String)
  func GotFeatures(_ url: String, _ features: [Any])
  func getMap() -> Map
  func addFeature(_ feature: MapFeature)
  func removeFeature(_ feature: MapFeature)
}

public protocol HasFill {
  var FillColor: Int32 { get set }
  var FillOpacity: Float { get set }
}

public protocol HasStroke {
  var StrokeColor: Int32 { get set }
  var StrokeOpacity: Float { get set }
  var StrokeWidth: Int32 { get set }
}

@objc public protocol MapFeatureCollection: MapFeatureContainer {
  var Source: String { get set }
  var Visible: Bool { get set }

  func LoadFromURL(_ url: String)
  func GotFeatures(_ url: String, _ features: [Any])
  func LoadError(_ url: String, _ responseCode: Int32, _ message: String)
}

public protocol MapCircle: MapFeature, HasFill, HasStroke {
  var Radius: Double { get set }
  var Latitude: Double { get set }
  var Longitude: Double { get set }
  
  func SetLocation(_ latitude: Double, _ longitude: Double)
  func updateCenter(_ latitude: Double, _ longitude: Double)
}

public protocol MapRectangle: MapFeature, HasFill, HasStroke {
  var EastLongitude: Double { get set }
  var NorthLatitude: Double { get set }
  var SouthLatitude: Double { get set }
  var WestLongitude: Double { get set }
  var Center: [Double] { get }
  var Bounds: [[Double]] { get }
  
  /**
   * Updates the rectangle from the native view. This method _should not_ attempt to update the
   * native view as this may result in an infinite loop since the update may call this
   * implementation.
   *
   * @param north  the north latitude of the rectangle bounds
   * @param west  the west longitude of the rectangle bounds
   * @param south  the south latitude of the rectangle bounds
   * @param east  the east longitude of the rectangle bounds
   */
  func updateBounds(north: Double, west: Double, south: Double, east: Double)
  func SetCenter(_ latitude: Double, _ longitude: Double)
}

public protocol MapMarker: MapFeature, HasFill, HasStroke {
  var ImageAsset: String { get set }
  var Latitude: Double { get set }
  var Longitude: Double { get set }
  var AnchorHorizontal: Int32 { get set }
  var AnchorVertical: Int32 { get set }
  var ShowShadow: Bool { get set }
  var Width: Int32 { get set }
  var Height: Int32 { get set }
  
  func SetLocation(_ latitude: Double, _ longitude: Double)
}

public protocol MapLineString: MapFeature, HasStroke {
  var Points: [Any] { get set }
  func updatePoints(points: [Any]) throws
}

public protocol MapPolygon: MapFeature, HasFill, HasStroke {
  var Points: [Any] { get set }
  var HolePoints: [Any] { get set }

  func updatePoints(points: [Any]) throws
  func updateHolePoints(points: [Any]) throws
}

public enum MapFeatureType: String {
  case TYPE_MARKER = "Marker"
  case TYPE_CIRCLE = "Circle"
  case TYPE_RECTANGLE = "Rectangle"
  case TYPE_POINT = "Point"
  case TYPE_LINESTRING = "LineString"
  case TYPE_POLYGON = "Polygon"
  case TYPE_MULTIPOINT = "MultiPoint"
  case TYPE_MULTILINESTRING = "MultiLineString"
  case TYPE_MULTIPOLYGON = "MultiPolygon"
}

enum MapType: Int32 {
  case ROADS = 1
  case AERIAL = 2
  case TERRAIN = 3
}
