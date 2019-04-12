// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@objc public protocol VisibleComponent: Component {
  var Width: Int32 { get set }
  func setWidthPercent(_ toPercent: Int32)
  var Height: Int32 { get set }
  func setHeightPercent(_ toPercent: Int32)
}
