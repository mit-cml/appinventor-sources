// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import CoreMotion

fileprivate let kDefaultGyroscopeSamplingRate = 0.1
fileprivate let kDefaultGyroscopeTolerance: Float = 0.5

open class GyroscopeSensor: NonvisibleComponent {
  fileprivate var _enabled = false
  fileprivate var _xAngularVelocity: Float32 = 0
  fileprivate var _yAngularVelocity: Float32 = 0
  fileprivate var _zAngularVelocity: Float32 = 0
  fileprivate let _motion = CMMotionManager()
  fileprivate static let gyroQueue = OperationQueue()

  public override init(_ parent: ComponentContainer) {
    _motion.gyroUpdateInterval = kDefaultGyroscopeSamplingRate
    super.init(parent)
    Enabled = true
  }

  open var Available: Bool {
    get {
      return _motion.isGyroAvailable
    }
  }

  open var Enabled: Bool {
    get {
      return _enabled
    }
    set(shouldEnable) {
      if Available, _enabled != shouldEnable {
        _enabled = shouldEnable
        if shouldEnable {
          _motion.startGyroUpdates(to: GyroscopeSensor.gyroQueue, withHandler: processUpdate)
        } else {
          _motion.stopGyroUpdates()
        }
      }
    }
  }

  open var XAngularVelocity: Float32 {
    get {
      return _xAngularVelocity
    }
  }

  open var YAngularVelocity: Float32 {
    get {
      return _yAngularVelocity
    }
  }

  open var ZAngularVelocity: Float32 {
    get {
      return _zAngularVelocity
    }
  }

  fileprivate func processUpdate(data: CMGyroData?, error: Error?) {
    if let gyroError = error {
      _form.dispatchErrorOccurredEvent(self, "GyroscopeChanged", ErrorMessage.ERROR_IOS_GYROSCOPE_SENSOR_DATA_ERROR.code, ErrorMessage.ERROR_IOS_GYROSCOPE_SENSOR_DATA_ERROR.message, gyroError.localizedDescription)
      _motion.stopGyroUpdates()
    } else if let gyroData = data {
      var changed = significant(&_xAngularVelocity, gyroData.rotationRate.x)
      changed = significant(&_yAngularVelocity, gyroData.rotationRate.y) || changed
      changed = significant(&_zAngularVelocity, gyroData.rotationRate.z) || changed
      if changed
         {
        GyroscopeChanged(xAngularVelocity: _xAngularVelocity, yAngularVelocity: _yAngularVelocity, zAngularVelocity: _zAngularVelocity, timestamp: UInt64(gyroData.timestamp * 10E9))
      }
    }
  }

  open func GyroscopeChanged(xAngularVelocity: Float32, yAngularVelocity: Float32, zAngularVelocity: Float32, timestamp: UInt64) {
    EventDispatcher.dispatchEvent(of: self, called: "GyroscopeChanged", arguments: NSNumber(value: xAngularVelocity)
      , NSNumber(value: yAngularVelocity), NSNumber(value: zAngularVelocity), NSNumber(value: timestamp))

  }

  fileprivate func radToDeg(_ radians: Double) -> Float32 {
    return Float32(radians * 180 / .pi)
  }

  fileprivate func significant(_ previous: inout Float32, _ current: Double) -> Bool {
    if abs(previous - radToDeg(current)) > kDefaultGyroscopeTolerance {
      previous = radToDeg(current)
      return true
    }
    return false
  }
}
