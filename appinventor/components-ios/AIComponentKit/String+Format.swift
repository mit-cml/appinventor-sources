// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

extension String {
  public init(format: String, messageArgs: [Any]) {
    var formatted = "", next = format.index(of: "%"), i = 0, f = format
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
      next = f.index(of: "%")
    }
    formatted += f
    self.init(formatted)
  }
}
