// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

public func formatAsDecimal(_ value: Double, _ places: Int) -> String {
  let format = NumberFormatter()
  format.maximumFractionDigits = places
  return format.string(from: NSNumber(value: value))!
}
