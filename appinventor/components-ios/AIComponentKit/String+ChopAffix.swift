// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

extension String {
  func chopPrefix(count: Int = 1) -> String {
    return self.substring(from:index(self.startIndex, offsetBy: count))
  }
  
  func chopSuffix(count: Int = 1) -> String {
    return self.substring(to: index(self.endIndex, offsetBy: -count))
  }
}
