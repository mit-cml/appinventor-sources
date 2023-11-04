// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

open class Chart2DDataModel: ChartDataModel<DGCharts.ChartDataEntry, DGCharts.ChartData, DGCharts.ChartViewBase> {
  init(data: DGCharts.ChartData, view: DGCharts.ChartViewBase) {
    super.init(data: data, dataset: dataset, view: view)
  }
  
  public func getTupleSize() -> Int {
    return 2
  }
  
  public func getTupleFromEntry(entry: ChartDataEntry) -> YailList<AnyObject> {
    // Create a list with the X and Y values of the entry, and convert the generic List to a YailList
    var tupleEntries: Array<Double> = [entry.x, entry.y]
    return tupleEntries as! YailList<AnyObject>
  }
}
