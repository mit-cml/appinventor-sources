// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

class ScatterChartDataModel: PointChartDataModel {
  var chartDataEntry: Array<ChartDataEntry> = []

  init(data: DGCharts.ScatterChartData, view: ScatterChartView) {
    super.init(data: data, view: view)
    let dataset = ScatterChartDataSet(entries: chartDataEntry, label: " ")
    self.dataset = dataset
    self.data.dataSets.append(dataset)
    setDefaultStylingProperties()
  }

  init(data: DGCharts.ScatterChartData, view: ScatterChartView, dataset: DGCharts.ChartDataSet) {
    super.init(data: data, view: view)
    self.dataset = dataset
    self.data.dataSets.append(dataset)
    setDefaultStylingProperties()
  }

  public override func addEntryFromTuple(_ tuple: YailList<AnyObject>) {
    // Assuming getEntryFromTuple is a function that returns an optional ChartDataEntry.
    // The tuple parameter type adjusted for Swift.
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

  public override func setDefaultStylingProperties() {
    guard let scatterDataSet = dataset as? DGCharts.ScatterChartDataSet else {
      return
    }
    scatterDataSet.setScatterShape(.circle)
  }

  public func setPointShape(_ shape: PointStyle) {
    guard let scatterDataSet = dataset as? DGCharts.ScatterChartDataSet else {
      return
    }
    switch shape {
    case PointStyle.Circle:
      scatterDataSet.setScatterShape(.circle)
      break
    case PointStyle.Square:
      scatterDataSet.setScatterShape(.square)
      break
    case PointStyle.Triangle:
      scatterDataSet.setScatterShape(.triangle)
      break
    case PointStyle.Cross:
      scatterDataSet.setScatterShape(.cross)
      break
    case PointStyle.X:
      scatterDataSet.setScatterShape(.x)
      break
    default:
      break
    }
  }

}

