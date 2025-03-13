// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

public enum EntryCriterion {
  case All
  case XValue
  case YValue
}

open class ChartDataModel: DataModel {
  let data: DGCharts.ChartData
  var dataset: DGCharts.ChartDataSet?
  let view: ChartView

  init(data: DGCharts.ChartData, view: ChartView) {
    self.data = data
    self.view = view
  }

  var tupleSize: Int {
    return 0
  }

  func tupleFromEntry(entry: DGCharts.ChartDataEntry) -> YailList<AnyObject> {
    return YailList()
  }

  func setColor(_ argb: UIColor) {
    dataset?.setColor(argb)
  }

  func setColors(_ argb: [UIColor]) {
    dataset?.setColors(argb.map({ color in
      return color
    }), alpha: 1.0)
  }

  func setLabel(_ text: String) {
    dataset?.label = text
  }
  
  func setDataLabelColor(_ argb: UIColor) {
    dataset?.valueTextColor = argb
  }

  func importFromList(_ list: [AnyObject]) {
    for entry in list {
      var tuple : YailList<AnyObject>?
      if let entry = entry as? YailList<AnyObject> {
        tuple = entry
      } else if let entry = entry as? Array<AnyObject> {
        tuple = YailList(array: entry)
      }

      if let tuple = tuple {
        addEntryFromTuple(tuple)
      }
    }
  }

  func removeValues(_ list: [AnyObject]) {
    for entry in list {
      var tuple: YailList<AnyObject>?
      if let entry = entry as? YailList<AnyObject> {
        tuple = entry
      } else if let entry = entry as? Array<AnyObject> {
        tuple = entry as? YailList<AnyObject>
      } else if let entry = entry as? SCMSymbol {
        continue
      }
      if tuple == nil {
        continue
      }
      // attempt to remove entry
      removeEntryFromTuple(tuple!)
    }
  }

  func importFromColumns(_ columns: NSArray, _ hasHeaders: Bool) {
    // bet a yaillist of tuples from the specified columns
    let tuples = getTuplesFromColumns(columns, hasHeaders)

    // use the generated tuple list in the importFromList method to import the data
    importFromList(tuples as [AnyObject])
  }

  func getTuplesFromColumns(_ columns: NSArray, _ hasHeaders: Bool) -> NSArray {
    var entries = columns.map({ ($0 as? Array<AnyObject>)?.count ?? 0 }).min() ?? 0
    var tuples: Array<YailList<AnyObject>> = []
    for i in stride(from: hasHeaders ? 1 : 0, to: entries, by: 1) {
      var tupleElements: Array<String> = []
      for j in stride(from: 0, to: columns.count, by: 1) {
        let value = columns[j]
        // invalid column specified; add default value (minus one to compensate for skipped value)
        if value is YailList<AnyObject> {
          tupleElements.append(getDefaultValue(i - 1))
          continue
        }
        guard let column = value as? NSArray else {
          continue
        }
        tupleElements.append(String(describing: column[i]))
      }
      tuples.append(YailList(array: tupleElements))
    }
    return tuples as NSArray
  }

  func removeEntryFromTuple(_ tuple: YailList<AnyObject>) {
    guard let entry = getEntryFromTuple(tuple) else {
      // Not a valid entry
      return
    }

    let index: Int32 = findEntryIndex(entry)
    removeEntry(Int(index))
  }

  func removeEntry(_ index: Int) {
    if index >= 0 {
      _entries.remove(at: index)
    }
  }

  func doesEntryExist(_ tuple: YailList<AnyObject>) -> Bool {
    guard let entry = getEntryFromTuple(tuple) else {
      // Not a valid entry
      return false
    }
    let index: Int32 = findEntryIndex(entry)
    return index >= 0
  }

  func findEntriesByCriterion(_ value: String, _ criterion: EntryCriterion) -> YailList<AnyObject> {
    var entries: Array<YailList<AnyObject>> = []
    let entriesYail: YailList<AnyObject> = []
    for entry in chartEntries {
      if isEntryCriterionSatisfied(entry, criterion, value: value) {
        entries.append(getTupleFromEntry(entry))
      }
    }
    for entry in entries {
      entriesYail.add(entry)
    }
    // return entries as! YailList<AnyObject>
    return entriesYail
  }

  func getEntriesAsTuples() -> YailList<AnyObject> {
    return findEntriesByCriterion("0", EntryCriterion.All)
  }

  func isEntryCriterionSatisfied(_ entry: DGCharts.ChartDataEntry, _ criterion: EntryCriterion, value: String) -> Bool {
    var criterionSatisfied: Bool = false
    switch criterion {
    case .All: // criterion satisfied no matter the value, sicne all entries should be returned
      criterionSatisfied = true
      break

    case .XValue:
      if let entry = entry as? PieChartDataEntry {
        let pieEntry : PieChartDataEntry = entry
        criterionSatisfied = pieEntry.label == value
      } else {
        let xValue: Float = Float(value)!
        var compareValue: Float = Float(entry.x)
        if entry is BarChartDataEntry {
          compareValue = Float(floor(compareValue))
        }
        criterionSatisfied = (compareValue == xValue)
      }
      break

    case .YValue:
      let yValue: Float = Float(value)!
      criterionSatisfied = (Float(entry.y) == yValue)
      break

    default:
      // should throw IllegalArgumentException
      print("Unknown criterion \(criterion)")
    }
    return criterionSatisfied
  }

  func getEntryFromTuple(_ tuple: YailList<AnyObject>) -> DGCharts.ChartDataEntry? {
    fatalError("Abstract")

  }

  func getTupleFromEntry(_ entry: DGCharts.ChartDataEntry) -> YailList<AnyObject> {
    fatalError("Abstract")

  }

  func findEntryIndex(_ entry: DGCharts.ChartDataEntry) -> Int32 {
    for (index, currentEntry) in entries.enumerated() {
      if areEntriesEqual(currentEntry, entry){
        return Int32(index)
      }
    }
    return -1

  }

  func addTimeEntry(_ tuple: YailList<AnyObject>) {
    if _entries.count > maximumTimeEntries {
      _entries.remove(at: 0)
    }
    // add entry from teh specified tuple
    addEntryFromTuple(tuple)
  }

  override func areEntriesEqual(_ e1: AnyObject, _ e2: AnyObject) -> Bool {
    guard let e1 = e1 as? DGCharts.ChartDataEntry, let e2 = e2 as? DGCharts.ChartDataEntry else {
      return super.areEntriesEqual(e1, e2)
    }
    return e1 == e2
  }

  public var chartEntries: [DGCharts.ChartDataEntry] {
    return _entries as? [DGCharts.ChartDataEntry] ?? []
  }
}
