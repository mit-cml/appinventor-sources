// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MapKit
import GEOSwift

let kOneDegreeMeters = kEarthRadius * .pi / 180

/**
 * A class used to display a callout on an overlay (Circle, Line, Polygon, Rectangle)
 * Because we only care about the callout, size and width are 0
 */
open class PolygonMarker: Marker {
  public override init(_ container: MapFeatureContainer) {
    super.init(container)
    container.getMap().featureCount -= 1
    self.EnableInfobox = true
    self.Height = 0
    self.Width = 0
  }
}

@objc open class PolygonBase: MapFeatureBase {
  var _overlay: MapOverlayShape? = nil
  internal var needFullUpdate = true
  internal var initialized = false
  fileprivate var _calloutEnabled = false
  fileprivate var _marker: PolygonMarker
  fileprivate var _fillColor: Int32 = 0
  fileprivate var _fillOpacity: Float = 1
  fileprivate var _strokeColor: Int32 = 0
  fileprivate var _strokeOpacity: Float = 1
  fileprivate var _strokeWidth: Int32 = 0
  fileprivate var _visible: Bool = true

  init(container: MapFeatureContainer) {
    _marker = PolygonMarker(container)
    super.init(container: container, view: MKAnnotationView())
  }

  @objc open func Initialize() {
    makeShape()
    initialized = true
  }

  /**
   * This is the marker used to show an Infobox for a Shape
   * It is added and removed by the parent Map
   */
  open var marker: PolygonMarker {
    return _marker
  }

  // MARK: methods
  open override var Centroid: CLLocationCoordinate2D {
    get {
      return CLLocationCoordinate2DMake(_shape?.centroid()?.coordinate.y ?? 0, _shape?.centroid()?.coordinate.x ?? 0)
    }
  }

  @objc open override var Description: String {
    didSet {
      _marker.Description = annotation.subtitle ?? ""
    }
  }

  @objc open override var EnableInfobox: Bool {
    get {
      return _calloutEnabled
    }
    set(enable) {
      _calloutEnabled = enable
    }
  }

  @objc open var FillColor: Int32 {
    get {
      return _fillColor
    }
    set(color) {
      _overlay?.fillColor = color
      _fillColor = color
    }
  }
  
  @objc open var FillOpacity: Float {
    get {
      return _fillOpacity
    }
    set(opacity) {
      _overlay?.fillOpacity = opacity
      _fillOpacity = opacity
    }
  }

  @objc open override var StrokeColor: Int32 {
    get {
      return _strokeColor
    }
    set(color) {
      _overlay?.strokeColor = color
      _strokeColor = color
    }
  }
  
  @objc open override var StrokeOpacity: Float {
    get {
      return _strokeOpacity
    }
    set(opacity) {
      _overlay?.strokeOpacity = opacity
      _strokeOpacity = opacity
    }
  }

  @objc open override var StrokeWidth: Int32 {
    get {
      return _strokeWidth
    }
    set(width) {
      _overlay?.strokeWidth = width
      _strokeWidth = width
    }
  }

  @objc open override var Title: String? {
    didSet {
      _marker.Title = annotation.title
    }
  }

  @objc open override var Visible: Bool {
    get {
      return _visible
    }
    set {
      _visible = newValue
      _overlay?.visible = newValue
    }
  }

  /**
   * This is what is added to the map.
   */
  open var overlay: MapOverlayShape? {
    get {
      return _overlay
    }
    set(newOverlay) {
      let oldOverlay = _overlay
      if let newOverlay = newOverlay {
        newOverlay.fillColor = _fillColor
        newOverlay.fillOpacity = _fillOpacity
        newOverlay.strokeColor = _strokeColor
        newOverlay.strokeOpacity = _strokeOpacity
        newOverlay.strokeWidth = _strokeWidth
        newOverlay.visible = _visible
        newOverlay.feature = self
        _overlay = newOverlay
      }
      if let new = self._overlay {
        map?.replaceFeature(from: oldOverlay, to: new)
      }
    }
  }

  /**
   * This is called when the shape is dragged
   * Implementation is overriden by subclasses, except Circle
   */
  open func update(_ latitude: Double, _ longitude: Double) {
    updateCenter(latitude, longitude)
  }

  // used by Circle
  open func updateCenter(_ latitude: Double, _ longitude: Double) {}

  internal func makeShape() {}

  // MARK: shared methods
  @objc open override func HideInfobox() {
    _marker.HideInfobox()
  }
  
  @objc open override func ShowInfobox() {
    _marker.SetLocation(Centroid.latitude, Centroid.longitude)
    map?.removeFeature(_marker)
    map?.addFeature(_marker)
    let deadlineTime = DispatchTime.now() + .milliseconds(500)
    DispatchQueue.main.asyncAfter(deadline: deadlineTime) {
      self._marker.ShowInfobox()
    }
  }
}
