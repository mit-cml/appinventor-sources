//
//  AndroidNonvisibleComponent.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class NonvisibleComponent: NSObject, Component {
  private final var _dispatcher: HandlesEventDispatching

  public init(_ dispatcher: ComponentContainer) {
    self._dispatcher = (dispatcher.form?.dispatchDelegate)!
  }

  public func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  public var dispatchDelegate: HandlesEventDispatching {
    get {
      return _dispatcher
    }
  }
}
