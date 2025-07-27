//
//  MenuItem.swift
//  AIComponentKit
//
//  Created by Kashish Sharma on 21/07/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//


import UIKit

@available(iOS 14.5, *)
@objc(MenuItem)
open class MenuItem: NonvisibleComponent {
    private weak var _container: ComponentContainer?

    @objc open var Text: String = ""
    @objc open var Enabled: Bool = true
    @objc open var Visible: Bool = true
    @objc open var Icon: String = ""
    @objc open var ShowOnActionBar: Bool = false

    @objc public override init(_ container: ComponentContainer) {
        self._container = container
        super.init(container)
        // Add this MenuItem to its parent Menu
        if let menu = container as? Menu {
            menu.addChildComponent(self)
        }
    }

    @objc open func Click() {
        EventDispatcher.dispatchEvent(of: self, called: "Click")
    }
}
