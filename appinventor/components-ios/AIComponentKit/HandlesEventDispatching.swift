// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public protocol HandlesEventDispatching: NSCopying {
  func canDispatchEvent(of component: Component, called eventName: String) -> Bool
  func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool
  func dispatchGenericEvent(of component: Component, eventName: String, unhandled: Bool, arguments: [AnyObject])
}
