// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreBluetooth

@objc
open class BluetoothAdmin: NonvisibleComponent {
  // MARK: - Properties
  private var centralManager: CBCentralManager!
  private var _useCodes : Bool = false
  
  // MARK: - Initialization
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    centralManager = CBCentralManager(delegate: nil, queue: nil)
  }
  
  // MARK: - Bluetooth Functions
  @objc open var UseCodes: Bool {
    get {
      return _useCodes
    }
    set(argb) {
      _useCodes = argb
    }
  }
  
  @objc func MacAddress() {
    print("Cannot be implemented")
  }
  
  @objc func ValidateMacAddress(_ macAddress: String) {
    print("Cannot be implemented")
  }
  
  @objc func ValidateUserMacAddress() {
    print("Cannot be implemented")
  }
  
  @objc func State() {
    print("Cannot be implemented")
  }
  
  @objc func Enable() {
    openSettings()
  }
  
  @objc func Disable() {
    openSettings()
  }
  
  @objc func Toggle() {
    openSettings()
  }
  
  @objc func HasBluetooth() -> Bool {
    if centralManager.state == .poweredOn {
      // Bluetooth is available and powered on
      print("Device has Bluetooth")
      return true
    } else {
      // Bluetooth is not available or powered off
      print("Device does not have Bluetooth")
      return false
    }
  }
  
  @objc func Scan() {
    openSettings()
  }
  
  @objc func ScanMode() {
    print("Cannot be implemented: Scan Mode")
  }
  
  @objc func Pair(_ address: String) {
    print("Cannot be implemented: Pair with Bluetooth device")
  }
  
  @objc func Unpair(_ address: String) {
    print("Cannot be implemented: Unpair from Bluetooth device")
  }
  
  // MARK: - Events
  @objc func ErrorOccurred(message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "Error Occurred Event Called")
  }
  
  @objc func StateChanged(state: String) {
    EventDispatcher.dispatchEvent(of: self, called: "State Changed Event Called")
  }
  
  @objc func AfterScanning(pairedDevices: [Any], newDevices: [Any]) {
    EventDispatcher.dispatchEvent(of: self, called: "After Scanning Event Called")
  }
  
  @objc func AfterPairing(address: String) {
    EventDispatcher.dispatchEvent(of: self, called: "After Pairing Event Called")
  }
  
  @objc func AfterUnpairing(address: String) {
    EventDispatcher.dispatchEvent(of: self, called: "After Unpairing Event Called")
  }
  
  private func openSettings() {
    if let url = URL(string: "App-Prefs:root=General") {
      if UIApplication.shared.canOpenURL(url) {
        UIApplication.shared.open(url, options: [:], completionHandler: nil)
      }
    }
  }
}
