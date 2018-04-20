// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

extension String {
  func replace(target: String, withString: String) -> String{
    return self.replacingOccurrences(of: target, with: withString, options: NSString.CompareOptions.literal, range: nil)
  }
}
