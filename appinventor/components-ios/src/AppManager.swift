// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
