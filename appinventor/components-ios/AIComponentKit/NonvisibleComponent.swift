// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2020 Massachusetts Institute of Technology, All rights reserved.

import Foundation

open class NonvisibleComponent: NSObject, Component {
  @objc weak var _form: Form?

  @objc public init(_ container: ComponentContainer) {
    self._form = container.form
  }

  open func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  open var dispatchDelegate: HandlesEventDispatching? {
    get {
      return _form?.dispatchDelegate
    }
  }
}
