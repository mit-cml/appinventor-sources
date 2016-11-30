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

public class Picker: ButtonBase, AbstractMethodsForButton {
  private weak var _delegate: AbstractMethodsForPicker?

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
  }

  public func setDelegate(_ delegate: AbstractMethodsForPicker) {
    _delegate = delegate
    super.setDelegate(delegate as AbstractMethodsForButton)
  }

  public func click() {
    BeforePicking()
    _delegate?.open()
  }
  
  public func Open() {
    click()
  }
  
  public func BeforePicking() {
    EventDispatcher.dispatchEvent(of: self, called: "BeforePicking")
  }
  
  public func AfterPicking() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterPicking")
  }
}
