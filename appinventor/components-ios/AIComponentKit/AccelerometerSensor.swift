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

public class AccelerometerSensor: NonvisibleComponent {
  private let _manager = CMMotionManager()
  private var _enabled = false
  private var _x = 0.0
  private var _y = 0.0
  private var _z = 0.0
  private var _sensitivity: Int32 = 2

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
  }

  // MARK: AccelerometerSensor Properties
  public var MinimumInterval: Int32 {
    get {
      return Int32(_manager.accelerometerUpdateInterval * 1000)
    }
    set(interval) {
      _manager.accelerometerUpdateInterval = TimeInterval(Double(interval) / 1000.0)
    }
  }
  
  public var Sensitivity: Int32 {
    get {
      return _sensitivity
    }
    set(sensitivity) {
      if sensitivity >= 1 && sensitivity <= 3 {
        _sensitivity = sensitivity
      } else {
        _form?.dispatchErrorOccurredEvent(self, "Sensitivity", ErrorMessages.ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY.code, ErrorMessages.ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY.message)
      }
    }
  }
  
  public var Available: Bool {
    get {
      return _manager.isAccelerometerAvailable || _manager.isDeviceMotionAvailable
    }
  }
  
  public var Enabled: Bool {
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
              self?.AccelerationChanged(x: gravity.x, y: gravity.y, z: gravity.z)
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
              self?.AccelerationChanged(x: acceleration.x * GRAVITY, y: acceleration.y, z: acceleration.z)
            }
          }
        }
      } else {
        _manager.stopAccelerometerUpdates()
        _manager.stopDeviceMotionUpdates()
      }
    }
  }
  
  public var XAccel: Double {
    get {
      return _x
    }
  }
  
  public var YAccel: Double {
    get {
      return _y
    }
  }
  
  public var ZAccel: Double {
    get {
      return _z
    }
  }
  
  // MARK: AccelerometerSensor Events
  public func AccelerationChanged(x: Double, y: Double, z: Double) {
    _x = x
    _y = y
    _z = z
    EventDispatcher.dispatchEvent(of: self, called: "AccelerationChanged", arguments: NSNumber(floatLiteral: x), NSNumber(floatLiteral: y), NSNumber(floatLiteral: z))
  }

  public func Shaking() {
    EventDispatcher.dispatchEvent(of: self, called: "Events")
  }
}
