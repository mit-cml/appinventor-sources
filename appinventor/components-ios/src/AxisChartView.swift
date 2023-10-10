// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

/**
 * Base class for Chart Views (Chart UI) for Charts types that
 * have an axis.
 */
open class AxisChartView : ChartView{
  
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

  override initializeDefaultSettings() {
    
  }
  
}
