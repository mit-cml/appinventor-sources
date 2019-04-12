// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@objc public protocol ComponentContainer {
    var form: Form { get }
    func add(_ component: ViewComponent)
    func setChildWidth(of component: ViewComponent, to width: Int32)
    func setChildHeight(of component: ViewComponent, to height: Int32)
    var Width: Int32 { get }
    var Height: Int32 { get }
}
