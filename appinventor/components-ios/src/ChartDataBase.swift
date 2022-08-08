//
//  ChartDataBase.swift
//  AIComponentKit
//
//  Created by Evan Patton on 8/3/22.
//  Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation
import Charts

@objc class ChartDataBase: NSObject, Component, DataSourceChangeListener, ChartViewDelegate {
  var dispatchDelegate: HandlesEventDispatching?

  func copy(with zone: NSZone? = nil) -> Any {
    <#code#>
  }


}
