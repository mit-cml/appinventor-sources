// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2020-2021 Massachusetts Institute of Technology, All rights reserved.

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
}
