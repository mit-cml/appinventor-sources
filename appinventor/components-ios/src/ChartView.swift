// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

open class ChartView<D: Charts.ChartViewBase> {
  unowned let _chartComponent: Chart
  private let _workQueue = DispatchQueue(label: "Chart", qos: .userInitiated)
  
  var form: Form {
    return _chartComponent.form!
  }
  var chart: D
  
  var data: Charts.ChartData
  
  // TODO: do I need to make a uiHandler?
  // var uiHandler: Handler
  

  public init(_ chartComponent: Chart) {
    _chartComponent = chartComponent
  }

  public var backgroundColor: UIColor? {
    get {
      return chart.backgroundColor
    }
    set {
      chart.backgroundColor = newValue
    }
  }
  
  public var cDescription: String? {
    get {
      return chart.chartDescription.text
    }
    set {
      chart.chartDescription.text = newValue
    }
  }
  
  public var legendEnabled: Bool? {
    get {
      return chart.legend.enabled
    }
    set {
      chart.legend.enabled = newValue!
    }
  }
  
  // TODO: How would I make createChartModel abstract function in swift? Is this right?
  
  public func createChartModel() -> ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase> {
    preconditionFailure("This method must be overridden")

  }
  
  public func initializeDefaultSettings() {
    // Center the Legend
    chart.legend.horizontalAlignment = Legend.HorizontalAlignment.center
    chart.legend.wordWrapEnabled = true // Wrap Legend entries in case of many entries
  }
  
  /*
   Refreshes the Chart View to react to styling changes
   */
  
  // TODO: CHECK WHICH THIS IS RIGHT WAY TO DO CHARTS.INVALIDATE()
  public func refresh() {
    _workQueue.async {
      self.chart.notifyDataSetChanged()
    }
    // chart.reloadInputViews()
  }
  
  
  public func refresh(model: ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>) {
    var refreshTask: RefreshTask = RefreshTask(model.entries)
    // TODO: how to execute with Asynctask?
    
    // how to execute the refreshTask with the chartDataModel argument
    refreshTask.onPostExecute(result: model)
  }
  
  public func refresh(model: ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>, entries: Array<Charts.ChartDataEntry>) {
    var dataset : ChartDataSet = model.dataset
    dataset.replaceEntries(entries)
    chart.data?.notifyDataChanged()
    chart.notifyDataSetChanged()
    //chart.reloadInputViews()
  }
  
  // make RefreshTask
  private class RefreshTask {
    var _entries: Array<Charts.ChartDataEntry> = []
    unowned var _chartView: ChartView
    public init(_ owner: ChartView, _ entries: Array<Charts.ChartDataEntry>) {
      _entries = entries
      _chartView = owner
    }
    public func doInBackGround(chartDataModels: Array<ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>>) -> ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>{
      return chartDataModels[0]
    }

    public func onPostExecute(result: ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>) {

      // TODO: is it workqueue or dispatchqueue.main.async
      _chartView._workQueue.async {
        self._chartView.refresh(model: result, entries: self._entries)

      }

      /*DispatchQueue.main.async() {
        self._chartView.refresh(model: result, entries: self._entries)
      }*/
    }

  }
  

}
