// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import Alamofire
import GEOSwift

@objc class FeatureCollection: MapFeatureContainerBase, MapFeatureCollection {
  fileprivate var _features = [MapFeature]()
  fileprivate var _source: String = ""
  fileprivate weak var _map: Map?
  fileprivate weak var _form: Form?

  @objc override init(_ parent: ComponentContainer) {
    if let mapContainer = parent as? MapFeatureContainer {
      _map = mapContainer.getMap()
    }
    _form = parent.form
    super.init(parent)
  }

  // MARK: properties
  @objc open var Features: YailList<MapFeature> {
    get {
      return YailList<MapFeature>(array: _features as [AnyObject])
    }
    set(features) {
      while _features.count > 0 {
        removeFeature(_features[0])
      }
      for feature in features {
        if let feature = feature as? MapFeature {
          addFeature(feature)
        }
      }
    }
  }

  @objc open var Source: String {
    get {
      return _source
    }
    set(source) {
      _source = source
    }
  }

  // MARK: method
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
        Features = YailList<MapFeature>(array: features)
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

  // MARK: ComponentContainer implementation

  public var container: ComponentContainer? {
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

  open func getChildren() -> [Component] {
    return _features as [Component]
  }
}
