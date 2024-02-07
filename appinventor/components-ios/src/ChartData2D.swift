// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@objc class ChartData2D: ChartDataBase {
  
  override init(_ chartContainer: Chart) {
    super.init(chartContainer)
    chartContainer.addDataComponent(self)
    dataFileColumns = [" ", " "]
    sheetColumns = [" ", " "]
    webColumns = [" ", " "]
  }
  @objc func AddEntry(_ x: String, _ y: String){
    // create a 2-tuple, and add the tuple to the Data series
    DispatchQueue.main.async {
      var pair: YailList<AnyObject> = [x, y]
      self._chartDataModel?.addEntryFromTuple(pair)
      // refresh chart with new data
      self.refreshChart()
    }
  }
}
