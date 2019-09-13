// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

open class HorizontalScrollArrangement: HVArrangement {
  @objc public init(_ parent: ComponentContainer) {
    super.init(parent, orientation: .horizontal, scrollable: true)
  }
}
