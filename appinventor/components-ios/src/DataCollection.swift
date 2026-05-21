// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc open class DataCollection: NSObject, DataSource {
  weak var container: ComponentContainer?
  var dataModel: DataModel?

  public init(_ container: ComponentContainer) {
    self.container = container
    super.init()
  }

  func getDataValue(_ key: AnyObject?) -> [Any] {
    return dataModel?.entries ?? []
  }
  
  var dataFileColumns: [String]?
  var useSheetHeaders: Bool = false
  var sheetsColumns: [String]?
  var webColumns: [String]?
  var dataSourceKey: String?
  var _elements: String?
  var _initialized: Bool = false
  var _tick: Int = 0

  // TODO : We need to test this to make sure it doesn't create a reference cycle.
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

  static func castToDouble(_ origList: YailList<AnyObject>) -> [Double] {
    var result: [Double] = []
    for item in origList {
      if item is SCMSymbol {
        continue
      }
      if let item = item as? NSNumber {
        result.append(item.doubleValue)
      } else if let item = item as? NSString, let value = Double(item as String) {
        result.append(value)
      }
    }
    return result
  }
}
