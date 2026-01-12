// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

// performs a binary search
public func binarySearch(_ entry: DGCharts.ChartDataEntry, _ entries: [DGCharts.ChartDataEntry]) -> (Int) {
  // creates entriesCopy because entries is not mutable
  var entriesCopy = entries
  let index = entriesCopy.partition(by: { $0.x >= entry.x} )
  let found = index != entriesCopy.count && entriesCopy[index] == entry
  return (index)
}
