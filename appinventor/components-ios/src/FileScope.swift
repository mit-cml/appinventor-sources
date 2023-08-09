//
//  FileScope.swift
//  AIComponentKit
//
//  Created by Amelia Xiang Zhang on 8/9/23.
//  Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

public class FileScope: OptionList {
  public static let App = FileScope("App")
  public static let Asset = FileScope("Asset")
  public static let Cache = FileScope("Cache")
  public static let Legacy = FileScope("Legacy")
  public static let Private = FileScope("Private")
  public static let Shared = FileScope("Shared")
  
  private static let LOOKUP: [String:FileScope] = [
    "App": FileScope.App,
    "Asset": FileScope.Asset,
    "Cache": FileScope.Cache,
    "Legacy": FileScope.Legacy,
    "Private": FileScope.Private,
    "Shared": FileScope.Shared,
  ]
  
  let label: NSString
  
  private init(_ label: NSString) {
    self.label = label
  }
  
  class func fromUnderlyingValue(_ scope: String) -> FileScope? {
    return LOOKUP[scope]
  }
  
  public func toUnderlyingValue() -> AnyObject {
    return label
  }
}
