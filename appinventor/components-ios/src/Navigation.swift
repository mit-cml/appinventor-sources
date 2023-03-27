// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2022-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreLocation
import Alamofire

/**
 * The default URL for connecting to OpenRouteService.
 */
let kOpenRouteServiceUrl = "https://api.openrouteservice.org/v2/directions/"

/**
 * The `NavigationRequest` structure encapsulates the information needed by the OpenRouteService
 * to compute routing information.
 */
struct NavigationRequest: Codable {
  /**
   * The coordinates defining the route, in longitude-first order.
   */
  var coordinates: [[Double]]

  /**
   * The language for the navigation instructions.
   */
  var language: String

  /**
   * Construct a new `NavigationRequest`.
   *
   * - Parameters:
   *   - coordinates: An array containing the start and end points of the route, with longitude
   *     listed first.
   *   - language: The desired language for the navigation instructions.
   */
  public init(_ coordinates: [[Double]], _ language: String) {
    self.coordinates = coordinates
    self.language = language
  }
}

/**
 * Defines a `TransportMethod` type used by the `Navigation`
 * component. It models the enum of the same name from the
 * Java version.
 */
@objc open class TransportMethod : NSObject {
  @objc public static let Foot = TransportMethod("Foot", "foot-walking")
  @objc public static let Car = TransportMethod("Car", "driving-car")
  @objc public static let Bicycle = TransportMethod("Bicycle", "cycling-regular")
  @objc public static let Wheelchair = TransportMethod("Wheelchair", "wheelchair")

  private static let LOOKUP = [
    Foot.toUnderlyingValue(): Foot,
    Car.toUnderlyingValue(): Car,
    Bicycle.toUnderlyingValue(): Bicycle,
    Wheelchair.toUnderlyingValue(): Wheelchair
  ]

  private let name: String
  private let value: String

  public init(_ name: String, _ value: String) {
    self.name = name
    self.value = value
    super.init()
  }

  public func toUnderlyingValue() -> String {
    return value
  }

  public static func fromUnderlyingValue(_ value: String) -> TransportMethod? {
    return LOOKUP[value]
  }

  @objc public override var description: String {
    return name
  }
}

@objc open class Navigation : NonvisibleComponent {
  private var _apiKey = ""
  private var _startLocation = CLLocationCoordinate2D()
  private var _endLocation = CLLocationCoordinate2D()
  private var _method = TransportMethod.Foot
  private var _serviceUrl = kOpenRouteServiceUrl
  private var _language = "en"
  private var _lastResponse = YailDictionary()

  public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  // MARK: Properties

  @objc public var ApiKey: String {
    get {
      return _apiKey
    }
    set {
      _apiKey = newValue
    }
  }

  @objc public var EndLatitude: Float64 {
    get {
      return _endLocation.latitude as Float64
    }
    set {
      _endLocation.latitude = newValue as CLLocationDegrees
    }
  }

  @objc public func setEndLocation(_ feature: MapFeature) {
    guard let centroid = feature.geometry?.centroid() else {
      return
    }
    _endLocation = CLLocationCoordinate2D(latitude: centroid.coordinate.y, longitude: centroid.coordinate.x)
  }

  @objc public var EndLongitude: Float64 {
    get {
      return _endLocation.longitude as Float64
    }
    set {
      _endLocation.longitude = newValue as CLLocationDegrees
    }
  }

  @objc public var Language: String {
    get {
      return _language
    }
    set {
      _language = newValue
    }
  }

  @objc public var ResponseContent: YailDictionary {
    return _lastResponse
  }

  @objc public var ServiceURL: String {
    get {
      return _serviceUrl
    }
    set {
      _serviceUrl = newValue
    }
  }

  @objc public var StartLatitude: Float64 {
    get {
      return _startLocation.latitude as Float64
    }
    set {
      _startLocation.latitude = newValue as CLLocationDegrees
    }
  }

  @objc public func setStartLocation(_ feature: MapFeature) {
    guard let centroid = feature.geometry?.centroid() else {
      return
    }
    _startLocation = CLLocationCoordinate2D(latitude: centroid.coordinate.y, longitude: centroid.coordinate.x)
  }

  @objc public var StartLongitude: Float64 {
    get {
      return _startLocation.longitude as Float64
    }
    set {
      _startLocation.longitude = newValue as CLLocationDegrees
    }
  }

  @objc public var TransportationMethod: String {
    get {
      return _method.toUnderlyingValue()
    }
    set {
      if let method = TransportMethod.fromUnderlyingValue(newValue) {
        _method = method
      }
    }
  }

  @objc public var TransportationMethodAbstract: TransportMethod {
    get {
      return _method
    }
    set {
      _method = newValue
    }
  }

  // MARK: Methods

  @objc public func RequestDirections() {
    if _apiKey.isEmpty {
      _form?.dispatchErrorOccurredEvent(self, "RequestDirections", ErrorMessage.ERROR_INVALID_API_KEY.code)
      return
    }
    performRequest()
  }

  // MARK: Events

  @objc public func GotDirections(_ directions: YailList<AnyObject>, _ points: YailList<AnyObject>, _ distance: Float64, _ duration: Float64) {
    EventDispatcher.dispatchEvent(of: self, called: "GotDirections", arguments: directions, points, distance as AnyObject, duration as AnyObject)
  }

  // MARK: Private implementation

  private func performRequest() {
    let start = _startLocation
    let end = _endLocation
    let method = _method
    guard let url = URL(string: "\(_serviceUrl)\(method.toUnderlyingValue())/geojson/") else {
      return
    }
    var request = URLRequest(url: url)
    request.setValue(_apiKey, forHTTPHeaderField: "Authorization")
    request.setValue("application/json; charset=UTF-8", forHTTPHeaderField: "Content-Type")
    request.httpMethod = "POST"
    let data = NavigationRequest(getCoordinates(start, end), _language)
    request.httpBody = try? JSONEncoder().encode(data)
    let task = URLSession.shared.dataTask(with: request) { data, resp, error in
      if let error = error {
        // TODO: report an error here
        var status = 0
        if let resp = resp as? HTTPURLResponse {
          status = resp.statusCode
        }
        self._form?.dispatchErrorOccurredEvent(self, "RequestDirections", ErrorMessage.ERROR_ROUTING_SERVICE_ERROR, status, error.localizedDescription)
        return
      }
      guard let data = data else {
        // TODO: report an error here
        self._form?.dispatchErrorOccurredEvent(self, "RequestDirections", ErrorMessage.ERROR_UNABLE_TO_REQUEST_DIRECTIONS, "No response provided")
        return
      }
      let responseContent = String(data: data, encoding: .utf8) ?? ""
      #if DEBUG
      print("Response: \(responseContent)")
      #endif
      if let response = try? getYailObjectFromJson(responseContent, true) as? YailDictionary,
         let features = response["features"] as? YailList<AnyObject> {
        if !features.isEmpty {
          guard let feature = features[1] as? YailDictionary else {
            return
          }
          guard let summary = try? feature.getObjectAtKeyPath(["properties", "summary"]) as? YailDictionary else {
            return
          }
          let directions = YailList<AnyObject>(array: self.getDirections(feature))
          let coordinates = self.getLineStringCoords(feature)
          let distance = summary["distance"] as? Float64 ?? 0.0
          let duration = summary["duration"] as? Float64 ?? 0.0
          DispatchQueue.main.async {
            self._lastResponse = response
            self.GotDirections(directions, coordinates, distance, duration)
          }
        }
      }
    }
    task.resume()
  }

  private func getCoordinates(_ start: CLLocationCoordinate2D, _ end: CLLocationCoordinate2D) -> [[Double]] {
    return [[
      start.longitude,
      start.latitude
    ],[
      end.longitude,
      end.latitude
    ]]
  }

  private func getLineStringCoords(_ feature: YailDictionary) -> YailList<AnyObject> {
    let coords = try? feature.getObjectAtKeyPath(["geometry", "coordinates"]) as? YailList<AnyObject>
    if let coords = coords {
      return GeoJSONUtil.swapCoordinates(coords)
    } else {
      return YailList<AnyObject>()
    }
  }

  private func getDirections(_ feature: YailDictionary) -> [Any] {
    return (try? feature.walkKeyPath(["properties", "segments", YailDictionary.all(), "steps", YailDictionary.all(), "instruction"])) ?? []
  }
}

