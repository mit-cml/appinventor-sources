//
//  OptionList.swift
//  AIComponentKit
//
//  Created by Amelia Xiang Zhang on 8/9/23.
//  Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

@objc public protocol OptionList {
  func toUnderlyingValue() -> AnyObject
}
