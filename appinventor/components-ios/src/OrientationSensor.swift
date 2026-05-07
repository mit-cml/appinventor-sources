// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreMotion
import CoreLocation

open class OrientationSensor: NonvisibleComponent, CLLocationManagerDelegate {
  fileprivate let _motion = CMMotionManager()
  fileprivate let _location = CLLocationManager()
  /*
   * While the default value is True, we will keep _enabled to true, ensuring that the sensor starts.
   * This should be changed in the future
   */
  fileprivate var _enabled = true

  fileprivate var _hasPendingUpdate = false
  fileprivate var _azimuth: Double = 0.0
  fileprivate var _pitch: Double = 0.0
  fileprivate var _roll: Double = 0.0

  @objc public override init(_ parent: ComponentContainer) {
    super.init(parent)
    _location.delegate = self
  }

  @objc open func Initialize() {
    if _enabled {
      _enabled = false
      Enabled = true
    }
  }

  @objc open var Available: Bool {
    get {
      return _motion.isDeviceMotionAvailable
    }
  }

  @objc open var Enabled: Bool {
    get {
      return _enabled
    }
    set(shouldEnable) {
      if _enabled != shouldEnable, Available {
        _enabled = shouldEnable
        if shouldEnable {
          _motion.startDeviceMotionUpdates(using: .xTrueNorthZVertical, to: OperationQueue.main, withHandler: processUpdate)
          _location.startUpdatingHeading()
        } else {
          _motion.stopDeviceMotionUpdates()
          _location.stopUpdatingHeading()
        }
      }
    }
  }

  @objc open var Azimuth: Double {
    get {
      return _azimuth
    }
  }

  @objc open var Pitch: Double {
    get {
      return _pitch
    }
  }

  @objc open var Roll: Double {
    get {
      return _roll
    }
  }

  @objc open var Angle: Double {
    get {
      return radToDeg(atan2(degToRad(_pitch), -degToRad(_roll)))
    }
  }

  @objc open var Magnitude: Double {
    get {
      return 1.0 - cos(degToRad(_pitch)) * cos(degToRad(_roll))
    }
  }

  public func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
    _azimuth = newHeading.magneticHeading
    registerUpdate()
  }

  fileprivate func processUpdate(_ orientationData: CMDeviceMotion?, _ error: Error?) {
    if let data = orientationData {
      _pitch = -radToDeg(data.attitude.pitch)
      _roll = normalizeRoll(data.attitude.roll)
      switch UIDevice.current.orientation {
      case .landscapeLeft:
        let temp = _pitch
        _pitch = -_roll
        _roll = temp
        break
      case .landscapeRight:
        let temp = _pitch
        _pitch = _roll
        _roll = -temp
        break
      default:
        break
      }
      registerUpdate()
    } else if error != nil {
      if let error = error as NSError? {
        if error.domain == CMErrorDomain &&
            error.code == CMErrorDeviceRequiresMovement.rawValue {
          return  // ignore this
        }
      }
      Enabled = false
      _form?.dispatchErrorOccurredEvent(self, "OrientationChanged",
          Int32(ErrorMessage.ERROR_IOS_ORIENTATION_SENSOR_DATA_ERROR.rawValue),
          "\(error!)")
    }
  }

  private func registerUpdate() {
    if !_hasPendingUpdate {
      self._hasPendingUpdate = true
      DispatchQueue.main.async {
        defer {
          self._hasPendingUpdate = false
        }
        self.OrientationChanged(self._azimuth, self._pitch, self._roll)
      }
    }
  }

  @objc open func OrientationChanged(_ azimuth: Double, _ pitch: Double, _ roll: Double) {
    EventDispatcher.dispatchEvent(of: self, called: "OrientationChanged", arguments: NSNumber(floatLiteral: azimuth), NSNumber(floatLiteral: pitch), NSNumber(floatLiteral: roll))
  }

  fileprivate func normalizeAzimuth(_ yawInRadians: Double) -> Double {
    let azimuth = 270 - radToDeg(yawInRadians)
    return (azimuth > 360) ? azimuth - 360: azimuth
  }

  fileprivate func normalizeRoll(_ rollInRadians: Double) -> Double {
    let roll = -radToDeg(rollInRadians)
    if roll > 90 {
      return 180 - roll
    } else if roll < -90 {
      return  -(180 + roll)
    } else {
      return roll
    }
  }

  fileprivate func radToDeg(_ radians: Double) -> Double {
    return radians * 180 / .pi
  }

  fileprivate func degToRad(_ degrees: Double) -> Double {
    return degrees * .pi / 180
  }
}
