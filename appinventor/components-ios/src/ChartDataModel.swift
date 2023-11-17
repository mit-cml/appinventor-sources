// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

enum EntryCriterion {
  case All
  case XValue
  case YValue
}
// <E: DGCharts.ChartDataEntry, D: DGCharts.ChartData, C: DGCharts.ChartViewBase, V: ChartView>
open class ChartDataModel {
  let data: DGCharts.ChartData
  var dataset: DGCharts.ChartDataSet?
  let view: ChartView
  var _entries = [DGCharts.ChartDataEntry]()
  var maximumTimeEntries: Int32 = 200

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
  
  // TODO: CHANGED THIS
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

  func setElements(_ elements: String) {
    let tupleSize = self.tupleSize
    let entries = elements.split(",")
    print("tuplesize-1", tupleSize - 1)
    
    // entries.count - 1 because ranges are inclusive in Swift
    for i in stride(from: tupleSize - 1, to: entries.count - 1, by: tupleSize) {
      var tupleEntries: Array<String> = []
      
      // Iterate all over the tuple entries
      // First entry is in  (i - tupleSize + 1)
      for j in stride(from: tupleSize - 1, to: 0, by: -1) {
        var index: Int = i - j
        tupleEntries.append(entries[index])
      }
      
      // Add entry from the parsed tuple
      addEntryFromTuple(tupleEntries as! YailList<AnyObject>)
    }
  }

  func importFromList(_ list: [AnyObject]) {

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

  func importFromColumns(_ columns: YailList<AnyObject>, _ hasHeaders: Bool) {

  }

  func getTuplesFromColumns(_ columns: YailList<AnyObject>, _ hasHeaders: Bool) -> YailList<AnyObject> {
    fatalError("Not implemented yet")
  }

  func addEntryFromTuple(_ tuple: YailList<AnyObject>) {
    // abstract
  }

  func removeEntryFromTuple(_ tuple: YailList<AnyObject>) {
    var entry: DGCharts.ChartDataEntry = getEntryFromTuple(tuple)
    if entry != nil {
      var index: Int32 = findEntryIndex(entry)
      removeEntry(Int(index))
    }

  }

  func removeEntry(_ index: Int) {
    if index >= 0 {
      _entries.remove(at: index)
    }
  }

  func doesEntryExist(_ tuple: YailList<AnyObject>) -> Bool {
    var entry : DGCharts.ChartDataEntry = getEntryFromTuple(tuple)
    var index: Int32 = findEntryIndex(entry)
    return index >= 0
  }

  func findEntriesByCriterion(_ value: String, _ criterion: EntryCriterion) -> YailList<AnyObject> {
    var entries: Array<YailList<AnyObject>> = []
    for entry in _entries {
      if isEntryCriterionSatisfied(entry, criterion, value: value) {
        entries.append(getTupleFromEntry(entry))
      }
    }
    return entries as! YailList<AnyObject>
  }

  func getEntriesAsTuples() -> YailList<AnyObject> {
    return findEntriesByCriterion("0", EntryCriterion.All)
  }

  func isEntryCriterionSatisfied(_ entry: DGCharts.ChartDataEntry, _ criterion: EntryCriterion, value: String) -> Bool {
    return false
  }

  func getEntryFromTuple(_ tuple: YailList<AnyObject>) -> DGCharts.ChartDataEntry {
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

  func clearEntries() {
    _entries.removeAll()
  }

  func addTimeEntry(_ tuple: YailList<AnyObject>) {
    if _entries.count > maximumTimeEntries {
      _entries.remove(at: 0)
    }
    // add entry from teh specified tuple
    addEntryFromTuple(tuple)
  }

  func setMaximumTimeEntries(_ count: Int32) {
    maximumTimeEntries = count
  }

  func setDefaultStylingProperties() {
    // this method is left empty
  }

  func getDefaultValue(_ index: Int) -> String {
    return "\(index)"
  }

  func areEntriesEqual(_ e1: DGCharts.ChartDataEntry, _ e2: DGCharts.ChartDataEntry) -> Bool {
    return e1 == e2
  }

  public var entries: [DGCharts.ChartDataEntry] {
    return self._entries
  }
}
