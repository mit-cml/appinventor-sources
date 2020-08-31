// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import Alamofire
import GEOSwift

class FeatureCollection: MapFeatureContainerBase, MapFeatureCollection {
  fileprivate var _features = [MapFeature]()
  fileprivate var _source: String = ""
  fileprivate weak var _map: Map?
  fileprivate weak var _form: Form?

  override init(_ parent: ComponentContainer) {
    if let mapContainer = parent as? MapFeatureContainer {
      _map = mapContainer.getMap()
    }
    _form = parent.form
    super.init(parent)
  }

  // MARK: properties
  open var Features: [MapFeature] {
    get {
      return _features
    }
    set(features) {
      while _features.count > 0 {
        removeFeature(_features[0])
      }
      for feature in features {
        addFeature(feature)
      }
    }
  }

  open var Source: String {
    get {
      return _source
    }
    set(source) {
      _source = source
    }
  }

  // MARK: method
  open func FeatureFromDescription(_ description: [Any]) -> Any  {
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

  open func LoadFromURL(_ url: String) {
    Alamofire.request(url).validate(statusCode: 200...200).responseJSON { response in
      switch response.result {
      case .success(let data):
        if let json = data as? [String: Any],
          let features = GeoJSONUtil.processGeoJsonUrl(for: self, at: url, with: json) {
          self.GotFeatures(url, features)
        }
      case .failure:
        self.LoadError(url, Int32(response.response?.statusCode ?? -999), self.ERROR_IO_EXCEPTION)
      }
    }
  }

  open func setFeaturesFromGeoJSON(_ geoJson: String) {
    do {
      if let json = try getObjectFromJson(geoJson) as? [String: Any],
        let geoFeatures = GeoJSONUtil.processGeoJsonUrl(for: self, at: "<string>", with: json) as? [[Any]] {
        var features = [MapFeature]()
        for description in geoFeatures {
          try features.append(GeoJSONUtil.processGeoJSONFeature(container: self, description: description))
        }
        Features = features
      }
    } catch {
      LoadError("<string>", GeoJSONUtil.ERROR_CODE_JSON_PARSE_ERROR, GeoJSONUtil.ERROR_JSON_PARSE_ERROR)
    }
  }

  // MARK: events
  open override func FeatureClick(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureClick", arguments: feature as AnyObject)
    _map?.FeatureClick(feature)
  }

  open func FeatureLongClick(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureLongClick", arguments: feature as AnyObject)
    _map?.FeatureLongClick(feature)
  }

  open func FeatureStartDrag(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureStartDrag", arguments: feature as AnyObject)
    _map?.FeatureStartDrag(feature)
  }

  open func FeatureDrag(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureDrag", arguments: feature as AnyObject)
    _map?.FeatureDrag(feature)
  }

  open func FeatureStopDrag(_ feature: MapFeature) {
    EventDispatcher.dispatchEvent(of: self, called: "FeatureStopDag", arguments: feature as AnyObject)
    _map?.FeatureStopDrag(feature)
  }

  open func GotFeatures(_ url: String, _ features: [Any]) {
    _source = url
    EventDispatcher.dispatchEvent(of: self, called: "GotFeatures", arguments: url as NSString, features as NSObject)
  }

  open func LoadError(_ url: String, _ responseCode: Int32, _ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "LoadError", arguments: url as NSString, responseCode as NSNumber, message as NSString)
  }

  // MARK: reqyured methods
  func getMap() -> Map {
    return _map!
  }

  func addFeature(_ feature: MapFeature) {
    if !(feature is PolygonMarker) {
      _features.append(feature)
    }
    _map?.addFeature(feature)
  }

  func removeFeature(_ feature: MapFeature) {
    _features = _features.filter { return !$0.isEqual(feature) }
    _map?.removeFeature(feature)
  }

  var form: Form {
    get {
      return _form!
    }
  }
  
  public var container: ComponentContainer {
    get {
      return _container
    }
  }
  
  func add(_ component: ViewComponent) {}

  func setChildWidth(of component: ViewComponent, to width: Int32) {}

  func setChildHeight(of component: ViewComponent, to width: Int32) {}

  func setVisible(component: ViewComponent, to visibility: Bool) {
    component.view.isHidden = !visibility
  }

  func isVisible(component: ViewComponent) -> Bool {
    return !component.view.isHidden
  }
  
  public func isVisible() -> Bool {
    return _container.isVisible(component: self)
  }
}
