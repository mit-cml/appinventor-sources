// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@objc protocol DataSourceChangeListener {
  func onDataSourceValueChange(_ component: DataSource, _ key: String?, _ newValue: AnyObject?)
  func onReceiveValue(_ component: RealTimeDataSource, _ key: String?, _ value: AnyObject?)
}
