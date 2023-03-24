// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreMotion

private let GRAVITY = -9.81
private let SENSOR_CACHE_SIZE = 10

/**
 * An AccelerometerSensor class.  A component that can detect shaking and
 * measure acceleration approximately in three dimensions using SI units m/s^2.
 */
open class AccelerometerSensor: NonvisibleComponent {
  fileprivate let _manager = CMMotionManager()
  fileprivate var _enabled = false
  fileprivate var _x = 0.0
  fileprivate var _y = 0.0
  fileprivate var _z = 0.0
  fileprivate var _sensitivity: AccelerometerSensitivity = AccelerometerSensitivity.moderate
  
  // Cache for shake detection
  private var X_CACHE: CircularBuffer<Double>
  private var Y_CACHE: CircularBuffer<Double>
  private var Z_CACHE: CircularBuffer<Double>
  private var _timeLastShook: Int = 0
  
  // Shake Thresholds
  private let weakShakeThreshold: Double = 3.2
  private let moderateShakeThreshold: Double = 4.01
  private let strongShakeThreshold: Double = 4.3
  
  fileprivate var _needsUpdate = false
  
  public override init(_ parent: ComponentContainer) {
    X_CACHE = CircularBuffer(SENSOR_CACHE_SIZE, 0.0)
    Y_CACHE = CircularBuffer(SENSOR_CACHE_SIZE, 0.0)
    Z_CACHE = CircularBuffer(SENSOR_CACHE_SIZE, 0.0)
    super.init(parent)
    MinimumInterval = 400
    Sensitivity = AccelerometerSensitivity.moderate.rawValue
    Enabled = true
    // TODO: add registration following form update
    //    _form.registerForOnResume(self)
    //    _form.registerForOnStop(self)
  }

  // MARK: AccelerometerSensor Properties
  @objc open var MinimumInterval: Int32 {
    get {
      return Int32(_manager.accelerometerUpdateInterval * 1000)
    }
    set(interval) {
      _manager.accelerometerUpdateInterval = TimeInterval(Double(interval) / 1000.0)
    }
  }
  
  @objc open var Sensitivity: Int32 {
    get {
      return _sensitivity.rawValue
    }
    set(sensitivity) {
      if sensitivity >= 1 && sensitivity <= 3 {
        _sensitivity = AccelerometerSensitivity(rawValue: sensitivity)!
      } else {
        _form?.dispatchErrorOccurredEvent(self, "Sensitivity",
            ErrorMessage.ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY.code,
            ErrorMessage.ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY.message)
      }
    }
  }
  
  @objc open var Available: Bool {
    get {
      return _manager.isAccelerometerAvailable || _manager.isDeviceMotionAvailable
    }
  }
  
  @objc open var Enabled: Bool {
    get {
      return _enabled
    }
    set(enabled) {
      _enabled = enabled
      if enabled {
        if _manager.isDeviceMotionAvailable {
          _manager.startDeviceMotionUpdates(to: OperationQueue.main) {
            [weak self] (data: CMDeviceMotion?, error: Error?) in
            if let gravity = data?.gravity {
              self?.AccelerationChanged(gravity.x * GRAVITY, y: gravity.y * GRAVITY, z: gravity.z * GRAVITY)
            } else if (error != nil) {
              self?.Enabled = false
            }
          }
        } else {
          _manager.startAccelerometerUpdates(to: OperationQueue.main) {
            [weak self] (data: CMAccelerometerData?, error: Error?) in
            if (error != nil) {
              self?.Enabled = false
            } else if let acceleration = data?.acceleration {
              self?.AccelerationChanged(acceleration.x * GRAVITY, y: acceleration.y * GRAVITY, z: acceleration.z * GRAVITY)
            }
          }
        }
      } else {
        _manager.stopAccelerometerUpdates()
        _manager.stopDeviceMotionUpdates()
      }
    }
  }
  
  @objc open var LegacyMode: Bool {
    get {
      return false
    }
    set (legacyMode) {
      // not necessary for iOS
    }
  }
  
  @objc open var XAccel: Double {
    get {
      return _x
    }
  }
  
  @objc open var YAccel: Double {
    get {
      return _y
    }
  }
  
  @objc open var ZAccel: Double {
    get {
      return _z
    }
  }
  
  // MARK: AccelerometerSensor Events
  @objc open func AccelerationChanged(_ x: Double, y: Double, z: Double) {
    _x = x
    _y = y
    _z = z
    
    if !_needsUpdate {
      _needsUpdate = true
      OperationQueue.main.addOperation {
        defer {
          self._needsUpdate = false
        }
        self.X_CACHE.write(x)
        self.Y_CACHE.write(y)
        self.Z_CACHE.write(z)
        
        let currentTimeInMS = Int(NSDate().timeIntervalSince1970 * 1000)
        if (self.isShaking(self.X_CACHE, x) || self.isShaking(self.Y_CACHE, y) || self.isShaking(self.Z_CACHE, z)) && (self._timeLastShook == 0 || currentTimeInMS >= self._timeLastShook + Int(self.MinimumInterval)) {
          self._timeLastShook = currentTimeInMS
          self.Shaking()
        }
        
        EventDispatcher.dispatchEvent(of: self, called: "AccelerationChanged", arguments: NSNumber(floatLiteral: x), NSNumber(floatLiteral: y), NSNumber(floatLiteral: z))
      }
    }
  }
  
  private func isShaking(_ cache: CircularBuffer<Double>, _ currentValue: Double) -> Bool {
    // cache size is fixed
    let average = cache.buffer.reduce(0, +) / Double(cache.size)
    let diff = abs(average - currentValue)
    
    // values based on iPhone tests comparatively to Android phone
    switch _sensitivity {
    case AccelerometerSensitivity.weak:
      return diff > strongShakeThreshold
    case AccelerometerSensitivity.moderate:
      return diff > moderateShakeThreshold
    case AccelerometerSensitivity.strong:
      return diff > weakShakeThreshold
    }
  }
  
  @objc open func Shaking() {
    EventDispatcher.dispatchEvent(of: self, called: "Shaking")
  }
}

