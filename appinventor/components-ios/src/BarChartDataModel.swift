// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

open class BarChartDataModel: Chart2DDataModel {
  init(data: DGCharts.ChartData, view: PointChartView) {
    super.init(data: data, view: view)
  }
  public override func getTupleFromEntry(_ entry: ChartDataEntry) -> YailList<AnyObject> {
    var tupleEntries: Array<Float> = [Float(floor(entry.x)), entry.y]
    print("tupleEntries", tupleEntries)
    return tupleEntries as! YailList<AnyObject>
  }
  
}
