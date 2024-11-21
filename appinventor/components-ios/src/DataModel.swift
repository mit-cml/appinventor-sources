//
//  DataModel.swift
//  AIComponentKit
//
//  Created by David Kim on 3/28/24.
//  Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

protocol DataModel {
  var entries: [[Any]] { get set }
  var maximumTimeEntries: Int { get set }
  
  func addEntryFromTuple(_ tuple: [Any])
  func clearEntries()
  func getTupleSize() -> Int
  func setElements(_ elements: String)
  func importFromList(_ list: [Any])
  func removeValues(_ values: [Any])
  func importFromColumns(_ columns: [Any], hasHeaders: Bool)
  func getTuplesFromColumns(_ columns: [Any], hasHeaders: Bool) -> [[Any]]
  func removeEntryFromTuple(_ tuple: [Any])
  func removeEntry(at index: Int)
  func doesEntryExist(_ tuple: [Any]) -> Bool
  func findEntriesByCriterion(_ value: String, criterion: EntryCriterion) -> [[Any]]
  func getEntriesAsTuples() -> [[Any]]
  func isEntryCriterionSatisfied(_ entry: [Any], criterion: EntryCriterion, value: String) -> Bool
  func getEntryFromTuple(_ tuple: [Any]) -> [Any]?
  func getTupleFromEntry(_ entry: [Any]) -> [Any]
  func findEntryIndex(_ entry: [Any]) -> Int
  func addTimeEntry(_ tuple: [Any])
  func setMaximumTimeEntries(_ entries: Int)
  func setDefaultStylingProperties()
  func getDefaultValue(_ index: Int) -> String
  func areEntriesEqual(_ e1: [Any], _ e2: [Any]) -> Bool
  func getEntries() -> [[Any]]
}
