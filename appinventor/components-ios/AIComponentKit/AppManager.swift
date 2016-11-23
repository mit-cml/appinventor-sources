//
//  AppManager.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/29/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class AppManager {
  private static var _appManager: AppManager?
  
  private init() {
    
  }

  public class var shared: AppManager {
    get {
      if AppManager._appManager == nil {
        AppManager._appManager = AppManager()
      }
      return AppManager._appManager!
    }
  }
}
