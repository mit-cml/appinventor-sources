// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public func formatAsDecimal(_ value: Double, _ places: Int) -> String {
  let format = NumberFormatter()
  format.maximumFractionDigits = places
  return format.string(from: NSNumber(value: value))!
}
