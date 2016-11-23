//
//  BluetoothConnection.swift
//  AIComponentKit
//
//  Created by Evan Patton on 11/5/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public final class BluetoothClient: BluetoothConnectionBase {
 
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
  }

  // MARK: BluetoothClient Properties
  public var AddressesAndNames: [String] {
    get {
      
    }
  }

  // MARK: BluetoothClient Methods
  public func IsDevicePaired(_ device: String) -> Bool {
    
  }
  
  public func Connect(_ address: String) -> Bool {
    
  }

  public func ConnectWithUUID(_ address: String, _ uuid: String) -> Bool {
    
  }
}
