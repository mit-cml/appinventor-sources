// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

class LineChartBaseDataModel: PointChartDataModel {
  var chartDataEntry: Array<ChartDataEntry> = []
  init(data: DGCharts.LineChartData, view: LineChartView) {
    super.init(data: data, view: view)
    let dataset = LineChartDataSet(entries: chartDataEntry, label: " ")
    self.dataset = dataset
    self.data.dataSets = [dataset]
    setDefaultStylingProperties()
  }
  
  public override func addEntryFromTuple(_ tuple: YailList<AnyObject>) {
    let entry: ChartDataEntry = getEntryFromTuple(tuple)
    if entry != nil { // TODO: how to compare it to nil
      // TODO: DO I NEED TO DO THE BINARY SEARCH
      // var index: Int = entries.firstIndex(of: entry)!
      var index: Int = binarySearch(entry, entries) // I made this, must check
      if index < 0 {
        index = -index - 1
      } else {
        let entryCount: Int = entries.count
        
        while index < entryCount && entries[index].x == entry.x {
          index += 1
        }
      }
      _entries.insert(entry, at: index)
      DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
        self.dataset?.replaceEntries(self._entries)
      }
    }
  }
  
  public override func setColor(_ argb: UIColor) {
    super.setColor(argb)
    if let dataset = dataset as? DGCharts.LineChartDataSet {
      dataset.setCircleColor(argb) // also update the circle color
    }
  }
  
  public override func setColors(_ colors: [NSUIColor]) {
    super.setColors(colors)
    if let dataset = dataset as? DGCharts.LineChartDataSet {
      dataset.circleColors = colors
      // is this fine because set doesn't work
    }
  }
  
  public override func setDefaultStylingProperties() {
    if let dataset = dataset as? DGCharts.LineChartDataSet {
      dataset.drawValuesEnabled = true
      dataset.drawCircleHoleEnabled = true // also update the circle color
    }
  }
  
  public func setLineType(_ type: LineType) {
    if let dataset = dataset as? DGCharts.LineChartDataSet {
      dataset.drawCircleHoleEnabled = true// also update the circle color
      switch type {
      case LineType.Linear:
        dataset.mode = LineChartDataSet.Mode.linear
        break

      case LineType.Curved:
        dataset.mode = LineChartDataSet.Mode.cubicBezier
        break

      case LineType.Stepped:
        dataset.mode = LineChartDataSet.Mode.stepped
        break
      }
    } else {
      return
    }
  }
}

