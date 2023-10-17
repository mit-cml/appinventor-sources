// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

open public class ChartView {
  unowned let _chartComponent: Chart
  private let _workQueue = DispatchQueue(label: "Chart", qos: .userInitiated)
  
  var form: Form {
    return _chartComponent.form!
  }
  var chart: Charts.ChartViewBase
  
  var data: Charts.ChartData
  
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
  
  // TODO: How would I make createChartModel in swift?
  public var createChartModel : ChartDataModel = ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>(data: Charts.ChartData, dataset: Charts.ChartDataSet, view: ChartViewBase)
  
  public func initializeDefaultSettings() {
    // Center the Legend
    chart.legend.horizontalAlignment = Legend.HorizontalAlignment.center
    chart.legend.wordWrapEnabled = true // Wrap Legend entries in case of many entries
  }
  
  /*
   Refreshes the Chart View to react to styling changes
   */
  
  // TODO: CHECK IF THIS IS RIGHT WAY TO DO CHARTS.INVALIDATE()
  public func refresh() {
    chart.reloadInputViews()
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
    chart.reloadInputViews()
  }
  
  // make RefreshTask
  private class RefreshTask {
    var _entries: Array<Charts.ChartDataEntry> = []
    var _chart: ChartView
    public init(_ entries: Array<Charts.ChartDataEntry>) {
      _entries = entries
    }
    public func doInBackGround(chartDataModels: Array<ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>>) -> ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>{
      return chartDataModels[0]
    }

    public func onPostExecute(result: ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>) {
      //Instance member '_workQueue' of type 'ChartView' cannot be used on instance of nested type 'ChartView.RefreshTask'
      _chart._workQueue.async {
        self._chart.refresh(model: result, entries: self._entries)

      }

      /*DispatchQueue.main.async() {
        refresh(model: result, entries: _entries)
      }*/
    }

  }
  

}
