//
//  FileType.swift
//  AIComponentKit
//
//  Created by Jonathan Tjandra on 5/14/24.
//  Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
//

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
