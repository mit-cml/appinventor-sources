// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

open class PointChartDataModel: Chart2DDataModel {
  init(data: DGCharts.ChartData, view: PointChartView) {
    super.init(data: data, view: view)
  }
  public override func getEntryFromTuple(_ tuple: YailList<AnyObject>) -> ChartDataEntry? {
    //    // TODO: NEED TO HANDLE POSSIBLE ERRORS
    //    do {
    //      //var xValue = try tuple[0] as! String
    //      //var yValue = try tuple[1] as! String
    //
    //      // the getString() function in the Java code automatically adds a +1 to index
    //      let xValue: String = "\(tuple[0+1])"
    //      let yValue: String = "\(tuple[1+1])"
    //
    //      let x: Double = Double(xValue) ?? -1
    //      let y: Double = Double(yValue) ?? -1
    //      return ChartDataEntry(x: x, y: y)
    //    } catch {
    //
    //    }
    guard tuple.count >= 2,
          let rawX = tuple[0+1] as? String,
          let rawY = tuple[1+1] as? String else {
      // Handle error for insufficient chart entry values or type mismatch
      print("Error: Insufficient chart entry values")
      return nil
    }

    if let x = Double(rawX), let y = Double(rawY) {
      // Successfully parsed x and y values
      return ChartDataEntry(x: x, y: y)
    } else {
      // Dispatch an error event similar to the Java version or handle it accordingly.
      // Error for invalid chart entry values.
      return nil
    }
  }

}
