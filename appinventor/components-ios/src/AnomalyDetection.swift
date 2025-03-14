// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import DGCharts
import Foundation
import UIKit

@objc open class AnomalyDetection: DataCollection {
  @objc public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  // Detect anomalies based on Z-score and return an array of AnomalyResult objects for Objective-C compatibility
  @objc open func DetectAnomalies(_ dataList: YailList<AnyObject>, threshold: Double) -> YailList<AnyObject> {
    let stats = calculateStatistics(dataList: dataList)
    let mean = stats.mean
    let sd = stats.stdev
    var anomalies: [[NSNumber]] = []

    for (index, value) in dataList.enumerated() {
      guard let doubleValue = AnomalyDetection.toDouble(value) else { continue }
      let zScore = abs((doubleValue - mean) / sd)
      if zScore > threshold {
        anomalies.append([(index + 1) as NSNumber, doubleValue as NSNumber])
      }
    }

    return YailList<AnyObject>(array: anomalies)
  }

  // Detect anomalies in chart data and return an array of NSArrays, representing pairs of (x, y)
  @objc func DetectAnomaliesInChartData(_ chartData: ChartData2D, _ threshold: Double) -> YailList<AnyObject> {
    let entries = chartData.getDataValue(nil)
    let yValues = entries.compactMap { ($0 as? ChartDataEntry)?.y }
    let size = Double(yValues.count)
    let mean = yValues.reduce(0, +) / size
    let sd = sqrt(yValues.reduce(0) { $0 + pow($1 - mean, 2) } / size)

    var anomalies: [[NSNumber]] = []
    for (_, value) in entries.enumerated() {
      guard let entry = (value as? ChartDataEntry) else { continue }
      let z = abs((entry.y - mean) / sd)
      if z > threshold {
        anomalies.append([entry.x as NSNumber, entry.y as NSNumber])
      }
    }
    return YailList<AnyObject>(array: anomalies)
  }

  /**
   * Given a single anomaly: [(anomaly index, anomaly value)]
   *
   * 1. Iterate over the xList and delete value at anomaly index
   * 2. Iterate over the yList and delete the value at anomaly index with the same value as anomaly
   *    value
   * 3. combine the xList and yList after modification in a list of x and y pairs
   *
   * We assume x and y lists are the same size and are ordered.
   *
   * - Parameter anomaly:
   * - Parameter xList:
   * - Parameter yList:
   * - Returns: A list of combined x and y without the anomaly pair
   */
  @objc func CleanData(_ anomaly: YailList<AnyObject>, _ xList: YailList<AnyObject>, _ yList: YailList<AnyObject>) throws -> YailList<AnyObject> {
    let index = Int(try AnomalyDetection.getAnomalyIndex(anomaly))
    guard xList.count == yList.count, index > 0, index <= xList.count else {
      throw NSError(domain: "", code: 1)
    }

    var newXList = AnomalyDetection.castToDouble(xList)
    var newYList = AnomalyDetection.castToDouble(yList)
    newXList.remove(at: index - 1)
    newYList.remove(at: index - 1)

    let cleanedData = NSMutableArray()
    for i in 0..<newXList.count {
      cleanedData.add(YailList<AnyObject>(array: [newXList[i], newYList[i]]))
    }

    return YailList<AnyObject>(array: cleanedData)
  }

  public static func getAnomalyIndex(_ anomaly: YailList<AnyObject>) throws -> Double {
    if !anomaly.isEmpty, let value = toDouble(anomaly[1]) {
      return value
    } else {
      throw NSError(domain: "Invalid anomaly construct", code: 1)
    }
  }

  private static func toDouble(_ item: Any) -> Double? {
    if let item = item as? NSNumber {
      return item.doubleValue
    } else if let item = item as? NSString, let value = Double(item as String) {
      return value
    } else {
      return nil
    }
  }

  private func calculateStatistics(dataList: YailList<AnyObject>) -> (values: [Double], mean: Double, stdev: Double) {
    let values = DataCollection.castToDouble(dataList)
    let size = Double(values.count)
    let mean = values.reduce(0, +) / size
    let variance = values.reduce(0) { $0 + pow($1 - mean, 2) } / size
    let standardDeviation = sqrt(variance)

    return (values: values, mean: mean, stdev: standardDeviation)
  }
}
