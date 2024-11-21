//
//  DataCollection.swift
//  AIComponentKit
//
//  Created by David Kim on 3/28/24.
//  Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

@objc open class DataCollection: NSObject, DataSource {
  func getDataValue(_ key: AnyObject) -> AnyObject {
    return key
  }
  
  private var _dataFileColumns: [String]?
  var dataFileColumns: [String]? {
    get { _dataFileColumns }
    set { _dataFileColumns = newValue }
  }

  private var _useSheetHeaders: Bool = false
  var useSheetHeaders: Bool {
    get { _useSheetHeaders }
    set { _useSheetHeaders = newValue }
  }

  private var _sheetsColumns: [String]?
  var sheetsColumns: [String]? {
    get { _sheetsColumns }
    set { _sheetsColumns = newValue }
  }

  private var _webColumns: [String]?
  var webColumns: [String]? {
    get { _webColumns }
    set { _webColumns = newValue }
  }

  private var _dataSourceKey: String?
  var dataSourceKey: String? {
    get { _dataSourceKey }
    set { _dataSourceKey = newValue }
  }

  private var __elements: String?
  var _elements: String? {
    get { __elements }
    set { __elements = newValue }
  }

  private var __initialized: Bool = false
  var _initialized: Bool {
    get { __initialized }
    set { __initialized = newValue }
  }

  private var __tick: Int = 0
  var _tick: Int {
    get { __tick }
    set { __tick = newValue }
  }

  internal var listeners = Set<NSObject>()
  func addDataSourceChangeListener(_ listener: DataSourceChangeListener) {
    guard let listenerObject = listener as? NSObject else { return }
    listeners.insert(listenerObject)
    listener.onDataSourceValueChange(self, nil, nil)
  }

  func removeDataSourceChangeListener(_ listener: DataSourceChangeListener) {
    guard let listenerObject = listener as? NSObject else { return }
    listeners.remove(listenerObject)
  }
}
