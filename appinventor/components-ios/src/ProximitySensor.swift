// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit
import CoreMotion

@objc open class ProximitySensor: NonvisibleComponent {
  private var motionManager = CMMotionManager()
  private var enabled: Bool = true
  private var distance: Float = 0.0
  private var keepRunningWhenOnPause: Bool = false
  
  override init(_ container: ComponentContainer) {
    super.init(container)
  }
  
  deinit {
    stopListening()
    NotificationCenter.default.removeObserver(self)
  }
  
  @objc var Available: Bool {
    return UIDevice.current.isProximityMonitoringEnabled
  }
  
  @objc var Distance: Float {
    return distance
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
  
  @objc var KeepRunningWhenOnPause: Bool {
    get {
      return keepRunningWhenOnPause
    }
    set {
      keepRunningWhenOnPause = newValue
    }
  }
  
  @objc var MaximumRange: Float {
    return Float(motionManager.deviceMotion?.attitude.roll ?? 0.0)
  }
  
  @objc func Initialize() {
    guard enabled else {
      return
    }
    startListening()
  }
  
  @objc private func appDidEnterBackground() {
    if !keepRunningWhenOnPause {
      stopListening()
    }
  }
  
  @objc private func appWillEnterForeground() {
    if enabled {
      startListening()
    }
  }
  
  @objc private func proximityStateChanged(_ notification: Notification) {
    if enabled {
      let device = notification.object as? UIDevice
      if device?.proximityState ?? false {
        distance = 0.0
      } else {
        distance = MaximumRange
      }
      ProximityChanged(distance)
    }
  }
  
  @objc func ProximityChanged(_ distance: Float) {
    self.distance = distance
    EventDispatcher.dispatchEvent(of: self, called: "ProximityChanged", arguments: NSNumber(floatLiteral: Double(distance)))
  }
  
  private func startListening() {
    guard enabled else { return }
    
    // Check if the proximity sensor is available
    let device = UIDevice.current
    device.isProximityMonitoringEnabled = true
    // Register for proximity state changes
    NotificationCenter.default.addObserver(self, selector: #selector(proximityStateChanged(_:)), name: UIDevice.proximityStateDidChangeNotification, object: device)
    // Configure motion manager for maximum range
    motionManager.startDeviceMotionUpdates()

    NotificationCenter.default.addObserver(self, selector: #selector(appDidEnterBackground), name: UIApplication.didEnterBackgroundNotification, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(appWillEnterForeground), name: UIApplication.willEnterForegroundNotification, object: nil)
  }
  
  private func stopListening() {
    motionManager.stopDeviceMotionUpdates()
    NotificationCenter.default.removeObserver(self)
  }
  
}
