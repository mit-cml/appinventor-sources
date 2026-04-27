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

  // MARK: - DataFile Integration

  @objc open func ImportFromDataFile(_ dataFile: DataFile, _ xValueColumn: String, _ yValueColumn: String) {
    NSLog("ChartData2D: Attempting to import X: '\(xValueColumn)', Y: '\(yValueColumn)'")

    let xColumn = dataFile.getColumn(xValueColumn)
    let yColumn = dataFile.getColumn(yValueColumn)

    var xSwiftArray: [AnyObject] = []
    for item in xColumn { xSwiftArray.append(item as AnyObject) }

    var ySwiftArray: [AnyObject] = []
    for item in yColumn { ySwiftArray.append(item as AnyObject) }

    let count = min(xSwiftArray.count, ySwiftArray.count)
    if count <= 1 {
      NSLog("ChartData2D ERROR: Not enough data rows to plot.")
      return
    }

    func attemptPlotting(retriesLeft: Int) {
      if self.chartDataModel == nil {
        if retriesLeft > 0 {
          NSLog("ChartData2D: Chart canvas not ready yet. Waiting 0.1s... (\(retriesLeft) tries left)")
          DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            attemptPlotting(retriesLeft: retriesLeft - 1)
          }
          return
        } else {
          NSLog("ChartData2D FATAL ERROR: Chart failed to initialize after 1 second.")
          return
        }
      }

      for i in 1..<count {
        let cleanX = "\(xSwiftArray[i])".trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanY = "\(ySwiftArray[i])".trimmingCharacters(in: .whitespacesAndNewlines)

        if cleanX.isEmpty && cleanY.isEmpty { continue }

        let pair: YailList<AnyObject> = [cleanX as AnyObject, cleanY as AnyObject]
        self.chartDataModel?.addEntryFromTuple(pair)
      }

      NSLog("ChartData2D: Import complete. Triggering chart redraw.")
      self.onDataChange()
    }

    DispatchQueue.main.async {
      attemptPlotting(retriesLeft: 10)
    }
  }
}
