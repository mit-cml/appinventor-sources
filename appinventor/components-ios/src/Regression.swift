// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc open class Regression: DataCollection {
  private static let LINEAR_REGRESSION = LinearRegression()

  @objc public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  @objc open func CalculateLineOfBestFitValue(_ xList: YailList<AnyObject>, _ yList: YailList<AnyObject>, _ value: String) throws -> AnyObject {
    let xList = Regression.castToDouble(xList)
    let yList = Regression.castToDouble(yList)
    guard xList.count == yList.count else {
      throw NSError(domain: "Regression", code: 1)
    }
    let result = Regression.LINEAR_REGRESSION.compute(x: xList, y: yList)
    if let realValue = result[value] {
      return realValue as AnyObject
    } else {
      return result as AnyObject
    }
  }
}
