// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import Charts

@objc class Chart : ViewComponent, ComponentContainer, LifecycleDelegate {

  var _view = UIView()

  var container: ComponentContainer? {
    return _container
  }

  func add(_ component: ViewComponent) {
    <#code#>
  }

  func setChildWidth(of component: ViewComponent, to width: Int32) {
    <#code#>
  }

  func setChildHeight(of component: ViewComponent, to height: Int32) {
    <#code#>
  }

  func isVisible(component: ViewComponent) -> Bool {
    <#code#>
  }

  func setVisible(component: ViewComponent, to visibility: Bool) {
    <#code#>
  }


}
