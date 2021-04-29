// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

// let numberRegex = NSRegularExpression(pattern: "-?[1-9]?[0-9]+(\\.[0-9]+)?([eE][+-]?[0-9]+)?")
func getJsonRepresentation(_ object: AnyObject?) throws -> String {
  guard let object = object else {
    return "null"
  }
  let wrappedObject = [object]
  guard JSONSerialization.isValidJSONObject(wrappedObject) else {
    throw YailRuntimeError("Unable to serialize message", "JSONError")
  }
  let repr = try JSONSerialization.data(withJSONObject: wrappedObject)
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
  if let array = result as? Array<AnyObject> {
    return array[0]
  }
  return nil
}

/**
 * Parses the JSON content given in `json` into YAIL objects. The `useDicts` parameter controls
 * whether JSON objects are converted to `YailDictionary` (`true`) or associative `YailList`
 * (`false`).
 *
 * - Parameter json: The serialized JSON to parse
 * - Parameter useDicts: `true` if `YailDictionary` should be used for objects, otherwise `false`
 * - Returns: The content of `json` parsed into objects
 * - Throws: If `json` does not represent valid JSON, then an error will be thrown.
 */
func getYailObjectFromJson(_ json: String?, _ useDicts: Bool) throws -> AnyObject {
  let json = try getObjectFromJson(json)
  return convertJsonItem(json, useDicts)
}


fileprivate func getListFromJsonObject(_ json: NSDictionary) -> [[AnyObject]] {
  var returnList = [[AnyObject]]()
  for (key, value) in json {
    var nestedArray = [AnyObject]()
    nestedArray.append(key as AnyObject)
    nestedArray.append(convertJsonItem(value as AnyObject, false))
    returnList.append(nestedArray)
  }
  return returnList
}

fileprivate func getDictFromJsonObject(_ json: NSDictionary) -> [String:AnyObject] {
  var returnDict = [String:AnyObject]()
  for (key, value) in json {
    returnDict[key as! String] = convertJsonItem(value as AnyObject, true)
  }
  return returnDict
}

fileprivate func getListFromJsonArray(_ json: NSArray, _ useDicts: Bool) -> [AnyObject] {
  var returnList = [AnyObject]()
  for item in json {
    returnList.append(convertJsonItem(item as AnyObject, useDicts))
  }
  return returnList
}

fileprivate func convertJsonItem(_ item: AnyObject?, _ useDicts: Bool) -> AnyObject {
  if item == nil {
    return "null" as AnyObject
  }
  if let jsonObject = item as? NSDictionary {
    if useDicts {
      return getDictFromJsonObject(jsonObject) as AnyObject
    } else {
      return getListFromJsonObject(jsonObject) as AnyObject
    }
  }
  if let jsonArray = item as? NSArray {
    return getListFromJsonArray(jsonArray, useDicts) as AnyObject
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

func getStringListFromJsonArray(_ jsonArray: AnyObject?) -> [String] {
  if let array = jsonArray as? Array<String> {
    return array
  } else {
    return [String]()
  }
}

func writeFile(_ base64: String, _ fileExt: String) throws -> String {
  let randpart = arc4random()
  let resolvedPath = AssetManager.shared.pathForPrivateAsset("BinFile\(randpart)\(fileExt)")
  if let data = Data(base64Encoded: base64, options: .ignoreUnknownCharacters) {
    try data.write(to: URL(fileURLWithPath: resolvedPath))
  }
  return resolvedPath
}

func getJsonRepresentationIfValueFileName(_ value: AnyObject) -> String? {
  do {
    var valueList = [String]()
    if let valueString = value as? String {
      let valueJsonList = try getObjectFromJson(valueString)
      valueList = getStringListFromJsonArray(valueJsonList)
    } else if let valueArr = value as? Array<String> {
      valueList = valueArr
    } else {
      return nil
    }
    if valueList.count == 2 {
      if valueList[0].starts(with: ".") {
        let outputPath = try writeFile(valueList[1], valueList[0])
        return try getJsonRepresentation(outputPath as AnyObject)
      }
    }
  } catch {
    // Nothing to do here
  }
  return nil
}
