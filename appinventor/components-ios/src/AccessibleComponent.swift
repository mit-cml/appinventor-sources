// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public protocol AccessibleComponent {
  /**
   * The component's contrast controller for better visibility
   */
  var HighContrast : Bool { get set }

  /**
   * The component's Font controller for better visibility
   */
  var LargeFont : Bool { get set }
}
