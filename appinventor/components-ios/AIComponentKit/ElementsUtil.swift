//
//  ElementsUtil.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/29/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

extension String {
  func split(_ splitter: String) -> [String] {
    guard let regEx = try? NSRegularExpression(pattern: splitter, options: [])
    else {
      return []
    }
    let nsString = self as NSString
    let stop = "\u{001E}"
    let modifiedString = regEx.stringByReplacingMatches(in: self, options: [],
                                                        range: NSMakeRange(0, nsString.length),
                                                        withTemplate:stop)
    return modifiedString.components(separatedBy: stop)
  }
}

public func elementsFromString(_ itemString: String) -> [String] {
  let items = [String]()
  if itemString.characters.count > 0 {
    return itemString.split(" *, *")
  }
  return items
}
