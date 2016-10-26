//
//  HandlesEventDispatching.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation



@objc public protocol HandlesEventDispatching: NSCopying {
    func canDispatchEvent(of component: Component, called eventName: String) -> Bool
    func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool
}
