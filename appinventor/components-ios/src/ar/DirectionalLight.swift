// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import GLKit

@available(iOS 11.0, *)
open class DirectionalLight: ARLightBase, ARDirectionalLight {
  fileprivate var _shadowColor = UIColor.black.withAlphaComponent(50)
  fileprivate var _shadowOpacity: CGFloat = 50
  
  @objc init(_ container: ARLightContainer) {
    super.init(container, type: .directional)
  }
  
}

@available(iOS 11.0, *)
extension DirectionalLight: HasDirectionEffects {
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
extension DirectionalLight: CastsShadows {
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
extension DirectionalLight: CanLook {
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
