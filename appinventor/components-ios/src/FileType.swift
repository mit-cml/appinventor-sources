// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public class FileType: NSObject, OptionList {
  @objc public static let All = FileType("*/*")
  @objc public static let Audio = FileType("audio/*")
  @objc public static let Image = FileType("image/*")
  @objc public static let Video = FileType("video/*")

  private static let LOOKUP: [String:FileType] = [
    "*/*": FileType.All,
    "audio/*": FileType.Audio,
    "image/*": FileType.Image,
    "video/*": FileType.Video
  ]

  let label: NSString

  @objc private init(_ label: NSString) {
    self.label = label
  }

  @objc class func fromUnderlyingValue(_ scope: String) -> FileType? {
    return LOOKUP[scope]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return label
  }
}
