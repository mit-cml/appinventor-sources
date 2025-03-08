// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
          let x = PointChartDataModel.asDouble(tuple[0+1]),
          let y = PointChartDataModel.asDouble(tuple[1+1]) else {
      // Handle error for insufficient chart entry values or type mismatch
      print("Error: Insufficient chart entry values")
      return nil
    }

    return ChartDataEntry(x: x, y: y)
  }

  /**
   * Converts the argument into a Double if possible, otherwise returns nil.
   *
   * - Parameter o: Any item, possibly nil
   * - Returns: The Double representation of o if o can be represented as a valid Double.
   */
  public static func asDouble(_ o: Any?) -> Double? {
    if let value = o as? Double {
      return value
    } else if let value = o as? String {
      return Double(value)
    } else {
      return nil
    }
  }
}
