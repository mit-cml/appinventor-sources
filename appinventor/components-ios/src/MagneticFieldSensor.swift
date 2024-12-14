// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreMotion

@objc open class MagneticFieldSensor: NonvisibleComponent {
  private let motionManager = CMMotionManager()
  private var enabled : Bool = true
  
  @objc var AbsoluteStrength: Double {
    guard let magnetometerData = motionManager.magnetometerData?.magneticField else {
      return 0.0
    }
    let xStrength = Float(magnetometerData.x)
    let yStrength = Float(magnetometerData.y)
    let zStrength = Float(magnetometerData.z)
    return sqrt(Double(xStrength * xStrength + yStrength * yStrength + zStrength * zStrength))
  }
  @objc var Available: Bool {
    return motionManager.isMagnetometerAvailable
  }
  
  @objc var XStrength: Float {
    return Float(motionManager.magnetometerData?.magneticField.x ?? 0.0)
  }
  
  @objc var YStrength: Float {
    return Float(motionManager.magnetometerData?.magneticField.y ?? 0.0)
  }
  
  @objc var ZStrength: Float {
    return Float(motionManager.magnetometerData?.magneticField.z ?? 0.0)
  }
  
  override init(_ container: ComponentContainer) {
    super.init(container)
  }
  
  @objc var Enabled: Bool {
    get {
      return enabled
    }
    set(enable) {
      if enabled != enable {
        enabled = enable
        if enabled {
          startListening()
        } else {
          stopListening()
        }
      }
    }
  }
  
  @objc func Initialize() {
    guard enabled else {
      return
    }
    startListening()
  }
  
  @objc func MagneticChanged(_ xStrength: Float, _ yStrength: Float, _ zStrength: Float, _ absoluteStrength: Double) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "MagneticChanged", arguments: NSNumber(floatLiteral: Double(xStrength)), NSNumber(floatLiteral: Double(yStrength)), NSNumber(floatLiteral: Double(zStrength)), NSNumber(floatLiteral: Double(absoluteStrength)))
    }
  }
  
  private func startListening() {
    guard enabled else {
      return
    }
    
    if motionManager.isMagnetometerAvailable && !motionManager.isMagnetometerActive {
      motionManager.startMagnetometerUpdates(to: .main) { [weak self] (data, error) in
        guard let self = self, let magnetometerData = data?.magneticField else {
          return
        }
        
        let xStrength = Float(magnetometerData.x)
        let yStrength = Float(magnetometerData.y)
        let zStrength = Float(magnetometerData.z)
        let absoluteStrength = sqrt(Double(xStrength * xStrength + yStrength * yStrength + zStrength * zStrength))
        
        if self.enabled {
          self.MagneticChanged(xStrength, yStrength, zStrength, absoluteStrength)
        }
      }
    }
  }
  
  private func stopListening() {
    if motionManager.isMagnetometerActive {
      motionManager.stopMagnetometerUpdates()
    }
  }
}
