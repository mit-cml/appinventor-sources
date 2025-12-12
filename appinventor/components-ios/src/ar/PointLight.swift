// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import GLKit

@available(iOS 14.0, *)
class PointLight: ARLightBase, ARPointLight {
  fileprivate var _falloffType = ARFalloffType.quadratic
  
  @objc public init(_ container: ARLightContainer) {
    super.init(container, type: .directional)
    setupPointLight()
  }
  
  private func setupPointLight() {
    // Create point light component
    var lightComponent = PointLightComponent()
    lightComponent.color = .white
    lightComponent.intensity = 1000 // Default intensity
    lightComponent.attenuationRadius = 10.0 // Default attenuation radius
    
    _modelEntity.components.set(lightComponent)
  }
  
  override open var Intensity: Float {
    get {
      return _modelEntity.components[PointLightComponent.self]?.intensity ?? 1000
    }
    set(intensity) {
      guard var lightComponent = _modelEntity.components[PointLightComponent.self] else { return }
      lightComponent.intensity = intensity
      _modelEntity.components.set(lightComponent)
    }
  }
  
  override open var Color: Int32 {
    get {
      let color = _modelEntity.components[PointLightComponent.self]?.color ?? .white
      return Int32(color.hashValue)
    }
    set(color) {
      guard var lightComponent = _modelEntity.components[PointLightComponent.self] else { return }
      // lightComponent.color = UIColor(argbToColor(color)).cgColor
      _modelEntity.components.set(lightComponent)
    }
  }
}

@available(iOS 14.0, *)
extension PointLight: HasFalloff {
  @objc open var FalloffStartDistance: Float {
    get {
      // RealityKit doesn't have separate start/end distances, using attenuation radius
      return UnitHelper.metersToCentimeters(_modelEntity.components[PointLightComponent.self]?.attenuationRadius ?? 10.0)
    }
    set(distance) {
      guard var lightComponent = _modelEntity.components[PointLightComponent.self] else { return }
      lightComponent.attenuationRadius = UnitHelper.centimetersToMeters(max(0, distance))
      _modelEntity.components.set(lightComponent)
    }
  }
  
  @objc open var FalloffEndDistance: Float {
    get {
      return UnitHelper.metersToCentimeters(_modelEntity.components[PointLightComponent.self]?.attenuationRadius ?? 10.0)
    }
    set(distance) {
      guard var lightComponent = _modelEntity.components[PointLightComponent.self] else { return }
      lightComponent.attenuationRadius = UnitHelper.centimetersToMeters(max(0, distance))
      _modelEntity.components.set(lightComponent)
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
      
      // Note: RealityKit doesn't have direct falloff exponent control like SceneKit
      // The falloff behavior is handled internally by the framework
    }
  }
}

@available(iOS 14.0, *)
extension PointLight: HasPositionEffects {
  @objc open var XPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(_modelEntity.transform.translation.x)
    }
    set(position) {
      _modelEntity.transform.translation.x = UnitHelper.centimetersToMeters(position)
    }
  }
  
  @objc open var YPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(_modelEntity.transform.translation.y)
    }
    set(position) {
      _modelEntity.transform.translation.y = UnitHelper.centimetersToMeters(position)
    }
  }
  
  @objc open var ZPosition: Float {
    get {
      return UnitHelper.metersToCentimeters(_modelEntity.transform.translation.z)
    }
    set(position) {
      _modelEntity.transform.translation.z = UnitHelper.centimetersToMeters(position)
    }
  }
  
  @objc open func MoveBy(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    
    _modelEntity.transform.translation += SIMD3<Float>(xMeters, yMeters, zMeters)
  }
  
  @objc open func MoveTo(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    
    _modelEntity.transform.translation = SIMD3<Float>(xMeters, yMeters, zMeters)
  }
  
  @objc open func DistanceToNode(_ node: ARNode) -> Float {
    return UnitHelper.metersToCentimeters(distance(getPosition(), node.getPosition()))
  }
  
  @objc open func DistanceToSpotlight(_ light: ARSpotlight) -> Float {
    return UnitHelper.metersToCentimeters(distance(getPosition(), light.getPosition()))
  }
  
  @objc open func DistanceToPointLight(_ light: ARPointLight) -> Float {
    return UnitHelper.metersToCentimeters(distance(getPosition(), light.getPosition()))
  }
  
  @objc open func DistanceToDetectedPlane(_ detectedPlane: ARDetectedPlane) -> Float {
    return UnitHelper.metersToCentimeters(distance(getPosition(), detectedPlane.getPosition()))
  }
  
  open func getPosition() -> SIMD3<Float> {
    return _modelEntity.transform.translation
  }
}
