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

  public init(dispatcher: HandlesEventDispatching) {
    self._dispatcher = dispatcher
  }

  public var dispatchDelegate: HandlesEventDispatching {
    get {
      return _dispatcher
    }
  }
}
