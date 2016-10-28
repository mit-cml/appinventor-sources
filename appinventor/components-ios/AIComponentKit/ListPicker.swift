//
//  ListPicker.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class ListPicker: Picker {
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    _view.addTarget(self, action: #selector(click), for: UIControlEvents.primaryActionTriggered)
    parent.add(self)
  }
  
  public var ElementsFromString: String {
    get {
      return ""
    }
    set(itemstring) {
      
    }
  }
}
