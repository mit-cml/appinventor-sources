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
    
    /// Splits text into parts based on a separator
    public static func split(_ text: String, at: String) -> [String] {
        var parts = text.components(separatedBy: at)
        if at.isEmpty && parts.first == "" {
            parts.removeFirst()
        }
        return parts
    }

    
    // MARK: - Mapping Methods
    
    /// Replaces text using dictionary order
    @objc public static func replaceAllMappingsDictionary(
        _ text: String,
        _ mappings: YailDictionary
    ) -> String {
        return replaceAllMappings(text, mappings: mappings, order: mappingOrderDictionary)
    }
    
    /// Replaces text using longest string first order
    @objc public static func replaceAllMappingsLongestString(
        _ text: String,
        _ mappings: YailDictionary
    ) -> String {
        return replaceAllMappings(text, mappings: mappings, order: mappingOrderLongestStringFirst)
    }
    
    /// Replaces text using earliest occurrence order
    @objc public static func replaceAllMappingsEarliestOccurrence(
        _ text: String,
        _ mappings: YailDictionary
    ) -> String {
        return replaceAllMappings(text, mappings: mappings, order: mappingOrderEarliestOccurrence)
    }
    
    /// Main replacement method with custom ordering
    public static func replaceAllMappings(
        _ text: String,
        mappings: YailDictionary,
        order: MappingOrder
    ) -> String {
        if DEBUG {
            print("\(LOG_TAG_JOIN_STRINGS): inside replace mappings")
        }
        
        var stringMappings: [String: String] = [:]
        var keys: [String] = []
        
        // Convert all mappings to strings
        for (key, value) in mappings {
            let keyString = String(describing: key)
            let valueString = String(describing: value)
            
            if !stringMappings.keys.contains(keyString) {
                keys.append(keyString)
            }
            
            stringMappings[keyString] = valueString
        }
        
        // Apply ordering
        order.changeOrder(&keys, text: text)
        
        // Apply mappings
        return applyMappings(text, mappings: stringMappings, keys: keys)
    }
    
    /// Applies the mappings to the text using the specified key order
    private static func applyMappings(
        _ text: String,
        mappings: [String: String],
        keys: [String]
    ) -> String {
        var ranges: [Range] = []
        
        // Find all ranges that need to be replaced
        for key in keys {
            let pattern = NSRegularExpression.escapedPattern(for: key)
            guard let regex = try? NSRegularExpression(pattern: pattern) else { continue }
            
            let nsRange = NSRange(text.startIndex..<text.endIndex, in: text)
            let matches = regex.matches(in: text, range: nsRange)
            
            let replacement = mappings[key] ?? ""
            
            for match in matches {
                let range = Range(
                    start: match.range.location,
                    end: match.range.location + match.range.length,
                    text: replacement
                )
                
                // Only add non-overlapping ranges
                if !ranges.contains(where: { existing in
                    compareRanges(existing, range) == 0
                }) {
                    ranges.append(range)
                }
            }
        }
        
        // Sort ranges by end index (descending)
        ranges.sort { r1, r2 in
            compareRanges(r1, r2) < 0
        }
        
        // Apply replacements
        var result = text
        for range in ranges {
            let startIndex = result.index(result.startIndex, offsetBy: range.start)
            let endIndex = result.index(result.startIndex, offsetBy: range.end)
            result.replaceSubrange(startIndex..<endIndex, with: range.text)
        }
        
        return result
    }
}
