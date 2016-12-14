//
//  AppManager.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/29/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

open class AppManager {
  fileprivate static var _appManager: AppManager?
  
  fileprivate init() {
    
  }

  open class var shared: AppManager {
    get {
      if AppManager._appManager == nil {
        AppManager._appManager = AppManager()
      }
      return AppManager._appManager!
    }
  }
}
