//
//  ScatterWithTrendlineRenderer.swift
//  AIComponentKit
//
//  Created by David Kim on 9/17/24.
//  Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation
import UIKit
import Charts
import DGCharts

class ScatterWithTrendlineRenderer: ScatterChartRenderer {
  private let logTag = "ScatterWithTrendlineRenderer"
  private let debug = false

  init(chart: DGCharts.ScatterChartView, animator: Animator, viewPortHandler: ViewPortHandler) {
    super.init(dataProvider: chart, animator: animator, viewPortHandler: viewPortHandler)
  }

  override func drawData(context: CGContext) {
    guard let scatterData = dataProvider?.scatterData else { return }

    for dataSet in scatterData.dataSets {
      if dataSet.isVisible, let hasTrendline = dataSet as? HasTrendline {
        drawTrendline(context: context, dataSet: hasTrendline as! Trendline.ScatterChartBestFitDataSet)
      }
    }
    super.drawData(context: context)
  }

  private func drawTrendline(context: CGContext, dataSet: Trendline.ScatterChartBestFitDataSet) {
    guard dataSet.isVisible() else { return }
    guard let chart = dataProvider as? DGCharts.ScatterChartView else {
      print("Cannot cast dataProvider to ScatterChartView")
      return
    }

    let transformer = dataProvider?.getTransformer(forAxis: dataSet.axisDependency)
    let points = dataSet.getPoints(xMin: chart.chartXMin, xMax: chart.chartXMax, viewWidth: Int(viewPortHandler.contentWidth))

    if points.isEmpty {
      return
    }

    let linePath = CGMutablePath()
    points.enumerated().forEach { index, point in
      let point = CGPoint(x: point.x, y: point.y).applying(transformer!.valueToPixelMatrix)
      if index == 0 {
        linePath.move(to: point)
      } else {
        linePath.addLine(to: point)
      }
    }

    context.setStrokeColor(dataSet.getColor().cgColor)
    context.setLineWidth(dataSet.getLineWidth())
    if let dashPattern = dataSet.getDashPattern(), !dashPattern.isEmpty {
      context.setLineDash(phase: 0, lengths: dashPattern)
    } else {
      context.setLineDash(phase: 0, lengths: [])
    }

    context.addPath(linePath)
    context.strokePath()
  }
}
