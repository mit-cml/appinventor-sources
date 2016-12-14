//
//  Picker.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol AbstractMethodsForPicker: AbstractMethodsForButton {
  func open()
}

open class Picker: ButtonBase, AbstractMethodsForButton {
  fileprivate weak var _delegate: AbstractMethodsForPicker?

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
  }

  open func setDelegate(_ delegate: AbstractMethodsForPicker) {
    _delegate = delegate
    super.setDelegate(delegate as AbstractMethodsForButton)
  }

  open func click() {
    BeforePicking()
    _delegate?.open()
  }
  
  open func Open() {
    click()
  }
  
  open func BeforePicking() {
    EventDispatcher.dispatchEvent(of: self, called: "BeforePicking")
  }
  
  open func AfterPicking() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterPicking")
  }
}
