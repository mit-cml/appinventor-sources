// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MapKit
import CoreLocation
import GEOSwift
import Alamofire

private let ERROR_INVALID_NUMBER: String = "%s is not a valid number."
private let ERROR_LATITUDE_OUT_OF_BOUNDS: String = "Latitude %f is out of bounds."
private let  ERROR_LONGITUDE_OUT_OF_BOUNDS: String = "Longitude %f is out of bounds."
private let DEFAULT_CENTER: String = "42.359144, -71.093612"

private let MAX_ZOOM_LEVEL: Int = 18
private let MIN_ZOOM_LEVEL: Int = 1
private let kFingerSize: CGFloat = 1.0

private let ZOOM_LEVEL_0 = 80082944.031721031553788

func altitude(from zoom: Double) -> CLLocationDistance {
  return ZOOM_LEVEL_0 / pow(2.0, zoom - 1.0)
}

enum AIMapType: Int32 {
  case roads = 1
  case aerial = 2
  case terrain = 3
}

typealias CLLocationDirection = Double

// a custom map that resizes components as necessary (i.e. when device orientation changes). A workaround for a lack of constraints for Marker
class CustomMap: MKMapView {
  override func layoutSubviews() {
    super.layoutSubviews()
    for annotation in annotations {
      if let feature = annotation as? Marker {
        feature.resize()
      }
    }
  }

  open var initialized: Bool = false
}

/**
 * The Map Component.  A two dimensional-container that renders a map.
 */
open class Map: ViewComponent, MKMapViewDelegate, UIGestureRecognizerDelegate, MapFeatureContainer, LifecycleDelegate {

  /**
   * In order to clear the map's memory, we have to set the reference to nil for onDelete and onDestroy
   * This requires the map being optional
   * The non-underscored mapView serves as a wrapper
   * When used, _mapView should never be nil, but I added a check just in case
   */
  fileprivate var _mapView: CustomMap? = nil
  var mapView: CustomMap {
    get {
      return _mapView ?? CustomMap()
    }
  }

  private var _locationSensor: LocationSensor
  private var _mapType: AIMapType = .roads
  private var _scaleUnits: Int32 = 1 // swift doesn't have scale units??
  private var _zoomLevel: Int32 = 1
  private var _rotation: Float32 = 0.0
  private var _zoomControls: UIStackView
  private var _zoomInBtn: ZoomButton
  private var _zoomOutBtn: ZoomButton
  private var _mapIsReady: Bool = false
  private var _featuresState = 0
  private var _boundsChangeReady: Bool = false
  private var _terrainOverlay: MKTileOverlay?

  private var _activeOverlay: MapOverlayShape? = nil
  private var _lastPoint: CLLocationCoordinate2D? = nil
  private var _activeMarker: Marker? = nil
  private var _features = [MapFeature]()
  public var featureCount: Int32 = 0
  private var _heading: Double = 0

  /**
   * This value is used to indicate whether CenterFromString and ZoomLevel have been set
   * Used to prevent animating the initial rendering of the map
   */
  private var _initialized: Bool = false

  /**
   * These properties are used in order to properly trigger multiple updates to zoomLevel, center,
   * and the bounding box.
   * _zoomDidChange is used in order to properly fire the ZoomChanged() event.  Now, with
   * selfNeedsUpdate(), the update is changed prior to the BoundsChange being called.  Therefore, we
   * need to manually set when an update has been made.
   */
  private var _updatePending: Bool = false
  private var _zoomLevelUpdate: Int32? = nil
  private var _centerUpdate: CLLocationCoordinate2D? = nil
  private var _boundingBoxUpdate: MKMapRect? = nil
  private var _zoomDidChange: Bool = false

  private var _compass: AnyObject?
  @available(iOS 11.0, *)
  private var compass: MKCompassButton? {
    get {
      return _compass as? MKCompassButton
    }
    set(comp) {
      _compass = comp
    }
  }
  private var _scale: AnyObject?
  @available(iOS 11.0, *)
  private var scale: MKScaleView? {
    get {
      return _scale as? MKScaleView
    }
    set(scl) {
      _scale = scl
    }
  }

  public override init(_ parent: ComponentContainer) {
    featureCount = 0
    _mapView = CustomMap()
    _zoomInBtn = ZoomButton(zoom: .zoomIn)
    _zoomOutBtn = ZoomButton(zoom: .zoomOut)
    _zoomControls = UIStackView(arrangedSubviews: [_zoomInBtn, _zoomOutBtn])
    _locationSensor = AIComponentKit.LocationSensor(parent.form!)
    super.init(parent)
    if #available(iOS 11.0, *) {
      compass = MKCompassButton(mapView: mapView)
      if let compass = compass {
        compass.translatesAutoresizingMaskIntoConstraints = false
        mapView.addSubview(compass)
        mapView.showsCompass = false
        mapView.trailingAnchor.constraint(equalToSystemSpacingAfter: compass.trailingAnchor, multiplier: 1.0).isActive = true
        compass.topAnchor.constraint(equalToSystemSpacingBelow: mapView.topAnchor, multiplier: 1.0).isActive = true
      }
    }
    if #available(iOS 11.0, *) {
      scale = MKScaleView(mapView: mapView)
      if let scale = scale {
        scale.translatesAutoresizingMaskIntoConstraints = false
        mapView.addSubview(scale)
        mapView.showsScale = false
        mapView.trailingAnchor.constraint(equalToSystemSpacingAfter: scale.trailingAnchor, multiplier: 1.0).isActive = true
        mapView.bottomAnchor.constraint(equalTo: scale.bottomAnchor, constant: 32.0).isActive = true
      }
    }
    mapView.translatesAutoresizingMaskIntoConstraints = false
    mapView.delegate = self
    setupMapGestureRecognizers()
    EnableZoom = true
    EnablePan = true
    MapType = 1
    Rotation = 0.0
    ScaleUnits = 1
    ShowZoom = false
    ShowCompass = false
    ShowScale = false
    ShowUser = false
    EnableRotation = false
    parent.add(self)
    setupZoomControls()
    Width = kMapPreferredWidth
    Height = kMapPreferredHeight
  }

  @objc public func onDelete() {
    _features = []
    switch mapView.mapType {
    case .standard:
      mapView.mapType = .hybrid
    default:
      mapView.mapType = .standard
    }
    mapView.removeFromSuperview()
    mapView.removeOverlays(mapView.overlays)
    mapView.removeAnnotations(mapView.annotations)
    mapView.delegate = nil
    _mapView = nil
  }

  @objc public func onDestroy() {
    _features = []
    switch mapView.mapType {
    case .standard:
      mapView.mapType = .hybrid
    default:
      mapView.mapType = .standard
    }
    mapView.removeFromSuperview()
    mapView.removeOverlays(mapView.overlays)
    mapView.removeAnnotations(mapView.annotations)
    mapView.delegate = nil
    _mapView = nil
  }

  /**
   * Handles setting initial values for Center and ZoomLevel
   * Code is handled asynchronously to ensure that values from the companion are processed first
   */
  @objc open func Initialize() {
    if self._zoomLevelUpdate == nil {
      self.ZoomLevel = 13
    }
    if self._centerUpdate == nil {
      self.CenterFromString(DEFAULT_CENTER, animated: false)
    }
    self.setNeedsUpdate(animated: false)
    self._initialized = true
  }

  private func setupZoomControls() {
    _zoomControls.axis = .vertical
    _zoomControls.distribution = .fillEqually
    _zoomControls.alignment = .fill
    _zoomControls.spacing = 3
    _zoomControls.backgroundColor = UIColor.blue

    _zoomControls.heightAnchor.constraint(equalToConstant: 73).isActive = true
    _zoomControls.widthAnchor.constraint(equalToConstant: 35).isActive = true
    _zoomControls.translatesAutoresizingMaskIntoConstraints = false

    mapView.addSubview(_zoomControls)
    _zoomControls.topAnchor.constraint(equalTo: mapView.topAnchor, constant: 10).isActive = true
    _zoomControls.leftAnchor.constraint(equalTo: mapView.leftAnchor, constant: 10).isActive = true

    _zoomInBtn.addTarget(self, action: #selector(self.zoom(_:)), for: .touchUpInside)
    _zoomOutBtn.addTarget(self, action: #selector(self.zoom(_:)), for: .touchUpInside)
    _zoomInBtn.translatesAutoresizingMaskIntoConstraints = false
    _zoomOutBtn.translatesAutoresizingMaskIntoConstraints = false
  }

  private func setupMapGestureRecognizers() {
    let singleTapRecognizer = UITapGestureRecognizer(target: self, action: #selector(self.singleTap(_:)))
    singleTapRecognizer.numberOfTapsRequired = 1
    let doubleTapRecognizer = UITapGestureRecognizer(target: self, action: #selector(self.doubleTap(_:)))
    doubleTapRecognizer.numberOfTapsRequired = 2
    let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(self.longPress(_:)))
    singleTapRecognizer.require(toFail: doubleTapRecognizer)
    singleTapRecognizer.require(toFail: longPressRecognizer)
    doubleTapRecognizer.delegate = self
    mapView.addGestureRecognizer(singleTapRecognizer)
    mapView.addGestureRecognizer(doubleTapRecognizer)
    mapView.addGestureRecognizer(longPressRecognizer)
  }

  open override var view: UIView {
    get {
      return mapView
    }
  }

  open override var Height: Int32 {
    get {
      return super.Height
    }
    set(height) {
      setNestedViewHeight(nestedView: mapView, height: height, shouldAddConstraints: false)
    }
  }

  open override var Width: Int32 {
    get {
      return super.Width
    }
    set(width) {
      setNestedViewWidth(nestedView: mapView, width: width, shouldAddConstraints: false)
    }
  }
  
  @objc open var CenterFromString: String {
    get {
      return "" // write-only
    }
    set(center) {
      CenterFromString(center)
    }
  }
  
  @objc open var Latitude: Double {
    get {
      return mapView.centerCoordinate.latitude
    }
  }
  
  @objc open var Longitude: Double {
    get {
      return mapView.centerCoordinate.longitude
    }
  }
  
  @objc open var ZoomLevel: Int32 {
    get {
      return mapView.zoomLevel
    }
    set(zoomLevel) {
      if zoomLevel != _zoomLevel {
        _zoomLevel = zoomLevel
        if _mapIsReady || !_initialized {
          _zoomLevelUpdate = zoomLevel
        }
        if _mapIsReady {
          self.setNeedsUpdate()
        }
      }
    }
  }
  
  @objc open var EnableZoom: Bool {
    get {
      return mapView.isZoomEnabled
    }
    set(enabled) {
      mapView.isZoomEnabled = enabled
    }
  }
  
  @objc open var Rotation: Float32 {
    get {
      return Float(mapView.camera.heading)
    }
    set(rotation) {
      _rotation = rotation
      let center = mapView.camera.centerCoordinate
      let distance:CLLocationDistance
      if #available(iOS 13.0, *) {
        distance = mapView.camera.centerCoordinateDistance
      } else {
        distance = mapView.camera.altitude
      }
      let pitch = mapView.camera.pitch
      let camera = MKMapCamera(lookingAtCenter: center, fromDistance: distance, pitch: pitch,
          heading: CLLocationDirection(rotation))
      mapView.setCamera(camera, animated: true)
    }
  }
  
  @objc open var MapType: Int32 {
    get {
      return _mapType.rawValue
    }
    set(type) {
      if !(1...3 ~= type) {
        form?.dispatchErrorOccurredEvent(self, "MapType", ErrorMessage.ERROR_INVALID_MAP_TYPE.code,
           ErrorMessage.ERROR_INVALID_MAP_TYPE.message)
        return
      }

      _mapType = AIMapType(rawValue: type)!
      switch _mapType {
      case .roads:
        removeTerrainTileRenderer()
        mapView.mapType = .standard
      case .aerial:
        removeTerrainTileRenderer()
        mapView.mapType = .satellite
      case .terrain:
        mapView.mapType = .standard // set that way zooming in too far displays a visible grid
        setupTerrainTileRenderer()
      }
    }
  }
  
  @objc open var ScaleUnits: Int32 {
    get {
      return _scaleUnits
    }
    set(type) {
      if !(1...2 ~= type) {
        // throw error
        return
      }
      // set scale units somehow
    }
  }
  @objc open var ShowCompass: Bool {
    get {
      return mapView.showsCompass
    }
    set(showCompass) {
      if #available(iOS 11.0, *) {
        compass?.compassVisibility = showCompass ? .visible: .hidden
      } else {
        mapView.showsCompass = showCompass
      }
    }
  }
  @objc open var ShowScale: Bool {
    get {
      return mapView.showsScale
    }
    set(showScale) {
      if #available(iOS 11.0, *) {
        scale?.scaleVisibility = showScale ? .visible: .hidden
      } else {
        mapView.showsScale = showScale
      }
    }
  }
  
  @objc open var ShowZoom: Bool {
    get {
      return !_zoomControls.isHidden
    }
    set(showZoom) {
      _zoomControls.isHidden = !showZoom
    }
  }
  
  @objc open var ShowUser: Bool {
    get {
      return mapView.showsUserLocation
    }
    set(showUser) {
      mapView.showsUserLocation = showUser
    }
  }
  
  @objc open var EnableRotation: Bool {
    get {
      return mapView.isRotateEnabled
    }
    set(rotationEnabled) {
      mapView.isRotateEnabled = rotationEnabled
    }
  }
  
  @objc open var EnablePan: Bool {
    get {
      return mapView.isScrollEnabled
    }
    set(panEnabled) {
      mapView.isScrollEnabled = panEnabled
    }
  }

  open override var Visible: Bool {
    get {
      return !mapView.isHidden
    } set(isVisible) {
      mapView.isHidden = !isVisible
    }
  }
  
  @objc open var BoundingBox: [[Float32]] {
    get {
      return calculateBoundingBox()
    }
    set(boundingBox) {
      let northwest = boundingBox[0]
      let southeast = boundingBox[1]

      if checkValidLatLong(latitude: Double(northwest[0]), longitude: Double(northwest[1])) &&
        checkValidLatLong(latitude: Double(southeast[0]), longitude: Double(southeast[1])) {
        let northwest_lat = min(northwest[0], southeast[0])
        let northwest_long = max(northwest[1], southeast[1])
        let southeast_lat = max(northwest[0], southeast[0])
        let southeast_long = min(northwest[1], southeast[1])

        let northwestCoord = CLLocationCoordinate2D(latitude: Double(northwest_lat), longitude: Double(northwest_long))
        let southeastCoord = CLLocationCoordinate2D(latitude: Double(southeast_lat), longitude: Double(southeast_long))
        let nwPoint = MKMapPoint.init(northwestCoord)
        let sePoint = MKMapPoint.init(southeastCoord)
        let boundingRect = MKMapRect.init(x: fmin(nwPoint.x, sePoint.x), y: fmin(nwPoint.y, sePoint.y), width: fabs(nwPoint.x - sePoint.x), height: fabs(nwPoint.y - sePoint.y))
        
        // setting the Bounding Box is equivalent to setting both the zoomLevel and the mapCenter
        self.setNeedsUpdate()
        _boundingBoxUpdate = boundingRect
        _zoomLevelUpdate = nil
        _centerUpdate = nil
      }
    }
  }
  
  @objc open var LocationSensor: LocationSensor {
    get {
      return _locationSensor
    }
    set(locationSensor) {
      _locationSensor = locationSensor
    }
  }
  
  @objc open var UserLatitude: Double {
    get {
      return  (mapView.userLocation.location != nil) ? mapView.userLocation.coordinate.latitude : Double(-999)
    }
  }
  
  @objc open var UserLongitude: Double {
    get {
      return (mapView.userLocation.location != nil) ? mapView.userLocation.coordinate.longitude: Double(-999)
    }
  }

  @objc open var Features: YailList<MapFeature> {
    get {
      return YailList<MapFeature>(array: _features as [AnyObject])
    }
    set(features) {
      _features.removeAll()
      mapView.removeAnnotations(mapView.annotations)
      mapView.removeOverlays(mapView.overlays)
      for feature in features {
        if let feature = feature as? MapFeature {
          feature.copy(container: self)
        }
      }
    }
  }

  @objc open func CreateMarker(_ latitude: Double, _ longitude: Double) -> Marker {
    let marker = Marker(self)
    marker.SetLocation(latitude, longitude)
    return marker
  }

  public func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
    if annotation is MKUserLocation {
      return nil
    } else if let feature = annotation as? MapFeatureAnnotation {
      DispatchQueue.main.async {
        self.mapView.layoutSubviews()
      }
      return feature.view
    } else {
      return nil
    }
  }
  
  @objc open func PanTo(_ latitude: Double, _ longitude: Double, _ zoom: Int32) {
    if !(-90.0...90.0 ~= latitude) {
      InvalidPoint(String(format: ERROR_LATITUDE_OUT_OF_BOUNDS, latitude))
    } else if !(-180.0...180.0 ~= longitude) {
      InvalidPoint(String(format: ERROR_LONGITUDE_OUT_OF_BOUNDS, longitude))
    } else {
      _zoomLevelUpdate = Int32(zoom)
      _centerUpdate = CLLocationCoordinate2DMake(latitude, longitude)
      self.setNeedsUpdate()
    }
  }

  @objc open func Save(_ path: String) {
    do {
      try GeoJSONUtil.writeAsGeoJSON(features: _features, to: path)
    } catch let err {
      form?.dispatchErrorOccurredEvent(self, "Save",
          ErrorMessage.ERROR_EXCEPTION_DURING_MAP_SAVE.code,
          ErrorMessage.ERROR_EXCEPTION_DURING_MAP_SAVE.message, err.localizedDescription)
    }
  }
  
  @objc open func Ready() {
    EventDispatcher.dispatchEvent(of: self, called: "Ready")
  }

  public func mapViewDidFinishLoadingMap(_ mapView: MKMapView) {
    if !_mapIsReady {
      Ready()
      _mapIsReady = true
      DispatchQueue.main.async {
        self.mapView.setZoom(self._zoomLevel, Double(self.Rotation), animated: false)
      }
    }
  }

  public func mapViewDidFinishRenderingMap(_ mapView: MKMapView, fullyRendered: Bool) {
    if !_mapIsReady {
      Ready()
      _mapIsReady = true
      mapView.setCenterCoordinate(centerCoordinate: mapView.centerCoordinate, zoomLevel: Int(_zoomLevel), animated: false)
    }
  }
  
  @objc open func BoundsChange() {
    EventDispatcher.dispatchEvent(of: self, called: "BoundsChange")
  }

  public func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
    if _mapIsReady {
      if _boundsChangeReady {
        BoundsChange()
      } else {
        _boundsChangeReady = true
      }
    }
    let mapZoom = mapView.zoomLevel // in order to only calculate the zoomLevel once
    if _zoomDidChange || (_mapIsReady && _zoomLevel != mapZoom) {
      _zoomDidChange = false
      _zoomLevel = mapZoom
      ZoomChange()
      updateZoomControls()
    }
  }
  
  @objc open func ZoomChange() {
    EventDispatcher.dispatchEvent(of: self, called: "ZoomChange")
  }

  @objc private func zoom(_ sender: UIButton) {
    guard let zoomBtn = sender as? ZoomButton else {
      return
    }
    switch zoomBtn.zoom {
    case .zoomIn:
      if mapView.zoomLevel < MAX_ZOOM_LEVEL {
        mapView.setZoom(mapView.zoomLevel + 1, Double(_rotation), animated: true)
      }
    case .zoomOut:
      if mapView.zoomLevel > MIN_ZOOM_LEVEL {
        mapView.setZoom(mapView.zoomLevel - 1, Double(_rotation), animated: true)
      }
    }
  }

  private func updateZoomControls() {
    _zoomInBtn.isEnabled = _zoomLevel < MAX_ZOOM_LEVEL
    _zoomOutBtn.isEnabled = _zoomLevel > MIN_ZOOM_LEVEL
  }
  
  @objc open func InvalidPoint(_ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "InvalidPoint", arguments: message as NSString)
  }
  
  @objc open func TapAtPoint(_ latitude: Double, _ longitude: Double) {
    EventDispatcher.dispatchEvent(of: self, called: "TapAtPoint", arguments: latitude as NSNumber, longitude as NSNumber)
  }
  
  @objc open func singleTap(_ sender: UIGestureRecognizer) {
    if sender.state != .ended {
      return
    }
    let coordinate = getCoordinateFromTouchPoint(sender)
    getOverlayAtPoint(coordinate) { feature in
      return feature.EnableInfobox
    }
    if let overlay = _activeOverlay {
      if let marker = _activeMarker {
        marker.HideInfobox()
      }
      overlay.feature!.marker.SetLocation(coordinate.latitude, coordinate.longitude)
      _activeMarker = overlay.feature!.marker
      if let marker = _activeMarker {
        overlay.feature!.Click()
        mapView.removeAnnotation(marker.annotation)
        mapView.addAnnotation(marker.annotation)
        overlay.feature?.ShowInfobox()
         
      }
    } else {
      getOverlayAtPoint(coordinate) { _ in
        return true
      }
      if let overlay = _activeOverlay {
        overlay.feature!.Click()
      } else {
        if let marker = _activeMarker {
          marker.HideInfobox()
          _activeMarker = nil
        }
        TapAtPoint(coordinate.latitude, coordinate.longitude)
      }
    }
  }
  
  @objc open func DoubleTapAtPoint(_ latitude: Double, _ longitude: Double) {
    EventDispatcher.dispatchEvent(of: self, called: "DoubleTapAtPoint", arguments: latitude as NSNumber, longitude as NSNumber)
  }
  
  @objc open func doubleTap(_ sender: UIGestureRecognizer) {
    if sender.state != .ended {
      return
    }
    let coordinate = getCoordinateFromTouchPoint(sender)
    DoubleTapAtPoint(coordinate.latitude, coordinate.longitude)
  }

  @objc open func LongPressAtPoint(_ latitude: Double, _ longitude: Double) {
    EventDispatcher.dispatchEvent(of: self, called: "LongPressAtPoint", arguments: latitude as NSNumber, longitude as NSNumber)
  }

  @objc open func longPress(_ sender: UIGestureRecognizer) {
    let coordinate = getCoordinateFromTouchPoint(sender)
    switch sender.state {
    case .began:
      _lastPoint = coordinate
      getOverlayAtPoint(coordinate) { feature in
        return feature.Draggable
      }
      if let marker = _activeMarker {
        marker.HideInfobox()
        _activeMarker = nil
      }
      if let overlay = _activeOverlay {
        overlay.feature!.StartDrag()
      } else {
        getOverlayAtPoint(coordinate) { _ in
          return true
        }
      }
    case .changed:
      if let pastPoint = _lastPoint, let overlay = _activeOverlay, overlay.feature!.Draggable {
        overlay.feature!.update(coordinate.latitude - pastPoint.latitude, coordinate.longitude - pastPoint.longitude)
        overlay.feature!.Drag()

      }
      _lastPoint = coordinate
    case .ended:
      if let overlay = _activeOverlay {
        if overlay.feature!.Draggable {
          overlay.feature!.StopDrag()
        } else {
          overlay.feature!.LongClick()
        }
      } else {
        LongPressAtPoint(coordinate.latitude, coordinate.longitude)
      }
      _lastPoint = nil
      _activeOverlay = nil
    default:
      if let overlay = _activeOverlay {
        overlay.feature?.stopDrag()
      }
      _lastPoint = nil
      _activeOverlay = nil
      return
    }
  }

  fileprivate func getOverlayAtPoint(_ coordinate: CLLocationCoordinate2D, _ filter: (PolygonBase) -> Bool) {
    _activeOverlay = nil
    let latSpan = Double(kFingerSize / mapView.frame.size.height) * mapView.region.span.latitudeDelta
    let lonSpan = Double(kFingerSize / mapView.frame.size.width) * mapView.region.span.longitudeDelta
    let lat = coordinate.latitude, lon = coordinate.longitude
    let tapOverlay = Geometry.create("POLYGON((\(lon - lonSpan) \(lat - latSpan), \(lon - lonSpan) \(lat + latSpan), \(lon + lonSpan) \(lat + latSpan), \(lon + lonSpan) \(lat - latSpan), \(lon - lonSpan) \(lat - latSpan)))")
    for overlay in mapView.overlays {
      if let shape = overlay as? MapOverlayShape, let tapLocation = tapOverlay {
        if filter(shape.feature!), (shape.feature!._shape?.intersects(tapLocation) ?? false), shape.visible {
          if let past = _activeOverlay {
            _activeOverlay = past.feature!.index < shape.feature!.index ? shape: past
          } else {
            _activeOverlay = shape
          }
        }
      }
    }
  }

  public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
    return true
  }

  // TODO: Implement Feature Functions in Marker Branch
  private func CenterFromString(_ center: String, animated: Bool = true) {
    let parts = center.split(",")
    
    guard parts.count == 2 else {
      InvalidPoint("\(center) is not a valid point.")
      return
    }
    
    let lat = parts[0].trimmingCharacters(in: .whitespacesAndNewlines)
    let long = parts[1].trimmingCharacters(in: .whitespacesAndNewlines)
    
    guard lat.isNumber else {
      InvalidPoint(String(format: ERROR_INVALID_NUMBER, lat))
      return
    }
    guard long.isNumber else {
      InvalidPoint(String(format: ERROR_INVALID_NUMBER, long))
      return
    }
    guard let latitude = Double(lat) else {
      InvalidPoint(String(format: ERROR_INVALID_NUMBER, lat))
      return
    }
    guard let longitude = Double(long) else {
      InvalidPoint(String(format: ERROR_INVALID_NUMBER, long))
      return
    }
    
    if checkValidLatLong(latitude: latitude, longitude: longitude) {
      if _mapIsReady {
        _centerUpdate = CLLocationCoordinate2DMake(latitude, longitude)
        self.setNeedsUpdate()
      } else if _initialized {
        mapView.setCenter(CLLocationCoordinate2DMake(latitude, longitude), animated: animated)
      } else {
        _centerUpdate = CLLocationCoordinate2DMake(latitude, longitude)
      }
    }
  }

  private func checkValidLatLong(latitude: Double, longitude: Double) -> Bool {
    if !(-90.0...90.0 ~= latitude) {
      InvalidPoint(String(format: ERROR_LATITUDE_OUT_OF_BOUNDS, latitude))
      return false
    } else if !(-180.0...180.0 ~= longitude) {
      InvalidPoint(String(format: ERROR_LONGITUDE_OUT_OF_BOUNDS, longitude))
      return false
    }
    return true
  }

  private func calculateBoundingBox() -> [[Float32]] {
    let cornerCoordinates = mapView.visibleRectCornerCoordinates()
    let northWest = [Float32(cornerCoordinates.northWest.latitude), Float32(cornerCoordinates.northWest.longitude)]
    let southEast = [Float32(cornerCoordinates.southEast.latitude), Float32(cornerCoordinates.southEast.longitude)]
    let boundingBox: [[Float32]] = [northWest, southEast]
    return boundingBox
  }

  private func getCoordinateFromTouchPoint(_ gestureRecognizer: UIGestureRecognizer) -> CLLocationCoordinate2D {
    let touchPoint = gestureRecognizer.location(in: mapView)
    let touchCoordinate = mapView.convert(touchPoint, toCoordinateFrom: mapView)
    return touchCoordinate
  }

  /**
   * SetNeedsUpdate() is a method that works similarly to Views that require an update.  This function
   * handles the issue where there are multiple updates occuring + animating that are interacting with
   * with each other.  This will make it such that only the last update(s) are passed and set on the map.
   */
  private func setNeedsUpdate(animated: Bool = true) {
    if !_updatePending {
      _updatePending = true
      DispatchQueue.main.async {
        if let boundingBox = self._boundingBoxUpdate {
          let region = MKCoordinateRegion.init(boundingBox)
          let zoom = self.mapView.calculateZoomLevelFromRegion(region: region)
          /**
           * We first set from the centerUpdate and zoomLevel as those are set to nil once we set a
           * boundingBox.  Therefore, those will be nil unless they've been updated following the
           * boundingBox being set.  We want to set the most recent update(s), and therefore, we
           * attempt to set with the centerUpdate and zoomLevel update first, only pulling from the
           * boundingBox if those are nil.
           */

          let distance = altitude(from: zoom)
          let pitch = self.mapView.camera.pitch
          let camera = MKMapCamera(lookingAtCenter: self._centerUpdate ?? region.center,
              fromDistance: distance, pitch: pitch, heading: CLLocationDirection(self._rotation))
          self.mapView.setCamera(camera, animated: true)
        } else {
          self._zoomDidChange = self._zoomLevelUpdate != nil

          let distance = altitude(from: Double(self._zoomLevel))
          let pitch = self.mapView.camera.pitch
          let camera = MKMapCamera(lookingAtCenter: self._centerUpdate ?? self.mapView.centerCoordinate,
              fromDistance: distance, pitch: pitch, heading: CLLocationDirection(self._rotation))
          self.mapView.setCamera(camera, animated: true)
        }
        self._updatePending = false
        self._zoomLevelUpdate = nil
        self._centerUpdate = nil
        self._boundingBoxUpdate = nil
      }
    }
  }

  /**
   * Adds a custom tile overlay that matches the Terrain overlay on Android
   */
  private func setupTerrainTileRenderer() {
    let template = "https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer/tile/{z}/{y}/{x}"
    _terrainOverlay = MKTileOverlay(urlTemplate: template)
    _terrainOverlay!.canReplaceMapContent = true
    mapView.insertOverlay(_terrainOverlay!, at: 0, level: .aboveLabels)
  }

  private func removeTerrainTileRenderer() {
    if let overlay = _terrainOverlay {
      mapView.removeOverlay(overlay)
    }
  }

  public func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
    if let tileOverlay = overlay as? MKTileOverlay {
      return _mapType == .terrain ? MKTileOverlayRenderer(tileOverlay: tileOverlay) : MKOverlayRenderer()
    } else if let shape = overlay as? MapCircleOverlay {
      let renderer = MKCircleRenderer(circle: shape)
      shape.renderer = renderer
      return renderer
    } else if let shape = overlay as? MapLineOverlay {
      let renderer = MKPolylineRenderer(polyline: shape)
      shape.renderer = renderer
      return renderer
    } else if let shape = overlay as? MapPolygonOverlay {
      let renderer = MKPolygonRenderer(polygon: shape)
      shape.renderer = renderer
      return renderer
    } else {
      return MKOverlayRenderer()
    }
  }

  public func mapView(_ mapView: MKMapView, annotationView view: MKAnnotationView, didChange newState: MKAnnotationView.DragState, fromOldState oldState: MKAnnotationView.DragState) {
    if let marker = view.annotation as? Marker {
      switch newState {
      case .starting:
        marker.StartDrag()
        marker.startDrag()
      case .ending, .canceling:
        marker.LongClick()
        view.dragState = .none
        marker.stopDrag()
        marker.StopDrag()
      default:
        break
      }
    }
  }

  public func mapView(_ mapView: MKMapView, didDeselect view: MKAnnotationView) {
    if let feature = view.annotation as? MapFeatureBase {
      feature.stopDrag()
    }
  }

  public func FeatureClick(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureClick", arguments: feature)
  }

  public func FeatureLongClick(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureLongClick", arguments: feature)
  }

  public func FeatureStartDrag(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureStartDrag", arguments: feature)
  }

  public func FeatureDrag(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureDrag", arguments: feature)
  }

  public func FeatureStopDrag(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureStopDrag", arguments: feature)
  }

  public func getMap() -> Map {
    return self
  }

  public func addFeature(_ feature: MapFeature) {
    if let polygon = feature as? PolygonBase, let overlay = polygon.overlay {
      overlay.feature = polygon
      if let collection = overlay as? MapShapeCollection {
        mapView.addOverlays(collection.shapes, level: .aboveLabels)
      } else {
        mapView.addOverlay(overlay, level: .aboveLabels)
      }
    } else {
      mapView.addAnnotation(feature.annotation)
    }
    if !(feature is PolygonMarker) {
      _features.append(feature)
    }
  }

  public func removeFeature(_ feature: MapFeature) {
    if let overlay = (feature as? PolygonBase)?.overlay {
      if let collection = overlay as? MapShapeCollection {
        for shape in collection.shapes {
          mapView.removeOverlay(shape)
        }
      } else {
        mapView.removeOverlay(overlay)
      }
    } else {
      mapView.removeAnnotation(feature.annotation)
    }
    _features = _features.filter { item in
      return !item.isEqual(feature.annotation)
    }
  }

  public func replaceFeature(from oldOverlay: MapOverlayShape, to newOverlay: MapOverlayShape) {
    if let oldShapes = oldOverlay as? MapShapeCollection {
      if let last = oldShapes.shapes.last {
        if let newShapes = newOverlay as? MapShapeCollection {
          addCollection(newShapes, above: last)
        } else {
          mapView.insertOverlay(newOverlay, above: last)
        }
      } else {
        print("this should absolutely not be happening")
      }
      mapView.removeOverlays(oldShapes.shapes)
    } else if let newShapes = newOverlay as? MapShapeCollection {
      addCollection(newShapes, above: oldOverlay)
    } else {
      mapView.insertOverlay(newOverlay, above: oldOverlay)
      mapView.removeOverlay(oldOverlay)
    }
  }

  fileprivate func addCollection(_ shapes: MapShapeCollection, above oldShape: MapOverlayShape) {
    var previousShape = oldShape
    for shape in shapes.shapes {
      mapView.insertOverlay(shape, above: previousShape)
      previousShape = shape
    }
    mapView.removeOverlay(oldShape)
  }

  @objc open func FeatureFromDescription(_ description: [Any]) -> Any  {
    do {
      return try GeoJSONUtil.processGeoJSONFeature(container: self, description: description)
    } catch let err {
      if let error = err as? YailRuntimeError {
        return error.description
      } else {
        return err.localizedDescription
      }
    }
  }

  @objc open func LoadFromURL(_ url: String) {
    Alamofire.request(url).validate(statusCode: 200...200).responseJSON { response in
      switch response.result {
      case .success(let data):
        if let json = data as? [String: Any],
          let features = GeoJSONUtil.processGeoJsonUrl(for: self, at: url, with: json) {
          self.GotFeatures(url, features)
        }
      case .failure:
        self.LoadError(url, Int32(response.response?.statusCode ?? -999), GeoJSONUtil.ERROR_IO_EXCEPTION)
      }
    }
  }

  open func LoadError(_ url: String, _ responseCode: Int32, _ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "LoadError", arguments: url as NSString, responseCode as NSNumber, message as NSString)
  }

  open func GotFeatures(_ url: String, _ features: [Any]) {
    EventDispatcher.dispatchEvent(of: self, called: "GotFeatures", arguments: url as NSString, features as NSArray)
  }

  // MARK: ComponentContainer implementation

  public var container: ComponentContainer? {
    get {
      return _container
    }
  }

  public func add(_ component: ViewComponent) {}

  public func setChildWidth(of component: ViewComponent, to width: Int32) {}

  public func setChildHeight(of component: ViewComponent, to height: Int32) {}

  public func isVisible(component: ViewComponent) -> Bool {
    return !component.view.isHidden
  }

  public func setVisible(component: ViewComponent, to visibility: Bool) {
    component.view.isHidden = !visibility
  }

  open func getChildren() -> [Component] {
    return _features as [Component]
  }

#if MEMDEBUG
  deinit {
    NSLog("Deinitializing \(self)")
  }
#endif
}


extension MKMapView {
  typealias VisibleRectCoordinates = (northWest: CLLocationCoordinate2D, northEast: CLLocationCoordinate2D, southEast: CLLocationCoordinate2D, southWest: CLLocationCoordinate2D)

  func visibleRectCornerCoordinates() -> VisibleRectCoordinates {
    let nwPoint = MKMapPoint.init(x: self.visibleMapRect.minX, y: self.visibleMapRect.minY)
    let nePoint = MKMapPoint.init(x: self.visibleMapRect.maxX, y: self.visibleMapRect.minY)
    let sePoint = MKMapPoint.init(x: self.visibleMapRect.maxX, y: self.visibleMapRect.maxY)
    let swPoint = MKMapPoint.init(x: self.visibleMapRect.minX, y: self.visibleMapRect.maxY)
    
    return (northWest: nwPoint.coordinate, northEast: nePoint.coordinate, southEast: sePoint.coordinate, southWest: swPoint.coordinate)
  }
}

/**
 * @authors: (calculations: https://github.com/goto10/EBCExtensions & https://gist.github.com/PowerPan/ab6de0fc246d29ec2372ec954c4d966d)
 */
extension MKMapView {
  
  @objc open var zoomLevel: Int32 {
    get {
      return Int32(min(round(calculateZoomLevel()), Double(MAX_ZOOM_LEVEL)))
    }
  }

  open func setZoom(_ zoom: Int32, _ rotation: Double, animated: Bool) {
    guard zoom > 0 else {
      return
    }
    let distance = altitude(from: Double(zoom))
    let pitch = self.camera.pitch
    let camera = MKMapCamera(lookingAtCenter: self.centerCoordinate, fromDistance: distance,
        pitch: pitch, heading: CLLocationDirection(rotation))
    self.setCamera(camera, animated: true)
  }

  @objc func setCenterCoordinate(centerCoordinate: CLLocationCoordinate2D, zoomLevel: Int, animated: Bool) {
    // clamp large numbers to 28
    let zoom = min(zoomLevel, 28)

    // use the zoom level to compute the region
    let span = self.coordinateSpanWithMapView(mapView: self, centerCoordinate:centerCoordinate, andZoomLevel:zoom)
    let region = MKCoordinateRegion.init(center: centerCoordinate, span: span)
    
    // set the region like normal
    self.setRegion(region, animated: animated)
  }

  private var MERCATOR_OFFSET: Double {
    get {
      return 268435456
    }
  }
  private var MERCATOR_RADIUS: Double {
    get {
      return 85445659.44705395
    }
  }

  private func longitudeToPixelSpaceX (longitude: Double) -> Double {
    return round(MERCATOR_OFFSET + MERCATOR_RADIUS * longitude * .pi / 180.0)
  }

  private func latitudeToPixelSpaceY (latitude: Double) -> Double {
    let a = 1 + sinf(Float(latitude * .pi) / 180.0)
    let b = 1.0 - sinf(Float(latitude * .pi / 180.0)) / 2.0

    return round(MERCATOR_OFFSET - MERCATOR_RADIUS * Double(logf(a / b)))
  }

  private func pixelSpaceXToLongitude (pixelX: Double) -> Double {
    return ((round(pixelX) - MERCATOR_OFFSET) / MERCATOR_RADIUS) * 180.0 / .pi
  }

  private func pixelSpaceYToLatitude (pixelY: Double) -> Double {
    let expo = exp((round(pixelY) - MERCATOR_OFFSET) / MERCATOR_RADIUS)
    let expr = (.pi / 2.0 - 2.0 * atan(expo))
    return  expr * 180.0 / Double.pi
  }

  private func coordinateSpanWithMapView(mapView: MKMapView, centerCoordinate: CLLocationCoordinate2D, andZoomLevel zoomLevel:Int) -> MKCoordinateSpan {
    // convert center coordiate to pixel space
    let centerPixelX = self.longitudeToPixelSpaceX(longitude: centerCoordinate.longitude)
    let centerPixelY = self.latitudeToPixelSpaceY(latitude: centerCoordinate.latitude)

    // determine the scale value from the zoom level
    let zoomExponent = 20 - zoomLevel
    let zoomScale = CGFloat(pow(Double(2), Double(zoomExponent)))

    // scale the map’s size in pixel space
    let mapSizeInPixels = mapView.bounds.size
    let scaledMapWidth = mapSizeInPixels.width * zoomScale
    let scaledMapHeight = mapSizeInPixels.height * zoomScale

    // figure out the position of the top-left pixel
    let topLeftPixelX = CGFloat(centerPixelX) - (scaledMapWidth / 2)
    let topLeftPixelY = CGFloat(centerPixelY) - (scaledMapHeight / 2)

    // find delta between left and right longitudes
    let minLng: CLLocationDegrees = self.pixelSpaceXToLongitude(pixelX: Double(topLeftPixelX))
    let maxLng: CLLocationDegrees = self.pixelSpaceXToLongitude(pixelX: Double(topLeftPixelX + scaledMapWidth))
    let longitudeDelta: CLLocationDegrees = maxLng - minLng

    // find delta between top and bottom latitudes
    let minLat: CLLocationDegrees = self.pixelSpaceYToLatitude(pixelY: Double(topLeftPixelY))
    let maxLat: CLLocationDegrees = self.pixelSpaceYToLatitude(pixelY: Double(topLeftPixelY + scaledMapHeight))
    let latitudeDelta: CLLocationDegrees = -1 * (maxLat - minLat)

    // create and return the lat/lng span
    let span = MKCoordinateSpan.init(latitudeDelta: latitudeDelta, longitudeDelta: longitudeDelta)
    
    return span
  }

  private func calculateZoomLevel() -> Double {
    let centerPixelSpaceX = self.longitudeToPixelSpaceX(longitude: self.centerCoordinate.longitude)
    let lonLeft = self.centerCoordinate.longitude - (self.region.span.longitudeDelta / 2)
    let leftPixelSpaceX = self.longitudeToPixelSpaceX(longitude: lonLeft)
    let pixelSpaceWidth = abs(centerPixelSpaceX - leftPixelSpaceX) * 2

    let zoomScale = pixelSpaceWidth / Double(self.bounds.size.width)
    let zoomExponent = log2(zoomScale)
    let zoomLevel = 20 - zoomExponent

    return zoomLevel
  }

  /**
   * Allows for calculating the map zoom level based on a given MapRect
   */
  @objc public func calculateZoomLevelFromRegion(region: MKCoordinateRegion) -> Double {
    let centerPixelSpaceX = self.longitudeToPixelSpaceX(longitude: region.center.longitude)
    let lonLeft = region.center.longitude - (region.span.longitudeDelta / 2)
    let leftPixelSpaceX = self.longitudeToPixelSpaceX(longitude: lonLeft)
    let pixelSpaceWidth = abs(centerPixelSpaceX - leftPixelSpaceX) * 2

    let zoomScale = pixelSpaceWidth / Double(self.bounds.size.width)
    let zoomExponent = log2(zoomScale)
    let zoomLevel = 20 - zoomExponent

    return zoomLevel
  }
}


private class ZoomButton: UIButton {
  enum ZoomType {
    case zoomOut
    case zoomIn
  }

  var zoom: ZoomType
  @objc let disabledGreyBG = UIColor(red: 0.9569, green: 0.9569, blue: 0.9569, alpha: 1.0)
  @objc let disabledGreyText = UIColor(red: 0.8078, green: 0.8078, blue: 0.8078, alpha: 1.0)
  
  required init(zoom: ZoomType) {
    self.zoom = zoom
    super.init(frame: CGRect.zero)
    customizeBtn()
  }

  private func customizeBtn() {
    self.backgroundColor = UIColor.white
    self.setTitleColor(UIColor.gray, for: .normal)
    self.setTitleColor(disabledGreyText, for: .disabled)
    self.setTitle((zoom == ZoomType.zoomIn) ? "+" : "-", for: .normal)

    self.layer.borderWidth = 0.5
    self.layer.borderColor = UIColor.gray.cgColor
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override var isEnabled: Bool {
    didSet {
      backgroundColor = isEnabled ? UIColor.white : disabledGreyBG
    }
  }
}
