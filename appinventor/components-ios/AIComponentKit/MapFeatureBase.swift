// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import UIKit
import CoreLocation
import MapKit
import GEOSwift

@objc open class MapFeatureBase: NSObject, MKAnnotation, MapFeature, HasStroke, LifecycleDelegate {

  weak var _container: MapFeatureContainer?
  var _draggable = false
  var _description = ""
  weak var _map: Map? = nil
  var _title: String? = nil
  public var _view: MKAnnotationView

  // used to manually trigger drag events
  var _dragTimer: Timer?
  public var callout = UILabel()

  // This object is used to calculate distance between features
  var _shape: Geometry? = nil

  public var index: Int32

  public var coordinate: CLLocationCoordinate2D

  init(container: MapFeatureContainer, view: MKAnnotationView) {
    _container = container
    _view = view
    _map = container.getMap()
    coordinate = CLLocationCoordinate2DMake(0, 0)
    Type = ""
    index = container.getMap().featureCount
    container.getMap().featureCount += 1
    self.dispatchDelegate = container.form
    super.init()
    Description = ""
    Visible = true
    StrokeColor = -16777216
    StrokeWidth = 1
    Draggable = false
    title = ""
    callout.widthAnchor.constraint(lessThanOrEqualToConstant: 300).isActive = true
    callout.heightAnchor.constraint(greaterThanOrEqualToConstant: 1).isActive = true
    callout.numberOfLines = 0
    callout.lineBreakMode = .byWordWrapping
    _view.annotation = self
    DispatchQueue.main.async {
      container.addFeature(self)
    }
  }

  public func onResume() {

  }

  public func onPause() {
    if let timer = _dragTimer,  timer.isValid {
      StopDrag()
    }
    stopDrag()
  }

  public func onDelete() {
    stopDrag()
  }

  public func onDestroy() {
    stopDrag()
  }

  open func setMap(container: MapFeatureContainer) {
    _map = container.getMap()
  }

  open var geometry: Geometry? {
    get {
      return _shape
    }
  }

  // MARK: properties
  open var Centroid: CLLocationCoordinate2D {
    get {
      return coordinate
    }
  }

  open var Description: String {
    get {
      return _description
    }
    set(description) {
      _description = description
      setText()
    }
  }

  open var subtitle: String? {
    get {
      return _description
    }
  }

  open var Draggable: Bool {
    get {
      return _draggable
    }
    set(draggable) {
      _draggable = draggable
    }
  }

  open var EnableInfobox: Bool {
    get {
      return false
    }
    set(enabled) {}
  }

  open var StrokeColor: Int32 {
    get {
      guard let borderColor = _view.layer.borderColor else {
        return 0
      }
      return colorToArgb(UIColor(cgColor: borderColor))
    } set(color) {
      _view.layer.borderColor = argbToColor(color).cgColor
    }
  }

  open var StrokeWidth: Int32 {
    get {
      return Int32(_view.layer.borderWidth)
    }
    set(width) {
      _view.layer.borderWidth = CGFloat(width)
    }
  }

  open var title: String? {
    get {
      return " "
    }
    set(title) {
      _title = title!
      setText()
    }
  }

  // For App Inventor naming compatability
  open func Title() -> String {
    return _title ?? ""
  }

  open var `Type`: String


  open var Visible: Bool {
    get {
      return !_view.isHidden
    }
    set(visible) {
      _view.isHidden = !visible
    }
  }

  // This method is used to represent the callout text
  private func setText() {
    let text = NSMutableAttributedString(string: _title ?? "", attributes: [NSAttributedString.Key.font:UIFont.preferredFont(forTextStyle: .title3)])
    text.append(NSAttributedString(string: "\(_title == "" ? "": "\n")\(_description)", attributes: [NSAttributedString.Key.font:UIFont.preferredFont(forTextStyle: .body)]))
    callout.attributedText = text
  }

  // MARK: events
  open func Click() {
    EventDispatcher.dispatchEvent(of: self, called: "Click")
    _container?.FeatureClick(self)
  }

  open func Drag() {
    EventDispatcher.dispatchEvent(of: self, called: "Drag")
    _container?.FeatureDrag(self)
  }

  open func LongClick() {
    EventDispatcher.dispatchEvent(of: self, called: "LongClick")
    _container?.FeatureLongClick(self)
  }

  open func StartDrag() {
    EventDispatcher.dispatchEvent(of: self, called: "StartDrag")
    _container?.FeatureStartDrag(self)
  }

  open func StopDrag() {
    EventDispatcher.dispatchEvent(of: self, called: "StopDrag")
    _container?.FeatureStopDrag(self)
  }

  // MARK: Methods
  open func DistanceToFeature(_ mapFeature: MapFeatureBase, _ centroids: Bool) -> Double {
    if centroids {
      return distance(from: Centroid, to: mapFeature.Centroid)
    } else {
      if let circle = mapFeature as? Circle {
        return circle.DistanceToFeature(self, centroids)
      } else if let thisShape = _shape, let otherShape = mapFeature.geometry {
        return thisShape.distance(geometry: otherShape) * kOneDegreeMeters
      } else {
        return 0
      }
    }
  }

  open func DistanceToPoint(_ latitude: Double, _ longitude: Double, _ centroids: Bool) -> Double {
    let target = CLLocationCoordinate2DMake(latitude,longitude)
    if centroids {
      if let thisShape = _shape, let point = Waypoint(latitude: latitude, longitude: longitude) {
        let nearest = thisShape.nearestPoint(point)
        return distance(from: CLLocationCoordinate2DMake(nearest.y, nearest.x), to: target)
      } else {
        return 0
      }
    } else {
      return distance(from: Centroid, to: target)
    }
  }

  open func HideInfobox() {}

  open func ShowInfobox() {}

  // MARK: drag helpers
  open func startDrag() {
    _dragTimer?.invalidate()
    _dragTimer = Timer.scheduledTimer(timeInterval: 0.01, target: self, selector: #selector(Drag), userInfo: nil, repeats: true) // we have to start a timer, because MKMapView does not fire a drag event
  }

  open func stopDrag() {
    _dragTimer?.invalidate()
  }

  func timerFired() {
    _map?.FeatureDrag(self)
  }

  open func removeFromMap() {
    _map?.removeFeature(self)
  }

  public var dispatchDelegate: HandlesEventDispatching

  public func copy(with zone: NSZone? = nil) -> Any {
    return self
  }

  /**
   * MARK: methods for handling copying
   * These methods primarily exist for passing features between Maps
   */
  open func copy(container: MapFeatureContainer) {}

  open func copy(from source: MapFeatureBase) {
    self.Draggable = source.Draggable
    self.Description = source.Description
    self.EnableInfobox = source.EnableInfobox
    self.StrokeColor = source.StrokeColor
    self.StrokeWidth = source.StrokeWidth
    self.title = source.Title()
    self.Visible = source.Visible
  }
}
