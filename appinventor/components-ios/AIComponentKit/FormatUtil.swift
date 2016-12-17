//
//  FormatUtil.swift
//  AIComponentKit
//
//  Created by Evan Patton on 12/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public func formatAsDecimal(_ value: Double, _ places: Int) -> String {
  var format = NumberFormatter()
  format.maximumFractionDigits = places
  return format.string(from: NSNumber(value: value))!
}
