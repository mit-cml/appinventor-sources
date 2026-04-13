// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import GLKit

@available(iOS 14.0, *)
open class DirectionalLight: ARLightBase, ARDirectionalLight {
  private var _shadowColor = UIColor.black.withAlphaComponent(0.5)
  private var _shadowOpacity: CGFloat = 50
  private var _castsShadows: Bool = false
  
  @objc init(_ container: ARLightContainer) {
    super.init(container, type: .directional)
    setupDirectionalLight()
  }
  
  private func setupDirectionalLight() {
    // Create directional light component
    var lightComponent = DirectionalLightComponent()
    lightComponent.color = .white
    lightComponent.intensity = 1000 // Default intensity
    lightComponent.isRealWorldProxy = false
    
    _modelEntity.components.set(lightComponent)
    
    // Set initial shadow configuration
    updateShadowSettings()
  }
  
  private func updateShadowSettings() {
    guard let lightComponent = _modelEntity.components[DirectionalLightComponent.self] else { return }
    
    if _castsShadows {
      //lightComponent.shadow = DirectionalLightComponent.Shadow()
    } else {
      //lightComponent.shadow = nil
    }
    
    _modelEntity.components.set(lightComponent)
  }
  
  override open var Intensity: Float {
    get {
      return _modelEntity.components[DirectionalLightComponent.self]?.intensity ?? 1000
    }
    set(intensity) {
      guard var lightComponent = _modelEntity.components[DirectionalLightComponent.self] else { return }
      lightComponent.intensity = intensity
      _modelEntity.components.set(lightComponent)
    }
  }
  
  override open var Color: Int32 {
    get {
      let color = _modelEntity.components[DirectionalLightComponent.self]?.color ?? .white
      return Int32(color.hashValue)
    }
    set(color) {
      guard var lightComponent = _modelEntity.components[DirectionalLightComponent.self] else { return }
     // lightComponent.color = UIColor(argbToColor(color)).cgColor
      _modelEntity.components.set(lightComponent)
    }
  }
}

@available(iOS 14.0, *)
extension DirectionalLight: HasDirectionEffects {
  @objc open var XRotation: Float {
    get {
      let euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      return GLKMathRadiansToDegrees(euler.x)
    }
    set(rotation) {
      var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      euler.x = GLKMathDegreesToRadians(rotation)
      _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
    }
  }
  
  @objc open var YRotation: Float {
    get {
      let euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      return GLKMathRadiansToDegrees(euler.y)
    }
    set(rotation) {
      var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      euler.y = GLKMathDegreesToRadians(rotation)
      _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
    }
  }
  
  @objc open var ZRotation: Float {
    get {
      let euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      return GLKMathRadiansToDegrees(euler.z)
    }
    set(rotation) {
      var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
      euler.z = GLKMathDegreesToRadians(rotation)
      _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
    }
  }
  
  @objc open func RotateXBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
    euler.x += radians
    _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
  }
  
  @objc open func RotateYBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
    euler.y += radians
    _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
  }
  
  @objc open func RotateZBy(_ degrees: Float) {
    let radians = GLKMathDegreesToRadians(degrees)
    var euler = quaternionToEulerAngles(_modelEntity.transform.rotation)
    euler.z += radians
    _modelEntity.transform.rotation = eulerAnglesToQuaternion(euler)
  }
  
  // Quaternion/Euler conversion helpers (same as ARNodeBase)
  private func quaternionToEulerAngles(_ q: simd_quatf) -> SIMD3<Float> {
    let w = q.vector.w
    let x = q.vector.x
    let y = q.vector.y
    let z = q.vector.z
    
    let sinr_cosp = 2 * (w * x + y * z)
    let cosr_cosp = 1 - 2 * (x * x + y * y)
    let roll = atan2(sinr_cosp, cosr_cosp)
    
    let sinp = 2 * (w * y - z * x)
    let pitch: Float
    if abs(sinp) >= 1 {
      pitch = copysign(Float.pi / 2, sinp)
    } else {
      pitch = asin(sinp)
    }
    
    let siny_cosp = 2 * (w * z + x * y)
    let cosy_cosp = 1 - 2 * (y * y + z * z)
    let yaw = atan2(siny_cosp, cosy_cosp)
    
    return SIMD3<Float>(roll, pitch, yaw)
  }
  
  private func eulerAnglesToQuaternion(_ euler: SIMD3<Float>) -> simd_quatf {
    let cx = cos(euler.x * 0.5)
    let sx = sin(euler.x * 0.5)
    let cy = cos(euler.y * 0.5)
    let sy = sin(euler.y * 0.5)
    let cz = cos(euler.z * 0.5)
    let sz = sin(euler.z * 0.5)
    
    let w = cx * cy * cz + sx * sy * sz
    let x = sx * cy * cz - cx * sy * sz
    let y = cx * sy * cz + sx * cy * sz
    let z = cx * cy * sz - sx * sy * cz
    
    return simd_quatf(ix: x, iy: y, iz: z, r: w)
  }
}

@available(iOS 14.0, *)
extension DirectionalLight: CastsShadows {
  @objc open var CastsShadows: Bool {
    get {
      return _castsShadows
    }
    set(castsShadows) {
      _castsShadows = castsShadows
      updateShadowSettings()
    }
  }
  
  @objc open var ShadowColor: Int32 {
    get {
      return colorToArgb(_shadowColor)
    }
    set(color) {
      _shadowColor = argbToColor(color)
      updateShadowSettings()
    }
  }
  
  @objc open var ShadowOpacity: Int32 {
    get {
      return Int32(round(_shadowOpacity))
    }
    set(opacity) {
      _shadowOpacity = CGFloat(min(max(0, opacity), 100))
      _shadowColor = _shadowColor.withAlphaComponent(_shadowOpacity / 100)
      updateShadowSettings()
    }
  }
}

@available(iOS 14.0, *)
extension DirectionalLight: CanLook {
  @objc open func LookAtNode(_ node: ARNode) {
    lookAtPosition(node.getPosition())
  }
  
  @objc open func LookAtDetectedPlane(_ detectedPlane: ARDetectedPlane) {
    lookAtPosition(detectedPlane.getPosition())
  }
  
  @objc open func LookAtSpotlight(_ light: ARSpotlight) {
    lookAtPosition(light.getPosition())
  }
  
  @objc open func LookAtPointLight(_ light: ARPointLight) {
    lookAtPosition(light.getPosition())
  }
  
  @objc open func LookAtPosition(_ x: Float, _ y: Float, _ z: Float) {
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    lookAtPosition(SIMD3<Float>(xMeters, yMeters, zMeters))
  }
  
  private func lookAtPosition(_ targetPosition: SIMD3<Float>) {
    let currentPosition = _modelEntity.transform.translation
    let direction = normalize(targetPosition - currentPosition)
    
    // Calculate rotation to look at target
    let up = SIMD3<Float>(0, 1, 0)
    let right = normalize(cross(up, direction))
    let actualUp = cross(direction, right)
    
    // Create rotation matrix and convert to quaternion
    let rotationMatrix = float3x3(right, actualUp, direction)
    _modelEntity.transform.rotation = simd_quatf(rotationMatrix)
  }
}
