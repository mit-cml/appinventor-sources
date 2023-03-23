// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreMotion

fileprivate let kIntervalVariation = 250
fileprivate let kNumIntervals = 2
fileprivate let kWindowSize = 100
fileprivate let kStrideLength = 0.73
fileprivate let kPeakValleyRange = 40 / 9.81

func currentTimeInMillis() -> Int64 {
  return Int64(Date.init().timeIntervalSince1970 * 1000)
}

open class Pedometer: NonvisibleComponent {
  fileprivate let _motion = CMMotionManager()
  fileprivate let _pedometer = CMPedometer()

  fileprivate var _stopDetectionTimeout: Int32 = 2000
  fileprivate var _winPos = 0, _intervalPos = 0
  fileprivate var _numWalkingSteps: Int32 = 0, _numRawSteps: Int32 = 0
  fileprivate var _lastValley = 0.0
  fileprivate var _lastValues = [Double](repeating: 0.0, count: kWindowSize)
  fileprivate var _strideLength = kStrideLength
  fileprivate var _totalDistance = 0.0
  fileprivate var _stepInterval = [Int64](repeating: 0, count: kNumIntervals)
  fileprivate var _stepTimestamp: Int64 = 0
  fileprivate var _startTime: Int64 = 0, _prevStopTime: Int64 = 0
  fileprivate var _foundValley = false
  fileprivate var _startPeaking = false
  fileprivate var _foundNonStep = true
  fileprivate var _paused = true

  fileprivate var _avgWindow = [Double](repeating: 0.0, count: 10)
  fileprivate var _avgPos = 0

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    let settings = UserDefaults()
    if let data = settings.persistentDomain(forName: "PedometerPrefs") {
      _strideLength = data["strideLength"] as? Double ?? 0
      _totalDistance = data["distance"] as? Double ?? 0
      _numRawSteps = data["prevStepCount"] as? Int32 ?? 0
      _prevStopTime = data["clockTime"] as? Int64 ?? 0
      _numWalkingSteps = _numRawSteps
    }
    _startTime = currentTimeInMillis()
  }

  @objc open func Start() {
    if _paused {
      _paused = false
      _startTime = currentTimeInMillis()
      if _motion.isAccelerometerAvailable {
        _motion.startAccelerometerUpdates(to: .main, withHandler: processAccelerationData)
      } else {
        Stop()
      }
    }
  }

  @objc open func Stop() {
    Pause()
  }

  @objc open func Reset() {
    _numWalkingSteps = 0
    _numRawSteps = 0
    _totalDistance = 0
    _prevStopTime = 0
    _startTime = currentTimeInMillis()
  }

  @objc open func Resume() {
    Start()
  }

  @objc open func Pause() {
    if !_paused {
      _paused = true
      _prevStopTime += currentTimeInMillis() - _startTime
      _motion.stopAccelerometerUpdates()
    }
  }

  @objc open func Save() {
    let settings = UserDefaults()
    settings.setPersistentDomain([
      "clockTime": _paused ? _prevStopTime: _prevStopTime + (currentTimeInMillis() - _startTime),
      "closeTime": currentTimeInMillis(),
      "distance": _totalDistance,
      "prevStepCount": _numRawSteps,
      "strideLength": _strideLength,
      ], forName: "PedometerPrefs")
  }

  fileprivate func processAccelerationData(_ accelerationData: CMAccelerometerData?, error: Error?) {
    if let data = accelerationData {
      let magnitude = pow(data.acceleration.x, 2) + pow(data.acceleration.y, 2) + pow(data.acceleration.z, 2)
      let mid = getMid()

      if _startPeaking, isPeak() {
          if _foundValley, _lastValues[mid] - _lastValley > kPeakValleyRange {
          let timestamp = currentTimeInMillis()
          _stepInterval[_intervalPos] = timestamp - _stepTimestamp
          _intervalPos = (_intervalPos + 1) % kNumIntervals
          _stepTimestamp = timestamp

          if stepsEquallySpaced() {
            if _foundNonStep {
              _numWalkingSteps += Int32(kNumIntervals)
              _totalDistance += _strideLength * Double(kNumIntervals)
              _foundNonStep = false
            }
            _numWalkingSteps += 1
            WalkStep(_numWalkingSteps, _totalDistance)
            _totalDistance += StrideLength
          } else {
            _foundNonStep = true
          }
          _numRawSteps += 1
          SimpleStep(_numRawSteps, _totalDistance)
          _foundValley = false
        }
      }

      if _startPeaking, isValley() {
        _foundValley = true
        _lastValley = _lastValues[mid]
      }

      _avgWindow[_avgPos] = magnitude
      _avgPos = (_avgPos + 1) % _avgWindow.count
      _lastValues[_winPos] = 0
      for m in _avgWindow { _lastValues[_winPos] += m  }
      _lastValues[_winPos] /= Double(_avgWindow.count)

      if _startPeaking || _winPos > 1 {
        var i = _winPos
        if i < -1 {
          i += kWindowSize - 1
        }
        _lastValues[_winPos] += 2 * _lastValues[i]
        if i < -1 {
          i += kWindowSize - 1
        }
        _lastValues[_winPos] += _lastValues[i]
        _lastValues[_winPos] /= 4
      } else if !_startPeaking, _winPos == 1 {
        _lastValues[1] = (_lastValues[1] + _lastValues[0]) / 2.0
      }

      let elapsedTime = currentTimeInMillis()
      if elapsedTime - _stepTimestamp > StopDetectionTimeout {
        _stepTimestamp = elapsedTime
      }

      if _winPos == kWindowSize - 1, !_startPeaking {
        _startPeaking = true
      }

      _winPos = (_winPos + 1) % kWindowSize
    } else if error != nil {
      Stop()
    }
  }

  fileprivate func getMid() -> Int {
    return  (_winPos + kWindowSize / 2) % kWindowSize
  }

  @objc open func SimpleStep(_ steps: Int32, _ distance: Double) {
    EventDispatcher.dispatchEvent(of: self, called: "SimpleStep", arguments: NSNumber(value: steps), NSNumber(floatLiteral: distance))
  }

  @objc open func WalkStep(_ steps: Int32, _ distance: Double) {
    EventDispatcher.dispatchEvent(of: self, called: "WalkStep", arguments: NSNumber(value: steps), NSNumber(floatLiteral: distance))
  }

  @objc open var StrideLength: Double {
    get {
      return _strideLength
    }
    set(newLength) {
      _strideLength = newLength
    }
  }

  @objc open var StopDetectionTimeout: Int32 {
    get {
      return _stopDetectionTimeout
    }
    set(newTimeout) {
      _stopDetectionTimeout = newTimeout
    }
  }

  @objc open var Distance: Double {
    return _totalDistance
  }

  @objc open var ElapsedTime: Int64 {
    return _prevStopTime + (_paused ? 0: currentTimeInMillis() - _startTime)
  }

  @objc open var SimpleSteps: Int32 {
    return _numRawSteps
  }

  @objc open var WalkSteps: Int32 {
    return _numWalkingSteps
  }

  fileprivate func stepsEquallySpaced() -> Bool {
    var avg = 0.0, num = 0

    for interval in _stepInterval {
      if interval > 0 {
        avg += Double(interval)
        num += 1
      }
    }
    avg = avg / Double(num)
    for interval in _stepInterval {
      if abs(Double(interval) - avg) > Double(kIntervalVariation) {
        return false
      }
    }
    return true
  }

  fileprivate func isPeak() -> Bool {
    let mid = getMid()
    for (index, value) in _lastValues.enumerated() {
      if index != mid, value > _lastValues[mid] {
        return false
      }
    }
    return true
  }

  fileprivate func isValley() -> Bool {
    let mid = getMid()
    for (index, value) in _lastValues.enumerated() {
      if index != mid, value < _lastValues[mid] {
        return false
      }
    }
    return true
  }

  // MARK: deprecated methods

  @objc open func StartedMoving() {}

  @objc open func StoppedMoving() {}

  @objc open var UseGPS: Bool = false

  @objc open func CalibrationFailed() {}

  @objc open func GPSAvailable() {}

  @objc open func GPSLost() {}

  @objc open func CalibrateStrideLength(_ calibrate: Bool) {}

  @objc open func CalibrateStrideLength() -> Bool { return false }

  @objc open func Moving() -> Bool { return false }
}
