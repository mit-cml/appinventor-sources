// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

@objc open class ChartData2D: ChartDataBase {

  override init(_ chartContainer: Chart) {
    super.init(chartContainer)
    dataFileColumns = [" ", " "]
    sheetColumns = [" ", " "]
    webColumns = [" ", " "]
  }

  @objc func AddEntry(_ x: String, _ y: String) {
    // create a 2-tuple, and add the tuple to the Data series
    let pair: YailList<AnyObject> = [x, y]
    chartDataModel?.addEntryFromTuple(pair)
    // refresh chart with new data
    onDataChange()
  }

  @objc func RemoveEntry(_ x: String, _ y: String){
    // create a 2-tuple, and remove the tuple from the Data series
    let pair: YailList<AnyObject> = [x, y]
    chartDataModel?.removeEntryFromTuple(pair)
    // refresh chart with new data
    onDataChange()
  }

  // checks whether an (x, y) entry exists in the Coordinate Data. Returns true if the Entry exists, and false otherwise.
  @objc func DoesEntryExist(_ x: String, _ y: String) -> Bool{
    /* Original:
     DispatchQueue.main.sync {
      var pair: YailList<AnyObject> = [x, y]
      return self._chartDataModel!.doesEntryExist(pair) // TODO: is the ! okay?
    }*/

    let group = DispatchGroup()
    group.enter()
    var holder: Bool = false // holder variable for returned value of doesEntryExist()
    // avoid deadlocks by not using .main queue here
    DispatchQueue.global(qos: .default).async {
      var pair: YailList<AnyObject> = [x, y]
      holder =  self.chartDataModel!.doesEntryExist(pair)
      group.leave()
    }
    group.wait()
    return holder
  }

  // Highlights data points of choice on the Chart in the color of choice. This block expects a list of data points, each data pointis an index, value pair
  @objc func HighlightDataPoints(_ dataPoints: YailList<AnyObject>, _ color: Int32) {
    if chartDataModel?.highlightPoints(dataPoints as [AnyObject], color) == true {
      onDataChange()
    }
  }
}
