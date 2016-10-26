//
//  PhoneStatus.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class PhoneStatus : NonvisibleComponent {
  func GetWifiIpAddress() -> String {
    return NetworkUtils.getIPAddress()
  }
  
  func isDirect() -> Bool {
    //TODO: implementation
    return false
  }
}
