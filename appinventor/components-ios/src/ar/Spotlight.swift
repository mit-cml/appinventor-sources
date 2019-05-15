// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 11.0, *)
class Spotlight: ARLightBase, ARSpotlight {
  fileprivate var _falloffType = ARFalloffType.quadratic
  fileprivate var _shadowColor = UIColor.black.withAlphaComponent(50)
  fileprivate var _shadowOpacity: CGFloat = 50
  
  @objc public init(_ container: ARLightContainer) {
    super.init(container, type: .spot)
    _light.zNear = 0.01
    _light.zFar = 10.0
  }
  
  @objc open var SpotInnerAngle: Float {
    get {
      return Float(_light.spotInnerAngle)
    }
    set(innerAngle) {
      let angle = min(max(innerAngle, 0), 180)
      _light.spotInnerAngle = CGFloat(angle)
    }
  }
  
  @objc open var SpotOuterAngle: Float {
    get {
      return Float(_light.spotOuterAngle)
    }
    set(outerAngle) {
      let angle = min(max(outerAngle, 0), 180)
      _light.spotOuterAngle = CGFloat(angle)
    }
  }
  
  @objc open var MinimumDistanceForShadows: Float {
    get {
      return UnitHelper.metersToCentimeters(_light.zNear)
    }
    set(distance) {
      let zNear = max(0, distance)
      _light.zNear = UnitHelper.centimetersToMeters(zNear)
    }
  }
  
  @objc open var MaximumDistanceForShadows: Float {
    get {
      return UnitHelper.metersToCentimeters(_light.zFar)
    }
    set(distance) {
      let zFar = max(0, distance)
      _light.zFar = UnitHelper.centimetersToMeters(zFar)
    }
  }
}

@available(iOS 11.0, *)
extension Spotlight: CastsShadows {
  @objc open var CastsShadows: Bool {
    get {
      return _light.castsShadow
    }
    set(castsShadows) {
      _light.castsShadow = castsShadows
    }
  }
  
  @objc open var ShadowColor: Int32 {
    get {
      if let color = _light.shadowColor as? UIColor {
        return colorToArgb(color)
      }
      return Int32(bitPattern: AIComponentKit.Color.none.rawValue)
    }
    set(color) {
      _shadowColor = argbToColor(color)
      _light.shadowColor = _shadowColor.withAlphaComponent(shadowAlpha())
    }
  }
  
  @objc open var ShadowOpacity: Int32 {
    get {
      return Int32(round(_shadowOpacity))
    }
    set(opacity) {
      _shadowOpacity = CGFloat(min(max(0, opacity), 100))
      _light.shadowColor = _shadowColor.withAlphaComponent(shadowAlpha())
    }
  }
  
  private func shadowAlpha() -> CGFloat {
    return _shadowOpacity / 100
  }
}

@available(iOS 11.0, *)
extension Spotlight: HasPositionEffects {
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

@available(iOS 11.0, *)
extension Spotlight: HasDirectionEffects {
  @objc open var XRotation: Float {
    get {
      return GLKMathRadiansToDegrees(getNode().simdEulerAngles.x)
    }
    set(rotation) {
      getNode().simdEulerAngles.x = GLKMathDegreesToRadians(rotation)
    }
  }
  
  @objc open var YRotation: Float {
    get {
      return GLKMathRadiansToDegrees(getNode().simdEulerAngles.y)
    }
    set(rotation) {
      getNode().simdEulerAngles.y = GLKMathDegreesToRadians(rotation)
    }
  }
  
  @objc open var ZRotation: Float {
    get {
      return GLKMathRadiansToDegrees(getNode().simdEulerAngles.z)
    }
    set(rotation) {
      getNode().simdEulerAngles.z = GLKMathDegreesToRadians(rotation)
    }
  }
  
  @objc open func RotateXBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    getNode().simdEulerAngles.x += radians
  }
  
  @objc open func RotateYBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    getNode().simdEulerAngles.y += radians
  }
  
  @objc open func RotateZBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    getNode().simdEulerAngles.z += radians
  }
}

@available(iOS 11.0, *)
extension Spotlight: HasFalloff {
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
extension Spotlight: CanLook {
  @objc open func LookAtNode(_ node: ARNode) {
    getNode().look(at: node.getPosition())
  }
  
  @objc open func LookAtDetectedPlane(_ detectedPlane: ARDetectedPlane) {
    getNode().look(at: detectedPlane.getPosition())
  }
  
  @objc open func LookAtSpotlight(_ light: ARSpotlight) {
    getNode().look(at: light.getPosition())
  }
  
  @objc open func LookAtPointLight(_ light: ARPointLight) {
    getNode().look(at: light.getPosition())
  }
  
  @objc open func LookAtPosition(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    
    getNode().simdLook(at: simd_float3(xMeters, yMeters, zMeters))
  }
}
