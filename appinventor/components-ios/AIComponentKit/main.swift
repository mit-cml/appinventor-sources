// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SchemeKit

var initialized = false

public class AppInventorRuntime {
  /**
   * Perform any special behavior needed for intiializing App Inventor's component runtime.
   */
  public static func initialize() {
    if (!initialized) {
      SCMNameResolver.register(Component.self, forName: "AIComponentKit.Component")
      initialized = true
    }
  }
}
