// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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

  @objc open func click() {
    BeforePicking()
    _delegate?.open()
  }
  
  @objc open func Open() {
    click()
  }
  
  @objc open func BeforePicking() {
    EventDispatcher.dispatchEvent(of: self, called: "BeforePicking")
  }
  
  @objc open func AfterPicking() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterPicking")
  }
}
