// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

class SystemVariables {
  /**
   * Specifies whether the user is a new user. The default (true) is set in `AppDelegate.swift`.
   */
  static var newUser: Bool {
    get {
      /// Standard boolean defaults to false
      return UserDefaults.standard.bool(forKey: "isNewUser")
    }

    /// Run after Onboarding to ensure that it never runs again for the same user
    set(value) {
      UserDefaults.standard.set(value, forKey: "isNewUser")
    }
  }
  
  /**
   * Specifies whether the user is currently in a connected app screen.
   * Allows for changing the viewable options in the menu button.
   * Default (false) is set in `AppDelegate.swift`
   */
  static var inConnectedApp: Bool {
    
    /// Access variable to add "Download Project" option when connected to an app.
    get {
      return UserDefaults.standard.bool(forKey: "isInConnectedApp")
    }
    
    set(value) {
      UserDefaults.standard.set(value, forKey: "isInConnectedApp")
    }
  }
}
