//
//  LinearLayout.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/24/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

class LinearLayout: Layout {
  fileprivate var _view = UIStackView()
  
  init() {
    _view.spacing = 0.0
    _view.axis = NSLayoutConstraint.Axis.vertical
  }
  
  func getLayoutManager() -> UIView {
    return _view
  }
  
  func add(_ component: ViewComponent) {
    _view.addArrangedSubview(component.view)
  }
}
