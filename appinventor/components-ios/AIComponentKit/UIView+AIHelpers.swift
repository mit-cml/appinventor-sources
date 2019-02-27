// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import UIKit

extension UIView {
  func addDefaultDimension(for dimension: NSLayoutDimension, length: CGFloat) {
    let constraint = dimension.constraint(equalToConstant: length)
    constraint.priority = UILayoutPriority.defaultLow
    addConstraint(constraint)
  }
}
