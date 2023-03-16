//
//  AccessibleComponent.swift
//  AIComponentKit
//
//  Created by David Kim on 1/30/23.
//  Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

@objc public protocol AccessibleComponent {
  
  var HighContrast : Bool { get set }
  
  var LargeFont : Bool { get set }
}
