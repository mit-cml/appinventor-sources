//
//  ScatterChartView.swift
//  AIComponentKit
//
//  Created by David Kim on 3/6/24.
//  Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation
import DGCharts

open class ScatterChartView: PointChartView {

  override init(_ chartComponent: Chart) {
    super.init(chartComponent)

    chart = DGCharts.ScatterChartView()
    data = DGCharts.ScatterChartData()
    chart?.data = data

    initializeDefaultSettings()
  }

  public override func createChartModel() -> ChartDataModel {
    return ScatterChartDataModel(data: data as! ScatterChartData, view: self)
  }
}
