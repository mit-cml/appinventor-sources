//
//  AssetManager.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/25/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class AssetManager: NSObject {
  private static var manager: AssetManager?
  
  public class var shared: AssetManager {
    get {
      if manager == nil {
        manager = AssetManager()
      }
      return manager!
    }
  }
}
