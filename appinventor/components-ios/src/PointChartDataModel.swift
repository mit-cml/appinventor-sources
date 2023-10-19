// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

open class PointChartDataModel: ChartData2DModel {
  // <E: Charts.ChartDataEntry, D: BarLineScatterCandleBubbleChartData, V: ChartViewBase>
  // TODO: shouldn't this be a ChartView, not a ChartViewBase? ChartViewBase does not have form has a var
  init(data: BarLineScatterCandleBubbleChartData, view: PointChartView) {
    super.init(data: data, view: view)
  }
  public func getEntryFromTuple(tuple: YailList<AnyObject>) -> ChartDataEntry {
    // TODO: NEED TO HANDLE POSSIBLE ERRORS
    do {
      var xValue = try tuple[0] as! String
      var yValue = try tuple[1] as! String
      var x: Double = Double(xValue) ?? -1
      var y: Double = Double(yValue) ?? -1
      return ChartDataEntry(x: x, y: y)
      // TODO: change ERROR MESSAGE, do I need to add error messages
      view.form?.form?.dispatchErrorOccurredEvent(chart, "GetEntryFromTuple", ErrorMessage.ERROR_ACTIVITY_STARTER_NO_ACTION_INFO, xValue, yValue)

    } catch {
    }
    
  }
  
}
