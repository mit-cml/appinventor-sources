//
//  TinyDB.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import CoreData

public class TinyDB: NonvisibleComponent {

  private var storage: NSPersistentStore!

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
  }

  public func StoreValue(_ tag: String, _ valueToStore: AnyObject) {
    
  }
  
  public func GetValue(_ tag: String, _ valueIfTagNotThere: AnyObject) -> AnyObject {
    return valueIfTagNotThere
  }
  
  public func GetTags() -> [String] {
    return [String]()
  }

  public func ClearAll() {
    
  }
  
  public func ClearTag(_ tag: String) {
    
  }
}
