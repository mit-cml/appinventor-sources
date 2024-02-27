// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

open class BarChartDataModel: Chart2DDataModel {
  var chartDataEntry: Array<BarChartDataEntry> = []

  init(data: DGCharts.BarChartData, view: BarChartView) {
    super.init(data: data, view: view)
    let dataset = BarChartDataSet(entries: chartDataEntry, label: " ")
    self.dataset = dataset
    self.data.dataSets = [dataset]
    setDefaultStylingProperties()
  }

  public override func getEntryFromTuple(_ tuple: YailList<AnyObject>) -> BarChartDataEntry {
    print(tuple)
    guard tuple.count >= 2,
          let rawX = tuple[0+1] as? String,
          let rawY = tuple[1+1] as? String else {
      // Handle error for insufficient chart entry values or type mismatch
      // TODO: we might want to give a warning instead
      fatalError("Error: Insufficient chart entry values or type mismatch")
    }

    if let x = Float(rawX), let y = Float(rawY) {
      // Floor the x value and convert to Int as the Bar Chart uses x entries as an index
      let flooredX = Int(floor(x))
      return BarChartDataEntry(x: Double(flooredX), y: Double(y))
    } else {
      // Handle number format exception
      // TODO: we might want to give a warning instead
      fatalError("Error: Invalid chart entry values for \(rawX), \(rawY)")
    }
  }

  public override func addEntryFromTuple(_ tuple: YailList<AnyObject>) {
    let entry = getEntryFromTuple(tuple)

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

    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
      self.dataset?.replaceEntries(self._entries)
    }
    
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
