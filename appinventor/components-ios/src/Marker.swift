// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MapKit
import GEOSwift

let kDefaultMarkerWidth: CGFloat = 32
let kDefaultMarkerHeight: CGFloat = 39

/**
 * MKAnnotationView does not appear to allow a UILongPressGestureRecognizer to
 * fire if it's isDraggable property is set to false. We use this delegate to
 * override this behavior so that we can detect LongPress events even if we
 * are not dragging the Marker.
 */
class LCHelper : NSObject, UIGestureRecognizerDelegate {
  func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
    return true
  }

  func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive press: UIPress) -> Bool {
    return true
  }

  func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
    return true
  }

  func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRequireFailureOf otherGestureRecognizer: UIGestureRecognizer) -> Bool {
    return false
  }

  func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldBeRequiredToFailBy otherGestureRecognizer: UIGestureRecognizer) -> Bool {
    return false
  }

  func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
    return true
  }
}

@objc open class Marker: MapFeatureBase, MapMarker {
  fileprivate var _imageView = MKAnnotationView()
  fileprivate var _pinView = MKPinAnnotationView()

  fileprivate var _anchorHorizontal: HorizontalGravity = .center
  fileprivate var _anchorVertical: VerticalGravity = .bottom
  fileprivate var _height: Int32 = kLengthPreferred
  fileprivate var _width: Int32 = kLengthPreferred
  fileprivate var _preferredWidth: CGFloat = kDefaultMarkerWidth
  fileprivate var _preferredHeight: CGFloat = kDefaultMarkerHeight
  fileprivate var _imagePath: String = "empty"
  fileprivate var _strokeColor: Int32 = colorToArgb(UIColor.black)
  fileprivate var _strokeOpacity: Float = 1
  fileprivate var _strokeWidth: Int32 = 1
  fileprivate static let _lchelper = LCHelper()

  typealias View = MKAnnotationView

  @objc public init(_ container: MapFeatureContainer) {
    ShowShadow = false
    _pinView.pinTintColor = MKPinAnnotationView.redPinColor()
    _imageView.addSubview(_pinView)
    super.init(container: container, view: _imageView)
    Type = MapFeatureType.TYPE_MARKER.rawValue
    intializeConstraints()
    AnchorHorizontal = HorizontalGravity.center.rawValue
    AnchorVertical = VerticalGravity.bottom.rawValue
    ImageAsset = ""
    FillColor = colorToArgb(UIColor.red)
    FillOpacity = 1
  }

  private func intializeConstraints() {
    _pinView.annotation = annotation
    _pinView.translatesAutoresizingMaskIntoConstraints = false
    _pinView.leftAnchor.constraint(equalTo: _imageView.leftAnchor).isActive = true
    _pinView.rightAnchor.constraint(equalTo: _imageView.rightAnchor).isActive = true
    _pinView.topAnchor.constraint(equalTo: _imageView.topAnchor).isActive = true
    _pinView.bottomAnchor.constraint(equalTo: _imageView.bottomAnchor).isActive = true
    _imageView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(handleTap)))
    let gesture = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress))
    gesture.delegate = Marker._lchelper
   _imageView.addGestureRecognizer(gesture)
  }

  // used for resizing the Marker when the parent Map size changes
  open func resize() {
    setHeight(_height)
    setWidth(_width)
    AnchorHorizontal = _anchorHorizontal.rawValue
    AnchorVertical = _anchorVertical.rawValue
  }

  @objc open var AnchorHorizontal: Int32 {
    get {
      return _anchorHorizontal.rawValue
    }
    set(anchor) {
      if !(1...3 ~= anchor) {
        _container?.form?.dispatchErrorOccurredEvent(self, "AnchorHorizontal",
            ErrorMessage.ERROR_INVALID_ANCHOR_HORIZONTAL.code,
            ErrorMessage.ERROR_INVALID_ANCHOR_HORIZONTAL.message, anchor)
        return
      }

      _anchorHorizontal = HorizontalGravity(rawValue: anchor)!
      switch _anchorHorizontal {
      case .center:
        _imageView.centerOffset.x = 0
      case .left:
        _imageView.centerOffset.x = _preferredWidth / 2
      case .right:
        _imageView.centerOffset.x = -_preferredWidth / 2
      }
    }
  }

  @objc open var AnchorVertical: Int32 {
    get {
      return _anchorVertical.rawValue
    }
    set(anchor) {
      if !(1...3 ~= anchor) {
        _container?.form?.dispatchErrorOccurredEvent(self, "AnchorVertical",
            ErrorMessage.ERROR_INVALID_ANCHOR_VERTICAL.code,
            ErrorMessage.ERROR_INVALID_ANCHOR_VERTICAL.message, anchor)
        return
      }

      _anchorVertical = VerticalGravity(rawValue: anchor)!
      switch _anchorVertical {
      case .bottom:
        _imageView.centerOffset.y = -_preferredHeight / 2
      case .center:
        _imageView.centerOffset.y = 0
      case .top:
        _imageView.centerOffset.y = _preferredHeight / 2
      }
    }
  }

  @objc open override var Draggable: Bool {
    didSet {
      _imageView.isDraggable = _draggable
      _pinView.isDraggable = _draggable
    }
  }

  @objc open override var EnableInfobox: Bool {
    get {
      return _imageView.canShowCallout
    }
    set(enabled) {
      _imageView.canShowCallout = enabled
      _pinView.canShowCallout = enabled
    }
  }

  @objc open var FillColor: Int32 {
    get {
      return colorToArgb(_pinView.pinTintColor)
    }
    set(color) {
      _pinView.pinTintColor = argbToColor(color).withAlphaComponent(_pinView.pinTintColor.cgColor.alpha)
      _imageView.backgroundColor = _imagePath == "" ? UIColor.clear: _pinView.pinTintColor
    }
  }
  
  @objc open var FillOpacity: Float {
    get {
      return Float(_pinView.pinTintColor.cgColor.alpha)
    }
    set(opacity) {
      _pinView.pinTintColor = _pinView.pinTintColor.withAlphaComponent(CGFloat(opacity))
      _imageView.backgroundColor = _imagePath == "" ? UIColor.clear: _pinView.pinTintColor
    }
  }

  @objc open var ImageAsset: String {
    get {
      return _imagePath
    }
    set(path) {
      if path != _imagePath {
        var preferredWidth = kDefaultMarkerWidth
        var preferredHeight = kDefaultMarkerHeight
        if let image = AssetManager.shared.imageFromPath(path: path) {
          preferredHeight = image.size.height
          preferredWidth = image.size.width
          _imageView.image = image
          _imagePath = path
          _pinView.isHidden = true
        } else {
          _imageView.image = nil
          _imagePath = ""
          _pinView.isHidden = false
        }
        if _height == kLengthPreferred {
          _preferredHeight = preferredHeight
        }
        if _width == kLengthPreferred {
          _preferredWidth = preferredWidth
        }
        StrokeColor = _strokeColor
        StrokeWidth = _strokeWidth
        _imageView.backgroundColor = _imagePath == "" ? UIColor.clear: _pinView.pinTintColor
        resize()
      }
    }
  }

  @objc open var Height: Int32 {
    get {
      return _height
    }
    set(height) {
      _height = height
      resize()
    }
  }

  @objc open func setHeightPercent(_ height: Int32) {
    Height = -(1000 + height)
  }

  @objc open var Latitude: Double {
    get {
      return annotation.coordinate.latitude
    }
    set(latitude) {
      if !(-90.0...90 ~= latitude) {
        _container?.form?.dispatchErrorOccurredEvent(self, "Latitude",
            ErrorMessage.ERROR_INVALID_LATITUDE.code,
            ErrorMessage.ERROR_INVALID_LATITUDE.message, latitude)
      } else {
        annotation.coordinate.latitude = latitude
        _shape = Waypoint(latitude: annotation.coordinate.latitude,
            longitude: annotation.coordinate.longitude)
      }
    }
  }

  @objc open var Longitude: Double {
    get {
      return annotation.coordinate.longitude
    }
    set(longitude) {
      if !(-180.0...180 ~= longitude) {
        _container?.form?.dispatchErrorOccurredEvent(self, "Longitude",
            ErrorMessage.ERROR_INVALID_LONGITUDE.code,
            ErrorMessage.ERROR_INVALID_LONGITUDE.message, longitude)
      } else {
        annotation.coordinate.longitude = longitude
        _shape = Waypoint(latitude: annotation.coordinate.latitude,
            longitude: annotation.coordinate.longitude)
      }
    }
  }

  /*
   * Deprecated. Included for backwards compatability
   */
  @objc public var ShowShadow: Bool

  @objc open override var StrokeColor: Int32 {
    get {
      return _strokeColor
    }
    set(color) {
      _strokeColor = color
      _imageView.layer.borderColor = _imagePath == "" ? UIColor.clear.cgColor: argbToColor(color).cgColor
    }
  }

  @objc open override var StrokeWidth: Int32 {
    get {
      return _strokeWidth
    }
    set(width) {
      _strokeWidth = width
      _imageView.layer.borderWidth = _imagePath == "" ? 0: CGFloat(width)
    }
  }

  @objc open var Width: Int32 {
    get {
      return _width
    }
    set(width) {
      _width = width
      resize()
    }
  }

  @objc open func setWidthPercent(_ width: Int32) {
    Width = -(1000 + width)
  }

  // MARK: height and width helpers
  private func setHeight(_ height: Int32) {
    if height >= 0 {
      _preferredHeight = CGFloat(height)
    } else if height == kLengthPreferred {
      _preferredHeight = _imagePath == "" ? kDefaultMarkerHeight: _imageView.frame.height
    } else if let parentHeight = map?.mapView.frame.height {
      if height == kLengthFillParent {
        _preferredHeight = parentHeight
      } else if height <= kLengthPercentTag {
        _preferredHeight = parentHeight * CGFloat(-(height + 1000)) / 100.0
      }
    }
    _imageView.frame.size.height = _preferredHeight
  }

  private func setWidth(_ width: Int32) {
    if width >= 0 {
      _preferredWidth = CGFloat(width)
    } else if width == kLengthPreferred {
      _preferredHeight = _imagePath == "" ? kDefaultMarkerWidth: _imageView.frame.width
    } else if let parentWidth = map?.mapView.frame.width {
      if width == kLengthFillParent {
        _preferredWidth = parentWidth
      } else if width <= kLengthPercentTag {
        _preferredWidth = parentWidth * CGFloat(-(width + 1000)) / 100.0
      }
    }
    _imageView.frame.size.width = _preferredWidth
    _imageView.calloutOffset.x = CGFloat(-_preferredWidth / 4)
  }

  // MARK: methods
  @objc open func BearingToFeature(_ feature: MapFeatureBase, _ centroids: Bool) -> Double {
    if centroids {
      return bearing(from: annotation.coordinate, to: feature.Centroid)
    } else if let thisShape = _shape, let otherShape = feature.geometry {
      let closestPoint = otherShape.nearestPoint(thisShape)
      return bearing(from: annotation.coordinate,
          to: CLLocationCoordinate2DMake(closestPoint.y, closestPoint.x))
    } else {
      return 0
    }
  }

  @objc open func BearingToPoint(_ latitude: Double, _ longitude: Double) -> Double {
    return bearing(from: annotation.coordinate,
        to: CLLocationCoordinate2D(latitude: latitude, longitude: longitude))
  }

  @objc open override func HideInfobox() {
    map?.mapView.deselectAnnotation(annotation, animated: true)
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
    _shape = Waypoint(latitude: annotation.coordinate.latitude,
        longitude: annotation.coordinate.longitude)
  }

  @objc open override func ShowInfobox() {
    var changed = false
    if !_imageView.canShowCallout {
      changed = true
      _imageView.canShowCallout = true
    }
    map?.mapView.selectAnnotation(annotation, animated: true)
    if changed {
      _imageView.canShowCallout = false
    }
  }

  open override func copy(container: MapFeatureContainer) {
    let newMarker = Marker(container)
    newMarker.copy(from: self)
    newMarker.AnchorHorizontal = _anchorHorizontal.rawValue
    newMarker.AnchorVertical = _anchorVertical.rawValue
    newMarker.FillColor = FillColor
    newMarker.FillOpacity = FillOpacity
    newMarker.Height = _height
    newMarker.ImageAsset = _imagePath
    newMarker.Latitude = Latitude
    newMarker.Longitude = Longitude
    newMarker.Width = _width
  }

  @objc fileprivate func handleTap(gesture: UITapGestureRecognizer) {
    Click()
  }

  @objc fileprivate func handleLongPress(gesture: UILongPressGestureRecognizer) {
    // On Android, LongClick won't fire if we are able to Drag (it causes a
    // DragStart/DragEnd event pair). We implement this check here to match
    // the behavior.
    if gesture.state == .ended && !Draggable {
      LongClick()
    }
  }
}
