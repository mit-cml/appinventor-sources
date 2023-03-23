// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

extension String {
  func chopPrefix(count: Int = 1) -> String {
    return String(self[index(self.startIndex, offsetBy: count)...])
  }
  
  func chopSuffix(count: Int = 1) -> String {
    return String(self[..<index(self.endIndex, offsetBy: -count)])
  }
}
