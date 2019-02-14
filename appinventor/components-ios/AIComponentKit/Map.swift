// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import MapKit
import CoreLocation

private let ERROR_INVALID_NUMBER: String = "%s is not a valid number."
private let ERROR_LATITUDE_OUT_OF_BOUNDS: String = "Latitude %f is out of bounds."
private let  ERROR_LONGITUDE_OUT_OF_BOUNDS: String = "Longitude %f is out of bounds."

private let DEFAULT_CENTER: String = "42.359144, -71.093612"

private let MAX_ZOOM_LEVEL: Int = 18
private let MIN_ZOOM_LEVEL: Int = 1

enum AIMapType: Int32 {
  case roads = 1
  case aerial = 2
  case terrain = 3
}

// TODO: update inheritance following creation of MapFeatureContainerBase in Marker branch

/**
 * The Map Component.  A two dimensional-container that renders a map.
 */
open class Map: ViewComponent, MKMapViewDelegate, UIGestureRecognizerDelegate {
  @objc final var _mapView: MKMapView
  private var _locationSensor: LocationSensor
  private var _mapType: AIMapType = .roads
  private var _zoomLevel: Int32 = 1
  private var _zoomControls: UIStackView
  private var _zoomInBtn: ZoomButton
  private var _zoomOutBtn: ZoomButton
  private var _mapIsReady: Bool = false
    private var _boundsChangeReady: Bool = false
  private var _terrainOverlay: MKTileOverlay?
  
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
  
  public override init(_ parent: ComponentContainer) {
    _mapView = MKMapView()
    _zoomInBtn = ZoomButton(zoom: .zoomIn)
    _zoomOutBtn = ZoomButton(zoom: .zoomOut)
    _zoomControls = UIStackView(arrangedSubviews: [_zoomInBtn, _zoomOutBtn])
    _mapView.translatesAutoresizingMaskIntoConstraints = false
    _locationSensor = AIComponentKit.LocationSensor(parent.form)
    super.init(parent)
    _mapView.delegate = self
    setupMapGestureRecognizers()
    ZoomLevel = 13
    CenterFromString(DEFAULT_CENTER, animated: false)
    EnableZoom = true
    EnablePan = true
    MapType = 1
    ShowZoom = false
    ShowCompass = false
    ShowUser = false
    EnableRotation = false
    parent.add(self)
    setupZoomControls()
    Width = kMapPreferredWidth
    Height = kMapPreferredHeight
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
    
    _mapView.addSubview(_zoomControls)
    _zoomControls.topAnchor.constraint(equalTo: _mapView.topAnchor, constant: 10).isActive = true
    _zoomControls.leftAnchor.constraint(equalTo: _mapView.leftAnchor, constant: 10).isActive = true
    
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
    _mapView.addGestureRecognizer(singleTapRecognizer)
    _mapView.addGestureRecognizer(doubleTapRecognizer)
    _mapView.addGestureRecognizer(longPressRecognizer)
  }
  
  open override var view: UIView {
    get {
      return _mapView
    }
  }
  
  open override var Height: Int32 {
    get {
      return super.Height
    }
    set(height) {
      setNestedViewHeight(nestedView: _mapView, height: height, shouldAddConstraints: false)
    }
  }
  
  open override var Width: Int32 {
    get {
      return super.Width
    }
    set(width) {
      setNestedViewWidth(nestedView: _mapView, width: width, shouldAddConstraints: false)
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
      return _mapView.centerCoordinate.latitude
    }
  }
  
  @objc open var Longitude: Double {
    get {
      return _mapView.centerCoordinate.longitude
    }
  }
  
  @objc open var ZoomLevel: Int32 {
    get {
      return _mapView.zoomLevel
    }
    set(zoomLevel) {
      if zoomLevel != _zoomLevel {
        _zoomLevel = zoomLevel
        if _mapIsReady {
          _zoomLevelUpdate = zoomLevel
          self.setNeedsUpdate()
        }
      }
    }
  }
  
  @objc open var EnableZoom: Bool {
    get {
      return _mapView.isZoomEnabled
    }
    set(enabled) {
      _mapView.isZoomEnabled = enabled
    }
  }
  
  @objc open var MapType: Int32 {
    get {
      return _mapType.rawValue
    }
    set(type) {
      if !(1...3 ~= type) {
        _container.form.dispatchErrorOccurredEvent(self, "MapType", ErrorMessage.ERROR_INVALID_MAP_TYPE.code, ErrorMessage.ERROR_INVALID_MAP_TYPE.message)
        return
      }
      _mapType = AIMapType(rawValue: type)!
      switch _mapType {
      case .roads:
        removeTerrainTileRenderer()
        _mapView.mapType = .standard
      case .aerial:
        removeTerrainTileRenderer()
        _mapView.mapType = .satellite
      case .terrain:
        _mapView.mapType = .standard // set that way zooming in too far displays a visible grid
        setupTerrainTileRenderer()
      }
    }
  }
  
  @objc open var ShowCompass: Bool {
    get {
      return _mapView.showsCompass
    }
    set(showCompass) {
      _mapView.showsCompass = showCompass
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
      return _mapView.showsUserLocation
    }
    set(showUser) {
      _mapView.showsUserLocation = showUser
    }
  }
  
  @objc open var EnableRotation: Bool {
    get {
      return _mapView.isRotateEnabled
    }
    set(rotationEnabled) {
      _mapView.isRotateEnabled = rotationEnabled
    }
  }
  
  @objc open var EnablePan: Bool {
    get {
      return _mapView.isScrollEnabled
    }
    set(panEnabled) {
      _mapView.isScrollEnabled = panEnabled
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
      return  (_mapView.userLocation.location != nil) ? _mapView.userLocation.coordinate.latitude : Double(-999)
    }
  }
  
  @objc open var UserLongitude: Double {
    get {
      return (_mapView.userLocation.location != nil) ? _mapView.userLocation.coordinate.longitude: Double(-999)
    }
  }
  
  @objc open func CreateMarker(_ latitude: Double, _ longitude: Double) {
    // TODO: in Marker Branch
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
    // TODO: in Marker Branch (when implementing features)
  }
  
  @objc open func Ready() {
    EventDispatcher.dispatchEvent(of: self, called: "Ready")
  }
  
  public func mapViewDidFinishLoadingMap(_ mapView: MKMapView) {
    if !_mapIsReady {
      Ready()
      _mapIsReady = true
      _mapView.zoomLevel = _zoomLevel
    }
  }
  
  public func mapViewDidFinishRenderingMap(_ mapView: MKMapView, fullyRendered: Bool) {
    if !_mapIsReady {
      Ready()
      _mapIsReady = true
      _mapView.setCenterCoordinate(centerCoordinate: _mapView.centerCoordinate, zoomLevel: Int(_zoomLevel), animated: false)
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
    let mapZoom = _mapView.zoomLevel // in order to only calculate the zoomLevel once
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
      if _mapView.zoomLevel < MAX_ZOOM_LEVEL {
        _mapView.zoomLevel += 1
      }
    case .zoomOut:
      if _mapView.zoomLevel > MIN_ZOOM_LEVEL {
        _mapView.zoomLevel -= 1
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
    TapAtPoint(coordinate.latitude, coordinate.longitude)
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
    if sender.state != .ended {
      return
    }
    let coordinate = getCoordinateFromTouchPoint(sender)
    LongPressAtPoint(coordinate.latitude, coordinate.longitude)
  }
  
  public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
    return true
  }
  
  // TODO: Implement Feature Functions in Marker Branch
  private func CenterFromString(_ center: String, animated: Bool = true) {
    let parts = center.split(",")
    let lat = parts[0].trimmingCharacters(in: .whitespacesAndNewlines)
    let long = parts[1].trimmingCharacters(in: .whitespacesAndNewlines)
    if parts.count != 2 {
      InvalidPoint("\(center) is not a valid point.")
      return
    } else if !lat.isNumber {
      InvalidPoint(String(format: ERROR_INVALID_NUMBER, lat))
    } else if !long.isNumber {
      InvalidPoint(String(format: ERROR_INVALID_NUMBER, long))
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
      } else {
        _mapView.setCenter(CLLocationCoordinate2DMake(latitude, longitude), animated: animated)
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
    let cornerCoordinates = _mapView.visibleRectCornerCoordinates()
    let northWest = [Float32(cornerCoordinates.northWest.latitude), Float32(cornerCoordinates.northWest.longitude)]
    let southEast = [Float32(cornerCoordinates.southEast.latitude), Float32(cornerCoordinates.southEast.longitude)]
    let boundingBox: [[Float32]] = [northWest, southEast]
    return boundingBox
  }
  
  private func getCoordinateFromTouchPoint(_ gestureRecognizer: UIGestureRecognizer) -> CLLocationCoordinate2D {
    let touchPoint = gestureRecognizer.location(in: _mapView)
    let touchCoordinate = _mapView.convert(touchPoint, toCoordinateFrom: _mapView)
    return touchCoordinate
  }
  
  /**
   * SetNeedsUpdate() is a method that works similarly to Views that require an update.  This function
   * handles the issue where there are multiple updates occuring + animating that are interacting with
   * with each other.  This will make it such that only the last update(s) are passed and set on the map.
   */
  private func setNeedsUpdate() {
    if !_updatePending {
      _updatePending = true
      DispatchQueue.main.async {
        if let boundingBox = self._boundingBoxUpdate {
          let region = MKCoordinateRegion.init(boundingBox)
          let zoom = self._mapView.calculateZoomLevelFromRegion(region: region)
          /**
           * We first set from the centerUpdate and zoomLevel as those are set to nil once we set a
           * boundingBox.  Therefore, those will be nil unless they've been updated following the
           * boundingBox being set.  We want to set the most recent update(s), and therefore, we
           * attempt to set with the centerUpdate and zoomLevel update first, only pulling from the
           * boundingBox if those are nil.
           */
          self._mapView.setCenterCoordinate(centerCoordinate: self._centerUpdate ?? region.center, zoomLevel: Int(self._zoomLevelUpdate ?? Int32(zoom)), animated: true)
        } else {
          self._zoomDidChange = self._zoomLevelUpdate != nil
          self._mapView.setCenterCoordinate(centerCoordinate: self._centerUpdate ?? self._mapView.centerCoordinate, zoomLevel: Int(self._zoomLevelUpdate ?? self._zoomLevel), animated: true)
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
    _mapView.addOverlay(_terrainOverlay!, level: .aboveLabels)
  }
  
  private func removeTerrainTileRenderer() {
    if let overlay = _terrainOverlay {
      _mapView.removeOverlay(overlay)
    }
  }
  
  public func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
    guard let tileOverlay = overlay as? MKTileOverlay else {
      return MKOverlayRenderer()
    }
    
    return _mapType == .terrain ? MKTileOverlayRenderer(tileOverlay: tileOverlay) : MKOverlayRenderer()
  }
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
    set(zoom) {
      self.setCenterCoordinate(centerCoordinate: self.centerCoordinate, zoomLevel: Int(zoom), animated: true)
    }
  }
  
  @objc func setCenterCoordinate(centerCoordinate: CLLocationCoordinate2D, zoomLevel: Int, animated: Bool) {
    // clamp large numbers to 28
    let zoom = min(zoomLevel, 28)
    
    // use the zoom level to compute the region
    let span = self.coordinateSpanWithMapView(mapView: self, centerCoordinate:centerCoordinate, andZoomLevel:zoom)
    let region = MKCoordinateRegion.init(center: centerCoordinate, span: span)
    
    // set the region like normal
    self.setRegion(region, animated:animated)
    
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
