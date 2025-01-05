// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public class LOBFValues: NSObject, OptionList {
  @objc public static let CorrCoef = LOBFValues("correlation coefficient")
  @objc public static let Slope = LOBFValues("slope")
  @objc public static let Yintercept = LOBFValues("Yintercept")
  @objc public static let Predictions = LOBFValues("predictions")
  @objc public static let AllValues = LOBFValues("all values")
  @objc public static let QuadraticCoefficient = LOBFValues("Quadratic Coefficient")
  @objc public static let LinearCoefficient = LOBFValues("slope")
  @objc public static let ExponentialCoefficient = LOBFValues("a")
  @objc public static let ExponentialBase = LOBFValues("b")
  @objc public static let LogarithmCoefficient = LOBFValues("b")
  @objc public static let LogarithmConstant = LOBFValues("a")
  @objc public static let XIntercepts = LOBFValues("XIntercepts")
  @objc public static let RSquared = LOBFValues("r^2")

  private static let LOOKUP: [String:LOBFValues] = generateOptionsLookup(
    CorrCoef,
    Slope,
    Yintercept,
    Predictions,
    AllValues,
    QuadraticCoefficient,
    LinearCoefficient,
    ExponentialCoefficient,
    ExponentialBase,
    LogarithmCoefficient,
    LogarithmConstant,
    XIntercepts,
    RSquared
  )

  let label: NSString

  @objc private init(_ label: NSString) {
    self.label = label
  }

  @objc class func fromUnderlyingValue(_ name: String) -> LOBFValues? {
    return LOOKUP[name]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return label
  }
}
