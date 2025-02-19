// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

open class BarChartDataModel: Chart2DDataModel {
  private var chartDataEntry: Array<BarChartDataEntry> = []
  private var legendEntries = [LegendEntry]()

  init(data: DGCharts.BarChartData, view: BarChartView) {
    super.init(data: data, view: view)

    let dataset = BarChartDataSet(entries: chartDataEntry, label: " ")
    self.dataset = dataset
    self.data.dataSets = [dataset]
    setDefaultStylingProperties()
  }

  public override func addEntryFromTuple(_ tuple: YailList<AnyObject>) {
    //let entry = getEntryFromTuple(tuple)

    guard let entry = getEntryFromTuple(tuple) else {
      // Not a valid entry
      return
    }

    let x = Int(entry.x)

    // Check for negative x value, which is invalid
    guard x >= 0 else {
      return
    }

    // If x is within the bounds of the entries array, replace the entry at that index
    if x < entries.count {
      _entries.insert(entry, at: x)
    } else {
      // If x is beyond the current range of entries, fill the gap with placeholder entries
      while entries.count < x {
        _entries.append(BarChartDataEntry(x: Double(entries.count), y: 0.0))
      }
      // Add the new entry at the end
      _entries.append(entry)
    }
  }


  public override func getEntryFromTuple(_ tuple: YailList<AnyObject>) -> ChartDataEntry? {
    guard tuple.count >= 3 else {
      // Handle error for insufficient chart entry values or type mismatch
      return nil
    }

    let rawX = "\(tuple[1])"
    let rawY = "\(tuple[2])"

    if let x = Double(rawX), let y = Double(rawY) {
      // Floor the x value and convert to Int as the Bar Chart uses x entries as an index
      let flooredX = Int(floor(x))
      return BarChartDataEntry(x: Double(flooredX), y: y)
    } else {
      // Handle number format exception
      return nil
    }
  }

  public override func areEntriesEqual(_ e1: AnyObject, _ e2: AnyObject) -> Bool {
    guard let barEntry1 = e1 as? BarChartDataEntry,
          let barEntry2 = e2 as? BarChartDataEntry else {
      return false
    }

    // Floor the x values to compare without the decimal part,
    // and directly compare the y values.
    return floor(barEntry1.x) == floor(barEntry2.x) && barEntry1.y == barEntry2.y
  }


  public func getTupleFromEntry(_ entry: DGCharts.BarChartDataEntry) -> YailList<AnyObject> {
    let tupleEntries: Array<Float> = [Float(floor(entry.x)), Float(entry.y)]
    return tupleEntries as! YailList<AnyObject>
  }

  public override func setDefaultStylingProperties() {
    if let dataset = dataset as? DGCharts.BarChartDataSet {
      dataset.drawValuesEnabled = true
    }
  }
}
