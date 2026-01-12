// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public final class BluetoothClient: BluetoothConnectionBase {
  fileprivate let TAG = "BluetoothClient"
 
  @objc public init(_ parent: ComponentContainer) {
    super.init(parent, TAG)
  }

  // MARK: BluetoothClient Properties
  @objc public var AddressesAndNames: [String] {
    get {
      return []
    }
  }

  // MARK: BluetoothClient Methods
  @objc public func IsDevicePaired(_ device: String) -> Bool {
    return false
  }
  
  @objc public func Connect(_ address: String) -> Bool {
    return false
  }

  @objc public func ConnectWithUUID(_ address: String, _ uuid: String) -> Bool {
    return false
  }
}
