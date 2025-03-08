// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public func determineMaximumListSize(matrix: [AnyObject]) -> Int {
  return matrix.map {
    guard let row = $0 as? [AnyObject] else {
      return 0
    }
    if row.count > 0 && row[0] is SCMSymbol {
      return row.count - 1
    }
    return row.count
  }.max() ?? 0
}

public func getTranspose(matrix: [AnyObject]) -> NSMutableArray {
  let w = determineMaximumListSize(matrix: matrix)
  let result = NSMutableArray()
  guard w > 0 else {
    result.add(NSMutableArray())  // base case
    return result
  }
  for _ in 0..<w {
    result.add(NSMutableArray())
  }
  for i in 0..<matrix.count {
    guard let row = matrix[i] as? [AnyObject] else {
      continue
    }
    let start: Int
    if row.count > 0 && row[0] is SCMSymbol {
      start = 1
    } else {
      start = 0
    }
    for j in stride(from: start, to: row.count, by: 1) {
      (result[j - start] as! NSMutableArray).add(row[j])
    }
    if row.count < w {
      for j in row.count..<w {
        (result[j] as! NSMutableArray).add("")
      }
    }
  }
  return result
}
