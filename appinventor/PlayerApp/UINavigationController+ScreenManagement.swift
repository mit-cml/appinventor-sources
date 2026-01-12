// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import AIComponentKit
import Foundation
import UIKit

extension UINavigationController {
  public func screenBefore(_ screen: Form) -> Form? {
    guard viewControllers.first != screen else {
      // First screen does not have a parent screen
      return nil
    }
    guard let index = viewControllers.firstIndex(of: screen) else {
      // Screen is not in the view controller
      return nil
    }
    return viewControllers[viewControllers.index(before: index)] as? Form
  }
}
