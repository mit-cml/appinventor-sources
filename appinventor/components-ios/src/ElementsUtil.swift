// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
  if itemString.count > 0 {
    return itemString.split(" *, *")
  }
  return items
}
