//
//  AccelerometerSensor.swift
//  AIComponentKit
//
//  Created by Evan Patton on 11/5/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import CoreMotion

private let GRAVITY = 9.8

open class AccelerometerSensor: NonvisibleComponent {
  fileprivate let _manager = CMMotionManager()
  fileprivate var _enabled = false
  fileprivate var _x = 0.0
  fileprivate var _y = 0.0
  fileprivate var _z = 0.0
  fileprivate var _sensitivity: Int32 = 2

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
  }

  // MARK: AccelerometerSensor Properties
  open var MinimumInterval: Int32 {
    get {
      return Int32(_manager.accelerometerUpdateInterval * 1000)
    }
    set(interval) {
      _manager.accelerometerUpdateInterval = TimeInterval(Double(interval) / 1000.0)
    }
  }
  
  open var Sensitivity: Int32 {
    get {
      return _sensitivity
    }
    set(sensitivity) {
      if sensitivity >= 1 && sensitivity <= 3 {
        _sensitivity = sensitivity
      } else {
        _form.dispatchErrorOccurredEvent(self, "Sensitivity", ErrorMessages.ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY.code, ErrorMessages.ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY.message)
      }
    }
  }
  
  open var Available: Bool {
    get {
      return _manager.isAccelerometerAvailable || _manager.isDeviceMotionAvailable
    }
  }
  
  open var Enabled: Bool {
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
              self?.AccelerationChanged(gravity.x, y: gravity.y, z: gravity.z)
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
              self?.AccelerationChanged(acceleration.x * GRAVITY, y: acceleration.y, z: acceleration.z)
            }
          }
        }
      } else {
        _manager.stopAccelerometerUpdates()
        _manager.stopDeviceMotionUpdates()
      }
    }
  }
  
  open var XAccel: Double {
    get {
      return _x
    }
  }
  
  open var YAccel: Double {
    get {
      return _y
    }
  }
  
  open var ZAccel: Double {
    get {
      return _z
    }
  }
  
  // MARK: AccelerometerSensor Events
  open func AccelerationChanged(_ x: Double, y: Double, z: Double) {
    _x = x
    _y = y
    _z = z
    EventDispatcher.dispatchEvent(of: self, called: "AccelerationChanged", arguments: NSNumber(floatLiteral: x), NSNumber(floatLiteral: y), NSNumber(floatLiteral: z))
  }

  open func Shaking() {
    EventDispatcher.dispatchEvent(of: self, called: "Events")
  }
}
