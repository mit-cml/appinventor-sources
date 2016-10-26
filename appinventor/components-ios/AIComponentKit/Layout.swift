//
//  Layout.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/24/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

extension UIView {
  func removeAllViews() {
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
