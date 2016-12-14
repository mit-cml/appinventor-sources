//
//  JsonUtil.swift
//  AIComponentKit
//
//  Created by Evan Patton on 12/13/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

// let numberRegex = NSRegularExpression(pattern: "-?[1-9]?[0-9]+(\\.[0-9]+)?([eE][+-]?[0-9]+)?")

func getJsonRepresentation(_ object: AnyObject?) throws -> String {
  if object == nil {
    return "null"
  }
  let object = [object]
  let repr = try JSONSerialization.data(withJSONObject: object)
  let trimmed = repr.subdata(in: repr.startIndex.advanced(by: 1)..<repr.endIndex.advanced(by: -1))
  return String(data: trimmed, encoding: .utf8)!
}

func getObjectFromJson(_ json: String?) throws -> AnyObject? {
  if json == nil || json == "" {
    return "" as NSString
  }
  // NSJSONSerialization can only parse arrays and objects at the top level, so we wrap value here
  let json = "[\(json!)]"
  let result = try JSONSerialization.jsonObject(with: json.data(using: .utf8)!,
                                                options: JSONSerialization.ReadingOptions.mutableContainers)
  if result is NSArray {
    return (result as! Array<AnyObject>)[0]
  }
  return nil
}
