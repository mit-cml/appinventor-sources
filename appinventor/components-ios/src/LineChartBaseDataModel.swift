// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

class LineChartBaseDataModel: PointChartDataModel {
  init(data: DGCharts.LineChartData, view: LineChartViewBase) {
    super.init(data: data, view: view)
    let dataset = LineChartDataSet(entries: chartEntries, label: " ")
    self.dataset = dataset
    self.data.dataSets.append(dataset)
    setDefaultStylingProperties()
  }

  init(data: DGCharts.LineChartData, view: LineChartViewBase, dataset: DGCharts.ChartDataSet) {
    super.init(data: data, view: view)
    self.dataset = dataset
    self.data.dataSets.append(dataset)
    setDefaultStylingProperties()
  }

  public override func addEntryFromTuple(_ tuple: YailList<AnyObject>) {
    guard let entry = getEntryFromTuple(tuple) else {
      // Not a valid entry
      return
    }
    // Assuming a correctly implemented binarySearch function that returns the index
    // where the entry should be inserted or the negative index - 1 if not found.
    var index = binarySearch(entry, chartEntries)
    if index < 0 {
      index = -(index + 1)
    } else {
      let entryCount = entries.count
      while index < entryCount && entries[index].x == entry.x  {
        index += 1
      }
    }

    // Insert the entry into the entries array.
    _entries.insert(entry, at: index)

    // Assuming you're updating some dataset that needs to be replaced entirely.
    // Performing UI updates asynchronously on the main thread.
    DispatchQueue.main.async {
      self.dataset?.replaceEntries(self.chartEntries)
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

//  @objc open var `LineType`: Line {
//    get {
//      return _type
//    }
//    set {
//      _type = newValue
//      setLineType(_type)
//    }
//  }

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
      default:
        break
      }
    } else {
      return
    }
  }
}
