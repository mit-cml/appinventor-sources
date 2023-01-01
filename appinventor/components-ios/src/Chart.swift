// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import Charts

@objc enum ChartType: Int32 {
  case Line = 0
  case Scatter = 1
  case Area = 2
  case Bar = 3
  case Pie = 4
}

@objc open class Chart : ViewComponent, ComponentContainer, LifecycleDelegate, AbstractMethodsForViewComponent {

  var _view = UIView()
  var _chartView: ChartView?
  var _type = ChartType.Line
  var _description = ""
  var _backgroundColor: UIColor = argbToColor(Color.none.int32)
  var _pieRadius: Int32 = 100
  var _legendEnabled = true
  var _gridEnabled = true
  var _labels = [String]()

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    setDelegate(self)
  }

  @objc open override var view: UIView {
    return _view
  }

  open var container: ComponentContainer? {
    return _container
  }

  open func add(_ component: ViewComponent) {
    // Not implemented
  }

  open func setChildWidth(of component: ViewComponent, to width: Int32) {
    // Not implemented
  }

  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    // Not implemented
  }

  open func isVisible(component: ViewComponent) -> Bool {
    <#code#>
  }

  open func setVisible(component: ViewComponent, to visibility: Bool) {
    <#code#>
  }

  // MARK: Chart Properties

  @objc open var BackgroundColor: Int32 {
    get {
      return colorToArgb(_backgroundColor)
    }
    set {
      _backgroundColor = argbToColor(newValue)
      _chartView?.backgroundColor = _backgroundColor
    }
  }

  @objc open var Description: String {
    get {
      return _description
    }
    set {
      _description = newValue
      _chartView?.chartDescription?.text = _description
    }
  }

  @objc open var GridEnabled: Bool {
    get {
      return _gridEnabled
    }
    set {
      _gridEnabled = newValue
      if let chartView = _chartView as? BarLineChartViewBase {
        chartView.xAxis.drawGridLinesEnabled = newValue
        chartView.leftAxis.drawGridLinesEnabled = newValue
      }
    }
  }

  @objc open var Labels: [String] {
    get {
      return _labels
    }
    set {
      _labels = newValue
    }
  }

  @objc open var LabelsFromString: String {
    get {

    }
    set {
      Labels = newValue.split(",") as [String]
    }
  }

  @objc open var LegendEnabled: Bool {
    get {
      return _legendEnabled
    }
    set {
      _legendEnabled = newValue
      _chartView?.legend.enabled = _legendEnabled
    }
  }

  @objc open var PieRadius: Int32 {
    get {
      return _pieRadius
    }
    set {
      _pieRadius = newValue
      if let chartView = _chartView as? PieChartView {
        chartView.holeRadiusPercent = CGFloat(newValue) / 100.0
      }
    }
  }

  @objc open var `Type`: ChartType {
    get {
      return _type
    }
    set {
      let shouldReinitialize = _chartView != nil
      let newChartView = createChartView(for: newValue)
      _chartView?.removeFromSuperview()
      _type = newValue
      _chartView = newChartView
      _view.insertSubview(newChartView, at: 0)
      if shouldReinitialize {
        reinitializeChart()
      }
    }
  }

  // MARK: Chart events

  @objc open func EntryClick(_ series: Component, _ x: AnyObject, _ y: Double) {
    EventDispatcher.dispatchEvent(of: self, called: "EntryClick",
                                  arguments: series, x, y as AnyObject)
  }

  // MARK: Private Implementation

  public var chartView: ChartView? {
    return _chartView
  }

  private func createChartModel() -> ChartDataModel? {
    return _chartView?.createChartModel()
  }

  public func refresh() {
    _chartView?.refresh()
  }

  private func createChartView(for type: ChartType) -> ChartView {
    switch type {
    case .Line:
      return LineChartView(self)
    case .Scatter:
      return ScatterChartView(self)
    case .Area:
      return AreaChartView(self)
    case .Bar:
      return BarChartView(self)
    case .Pie:
      return PieChartView(self)
    }
  }

  private func reinitializeChart() {

  }

}
