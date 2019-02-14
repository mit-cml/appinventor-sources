// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

extension String {
  func chopPrefix(count: Int = 1) -> String {
    return String(self[index(self.startIndex, offsetBy: count)...])
  }
  
  func chopSuffix(count: Int = 1) -> String {
    return String(self[..<index(self.endIndex, offsetBy: -count)])
  }
}
