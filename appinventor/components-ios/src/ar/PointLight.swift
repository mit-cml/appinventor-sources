// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.0, *)
class PointLight: ARLightBase, ARPointLight {
  fileprivate var _falloffType = ARFalloffType.quadratic
  
  @objc public init(_ container: ARLightContainer) {
    super.init(container, type: .omni)
  }
}

@available(iOS 11.0, *)
extension PointLight: HasFalloff {
  @objc open var FalloffStartDistance: Float {
    get {
      return UnitHelper.metersToCentimeters(_light.attenuationStartDistance)
    }
    set(distance) {
      _light.attenuationStartDistance = UnitHelper.centimetersToMeters(max(0, distance))
    }
  }
  
  @objc open var FalloffEndDistance: Float {
    get {
      return UnitHelper.metersToCentimeters(_light.attenuationEndDistance)
    }
    set(distance) {
      _light.attenuationEndDistance = UnitHelper.centimetersToMeters(max(0, distance))
    }
  }
  
  @objc open var FalloffType: Int32 {
    get {
      return _falloffType.rawValue
    }
    set(falloff) {
      guard 0...2 ~= falloff else {
        _container?.form?.dispatchErrorOccurredEvent(self, "FalloffType", ErrorMessage.ERROR_INVALID_FALLOFF_TYPE.code, falloff)
        return
      }
      _falloffType = ARFalloffType.init(rawValue: falloff)!
      
      switch _falloffType {
      case .none:
        _light.attenuationFalloffExponent = 0.0
      case .linear:
        _light.attenuationFalloffExponent = 1.0
      case .quadratic:
        _light.attenuationFalloffExponent = 2.0
      }
    }
  }
}

@available(iOS 11.0, *)
extension PointLight: HasPositionEffects {
  @objc open var XPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(getNode().position.x)
    }
    set(position) {
      getNode().position.x = UnitHelper.centimetersToMeters(position)
    }
  }
  
  @objc open var YPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(getNode().position.y)
    }
    set(position) {
      getNode().position.y = UnitHelper.centimetersToMeters(position)
    }
  }
  
  @objc open var ZPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(getNode().position.z)
    }
    set(position) {
      getNode().position.z = UnitHelper.centimetersToMeters(position)
    }
  }
  
  @objc open func MoveBy(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    
    getNode().simdPosition += simd_float3(xMeters, yMeters, zMeters)
  }
  
  @objc open func MoveTo(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    
    getNode().simdPosition = simd_float3(xMeters, yMeters, zMeters)
  }
  
  @objc open func DistanceToNode(_ node: ARNode) -> Float {
    return UnitHelper.metersToCentimeters(getPosition().distanceFromPos(pos: node.getPosition()))
  }
  
  @objc open func DistanceToSpotlight(_ light: ARSpotlight) -> Float {
    return UnitHelper.metersToCentimeters(getPosition().distanceFromPos(pos: light.getPosition()))
  }
  
  @objc open func DistanceToPointLight(_ light: ARPointLight) -> Float {
    return UnitHelper.metersToCentimeters(getPosition().distanceFromPos(pos: light.getPosition()))
  }
  
  @objc open func DistanceToDetectedPlane(_ detectedPlane: ARDetectedPlane) -> Float {
    return UnitHelper.metersToCentimeters(getPosition().distanceFromPos(pos: detectedPlane.getPosition()))
  }
  
  open func getPosition() -> SCNVector3 {
    return getNode().position
  }
}
