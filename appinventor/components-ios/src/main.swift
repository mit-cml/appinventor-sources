// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
