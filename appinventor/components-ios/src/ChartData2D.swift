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
      let pair: YailList<AnyObject> = [x, y]
      self._chartDataModel?.addEntryFromTuple(pair)
      // refresh chart with new data
      self.refreshChart()
    }
  }
  
  @objc func RemoveEntry(_ x: String, _ y: String){
    // create a 2-tuple, and remove the tuple from the Data series
    DispatchQueue.main.async {
      let pair: YailList<AnyObject> = [x, y]
      self._chartDataModel?.removeEntryFromTuple(pair)
      // refresh chart with new data
      self.refreshChart()
    }
  }
  
  // checks whether an (x, y) entry exists in the Coordinate Data. Returns true if the Entry exists, and false otherwise.
  // TODO: how do i return something in DispatchQueue main
  @objc func DoesEntryExist(_ x: String, _ y: String) -> Bool{
    DispatchQueue.main.async {
      func call() -> Bool {
        var pair: YailList<AnyObject> = [x, y]
        return self._chartDataModel!.doesEntryExist(pair) // TODO: is the ! okay?
      }
    }
    return false
  }
  
}
