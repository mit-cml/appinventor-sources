// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

open class Chart2DDataModel: ChartDataModel {
  override init(data: DGCharts.ChartData, view: ChartView) {
    super.init(data: data, view: view)
  }

  public override func getTupleSize() -> Int {
    return 2
  }

  public override func getTupleFromEntry(_ entry: DGCharts.ChartDataEntry) -> YailList<AnyObject> {
    // Create a list with the X and Y values of the entry, and convert the generic List to a YailList
    let tupleEntries: Array<Double> = [entry.x, entry.y]
    let tupleEntriesYail: YailList<AnyObject> = []
    for element in tupleEntries {
      tupleEntriesYail.add(element)
    }
    return tupleEntriesYail
  }
}
