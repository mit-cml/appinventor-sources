// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import DGCharts

@objc class ChartData2D: ChartDataBase {
  
  override init(_ chartContainer: Chart) {
    super.init(chartContainer)
    dataFileColumns = [" ", " "]
    sheetColumns = [" ", " "]
    webColumns = [" ", " "]
  }
  
  @objc func AddEntry(_ x: String, _ y: String){
    // create a 2-tuple, and add the tuple to the Data series
    DispatchQueue.main.async {
      let pair: YailList<AnyObject> = [x, y]
      self._chartDataModel?.addEntryFromTuple(pair)
      // refresh chart with new data
      self.refreshChart()
    }
  }
  
  @objc func RemoveEntry(_ x: String, _ y: String){
    // create a 2-tuple, and remove the tuple from the Data series
    DispatchQueue.main.async {
      let pair: YailList<AnyObject> = [x, y]
      self._chartDataModel?.removeEntryFromTuple(pair)
      // refresh chart with new data
      self.refreshChart()
    }
  }
  
  // checks whether an (x, y) entry exists in the Coordinate Data. Returns true if the Entry exists, and false otherwise.
  @objc func DoesEntryExist(_ x: String, _ y: String) -> Bool{
    /* Original:
     DispatchQueue.main.sync {
      var pair: YailList<AnyObject> = [x, y]
      return self._chartDataModel!.doesEntryExist(pair) // TODO: is the ! okay?
    }*/
    
    let group = DispatchGroup()
    group.enter()
    var holder: Bool = false // holder variable for returned value of doesEntryExist()
    // avoid deadlocks by not using .main queue here
    DispatchQueue.global(qos: .default).async {
      var pair: YailList<AnyObject> = [x, y]
      holder =  self._chartDataModel!.doesEntryExist(pair)
      print("holder", holder)
      group.leave()
    }
    group.wait()
    return holder
  }
  
  // Highlights data points of choice on the Chart in the color of choice. This block expects a list of data points, each data pointis an index, value pair
  @objc func HighlightDataPoints(_ dataPoints: YailList<AnyObject>, _ color: Int) {
    let dataPointsList: Array<AnyObject> = dataPoints as! Array
    print("dataPointsList", dataPointsList)
    if !dataPoints.isEmpty {
      let entries = _chartDataModel?.entries
      print("entries", entries)
      print("entries count", entries?.count)
      
      // convert _colors to Int
      var colorsInt: Array<Int> = []
      for color in _colors {
        colorsInt.append(color as! Int)
      }
      
      var highlights: Array<Int> = [Int](repeating: 0, count: entries!.count)
      // if any colors are present, add them to highlights
      for index in colorsInt.indices {
        highlights[index] = colorsInt[index]
        print("highlights[index]", highlights[index])
      }
      print("highlights", highlights)
      for dataPoint in dataPointsList {
        if let dataPoint = dataPoint as? YailList<AnyObject> {
          print("dataPoint", dataPoint)
          print("type dataPoint", type(of: dataPoint[0]))
          var dataPointInt: Array<Int> = []
          for point in dataPoint {
            dataPointInt.append(point as! Int)
          }

          let dataPointIndex: Int = dataPointInt[0]  // anomaly detection replacement
          print("dataPointIndex", dataPointIndex)
          print("dataPointIndex-1", dataPointIndex-1)
          print("dataPointIndextype", type(of: dataPointIndex))
          print("highlights.count", highlights.count)
          print("highlights2", highlights)

          if dataPointIndex >= highlights.count {
            continue
          }
          highlights[dataPointIndex - 1] = color // TODO: WHY IS THIS ALWAYS OUT OF RANGE?
          print(highlights[dataPointIndex - 1])
        }
      }
      
      let lineDataSet: LineChartDataSet = _chartDataModel?.dataset as! LineChartDataSet
      var highlightsUI: Array<NSUIColor> = []
      for highlight in highlights {
        highlightsUI.append(argbToColor(Int32(highlight)))
      }
      lineDataSet.circleColors = highlightsUI
      onDataChange()
    }
  }
}
