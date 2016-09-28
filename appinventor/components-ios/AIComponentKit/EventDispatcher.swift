//
//  EventDispatcher.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/26/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

class EventDispatcher {
  public class func dispatchEvent(of component: Component, called: String, arguments: AnyObject...) {
    NSLog("EventDispatcher: Trying to dispatch event %s", called)
  }
}
