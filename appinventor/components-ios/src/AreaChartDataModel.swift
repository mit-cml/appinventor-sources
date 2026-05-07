// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

class AreaChartDataModel: LineChartBaseDataModel {
  public init(data: DGCharts.LineChartData, view: AreaChartView) {
    super.init(data: data, view: view)
  }

  override func setColor(_ argb: UIColor) {
    super.setColor(argb)
    if let dataset = self.dataset as? DGCharts.LineChartDataSet {
      dataset.fillColor = argb
    }
  }

  override func setColors(_ colors: [NSUIColor]) {
    super.setColors(colors)
    if let dataset = self.dataset as? DGCharts.LineChartDataSet, !colors.isEmpty {
      dataset.fillColor = colors[0]
    }
  }

  override func setDefaultStylingProperties() {
    super.setDefaultStylingProperties()
    if let dataset = self.dataset as? DGCharts.LineChartDataSet {
      dataset.drawFilledEnabled = true
      dataset.fillAlpha = CGFloat(0.333)
    }
  }
}

