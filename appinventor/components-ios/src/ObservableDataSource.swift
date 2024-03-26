// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@objc protocol ObservableDataSource: DataSource {
  func addDataObserver(_ listener: DataSourceChangeListener)
  func removeDataObserver(_ listener: DataSourceChangeListener)
  func notifyDataObservers(_ key: AnyObject?, _ newValue: AnyObject?)
}
