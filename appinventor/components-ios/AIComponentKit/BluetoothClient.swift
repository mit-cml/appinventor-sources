//
//  BluetoothConnection.swift
//  AIComponentKit
//
//  Created by Evan Patton on 11/5/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public final class BluetoothClient: BluetoothConnectionBase {
  fileprivate let TAG = "BluetoothClient"
 
  public init(_ parent: ComponentContainer) {
    super.init(parent, TAG)
  }

  // MARK: BluetoothClient Properties
  public var AddressesAndNames: [String] {
    get {
      return []
    }
  }

  // MARK: BluetoothClient Methods
  public func IsDevicePaired(_ device: String) -> Bool {
    return false
  }
  
  public func Connect(_ address: String) -> Bool {
    return false
  }

  public func ConnectWithUUID(_ address: String, _ uuid: String) -> Bool {
    return false
  }
}
