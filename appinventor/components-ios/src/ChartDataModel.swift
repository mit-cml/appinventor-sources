// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

enum EntryCriterion {
  case All
  case XValue
  case YValue
}

open class ChartDataModel<E: Charts.ChartDataEntry, D: Charts.ChartData, V: Charts.ChartViewBase> {
  let data: D
  let dataset: Charts.ChartDataSet
  let view: V
  var _entries = [E]()
  var maximumTimeEntries: Int32 = 200

  init(data: D, dataset: Charts.ChartDataSet, view: V) {
    self.data = data
    self.dataset = dataset
    self.view = view
  }

  var tupleSize: Int {
    return 0
  }

  func tupleFromEntry(entry: E) -> YailList<AnyObject> {
    return YailList()
  }

  func setColor(_ argb: Int32) {
    dataset.setColor(argbToColor(argb))
  }

  func setColors(_ argb: [Int32]) {
    dataset.setColors(argb.map({ color in
      return argbToColor(color)
    }), alpha: 1.0)
  }

  func setLabel(_ text: String) {
    dataset.label = text
  }

  func setElements(_ elements: String) {
    let tupleSize = self.tupleSize
    let entries = elements.split(",")
    // MARK: Finish
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

  }

  func importFromColumns(_ columns: YailList<AnyObject>, _ hasHeaders: Bool) {

  }

  func getTuplesFromColumns(_ columns: YailList<AnyObject>, _ hasHeaders: Bool) -> YailList<AnyObject> {
          
  }

  func addEntryFromTuple(_ tuple: YailList<AnyObject>) {

  }

  func removeEntryFromTuple(_ tuple: YailList<AnyObject>) {

  }

  func removeEntry(_ index: Int) {

  }

  func doesEntryExist(_ tuple: YailList<AnyObject>) -> Bool {
    return false
  }

  func findEntriesByCriterion(_ value: String, _ criterion: EntryCriterion) -> YailList<AnyObject> {
    return YailList()
  }

  func getEntriesAsTuples() -> YailList<AnyObject> {
    return YailList()
  }

  func isEntryCriterionSatisfied(_ entry: E, _ criterion: EntryCriterion, value: String) -> Bool {
    return false
  }

  func getEntryFromTuple(_ tuple: YailList<AnyObject>) -> E {
    
  }

  func getTupleFromEntry(_ entry: E) -> YailList<AnyObject> {

  }

  func findEntryIndex(_ entry: E) -> Int32 {

  }

  func clearEntries() {
    _entries.removeAll()
  }

  func addTimeEntry(_ tuple: YailList<AnyObject>) {

  }

  func setMaximumTimeEntries(_ count: Int32) {
    maximumTimeEntries = count
  }

  func setDefaultStylingProperties() {

  }

  func getDefaultValue(_ index: Int) -> String {
    return "\(index)"
  }

  func areEntriesEqual(_ e1: E, _ e2: E) -> Bool {
    return e1 == e2
  }

  public var entries: [E] {
    return self._entries
  }
}
