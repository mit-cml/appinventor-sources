// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

extension UIView {
  @objc func removeAllViews() {
    let subviews = self.subviews
    for view in subviews {
      view.removeFromSuperview()
    }
  }
}

protocol Layout {
  func getLayoutManager() -> UIView
  func add(_ component: ViewComponent)
}
