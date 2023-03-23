// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public protocol VisibleComponent: Component {
  var Width: Int32 { get set }
  func setWidthPercent(_ toPercent: Int32)
  var Height: Int32 { get set }
  func setHeightPercent(_ toPercent: Int32)
}
