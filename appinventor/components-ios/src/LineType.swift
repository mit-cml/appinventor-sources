// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation

public enum LineType: Int32, CaseIterable {
  case Linear = 0
  case Curved = 1
  case Stepped = 2
  
  var LOOKUP : [Int32 : LineType] {
    for val in LineType.allCases {
      LOOKUP[val.rawValue] = val
      return LOOKUP
    }
  }
  /*
  var LOOKUP: [Int32 : LineType] {
    get {
      return LOOKUP
    }
    set {
      for val in LineType.allCases {
        LOOKUP[val.rawValue] = val
      
      }
    }
  }*/
}

