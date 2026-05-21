// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public class FileScope: NSObject, OptionList {
  @objc public static let App = FileScope("App")
  @objc public static let Asset = FileScope("Asset")
  @objc public static let Cache = FileScope("Cache")
  @objc public static let Legacy = FileScope("Legacy")
  @objc public static let Private = FileScope("Private")
  @objc public static let Shared = FileScope("Shared")

  private static let LOOKUP: [String:FileScope] = [
    "App": FileScope.App,
    "Asset": FileScope.Asset,
    "Cache": FileScope.Cache,
    "Legacy": FileScope.Legacy,
    "Private": FileScope.Private,
    "Shared": FileScope.Shared
  ]

  let label: NSString

  @objc private init(_ label: NSString) {
    self.label = label
  }

  @objc class func fromUnderlyingValue(_ scope: String) -> FileScope? {
    return LOOKUP[scope]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return label
  }
}
