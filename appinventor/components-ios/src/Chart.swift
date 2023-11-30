// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import DGCharts

@objc enum ChartType: Int32 {
  case Line = 0
  case Scatter = 1
  case Area = 2
  case Bar = 3
  case Pie = 4
}

@objc open class Chart : ViewComponent, ComponentContainer, LifecycleDelegate, AbstractMethodsForViewComponent {
  public func getChildren() -> [Component] {
    return []
  }
  

  var _view = UIView()
  var _chartView: ChartView?
  var _type = ChartType.Line
  var _description = ""
  var _backgroundColor: UIColor = argbToColor(Color.none.int32)
  var _pieRadius: Int32 = 100
  var _legendEnabled = true
  var _gridEnabled = true
  var _labels = [String]()
  var _dataComponents: Array<ChartDataBase> = []




  @objc public override init(_ parent: ComponentContainer) {
    XFromZero = false
    YFromZero = false
    super.init(parent)
    setDelegate(self)
    parent.add(self)
  }

  @objc open func Initialize() {
    self.Type = self.Type
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
    return true
  }

  open func setVisible(component: ViewComponent, to visibility: Bool) {
     
  }

  // MARK: Chart Properties

  // CHANGED THIS FROM INTO TO UICOLOR BC _backgroundColor is a UIColor
  @objc open var BackgroundColor: UIColor {
    get {
      //return colorToArgb(_backgroundColor)
      return _backgroundColor
    }
    set {
      //_backgroundColor = argbToColor(newValue)
      _chartView?.backgroundColor = _backgroundColor
    }
  }

  @objc open var Description: String {
    get {
      return _description
    }
    set {
      _description = newValue
      _chartView?.chart?.chartDescription.text = _description
    }
  }

  @objc open var GridEnabled: Bool {
    get {
      return _gridEnabled
    }
    set {
      _gridEnabled = newValue
      if let chartView = _chartView as? AxisChartView {
        chartView.setGridEnabled(enabled: newValue)
      }
      /*if let chartView = _chartView as? BarLineChartViewBase {
        chartView.xAxis.drawGridLinesEnabled = newValue
        chartView.leftAxis.drawGridLinesEnabled = newValue
      }*/
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
        return ""
    }
    set{
      Labels = newValue.split(",") as [String]
    }
  }

  @objc open var LegendEnabled: Bool {
    get {
      return _legendEnabled
    }
    set {
      _legendEnabled = newValue
      _chartView?.chart?.legend.enabled = _legendEnabled
    }
  }

  @objc open var l: Int32 {
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

  internal var `Type`: ChartType {
    get {
      return _type
    }
    set {
      let shouldReinitialize = _chartView != nil
      let newChartView = createChartView(for: newValue)
      _chartView?.chart?.removeFromSuperview()
      _type = newValue
      _chartView = newChartView

      view.addSubview(newChartView.chart!)
      NSLayoutConstraint.activate([
        view.topAnchor.constraint(equalTo: newChartView.chart!.topAnchor),
        view.leadingAnchor.constraint(equalTo: newChartView.chart!.leadingAnchor),
        view.widthAnchor.constraint(equalTo: newChartView.chart!.widthAnchor),
        view.heightAnchor.constraint(equalTo: newChartView.chart!.heightAnchor)
      ])
      if let newChartSubview = newChartView as? ChartView {
        _view.insertSubview(newChartSubview.getView(), at: 0)
      }
      
      if shouldReinitialize {
        reinitializeChart()
      }
    }
  }

  @objc open var XFromZero: Bool {
    didSet {
      if let chartView = (_chartView as AnyObject) as? AxisChartView {
        chartView.setXMinimum(zero: XFromZero)
      }
    }
    
  }
  
  @objc open var YFromZero: Bool {
    didSet {
      if let chartView = (_chartView as AnyObject) as? AxisChartView {
        chartView.setYMinimum(zero: YFromZero)
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
    _chartView?.chart?.data?.notifyDataChanged()
    _chartView?.chart?.notifyDataSetChanged()
  }

  private func createChartView(for type: ChartType) -> ChartView {
    switch type {
    case .Line:
      return LineChartView(self) as ChartView
    case .Scatter:
      //return ScatterChartView(frame: .zero)
      fatalError("whoops")
    case .Area:
      return (LineChartView(self) as AnyObject) as! ChartView
    case .Bar:
      //return BarChartView(frame: .zero)
      fatalError("whoops")
    case .Pie:
      //return PieChartView(frame: .zero)
      fatalError("whoops")
    }
  }

  private func reinitializeChart() {
    for dataComponent in _dataComponents {
      // must implement initChartData() first
    }
    Description = _description
    BackgroundColor = _backgroundColor
  }

  func addDataComponent(_ dataComponent: ChartDataBase) {
    _dataComponents.append(dataComponent)
  }


}
