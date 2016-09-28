//
//  MockComponent.swift
//  SchemeKit
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

class MockComponent {
  private var _latitude: Double = 0
  private var _longitude: Double = 0

  public init() {
  }

  public var Latitude: Double {
    get {
      return _latitude
    }
    set(latitude) {
      _latitude = latitude
    }
  }

  public var Longitude: Double {
    get {
      return _longitude
    }
    set(longitude) {
      _longitude = longitude
    }
  }

  public func SetCenter(latitude: Double, longitude: Double) {
    _latitude = latitude
    _longitude = longitude
  }
  
  public func ZoomChange(zoom: Int32) {
    NSLog("ZoomChange event: %d", zoom)
  }
}
