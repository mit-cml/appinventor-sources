// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class NonvisibleComponent: NSObject, Component {
  @objc weak var _form: Form?

  @objc public init(_ container: ComponentContainer) {
    self._form = container.form
    super.init()
    _form?.add(self)
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
