// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc protocol ObservableDataSource: DataSource {
  func addDataObserver(_ listener: DataSourceChangeListener)
  func removeDataObserver(_ listener: DataSourceChangeListener)
  func notifyDataObservers(_ key: AnyObject?, _ newValue: AnyObject?)
}
