//
//  AndroidNonvisibleComponent.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

open class NonvisibleComponent: NSObject, Component {
  final var _form: Form

  public init(_ container: ComponentContainer) {
    self._form = container.form
  }

  open func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  open var dispatchDelegate: HandlesEventDispatching {
    get {
      return _form.dispatchDelegate
    }
  }
}
