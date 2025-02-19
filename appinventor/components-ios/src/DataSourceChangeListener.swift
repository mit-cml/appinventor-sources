// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc protocol DataSourceChangeListener {
  func onDataSourceValueChange(_ component: DataSource, _ key: String?, _ newValue: AnyObject?)
  func onReceiveValue(_ component: RealTimeDataSource, _ key: String?, _ value: AnyObject?)
}
