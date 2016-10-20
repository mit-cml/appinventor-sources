//
//  Button.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public final class Button: ButtonBase, AbstractMethodsForButton {
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    _view.titleLabel?.text = "Button1"
    parent.add(self)
  }

  public func click() {
    Click()
  }

  public func Click() {
    EventDispatcher.dispatchEvent(of: self, called: "Click")
  }
  
  public func LongClick() {
    EventDispatcher.dispatchEvent(of: self, called: "LongClick")
  }
}
