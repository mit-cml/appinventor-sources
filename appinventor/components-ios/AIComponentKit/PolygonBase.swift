// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

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
  fileprivate var _strokeColor: Int32 = 0
  fileprivate var _strokeWidth: Int32 = 0
  fileprivate var _visible: Bool = true

  init(container: MapFeatureContainer) {
    _marker = PolygonMarker(container)
    super.init(container: container, view: MKAnnotationView())
    overlay = _overlay
  }

  open func Initialize() {
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

  open override var Description: String {
    didSet {
      _marker.Description = _description
    }
  }

  open override var EnableInfobox: Bool {
    get {
      return _calloutEnabled
    }
    set(enable) {
      _calloutEnabled = enable
    }
  }

  open var FillColor: Int32 {
    get {
      return _fillColor
    }
    set(color) {
      _overlay?.fillColor = color
      _fillColor = color
    }
  }

  open override var StrokeColor: Int32 {
    get {
      return _strokeColor
    }
    set(color) {
      _overlay?.strokeColor = color
      _strokeColor = color
    }
  }

  open override var StrokeWidth: Int32 {
    get {
      return _strokeWidth
    }
    set(width) {
      _overlay?.strokeWidth = width
      _strokeWidth = width
    }
  }

  open override var title: String? {
    didSet {
      _marker.title = _title
    }
  }

  open override var Visible: Bool {
    didSet {
      _visible = Visible
      _overlay?.visible = Visible
    }
  }

  /**
   * This is what is added to the map.
   * Execution is on a DispatchQueue to prevent a race condition with the MKMapView
   */
  open var overlay: MapOverlayShape? {
    get {
      return _overlay
    }
    set(newOverlay) {
      let addNeeded = !initialized
      DispatchQueue.main.async {
        let oldOverlay = self._overlay
        self._overlay = newOverlay
        self._overlay?.fillColor = self._fillColor
        self._overlay?.strokeColor = self._strokeColor
        self._overlay?.strokeWidth = self._strokeWidth
        self._overlay?.visible = self._visible
        self._overlay?.feature = self
        if addNeeded {
          self._map?.addFeature(self)
        } else if let old = oldOverlay, let new = self._overlay {
          self._map?.replaceFeature(from: old, to: new)
        }
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
  open override func HideInfobox() {
    _marker.HideInfobox()
  }
  
  open override func ShowInfobox() {
    _marker.SetLocation(Centroid.latitude, Centroid.longitude)
    _map?.removeFeature(_marker)
    _map?.addFeature(_marker)
    DispatchQueue.main.async {
      self._marker.ShowInfobox()
    }
  }
}
