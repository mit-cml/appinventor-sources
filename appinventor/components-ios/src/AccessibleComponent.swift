//
//  AccessibleComponent.swift
//  AIComponentKit
//
//  Created by David Kim on 1/30/23.
//  Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

@objc public protocol AccessibleComponent {

  func isHighContrast() -> Bool

  func setHighContrast(to isHighContrast: Bool)

  func isLargeFont() -> Bool

  func setLargeFont(to isLargeFont: Bool)
}
