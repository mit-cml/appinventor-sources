// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

extension Array {
  /**
   * Converts the contents of this array into a new `[String]`. If the receiver is already
   * a `[String]`, this will make a shallow copy.
   *
   * - Returns: a new `Array` containing `String` representations of the receiver's elements
   */
  public func toStringArray() -> [String] {
    var copy = [String]()
    var first = true
    for el in self {
      if first && el is SCMSymbol {  // skip *list* header
        first = false
      } else {
        copy.append(toString(el))
      }
    }
    return copy
  }
}
