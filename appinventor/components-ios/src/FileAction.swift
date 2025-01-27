// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public class FileAction: NSObject, OptionList {
  @objc public static let PickExistingFile = FileAction("Pick Existing File")
  @objc public static let PickNewFile = FileAction("Pick New File")
  @objc public static let PickDirectory = FileAction("Pick Directory")

  private static let LOOKUP: [String:FileAction] = [
    "Pick Existing File": FileAction.PickExistingFile,
    "Pick New File": FileAction.PickNewFile,
    "Pick Directory": FileAction.PickDirectory
  ]

  let label: NSString

  @objc private init(_ label: NSString) {
    self.label = label
  }

  @objc class func fromUnderlyingValue(_ scope: String) -> FileAction? {
    return LOOKUP[scope]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return label
  }
}
