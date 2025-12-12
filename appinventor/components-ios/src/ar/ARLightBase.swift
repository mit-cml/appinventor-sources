// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

enum LightType {
  case ambient
  case directional
  case point
  case spot
}

@available(iOS 14.0, *)
open class ARLightBase: NSObject, ARLight {
  weak var _container: ARLightContainer?
  public var _modelEntity: Entity
  private var _lightType: LightType
  
  init(_ container: ARLightContainer, type: LightType) {
    _container = container
    _lightType = type
    _modelEntity = Entity()
    super.init()
  }
  
  @objc open var `Type`: String {
    get {
      switch _lightType {
      case .ambient:
        return "Ambient"
      case .directional:
        return "Directional"
      case .point:
        return "Point"
      case .spot:
        return "Spot"
      }
    }
  }
  
  @objc open var Color: Int32 {
    get {
      // Default implementation - subclasses should override
      return Int32(bitPattern: AIComponentKit.Color.white.rawValue)
    }
    set(color) {
      // Default implementation - subclasses should override
    }
  }
  
  @objc open var Temperature: Float {
    get {
      // RealityKit doesn't have temperature - return default
      return 6500 // Default daylight temperature
    }
    set(temperature) {
      // RealityKit doesn't support color temperature directly
      // Could convert to color if needed
    }
  }
  
  @objc open var Intensity: Float {
    get {
      // Default implementation - subclasses should override
      return 1000
    }
    set(intensity) {
      // Default implementation - subclasses should override
    }
  }
  
  // MARK: - Component Protocol Implementation
  @objc open var Width: Int32 {
    get { return 0 }
    set {}
  }
  
  @objc open var Height: Int32 {
    get { return 0 }
    set {}
  }
  
  @objc open var dispatchDelegate: HandlesEventDispatching? {
    get { return _container?.form?.dispatchDelegate }
  }
  
  public func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  public func setWidthPercent(_ toPercent: Int32) {}
  public func setHeightPercent(_ toPercent: Int32) {}
  
  // MARK: - Helper Methods
  
  func colorToArgb(_ color: UIColor) -> Int32 {
    var red: CGFloat = 0
    var green: CGFloat = 0
    var blue: CGFloat = 0
    var alpha: CGFloat = 0
    
    color.getRed(&red, green: &green, blue: &blue, alpha: &alpha)
    
    let r = Int32(red * 255) & 0xFF
    let g = Int32(green * 255) & 0xFF
    let b = Int32(blue * 255) & 0xFF
    let a = Int32(alpha * 255) & 0xFF
    
    return (a << 24) | (r << 16) | (g << 8) | b
  }
  
  func argbToColor(_ argb: Int32) -> UIColor {
    let alpha = CGFloat((argb >> 24) & 0xFF) / 255.0
    let red = CGFloat((argb >> 16) & 0xFF) / 255.0
    let green = CGFloat((argb >> 8) & 0xFF) / 255.0
    let blue = CGFloat(argb & 0xFF) / 255.0
    return UIColor(red: red, green: green, blue: blue, alpha: alpha)
  }
}
