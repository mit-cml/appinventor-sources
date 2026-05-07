// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc open class DataModel: NSObject {
  var _entries: [AnyObject] = []
  public var maximumTimeEntries = 200

  public var entries: [AnyObject] {
    return _entries
  }

  func addEntryFromTuple(_ tuple: YailList<AnyObject>) {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func clearEntries() {
    _entries.removeAll()
  }

  func getTupleSize() -> Int {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func getEntries() -> [AnyObject] {
    return _entries
  }

  func setElements(_ elements: String) {
    let tupleSize = getTupleSize()

    let entries = elements.split(",")

    // entries.count - 1 because ranges are inclusive in Swift
    for i in stride(from: tupleSize - 1, to: entries.count, by: tupleSize) {
      var tupleEntries: Array<String> = []
      // Iterate all over the tuple entries
      for j in stride(from: tupleSize - 1, through: 0, by: -1) {
        let index: Int = i - j
        tupleEntries.append(entries[index])
      }

      // Add entry from the parsed tuple
      let yailListEntries: YailList<AnyObject> = []
      for tuple in tupleEntries {
        yailListEntries.add(tuple)
      }
      // addEntryFromTuple(tupleEntries as! YailList<AnyObject>)
      addEntryFromTuple(yailListEntries)

    }
  }

  func importFromList(_ list: [Any]) {
    // Pass
  }

  func removeValues(_ values: [Any]) {
    // Pass
  }

  func importFromColumns(_ columns: [Any], hasHeaders: Bool) {
    // Pass
  }

  func getTuplesFromColumns(_ columns: [Any], hasHeaders: Bool) -> [[Any]] {
    // Pass
    return []
  }

  func removeEntryFromTuple(_ tuple: [Any]) {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func removeEntry(at index: Int) {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func doesEntryExist(_ tuple: [Any]) -> Bool {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func findEntriesByCriterion(_ value: String, criterion: EntryCriterion) -> [[Any]] {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func getEntriesAsTuples() -> [[Any]] {
    return findEntriesByCriterion("0", criterion: .All)
  }

  func isEntryCriterionSatisfied(_ entry: [Any], criterion: EntryCriterion, value: String) -> Bool {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func getEntryFromTuple(_ tuple: [Any]) -> [Any]? {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func getTupleFromEntry(_ entry: [Any]) -> [Any] {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func findEntryIndex(_ entry: [Any]) -> Int {
    fatalError("Need to implement abstract method in \(type(of: self))")
  }

  func setDefaultStylingProperties() {
    // Optional for subclasses to implement
  }

  func getDefaultValue(_ index: Int) -> String {
    return "\(index)"
  }

  func areEntriesEqual(_ e1: AnyObject, _ e2: AnyObject) -> Bool {
    return e1 === e2
  }
}
