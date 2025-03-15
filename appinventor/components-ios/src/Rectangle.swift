// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MapKit
import GEOSwift

@objc open class Rectangle: PolygonBase, MapRectangle {
  fileprivate var _east = 0.0
  fileprivate var _north = 0.0
  fileprivate var _south = 0.0
  fileprivate var _west = 0.0

  @objc public init(_ container: MapFeatureContainer) {
    super.init(container: container)
    FillColor = colorToArgb(UIColor.red)
    FillOpacity = 1
    Type = MapFeatureType.TYPE_RECTANGLE.rawValue
  }

  // MARK: properties
  @objc open var Bounds: [[Double]] {
    get {
      return [[_north, _west], [_south, _east]]
    }
  }

  @objc open var Center: [Double] {
    get {
      return [Centroid.latitude, Centroid.longitude]
    }
  }

  @objc public var EastLongitude: Double {
    get {
      return _east
    }
    set(east) {
      if validate(longitude: east, for: "EastLongitude") {
        _east = east
        if initialized {
          makeShape()
        }
      }
    }
  }

  @objc public var NorthLatitude: Double {
    get {
      return _north
    }
    set(north) {
      if validate(latitude: north, for: "NorthLatitude") {
        _north = north
        if initialized {
          makeShape()
        }
      }
    }
  }

  @objc public var SouthLatitude: Double {
    get {
      return _south
    }
    set(south) {
      if validate(latitude: south, for: "SouthLatitude") {
        _south = south
        if initialized {
          makeShape()
        }
      }
    }
  }

  @objc public var WestLongitude: Double {
    get {
      return _west
    }
    set(west) {
      if validate(longitude: west, for: "WestLongitude") {
        _west = west
        if initialized {
          makeShape()
        }
      }
    }
  }

  // MARK: Methods
  @objc open func SetCenter(_ latitude: Double, _ longitude: Double) {
    if !(-90.0...90 ~= latitude) {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetCenter",
          ErrorMessage.ERROR_INVALID_LATITUDE.code,
          ErrorMessage.ERROR_INVALID_LATITUDE.message, latitude)
      return
    }
    if !(-180.0...180 ~= longitude) {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetCenter",
          ErrorMessage.ERROR_INVALID_LONGITUDE.code,
          ErrorMessage.ERROR_INVALID_LONGITUDE.message, longitude)
      return
    }
    update(latitude - Centroid.latitude, longitude - Centroid.longitude)
  }

  // MARK: helper methods
  open override func update(_ latitude: Double, _ longitude: Double) {
    _east += longitude
    _north += latitude
    _south += latitude
    _west += longitude
    if initialized {
      makeShape()
    }
  }

  public func updateBounds(north: Double, west: Double, south: Double, east: Double) {
    _east = east
    _north = north
    _south = south
    _west = west
    if initialized {
      makeShape()
    }
  }

  internal override func makeShape() {
    let geoString = "POLYGON((\(_east) \(_north), \(_east) \(_south), \(_west) \(_south), \(_west) \(_north), \(_east) \(_north)))"
    if let shape = Geometry.create(geoString), let mapShape = shape.mapShape() as? MKPolygon {
      _shape = shape
      overlay = MapPolygonOverlay(points: mapShape.points(), count: mapShape.pointCount)
    } else {
      NSLog("An unexpected error occured in rectangle. This should not be happening")
    }
  }

  open override func copy(container: MapFeatureContainer) {
    let rectangle = Rectangle(container)
    rectangle.copy(from: self)
    rectangle.updateBounds(north: NorthLatitude, west: WestLongitude, south: SouthLatitude, east: EastLongitude)
    rectangle.FillColor = FillColor
    rectangle.FillOpacity = FillOpacity
  }

  fileprivate func validate(latitude: Double = 0, longitude: Double = 0, for methodName: String) -> Bool {
    if !(-90.0...90 ~= latitude) {
      _container?.form?.dispatchErrorOccurredEvent(self, methodName,
          ErrorMessage.ERROR_INVALID_LATITUDE.code,
          ErrorMessage.ERROR_INVALID_LATITUDE.message, latitude)
      return false
    }
    if !(-180.0...180 ~= longitude) {
      _container?.form?.dispatchErrorOccurredEvent(self, methodName,
          ErrorMessage.ERROR_INVALID_LONGITUDE.code,
          ErrorMessage.ERROR_INVALID_LONGITUDE.message, longitude)
      return false
    }
    return true
  }
}
