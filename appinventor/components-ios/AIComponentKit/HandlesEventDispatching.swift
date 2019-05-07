// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@objc public protocol HandlesEventDispatching: NSCopying {
  func canDispatchEvent(of component: Component, called eventName: String) -> Bool
  func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool
  func dispatchGenericEvent(of component: Component, eventName: String, unhandled: Bool, arguments: [AnyObject])
}
