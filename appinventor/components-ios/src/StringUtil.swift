// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import SchemeKit

/**
 * Converts the given `object` to a `String` representation.
 *
 * - Parameter object: Any object to convert to a `String`
 *
 * - Returns: A `String` representing the object, or the object itself it is already a `String`.
 */
public func toString(_ object: Any?) -> String {
  guard let object = object else {
    return "null"
  }

  if let num = object as? NSNumber {
    if num.isBool {
      return num == true ? "true" : "false"
    } else {
      return String(describing: num)
    }
  } else if let object = object as? YailList<SCMValueProtocol> {
    if ReplForm.activeForm?.ShowListsAsJson ?? true {
      if let json = try? JSONSerialization.data(withJSONObject: object) {
        return String(data: json, encoding: .utf8) ?? "[]"
      } else {
        return "[]"
      }
    } else {
      return "(" + StringUtil.joinStrings(object, " ") + ")"
    }
  } else if let object = object as? String {
    return object
  } else {
    return String(describing: object)
  }
}

@objc class StringUtil: NSObject {
  /**
   * Constructs a new `String` by joining the elements of the `list` with the given `separator`.
   *
   * - Parameter list: A `YailList` of entities.
   * - Parameter separator: A `String` used to separate two entities in `list`.
   *
   * - Returns: A new `String` combining the elements of `list` with `separator`.
   */
  @objc static func joinStrings(_ list: YailList<SCMValueProtocol>, _ separator: NSString) -> String {
    let buffer = NSMutableString()
    var sep = NSString()
    for item in list {
      if item is SCMSymbol {
        continue
      }
      buffer.append(sep as String)
      buffer.append(toString(item))
      sep = separator
    }
    return buffer.copy() as! String
  }

  /**
   * Reverses a `String`. Called from runtime.scm to reverse a UTF-8 string in a Unicode-aware way.
   *
   * - Parameter str: A `String` to reverse
   *
   * - Returns: A new `String` with the Unicode codepoints reversed.
   */
  @objc static func reverseString(_ string: String) -> String {
    return String(string.reversed())
  }
}
