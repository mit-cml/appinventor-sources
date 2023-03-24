// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit

extension UIView {
  func addDefaultDimension(for dimension: NSLayoutDimension, length: CGFloat) {
    let constraint = dimension.constraint(equalToConstant: length)
    constraint.priority = UILayoutPriority.defaultLow
    addConstraint(constraint)
  }
}
