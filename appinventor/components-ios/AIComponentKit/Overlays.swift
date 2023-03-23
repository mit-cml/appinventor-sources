// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MapKit

/**
 * A generic protocol for handling Map overlays
 * (Circle, LineString, Polygon, Rectangle)
 */
public protocol MapOverlayShape: MKOverlay {
  /**
   * The object which houses all the overlay's internal properties
   * Intended to only be used internally
   */
  var props: ShapeProperties? { get set }

  /**
   * These values store the component and appropriate renderer
   * The renderer property is not used by collections
   */
  var feature: PolygonBase? { get set }
  var renderer: MKOverlayPathRenderer? { get }

  /**
   * used for grouping overlays for MutliPolygon/MultiLineString
   * allows properties to apply to all overlays simultaneously
   */
  var parentCollection: MapShapeCollection? { get }

  /**
   * These are properties passed down from the components
   * They are not capitalized to differentiate
   */
  var fillColor: Int32 { get set }
  var fillOpacity: Float { get set }
  var strokeColor: Int32 { get set }
  var strokeOpacity: Float { get set }
  var strokeWidth: Int32 { get set }
  var visible: Bool { get set }
}

/**
 * This struct is used to store properties common to all Shapes
 * Used to reduce code redundancy
 */
public struct ShapeProperties {
  fileprivate weak var renderer: MKOverlayPathRenderer? = nil {
    didSet {
      renderer?.fillColor = argbToColor(fillColor).withAlphaComponent(CGFloat(fillOpacity))
      renderer?.strokeColor = argbToColor(strokeColor).withAlphaComponent(CGFloat(strokeOpacity))
      renderer?.lineWidth = CGFloat(strokeWidth)
      renderer?.alpha = visible ? 1: 0
    }
  }

  fileprivate var fillColor: Int32 = 0 {
    didSet {
      renderer?.fillColor = argbToColor(fillColor).withAlphaComponent(CGFloat(fillOpacity))
    }
  }
  
  fileprivate var fillOpacity: Float = 1.0 {
    didSet {
      renderer?.fillColor = argbToColor(fillColor).withAlphaComponent(CGFloat(fillOpacity))
    }
  }

  fileprivate var strokeColor: Int32 = 0 {
    didSet {
      renderer?.strokeColor = argbToColor(strokeColor).withAlphaComponent(CGFloat(strokeOpacity))
    }
  }

  fileprivate var strokeWidth: Int32 = 0 {
    didSet {
      renderer?.lineWidth = CGFloat(strokeWidth)
    }
  }
  
  fileprivate var strokeOpacity: Float = 1 {
    didSet {
      renderer?.strokeColor = argbToColor(strokeColor).withAlphaComponent(CGFloat(strokeOpacity))
    }
  }

  fileprivate var visible: Bool = true {
    didSet {
      renderer?.alpha = visible ? 1: 0
    }
  }
}

/**
 * This extension allows for much of the common code to be shared
 * Serves as a wrapper for ShapeProperties
 */
extension MapOverlayShape where Self: MKShape {
  public var fillColor: Int32 {
    get {
      return props?.fillColor ?? 0
    }
    set(color) {
      props?.fillColor = color
    }
  }
  
  public var fillOpacity: Float {
    get {
      return props?.fillOpacity ?? 1
    }
    set(opacity) {
      props?.fillOpacity = opacity
    }
  }

  public var strokeColor: Int32 {
    get {
      return props?.strokeColor ?? 0
    }
    set(color) {
      props?.strokeColor = color
    }
  }
  
  public var strokeOpacity: Float {
    get {
      return props?.strokeOpacity ?? 1
    }
    set(opacity) {
      props?.strokeOpacity = opacity
    }
  }

  public var strokeWidth: Int32 {
    get {
      return Int32(renderer?.lineWidth ?? 0)
    }
    set(width) {
      props?.strokeWidth = width
    }
  }

  public var visible: Bool {
    get {
      return props?.visible ?? false
    }
    set(isVisible) {
      props?.visible = isVisible
    }
  }
}

/**
 * A class used to group multiple overlays and apply properties universally
 * Used primarily for handling MultiLineString and MultiPolygon
 */
@objc open class MapShapeCollection: MKShape, MapOverlayShape {
  public let shapes: [MapOverlayShape]
  public let boundingMapRect: MKMapRect = MKMapRect.null
  open weak var feature: PolygonBase? = nil {
    didSet {
      for shape in shapes {
        shape.feature = feature
      }
    }
  }

  // we do not need a parent collection or renderer
  open weak var parentCollection: MapShapeCollection? = nil
  public var renderer: MKOverlayPathRenderer? = nil

  // properties are handled separately
  public var props: ShapeProperties? = nil

  init(shapes: [MapOverlayShape]) {
    self.shapes = shapes
    super.init()
  }

  // MARK: properties
  open var fillColor: Int32 {
    get {
      return shapes.count == 0 ? 0: shapes[0].fillColor
    }
    set(color) {
      for shape in shapes {
        shape.fillColor = color
      }
    }
  }
  
  open var fillOpacity: Float {
    get {
      return shapes.count == 0 ? 1: shapes[0].fillOpacity
    }
    set(opacity) {
      for shape in shapes {
        shape.fillOpacity = opacity
      }
    }
  }

  open var strokeColor: Int32 {
    get {
      return shapes.count == 0 ? 0: shapes[0].strokeColor
    }
    set(color) {
      for shape in shapes {
        shape.strokeColor = color
      }
    }
  }
  
  open var strokeOpacity: Float {
    get {
      return shapes.count == 0 ? 1: shapes[0].strokeOpacity
    }
    set(opacity) {
      for shape in shapes {
        shape.strokeOpacity = opacity
      }
    }
  }

  open var strokeWidth: Int32 {
    get {
      return shapes.count == 0 ? 0: shapes[0].strokeWidth
    }
    set(width) {
      for shape in shapes {
        shape.strokeWidth = width
      }
    }
  }

  open var visible: Bool {
    get {
      return shapes.count == 0 ? false: shapes[0].visible
    }
    set(isVisible) {
      for shape in shapes {
        shape.visible = isVisible
      }
    }
  }
}

/*
 * An overlay handler for Circles
 */
open class MapCircleOverlay: MKCircle, MapOverlayShape {
  open weak var feature: PolygonBase? = nil
  open weak var parentCollection: MapShapeCollection? = nil
  public var props: ShapeProperties? = ShapeProperties()

  open var renderer: MKOverlayPathRenderer? {
    get {
      return props?.renderer ?? MKOverlayPathRenderer()
    }
    set(newRenderer) {
      if let circleRenderer = newRenderer as? MKCircleRenderer {
        props?.renderer = circleRenderer
      }
    }
  }
}

/**
 * An overlay handler for LineString
 */
open class MapLineOverlay: MKPolyline, MapOverlayShape {
  open weak var feature: PolygonBase? = nil
  open weak var parentCollection: MapShapeCollection? = nil
  public var props: ShapeProperties? = ShapeProperties()

  open var renderer: MKOverlayPathRenderer? {
    get {
      return props?.renderer ?? MKOverlayPathRenderer()
    }
    set(newRenderer) {
      if let lineRenderer = newRenderer as? MKPolylineRenderer {
        props?.renderer = lineRenderer
      }
    }
  }

  // Lines don't have a fill color. We override it here to prevent unwanted settings
  open var fillColor: Int32 {
    get {
      return 0
    }
    set(color) {}
  }
  
  // Lines don't have a fill color. We override it here to prevent unwanted settings
  open var fillOpacity: Float {
    get {
      return 1
    }
    set(opacity) {}
  }
}

/**
 * An overlay handler for Polygon and Rectangle
 */
open class MapPolygonOverlay: MKPolygon, MapOverlayShape {
  fileprivate weak var _renderer: MKPolygonRenderer? = nil
  fileprivate var _fillColor: Int32 = 0
  fileprivate var _fillOpacity: Float = 1
  fileprivate var _strokeColor: Int32 = 0
  fileprivate var _strokeWidth: Int32 = 0
  fileprivate var _strokeOpacity: Float = 1
  fileprivate var _visible: Bool = true
  open weak var feature: PolygonBase? = nil
  open weak var parentCollection: MapShapeCollection? = nil
  public var props: ShapeProperties? = ShapeProperties()

  open var renderer: MKOverlayPathRenderer? {
    get {
      return _renderer ?? MKOverlayPathRenderer()
    }
    set(newRenderer) {
      if let polygonRenderer = newRenderer as? MKPolygonRenderer {
        props?.renderer = polygonRenderer
      }
    }
  }
}

