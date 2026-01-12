// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public protocol OptionList {
  func toUnderlyingValue() -> AnyObject
}

/**
 * Generates a lookup table for classes that implement the `OptionList` protocol.
 *
 * - Parameter options: A varargs list of options
 * - Returns: A dictionary mapping the underlying value of each option to the option itself.
 */
func generateOptionsLookup<Key, T: OptionList>(_ options: T...) -> [Key:T] {
  var result = [Key:T]()
  options.forEach { item in
    guard let key = item.toUnderlyingValue() as? Key else {
      return
    }
    result[key] = item
  }
  return result
}
