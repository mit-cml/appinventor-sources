// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

extension String {
  public init(format: String, messageArgs: [Any]) {
    var formatted = "", next = format.firstIndex(of: "%"), i = 0, f = format
    while (next != nil) {
      formatted += f[..<next!]
      switch f[f.index(next!, offsetBy: 1)] {
      case "s":
        formatted += String(describing: messageArgs[i])
        i += 1
      case "d", "f":
        formatted += (messageArgs[i] as! NSNumber).stringValue
        i += 1
      case "%":
        formatted += "%"
      default:
        break
      }
      f = String(f[f.index(next!, offsetBy: 2)...])
      next = f.firstIndex(of: "%")
    }
    formatted += f
    self.init(formatted)
  }
}
