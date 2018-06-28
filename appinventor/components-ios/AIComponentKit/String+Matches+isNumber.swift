// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

extension String {
  func matches(_ regex: String) -> Bool {
    return self.range(of: regex, options: .regularExpression, range: nil, locale: nil) != nil
  }
}

extension String  {
  var isNumber: Bool {
    return !isEmpty && self.matches("^[-+]?[0-9]*[.]?[0-9]*$")
  }
}
