// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation

public enum LineType: Int32, CaseIterable {
  case Linear = 0
  case Curved = 1
  case Stepped = 2
  
  static var LOOKUP : [Int32 : LineType] {
    var table: [Int32 : LineType] = [:]
    for val in LineType.allCases {
      table[val.rawValue] = val
    }
    return table
  }
}

