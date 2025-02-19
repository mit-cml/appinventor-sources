// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit
import Charts
import DGCharts

class LineWithTrendlineRenderer: LineChartRenderer {
  init(chart: DGCharts.LineChartView, animator: Animator, viewPortHandler: ViewPortHandler) {
    super.init(dataProvider: chart, animator: animator, viewPortHandler: viewPortHandler)
  }

  override func drawData(context: CGContext) {
    guard let lineData = dataProvider?.lineData else { return }

    for dataSet in lineData.dataSets {
      if dataSet.isVisible, dataSet is HasTrendline {
        drawTrendline(context: context, dataSet: dataSet as! Trendline.LineChartBestFitDataSet)
      }
    }
    super.drawData(context: context)
  }

  func drawTrendline(context: CGContext, dataSet: Trendline.LineChartBestFitDataSet) {
    guard dataSet.isVisible else { return }
    guard let chart = dataProvider as? DGCharts.LineChartView else {
      print("Cannot cast \(dataProvider) as chart")
      return
    }
    let trans = dataProvider?.getTransformer(forAxis: dataSet.axisDependency)
    guard let valueToPixelMatrix = trans?.valueToPixelMatrix else { return }
    let isDrawSteppedEnabled = dataSet.mode == .stepped
    let phaseY = animator.phaseY

    let points = dataSet.getPoints(xMin: chart.chartXMin, xMax: chart.chartXMax, viewWidth: Int(viewPortHandler.contentWidth))
    if points.isEmpty {
      return
    }

    let linePath = CGMutablePath()
    for (index, point) in points.enumerated() {
      if index == 0 {

        let startPoint =
            CGPoint(
                x: CGFloat(point.x),
                y: CGFloat(point.y * phaseY))
            .applying(valueToPixelMatrix)

        linePath.move(to: startPoint)
      } else {
        let endPoint =
            CGPoint(
                x: CGFloat(point.x),
                y: CGFloat(point.y * phaseY))
            .applying(valueToPixelMatrix)
        linePath.addLine(to: endPoint)
      }
    }

    context.beginPath()
    context.addPath(linePath)
    context.setStrokeColor(dataSet.getColor().cgColor)
    context.setLineWidth(dataSet.getLineWidth())
    if let dashPattern = dataSet.getDashPattern(), !dashPattern.isEmpty {
      context.setLineDash(phase: 0, lengths: dashPattern)
    } else {
      context.setLineDash(phase: 0, lengths: [])
    }
    context.strokePath()
  }
}
