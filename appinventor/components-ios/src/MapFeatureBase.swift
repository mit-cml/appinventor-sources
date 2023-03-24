// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit
import CoreLocation
import MapKit
import GEOSwift

/**
 * The `MapFeatureAnnotation` class encapsulates any annotation created by a MapFeature.
 */
@objc open class MapFeatureAnnotation: NSObject, MKAnnotation {
  /**
   * The location of the feature on the map. The annotation view will be positioned at this point.
   */
  public var coordinate = CLLocationCoordinate2D()

  /**
   * The title shown in the annotation's detail view.
   */
  public var title: String?

  /**
   * The subtitle shown in the annotation's detail view.
   */
  public var subtitle: String?

  /**
   * The view associated with the annotation.
   *
   * **Note**: This must be a weak reference to prevent a retain cycle as the view strongly holds
   * a reference to the annotation.
   */
  public weak var view: MKAnnotationView?

  /**
   * The label shown on the popup that appears when the user interacts with the annotation's view.
   */
  public var callout = UILabel()

  /**
   * Construct a new annotation.
   *
   * - Parameter aView: The view associated with this annotation.
   */
  public init(view aView: MKAnnotationView) {
    view = aView
    super.init()
    callout.widthAnchor.constraint(lessThanOrEqualToConstant: 300).isActive = true
    callout.heightAnchor.constraint(greaterThanOrEqualToConstant: 1).isActive = true
    callout.numberOfLines = 0
    callout.lineBreakMode = .byWordWrapping
    aView.detailCalloutAccessoryView = callout
  }
}

@objc open class MapFeatureBase: NSObject, MapFeature, HasStroke, LifecycleDelegate {

  weak var _container: MapFeatureContainer?
  var _draggable = false

  // used to manually trigger drag events
  var _dragTimer: Timer?

  // This object is used to calculate distance between features
  var _shape: Geometry? = nil

  public var index: Int32

  private var _annotation: MapFeatureAnnotation

  init(container: MapFeatureContainer, view: MKAnnotationView) {
    _container = container
    Type = ""
    index = container.getMap().featureCount
    container.getMap().featureCount += 1
    _annotation = MapFeatureAnnotation(view: view)
    super.init()
    Description = ""
    Visible = true
    StrokeColor = -16777216
    StrokeOpacity = 1
    StrokeWidth = 1
    Draggable = false
    Title = ""
    container.addFeature(self)
  }

  @objc public func onPause() {
    if let timer = _dragTimer,  timer.isValid {
      StopDrag()
    }
    stopDrag()
  }

  @objc public func onDelete() {
    stopDrag()
  }

  @objc public func onDestroy() {
    stopDrag()
  }

  public var annotation: MapFeatureAnnotation {
    return _annotation
  }

  public var map: Map? {
    return _container?.getMap()
  }

  open var geometry: Geometry? {
    get {
      return _shape
    }
  }

  // MARK: properties
  open var Centroid: CLLocationCoordinate2D {
    get {
      return annotation.coordinate
    }
  }

  open var Description: String {
    get {
      return annotation.subtitle ?? ""
    }
    set(description) {
      annotation.subtitle = description
      setText()
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

  @objc open var StrokeColor: Int32 {
    get {
      guard let borderColor = annotation.view?.layer.borderColor else {
        return 0
      }
      return colorToArgb(UIColor(cgColor: borderColor))
    } set(color) {
      guard let opacity = annotation.view?.layer.borderColor?.alpha else {
        return
      }
      annotation.view?.layer.borderColor = argbToColor(color).withAlphaComponent(opacity).cgColor
    }
  }
  
  @objc open var StrokeOpacity: Float {
    get {
      guard let borderColor = annotation.view?.layer.borderColor else {
        return 0
      }
      return Float(borderColor.alpha)
    } set(opacity) {
      annotation.view?.layer.borderColor = annotation.view?.layer.borderColor?.copy(alpha: CGFloat(opacity))
    }
  }

  @objc open var StrokeWidth: Int32 {
    get {
      return Int32(annotation.view?.layer.borderWidth ?? 0)
    }
    set(width) {
      annotation.view?.layer.borderWidth = CGFloat(width)
    }
  }

  open var Title: String? {
    get {
      return annotation.title
    }
    set(title) {
      annotation.title = title
      setText()
    }
  }

  open var `Type`: String


  open var Visible: Bool {
    get {
      return !(annotation.view?.isHidden ?? true)
    }
    set(visible) {
      annotation.view?.isHidden = !visible
    }
  }

  // This method is used to represent the callout text
  private func setText() {
    let subTitle = annotation.subtitle ?? ""
    let text = NSAttributedString(string: subTitle,
        attributes: [NSAttributedString.Key.font:UIFont.preferredFont(forTextStyle: .body)])
    annotation.callout.attributedText = text
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
  @objc open func DistanceToFeature(_ mapFeature: MapFeatureBase, _ centroids: Bool) -> Double {
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

  @objc open func DistanceToPoint(_ latitude: Double, _ longitude: Double, _ centroids: Bool) -> Double {
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

  @objc open func HideInfobox() {}

  @objc open func ShowInfobox() {}

  // MARK: drag helpers
  open func startDrag() {
    _dragTimer?.invalidate()
    _dragTimer = Timer.scheduledTimer(timeInterval: 0.01, target: self, selector: #selector(Drag), userInfo: nil, repeats: true) // we have to start a timer, because MKMapView does not fire a drag event
  }

  open func stopDrag() {
    _dragTimer?.invalidate()
  }

  func timerFired() {
    _container?.FeatureDrag(self)
  }

  open func removeFromMap() {
    _container?.getMap().removeFeature(self)
  }

  public var dispatchDelegate: HandlesEventDispatching? {
    return _container?.form?.dispatchDelegate
  }

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
    self.Title = source.Title
    self.Visible = source.Visible
  }
}
