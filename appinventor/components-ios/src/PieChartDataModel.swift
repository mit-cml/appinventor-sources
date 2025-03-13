// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

class PieChartDataModel: Chart2DDataModel {

  private var legendEntries = [LegendEntry]()

  init(data: DGCharts.ChartData, view: PieChartView, chart: DGCharts.PieChartView) {
    super.init(data: data, view: view)

    setColor(UIColor(red: 140.0 / 255.0, green: 234.0 / 255.0, blue: 1.0, alpha: 1.0))

    dataset = PieChartDataSet(entries: [ChartDataEntry](), label: "")
    data.dataSets.append(dataset!)
    chart.data = data
    setDefaultStylingProperties()
  }

  public override func addEntryFromTuple(_ tuple: YailList<AnyObject>) {
    guard let entry = getEntryFromTuple(tuple) else {
      // Not a valid entry
      return
    }
    guard let label = tuple[1] as? String else {
      // Not a valid entry
      return
    }
    guard let colors = dataset?.colors else {
      return
    }
    _entries.append(entry)
    let legendEntry = LegendEntry(label: label)
    let entriesCount = _entries.count
    let index = (entriesCount - 1) % colors.count
    legendEntry.formColor = colors[index]
    legendEntries.append(legendEntry)
    (view as? PieChartView)?.addLegendEntry(legendEntry)
  }

  override func removeEntryFromTuple(_ tuple: YailList<AnyObject>) {
    guard let entry = getEntryFromTuple(tuple) as? PieChartDataEntry else {
      // Not a valid entry
      return
    }

    guard let label = tuple[1] as? String else {
      return
    }

    guard let legendEntry = legendEntries.first(where: { $0.label == label }) else {
      return
    }

    _entries.removeAll { ($0 as! PieChartDataEntry).label == entry.label && ($0 as! PieChartDataEntry).value == entry.value }
    legendEntries.removeAll { $0.label == label }
    (view as? PieChartView)?.removeLegendEntry(legendEntry)

  }

  public override func clearEntries() {
    super.clearEntries()

    (view as? PieChartView)?.removeLegendEntries(legendEntries)
    legendEntries.removeAll()
  }

  public override func getEntryFromTuple(_ tuple: YailList<AnyObject>) -> ChartDataEntry? {
    guard tuple.count >= 2 else {
      view.form.dispatchErrorOccurredEvent(view._chartComponent, "GetEntryFromTuple",
          ErrorMessage.ERROR_INSUFFICIENT_CHART_ENTRY_VALUES, 2 as NSNumber,
          tuple.count as NSNumber)
      return nil
    }
    let xValue = tuple[1] as? String
    let yValue = tuple[2]
    if let str = yValue as? String {
      if let y = Double(str) {
        return PieChartDataEntry(value: y, label: xValue)
      }
    } else if let num = yValue as? NSNumber {
      return PieChartDataEntry(value: num.doubleValue, label: xValue)
    }
    // Error condition: Expected y to be representable as Double
    view.form.dispatchErrorOccurredEvent(view._chartComponent, "GetEntryFromTuple",
        ErrorMessage.ERROR_INVALID_CHART_ENTRY_VALUES, xValue as AnyObject, yValue as AnyObject)
    return nil
  }

  public override func getTupleFromEntry(_ entry: ChartDataEntry) -> YailList<AnyObject> {
    guard let pieEntry = entry as? PieChartDataEntry else {
      return YailList()
    }

    return YailList(array: [pieEntry.label as? NSString ?? "", pieEntry.y as NSNumber])
  }

  override func setDefaultStylingProperties() {
    super.setDefaultStylingProperties()
    if let dataset = dataset as? PieChartDataSet {
      dataset.sliceSpace = 3
    }
  }

  public override func setColors(_ argb: [UIColor]) {
    super.setColors(argb)

    updateLegendColors()
  }

  public override func setColor(_ argb: UIColor) {
    setColors([argb])
  }

  override func areEntriesEqual(_ e1: AnyObject, _ e2: AnyObject) -> Bool {
    guard let p1 = e1 as? PieChartDataEntry, let p2 = e2 as? PieChartDataEntry else {
      return false
    }

    return p1.label == p2.label && p1.y == p2.y
  }

  // MARK: Private Implementation

  private func updateLegendColors() {
    guard let colors = dataset?.colors else {
      return
    }

    for i in 0..<legendEntries.count {
      let index = i % colors.count
      legendEntries[i].formColor = colors[index]
    }
  }
}

extension PieChartDataEntry {
  static func == (lhs: PieChartDataEntry, rhs: PieChartDataEntry) -> Bool {
    if lhs === rhs {
      return true
    }
    return lhs.label == rhs.label && lhs.value == rhs.value
  }
}
