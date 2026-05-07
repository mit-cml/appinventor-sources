// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

class LineChartDataModel: LineChartBaseDataModel {
  public init(data: DGCharts.LineChartData, view: LineChartView) {
    super.init(data: data, view: view)
  }

  public override init(data: DGCharts.LineChartData, view: LineChartViewBase, dataset: DGCharts.ChartDataSet) {
    super.init(data: data, view: view, dataset: dataset)
  }

  override func highlightPoints(_ points: [AnyObject], _ color: Int32) -> Bool {
    highlightColor = argbToColor(color)
    guard let lineDataSet = dataset as? LineChartDataSet else {
      return false  // We can only highlight points on line charts
    }
    var highlights = [Int32](repeating: self.color, count: entries.count)
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
    var changed = false
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
          changed = true
        }
      }
    }
    self.highlights = highlights.map(argbToColor(_:))
    lineDataSet.circleColors = self.highlights
    return changed
  }

  private class AnomalyManager {
    var indexes = Set<Int>()
    var xValues = Set<Double>()
  }
}

