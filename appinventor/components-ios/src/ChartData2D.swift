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
    refreshChart()
  }

  @objc func RemoveEntry(_ x: String, _ y: String){
    // create a 2-tuple, and remove the tuple from the Data series
    let pair: YailList<AnyObject> = [x, y]
    chartDataModel?.removeEntryFromTuple(pair)
    // refresh chart with new data
    refreshChart()
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
    let points = dataPoints as Array<AnyObject>

    guard !points.isEmpty, let entries = chartDataModel?.entries, let lineDataSet = chartDataModel?.dataset as? LineChartDataSet else {
      return
    }

    var anomalyMap: [Double:AnomalyManager] = [:]
    for (i, entry) in entries.enumerated() {
      guard let entry = entry as? DGCharts.ChartDataEntry else {
        continue
      }
      let y = entry.y
      var manager: AnomalyManager! = anomalyMap[y]
      if manager == nil {
        manager = AnomalyManager()
        anomalyMap[y] = manager
      }
      manager.xValues.insert(entry.x)
      manager.indexes.insert(i)
    }

    var highlights = [Int32](repeating: _color, count: entries.count)
    for point in points {
      guard let point = point as? Array<AnyObject>,
            point.count >= 3 else {
        continue
      }
      guard let y = point[2] as? Double else {
        continue
      }
      guard let anomalyManager = anomalyMap[y] else {
        continue
      }
      guard let x = point[1] as? Double else {
        continue
      }
      if anomalyManager.xValues.contains(x) || anomalyManager.indexes.contains(Int(x) - 1) {
        for index in anomalyManager.indexes {
          highlights[index] = color
        }
      }
    }

    lineDataSet.circleColors = highlights.map(argbToColor(_:))
    onDataChange()
  }

  private class AnomalyManager {
    var indexes = Set<Int>()
    var xValues = Set<Double>()
  }
}
