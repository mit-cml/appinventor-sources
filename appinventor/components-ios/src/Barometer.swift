// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreMotion

@objc open class Barometer: NonvisibleComponent {
  private let motionManager = CMAltimeter()
  private var refreshTimer: Timer?
  private var enabled = true
  @objc var AirPressure: NSNumber = 0.0
  @objc var Available: Bool = false
  @objc var RefreshTime: TimeInterval = 0.001
  
  override init(_ container: ComponentContainer) {
    super.init(container)
    checkAvailability()
  }
  
  @objc var Enabled: Bool {
    get {
      return enabled
    }
    set(newValue) {
      if enabled != newValue {
        enabled = newValue
        if enabled {
          startListening()
        } else {
          stopListening()
        }
      }
    }
  }
  
  @objc func AirPressureChanged(_ pressure: Double) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "AirPressureChanged", arguments: NSNumber(floatLiteral: pressure))
    }
  }
  
  @objc func refreshAirPressure() {
    motionManager.startRelativeAltitudeUpdates(to: OperationQueue.current!) { [weak self] (altitudeData, error) in
      guard let altitudeData = altitudeData, error == nil else {
        return
      }
      let pressure = self?.calculatePressure(from: altitudeData.relativeAltitude.doubleValue)
      self?.AirPressure = NSNumber(value: pressure ?? 0.0)
      if let pressure = pressure {
        self?.AirPressureChanged(pressure)
      }
    }
  }
  
  @objc func Initialize() {
    guard enabled else {
      return
    }
    startListening()
  }
  
  private func calculatePressure(from altitude: Double) -> Double {
    let pressureAtSeaLevel = 1013.25 // Standard atmospheric pressure at sea level in hPa
    let pressure = pressureAtSeaLevel * pow((1 - 2.25577e-5 * altitude), 5.25588)
    return pressure
  }
  
  func startListening() {
    guard Enabled && Available else {
      return
    }
    motionManager.startRelativeAltitudeUpdates(to: OperationQueue.current!) { [weak self] (altitudeData, error) in
      guard let altitudeData = altitudeData, error == nil else {
        return
      }
      let pressure = self?.calculatePressure(from: altitudeData.relativeAltitude.doubleValue)
      self?.AirPressure = NSNumber(value: pressure ?? 0.0)
      if let pressure = pressure {
        self?.AirPressureChanged(pressure)
      }
    }
    refreshTimer = Timer.scheduledTimer(timeInterval: RefreshTime, target: self, selector: #selector(refreshAirPressure), userInfo: nil, repeats: true)
  }
  
  func stopListening() {
    motionManager.stopRelativeAltitudeUpdates()
    refreshTimer?.invalidate()
    refreshTimer = nil
  }
  
  func checkAvailability() {
    Available = CMAltimeter.isRelativeAltitudeAvailable()
  }
}
