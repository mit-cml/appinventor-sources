//
//  AccessibleComponent.swift
//  AIComponentKit
//
//  Created by David Kim on 1/30/23.
//  Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

@objc public protocol AccessibleComponent {
  /**
   * The component's contrast controller for better visibility
   */
  var HighContrast : Bool { get set }
  /**
   * The component's Font controller for better visibility
   */
  var LargeFont : Bool { get set }
}
