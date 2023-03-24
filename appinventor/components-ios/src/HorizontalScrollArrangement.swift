// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class HorizontalScrollArrangement: HVArrangement {
  @objc public init(_ parent: ComponentContainer) {
    super.init(parent, orientation: .horizontal, scrollable: true)
  }
}
