// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

/**
 * Base class for Chart Views (Chart UI) for Charts types that
 * have an axis.
 */
open class AxisChartView : ChartView<Charts.BarLineChartViewBase> {

  // List containing Strings to use for the X Axis of the Axis Chart.
  // The first entry corresponds to an x value of 0, the second to
  // an x value of 1, and so on.
  var axisLabels: Array<String> = []
  
  /**
   * Creates a new Axis Chart View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent Chart component to link View to
   */
  override init(_ chartComponent: Chart) {
    super.init(_chartComponent)
  }


  public override func initializeDefaultSettings() {
    super.initializeDefaultSettings()
    
    var chartView: ChartView = ChartView(_chartComponent)

    chart.xAxis.labelPosition = XAxis.LabelPosition.bottom // Position X axis to the bottom
    chart.rightAxis.enabled = false // Disable right Y axis so there's only one
    
    // set the granularities both for the X and the Y axis to 1
    chart.xAxis.granularity = 1
    chart.leftAxis.granularity = 1
    
    chart.xAxis.valueFormatter = AppInventorValueFormatter(chart, axisLabels) as! any AxisValueFormatter
    if _chartComponent.XFromZero {
      chart.xAxis.axisMinimum = 0

    }
    if _chartComponent.YFromZero {
      chart.leftAxis.granularity = 1
    }
  }
  
  // sets whether the X origin should be fixed to 0
  public func setXMinimum (zero: Bool) {
    if zero {
      chart.xAxis.axisMinimum = 0
    } else {
      chart.xAxis.resetCustomAxisMax()
    }
  }
  
  public func setYMinimum (zero: Bool) {
    if zero {
      chart.leftAxis.axisMinimum = 0
    } else {
      chart.leftAxis.resetCustomAxisMax()
    }
  }
  
  public func setXBounds (minimum: Double, maximum: Double) {
    chart.xAxis.axisMinimum = minimum
    chart.xAxis.axisMaximum = maximum
  }
  
  public func setYBounds (minimum: Double, maximum: Double) {
    chart.leftAxis.axisMinimum = minimum
    chart.leftAxis.axisMaximum = maximum
  }
  
  public func setGridEnabled(enabled: Bool) {
    chart.xAxis.enabled = enabled
    chart.leftAxis.enabled = enabled
  }
  
  public func setLabels(labels: Array<String>) {
    self.axisLabels = labels
  }
  
  public class AppInventorValueFormatter : ValueFormatter {
    unowned var _chartView: BarLineChartViewBase
    var _axisLabels: Array<String>
    public init(_ owner: BarLineChartViewBase, _ axisLabels: Array<String>) {
      _chartView = owner
      _axisLabels = axisLabels
    }
    
    public func stringForValue(_ value: Double, entry: Charts.ChartDataEntry, dataSetIndex: Int, viewPortHandler: Charts.ViewPortHandler?) -> String {
      var integerValue: Int = Int(value.rounded())
      
      integerValue -= Int(_chartView.xAxis.axisMinimum)
      
      if integerValue >= 0 && integerValue < _axisLabels.count {
        return _axisLabels[integerValue]
      } else {
        return String(value)
      }
    }
    
    /*func getFormattedValue(value: Float) -> (String) {
      var integerValue: Int = Int(value.rounded())
      
      integerValue -= Int(chart.xAxis.axisMinimum)
      
      if integerValue >= 0 && integerValue < axisLabels.count {
        return axisLabels[integerValue]
      } else {
        return String(value)
      }
    }*/
    
  }
}
