//
//  Picker.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class Picker: ButtonBase, AbstractMethodsForButton {

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
  }

  public func click() {
    
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
