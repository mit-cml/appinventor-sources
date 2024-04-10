// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class NonvisibleComponent: NSObject, Component {
  public unowned let container: ComponentContainer

  @objc public init(_ container: ComponentContainer) {
    self.container = container
    super.init()
    form.add(self)
  }

  public var form: Form {
    return container.form
  }

  open func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  open var dispatchDelegate: HandlesEventDispatching {
    get {
      return form.dispatchDelegate
    }
  }
}
