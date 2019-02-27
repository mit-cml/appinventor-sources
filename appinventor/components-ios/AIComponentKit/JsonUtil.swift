// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.
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

func getPublicObjectFromJson(_ json: String?) throws -> AnyObject {
  let json = try getObjectFromJson(json)
  return convertJsonItem(json)
}

fileprivate func getListFromJsonObject(_ json: NSDictionary) -> [[AnyObject]] {
  var returnList = [[AnyObject]]()
  for (key, value) in json {
    var nestedArray = [AnyObject]()
    nestedArray.append(key as AnyObject)
    nestedArray.append(convertJsonItem(value as AnyObject))
    returnList.append(nestedArray)
  }
  return returnList
}

fileprivate func getListFromJsonArray(_ json: NSArray) -> [AnyObject] {
  var returnList = [AnyObject]()
  for item in json {
    returnList.append(convertJsonItem(item as AnyObject))
  }
  return returnList
}

fileprivate func convertJsonItem(_ item: AnyObject?) -> AnyObject {
  if item == nil {
    return "null" as AnyObject
  }
  if let jsonObject = item as? NSDictionary {
    return getListFromJsonObject(jsonObject) as AnyObject
  }
  if let jsonArray = item as? NSArray {
    return getListFromJsonArray(jsonArray) as AnyObject
  }
  if let bool = item as? Bool {
    return bool as AnyObject
  }
  if let string = item as? String {
    if string == "false" || string == "true" {
      return (string == "false") as AnyObject
    } else {
      return string as AnyObject
    }
  }
  if let number = item as? NSNumber {
    return number as AnyObject
  }
  return item.debugDescription as AnyObject
}
