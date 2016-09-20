//
//  ComponentContainer.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol ComponentContainer {
    var form: Form { get }
    func add(component: ViewComponent)
    func setChildWidth(of component: ViewComponent, width: Int32)
    func setChildHeight(of component: ViewComponent, height: Int32)
    var Width: Int32 { get }
    var Height: Int32 { get }
}
