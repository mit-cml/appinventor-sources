// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import CoreLocation
import Contacts

public enum LocationManagerStatus: String {
  case AVAILABLE = "Available"
  case OUT_OF_SERVICE = "Out of Service"
  case TEMPORARILY_UNAVAILABLE = "Temporarily Unavailable"
}

/**
 * LocationSensor class.  Sensor that provides GPS information on longitude, latitude
 * altitude, speed, and address.  This can also perform geocoding.
 * @author Nichole Clarke
 */
open class LocationSensor: NonvisibleComponent, CLLocationManagerDelegate {
  
  fileprivate static let UNKNOWN_VALUE: Double = 0
  fileprivate var _listening: Bool = false
  fileprivate var _providerName: String = "iOS"
  fileprivate let _allProviders: [String] = ["iOS"]
  fileprivate var _providerLocked: Bool = true // cannot change provider on iOS
  fileprivate var _locationManager: CLLocationManager = CLLocationManager()
  
  fileprivate var _timeInterval: Int = 60000 // 60 seconds
  fileprivate var _distanceInterval: Int = 5 // 5 meters
  
  private var _lastLocation: CLLocation?
  private var _longitude: Double = UNKNOWN_VALUE
  private var _latitude: Double = UNKNOWN_VALUE
  private var _altitude: Double = UNKNOWN_VALUE
  private var _speed: Float = Float(UNKNOWN_VALUE)
  private var _hasLocationData: Bool = false // has first fix for location
  private var _hasAltitude: Bool = false
  
  private let geocoder: CLGeocoder = CLGeocoder()
  
  private var _enabled: Bool = false
  
  public override init(_ container: ComponentContainer) {
    super.init(container)
    _locationManager.delegate = self
    // TODO: Setup Listener one listeners implemented
//    form.registerForOnResume(self)
//    form.registerForOnStop(self)
    
  }
  
  // MARK: LocationSensor Properties
  open var ProviderName: String {
    get {
      return _providerName
    }
    set(providerName) {
      // iOS does not currently allow setting providers
    }
  }
  
  open var ProviderLocked: Bool {
    get {
      return _providerLocked
    }
    set(providerLocked) {
      // cannot change provider on iOS
    }
  }
  
  open var TimeInterval: Int32 {
    get {
      return Int32(_timeInterval)
    }
    set(interval) {
      if (interval < 0 || interval > 1000000) {
        return
      }
      
      _timeInterval = Int(interval)
      
      if(_enabled) {
        RefreshProvider()
      }
    }
  }
  
  open var DistanceInterval: Int32 {
    get {
      return Int32(_distanceInterval)
    }
    set (interval) {
      if (interval < 0 || interval > 1000) {
        return
      }
      
      _distanceInterval = Int(interval)
      
      if(_enabled) {
        RefreshProvider()
      }
      
    }
  }
  
  open var HasLongitudeLatitude: Bool {
    get {
      return _hasLocationData && _enabled
    }
  }
  
  open var HasAltitude: Bool {
    get {
      return _hasAltitude && _enabled
    }
  }
  
  open var HasAccuracy: Bool {
    get {
      return Accuracy != LocationSensor.UNKNOWN_VALUE && _enabled
    }
  }
  
  open var Longitude: Double {
    get {
      return _longitude
    }
  }
  
  open var Latitude: Double {
    get {
      return _latitude
    }
  }
  
  open var Altitude: Double {
    get {
      return _altitude
    }
  }
  
  open var Accuracy: Double {
    get {
      if let _lastLocation = _lastLocation {
        return _lastLocation.horizontalAccuracy
      } else {
        return LocationSensor.UNKNOWN_VALUE
      }
    }
  }
  
  open var Enabled: Bool {
    get {
      return _enabled
    }
    set(enabled) {
      if !_enabled && enabled {
        PermissionHandler.RequestPermission(for: .location) {
          authorized, _ in
          if authorized {
            self._enabled = true
            self.RefreshProvider()
          } else {
            if self._enabled {
              self._form.dispatchErrorOccurredEvent(self, "Enabled", ErrorMessage.ERROR_LOCATION_SENSOR_UNEXPECTED_ERROR.code, ErrorMessage.ERROR_LOCATION_SENSOR_UNEXPECTED_ERROR.message, "Enabled should not be true.")
            }
            self._enabled = false
            self.stopListening()
          }
        }
      } else if _enabled && !enabled {
        _enabled = false
        stopListening()
      }
    }
  }
  
  open var CurrentAddress: String {
    get {
      return getAddressFromLocation(location: _lastLocation)
    }
  }
  
  open var AvailableProviders: [String] {
    get {
      return _allProviders
    }
  }
  
  // MARK: Events
  open func LocationChanged(_ latitude: Double, _ longitude: Double, _ altitude: Double, _ speed: Float) {
    EventDispatcher.dispatchEvent(of: self, called: "LocationChanged", arguments: latitude as NSNumber, longitude as NSNumber, altitude as NSNumber, speed as NSNumber)
  }
  
  open func LatitudeFromAddress(addressStr: String) -> Double {
    var latitude = LocationSensor.UNKNOWN_VALUE
    geocoder.geocodeAddressString(addressStr) { placemarks, error in
      guard let placemarks = placemarks, let location = placemarks.first?.location else {
        return
      }
      latitude = location.coordinate.latitude
    }
    return latitude
  }
  
  open func LongitudeFromAddress(addressStr: String) -> Double {
    var longitude = LocationSensor.UNKNOWN_VALUE
    geocoder.geocodeAddressString(addressStr) { placemarks, error in
      guard let placemarks = placemarks, let location = placemarks.first?.location else {
        return
      }
      longitude = location.coordinate.longitude
    }
    return longitude
  }
  
  // TODO: update handling of CurrentAddress following update to Android's handling of CurrentAddress
  //       currentAddress should be a function and not a property
  fileprivate func getAddressFromLocation (location: CLLocation?) -> String {
    var address = "No Address Available"
    guard let location = location else {
      return address
    }
    if -90...90 ~= location.coordinate.latitude && -180...180 ~= location.coordinate.longitude {
        self.geocoder.reverseGeocodeLocation(location, completionHandler: { placemarks, error in
          if let error = error {
            self._form.dispatchErrorOccurredEvent(self, "getAddressFromLocation", Int32(error._code), ErrorMessage.ERROR_LOCATION_SENSOR_UNEXPECTED_ERROR.message, error.localizedDescription)
            return
          } else if let placemarks = placemarks {
            guard let placemark = placemarks.first else {
              // do something here too --> Placemark was nil
              return
            }
            let postalAddress = CNMutablePostalAddress(placemark: placemark)
            let addressStr = CNPostalAddressFormatter().string(from: postalAddress)
            address = addressStr.isEmpty ? address : addressStr
          }
        })
    }
    return address
  }

  
  // MARK: LocationDelegate
  open func locationManagerDidPauseLocationUpdates(_ manager: CLLocationManager) {
    StatusChanged(.TEMPORARILY_UNAVAILABLE)
    onStop()
  }
  
  open func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    switch error._code {
    case CLError.locationUnknown.rawValue:
      break
    case CLError.geocodeCanceled.rawValue, CLError.geocodeFoundNoResult.rawValue, CLError.geocodeFoundPartialResult.rawValue:
        // TODO: when new implementation for CurrentAddress completed, errors should be handled here
      break
    case CLError.denied.rawValue:
      StatusChanged(.OUT_OF_SERVICE)
      onStop()
      _form.dispatchErrorOccurredEvent(self, "didFailWithError", Int32(error._code), ErrorMessage.ERROR_LOCATION_SENSOR_PERMISSION_DENIED.message)
    default:
      StatusChanged(.OUT_OF_SERVICE)
      onStop()
      _form.dispatchErrorOccurredEvent(self, "didFailWithError", Int32(error._code), ErrorMessage.ERROR_LOCATION_SENSOR_UNEXPECTED_ERROR.message, error.localizedDescription)
    }
  }
  
  open func locationManagerDidResumeLocationUpdates(_ manager: CLLocationManager) {
    StatusChanged(LocationManagerStatus.AVAILABLE)
    onResume()
  }

  open func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let lastLocation = locations.last else {
      return
    }
    
    var differenceInTime = Double(_timeInterval)
    if let previousTimestamp = _lastLocation?.timestamp {
      differenceInTime = lastLocation.timestamp.timeIntervalSince(previousTimestamp) * 1000 //ms conversion
    }
    
    if differenceInTime >= Double(_timeInterval) && (lastLocation.coordinate.longitude != LocationSensor.UNKNOWN_VALUE || lastLocation.coordinate.latitude != LocationSensor.UNKNOWN_VALUE) {
      _lastLocation = lastLocation
      _longitude = lastLocation.coordinate.longitude
      _latitude = lastLocation.coordinate.latitude
      _speed = Float(lastLocation.speed)
      _hasLocationData = true
      
      // Update altitude if exists else retain cached altitude
      if lastLocation.hasAccuracy() {
        _altitude = lastLocation.altitude
        _hasAltitude = true
      }
      
      let argLatitude = _latitude
      let argLongitude = _longitude
      let argAltitude = _altitude
      let argSpeed = _speed
      LocationChanged(argLatitude, argLongitude, argAltitude, argSpeed)
    }
  }
  
  open func StatusChanged(provider: String = "iOS", _ status: LocationManagerStatus) {
    if _enabled {
      EventDispatcher.dispatchEvent(of: self, called: "StatusChanged", arguments: provider as NSString, status.rawValue as NSString)
    }
  }
  
  open func RefreshProvider() {
    // we cannot blindly start updating and refreshing location --> enabled can be sent to true or false
    _locationManager.distanceFilter = _distanceInterval > Int(LocationSensor.UNKNOWN_VALUE) ? Double(_distanceInterval) : kCLDistanceFilterNone
    _locationManager.startUpdatingLocation()
    _listening = true
  }
  
  fileprivate func stopListening() {
    if _listening {
      _locationManager.stopUpdatingLocation()
      _listening = false
    }
  }
  
  open func onResume() {
    if _enabled {
      RefreshProvider()
    }
  }
  
  open func onStop() {
    stopListening()
  }
  
  open func onDelete() {
    stopListening()
  }
}

/**
 * An extension to CLLocation.  Adds boolean denoting if the location data has accuracy.
 * @author Nichole Clarke
 */
extension CLLocation {
  func hasAccuracy() -> Bool {
    return self.verticalAccuracy >= 0.0
  }
}

/**
 * An extension to CNMutablePostalAddress.  This creates an address from a placemark
 * via geocoding.
 * @author https://stackoverflow.com/a/37886956
 */
extension CNMutablePostalAddress {
  convenience init(placemark: CLPlacemark) {
    self.init()
    street = [placemark.subThoroughfare, placemark.thoroughfare]
      .flatMap { $0 }
      .joined(separator: " ")
    city = placemark.locality ?? ""
    state = placemark.administrativeArea ?? ""
    postalCode = placemark.postalCode ?? ""
    country = placemark.country ?? ""
    isoCountryCode = placemark.isoCountryCode ?? ""
    if #available(iOS 10.3, *) {
      subLocality = placemark.subLocality ?? ""
      subAdministrativeArea = placemark.subAdministrativeArea ?? ""
    }
  }
}
