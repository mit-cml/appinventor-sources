//
//  HorizontalArrangement.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

open class HorizontalArrangement: HVArrangement {
  public init(_ parent: ComponentContainer) {
    super.init(parent, orientation: .horizontal, scrollable: false)
  }
}
