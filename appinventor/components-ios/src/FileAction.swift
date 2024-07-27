//
//  FileAction.swift
//
//
//  Created by Jonathan Tjandra on 5/14/24.
//

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
