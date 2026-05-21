// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MapKit
import GEOSwift

// the number of edges for the circle. Used to match Android
let kNumCircleEdges = 60

@objc open class Circle: PolygonBase, MapCircle {
  fileprivate var _radius: Double = 0

  @objc public init(_ container: MapFeatureContainer) {
    super.init(container: container)
    Type = MapFeatureType.TYPE_CIRCLE.rawValue
    FillColor = colorToArgb(UIColor.red)
    FillOpacity = 1
  }

  // MARK: properties
  @objc open var Latitude: Double {
    get {
      return overlay?.coordinate.latitude ?? 0
    }
    set(latitude) {
      if !(-90.0...90 ~= latitude) {
        _container?.form?.dispatchErrorOccurredEvent(self, "Latitude",
            ErrorMessage.ERROR_INVALID_LATITUDE.code,
            ErrorMessage.ERROR_INVALID_LATITUDE.message, latitude)
      } else {
        annotation.coordinate.latitude = latitude
        if initialized {
          makeShape()
        }
      }
    }
  }

  @objc open var Longitude: Double {
    get {
      return overlay?.coordinate.longitude ?? 0
    }
    set(longitude) {
      if !(-180.0...180 ~= longitude) {
        _container?.form?.dispatchErrorOccurredEvent(self, "Longitude",
            ErrorMessage.ERROR_INVALID_LONGITUDE.code,
            ErrorMessage.ERROR_INVALID_LONGITUDE.message, longitude)
      } else {
        annotation.coordinate.longitude = longitude
        if initialized {
          makeShape()
        }
      }
    }
  }

  @objc open var Radius: Double {
    get {
      return _radius
    }
    set(radius) {
      _radius = radius
      if initialized {
        makeShape()
      }
    }
  }

  // MARK: methods
  @objc open override func DistanceToFeature(_ mapFeature: MapFeatureBase, _ centroids: Bool) -> Double {
    if centroids {
      return super.DistanceToFeature(mapFeature, true)
    } else {
      return max(mapFeature.DistanceToPoint(Latitude, Longitude, false) - Radius, 0)
    }
  }

  @objc open override func DistanceToPoint(_ latitude: Double, _ longitude: Double, _ centroids: Bool) -> Double {
    return max(super.DistanceToPoint(latitude, longitude, false) - (centroids ? 0: Radius), 0)
  }

  @objc public func SetLocation(_ latitude: Double, _ longitude: Double) {
    if !(-90.0...90 ~= latitude) {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetLocation",
          ErrorMessage.ERROR_INVALID_LATITUDE.code,
          ErrorMessage.ERROR_INVALID_LATITUDE.message, latitude)
      return
    }
    if !(-180.0...180 ~= longitude) {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetLocation",
          ErrorMessage.ERROR_INVALID_LONGITUDE.code,
          ErrorMessage.ERROR_INVALID_LONGITUDE.message, longitude)
      return
    }
    annotation.coordinate = CLLocationCoordinate2DMake(latitude, longitude)
    makeShape()
  }

  // MARK: private methods
  override open func updateCenter(_ latitude: Double, _ longitude: Double) {
    SetLocation(Latitude + latitude, Longitude + longitude)
  }

  /**
   * method used to recreate the shape.
   * Since iOS has a Circle, this is only used for internal distance and tap calculations
   */
  override func makeShape() {
    var geoString = "POLYGON(("
    for i in 0...kNumCircleEdges - 1 {
      geoString += coordinate(2 * .pi * Double(i) / Double(kNumCircleEdges))
    }
    geoString += coordinate(0) // for rounding errors
    if let shape = Geometry.create(geoString.chopSuffix() + "))") {
      _shape = shape
      overlay = MapCircleOverlay(center: annotation.coordinate, radius: _radius)
    }
  }

  fileprivate func coordinate(_ bearing: Double) -> String {
    let iLat = degToRad(annotation.coordinate.latitude)
    let iLon = degToRad(annotation.coordinate.longitude)
    let radiusChange = _radius / kEarthRadius
    let fLat = asin(sin(iLat) * cos(radiusChange) + cos(iLat) * sin(radiusChange) * cos(bearing))
    let fLon = iLon + atan2(sin(bearing) * sin(radiusChange) * cos(iLat), cos(radiusChange) - sin(iLat) * sin(fLat))
    return "\(radToDeg(fLon)) \(radToDeg(fLat)),"
  }

  open override func copy(container: MapFeatureContainer) {
    let circle = Circle(container)
    circle.copy(from: self)
    circle.FillColor = FillColor
    circle.Latitude = Latitude
    circle.Longitude = Longitude
    circle.Radius = Radius
    circle.index = index
  }
}
