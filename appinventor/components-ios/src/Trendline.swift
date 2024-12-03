//
//  Trendline.swift
//  AIComponentKit
//
//  Created by David Kim on 3/19/24.
//  Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation
import DGCharts
import Network
import UIKit

@objc public class BestFitModel: NSObject, OptionList {
  @objc public static let Linear = BestFitModel(0)
  @objc public static let Quadratic = BestFitModel(1)
  // @objc public static let Cubic = BestFitModel("Cubic")
  @objc public static let Exponential = BestFitModel(2)
  @objc public static let Logarithmic = BestFitModel(3)

  private static let LOOKUP: [Int32: BestFitModel] = [
    0: .Linear,
    1: .Quadratic,
    // "Cubic": .Cubic,
    2: .Exponential,
    3: .Logarithmic
  ]

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc public class func fromUnderlyingValue(_ value: Int32) -> BestFitModel? {
    return LOOKUP[value]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }
}

@objc public class StrokeStyle: NSObject {
  @objc public static let Solid = StrokeStyle(1)
  @objc public static let Dashed = StrokeStyle(2)
  @objc public static let Dotted = StrokeStyle(3)

  private static let lookup: [Int: StrokeStyle] = [
    1: Solid,
    2: Dashed,
    3: Dotted
  ]

  let value: Int

  @objc private init(_ value: Int) {
    self.value = value
  }

  @objc public class func fromUnderlyingValue(_ value: Int) -> StrokeStyle? {
    return lookup[value]
  }

  //TODO: how can we resolve this?
  //  @objc public class func fromUnderlyingValue(_ value: String) -> StrokeStyle? {
  //    if let intValue = Int(value) {
  //      return fromUnderlyingValue(intValue)
  //    }
  //    return nil
  //  }

  @objc public func toUnderlyingValue() -> Int {
    return value
  }
}

@objc public class Trendline: NSObject, ChartComponent, DataSourceChangeListener {
  public var dispatchDelegate: HandlesEventDispatching? {
    return _container.dispatchDelegate
  }

  public func copy(with zone: NSZone? = nil) -> Any {
    return self
  }

  static let debug = false

  private var _dashed: [CGFloat]?
  private var _dotted: [CGFloat]?
  private var _container: Chart
  private var _chartData: ChartData2D?
  private var _color: Int32 = AIComponentKit.Color.black.int32
  private var _extend: Bool = true
  private var _model = BestFitModel.Linear
  private var _strokeWidth: CGFloat = 1.0
  private var _strokeStyle = StrokeStyle.Solid
  private var _visible: Bool = true
  private var regression: LinearRegression = LinearRegression()
  private var quadraticRegression: QuadraticRegression = QuadraticRegression()
  private var exponentialRegression: ExponentialRegression = ExponentialRegression()
  private var logarithmicRegression: LogarithmicRegression = LogarithmicRegression()
  private var currentModel: TrendlineCalculator?
  private var lastResults: [String: Any] = [:]
  private var initialized: Bool = false
  private var dataModel: ChartDataModel?
  private var minX: Double = Double.infinity
  private var maxX: Double = -Double.infinity
  private var _density: CGFloat

  @objc public init(_ chartContainer: Chart) {
    self._container = chartContainer
    self._density = UIScreen.main.scale
    super.init()
    self._container.addDataComponent(self)
    self._dashed = createDashedPattern(density: _density)
    self._dotted = createDottedPattern(density: _density)
    self.currentModel = regression
  }

  @objc func Initialize() {
    initialized = true
    if dataModel == nil {
      initChartData()
    }
  }

  private func createDashedPattern(density: CGFloat) -> [CGFloat] {
    let dashLength = 10 * density
    let dashGap = 10 * density
    return [dashLength, dashGap]
  }

  private func createDottedPattern(density: CGFloat) -> [CGFloat] {
    let dotLength = 2 * density
    let dotGap = 10 * density
    return [dotLength, dotGap]
  }

  func draw(context: CGContext) {
    switch _strokeStyle {
    case .Dashed:
      context.setLineDash(phase: 0, lengths: _dashed ?? [])
    case .Dotted:
      context.setLineDash(phase: 0, lengths: _dotted ?? [])
    case .Solid:
      context.setLineDash(phase: 0, lengths: [])
    default:
      break
    }
  }

  func onDataSourceValueChange(_ component: DataSource, _ key: String?, _ newValue: AnyObject?) {
    lastResults.removeAll()

    guard let chartDataEntries = _chartData?.chartDataModel?._entries as? [DGCharts.ChartDataEntry] else {
      print("No entries in the data source")
      return
    }

    var x: [Double] = []
    var y: [Double] = []

    for entry in chartDataEntries {
      let xValue = entry.x
      let yValue = entry.y

      x.append(xValue)
      y.append(yValue)
    }


    guard !x.isEmpty, x.count >= 2, x.count == y.count else {
      print("Not enough entries in the data source or unequal X and Y data points")
      return
    }

    if let currentModel = currentModel {
      let computedResults = currentModel.compute(x: x, y: y)

      // Now you can use computedResults directly
      // For example, updating the state or refreshing the UI based on these results
      lastResults = computedResults

      if initialized {
        DispatchQueue.main.async {
          self._container.chartView?.refresh()
        }
      }
    } else {
      print("Error: Current model is not set")
    }
  }


  func onReceiveValue(_ component: RealTimeDataSource, _ key: String?, _ value: AnyObject?) {

  }

  @objc public var ChartData: ChartData2D {
    get {
      return _chartData!
    }
    set {
      if _chartData != nil {
        _chartData?.removeDataSourceChangeListener(self)
      }

      // Update chart data with the new value
      _chartData = newValue

      // Add this object as a listener to the new chart data if it is not nil
      if _chartData != nil {
        _chartData?.addDataSourceChangeListener(self)
      }
    }
  }

  @objc open var Color: Int32 {
    get {
      return self._color
    }
    set {
      self._color = newValue
      if initialized {
        _container.refresh()  // Implement refresh in your ChartView
      }
    }
  }

  @objc open var correlationCoefficient: Any {
    return lastResults["correlation coefficient"] ?? Double.nan
  }

  @objc open var exponentialBase: Any {
    return lastResults["b"] ?? Double.nan
  }

  @objc open var exponentialCoefficient: Any {
    return lastResults["a"] ?? Double.nan
  }

  @objc open var extend: Bool {
    get {
      return _extend
    }
    set {
      _extend = newValue
      if initialized {
        _container.refresh()
      }
    }
  }

  @objc open var linearCoefficient: Any {
    return lastResults["slope"] ?? Double.nan
  }

  @objc open var logarithmCoefficient: Any {
    return lastResults["b"] ?? Double.nan
  }

  @objc open var logarithmConstant: Any {
    return lastResults["a"] ?? Double.nan
  }

  // Example of converting result retrieval to Swift
  func getResult(for key: String) -> Any {
    lastResults[key] ?? Double.nan
  }

  @objc public func setModel(_ newModel: BestFitModel) {
    switch newModel {
    case .Linear:
      currentModel = LinearRegression()
    case .Quadratic:
      currentModel = QuadraticRegression()
    case .Exponential:
      currentModel = ExponentialRegression()
    case .Logarithmic:
      currentModel = LogarithmicRegression()
    default:
      print("Unknown model: \(String(describing: newModel))")
    }

    if initialized {
      _container.refresh()
    }
  }

  @objc open var predictions: [Double] {
    lastResults["predictions"] as? [Double] ?? []
  }

  @objc open var quadraticCoefficient: Any {
    (lastResults["x^2"]) ?? 0.0
  }

  @objc open var results: [String: Any] {
    lastResults
  }

  @objc open var rSquared: Any {
    (lastResults["r^2"]) ?? Double.nan
  }

  @objc open var strokeStyle: StrokeStyle {
    get {
      return _strokeStyle
    }
    set {
      _strokeStyle = newValue
      if initialized {
        _container.refresh()
      }
    }
  }

  @objc open var strokeWidth: CGFloat {
    get {
      return _strokeWidth
    }
    set {
      _strokeWidth = newValue
      if initialized {
        _container.refresh()
      }
    }
  }

  @objc open var Visible: Bool {
    get { return _visible }
    set {
      _visible = newValue
      if initialized {
        refreshContainer()
      }
    }
  }

  @objc open var XIntercepts: Any {
    if let intercepts = lastResults["Xintercepts"] {
      return intercepts
    }
    return []
  }

  @objc open var YIntercept: Any {
    if let intercept = lastResults["Yintercept"] {
      return intercept
    } else if let intercept = lastResults["intercept"] {
      return intercept
    }
    return Double.nan
  }

  func disconnectFromChartData() {
    // chartData?.removeDataSourceChangeListener(self)
    //lastResults.clear()
    lastResults.removeAll()
    refreshContainer()
  }

  func getResultValue(for key: String) -> Any? {
    return lastResults[key] ?? Double.nan
  }

  func updated(with results: [String: Any]) {
    // Dispatch updated event with results
  }

  @objc public func initChartData() {
    print("initChartData view is \(String(describing: _container))")
    if _container.chartView is ScatterChartView {
      dataModel = ScatterChartBestFitModel(container: _container, trendline: self)
    } else if _container.chartView is PointChartView {
      dataModel = LineChartBestFitModel(container: _container, trendline: self)
    }
  }

  func getDashPathEffect() {
    // Assuming this method is supposed to update the stroke style
    // This might need to be adjusted based on how StrokeStyle is used in your app
    switch _strokeStyle {
    case .Dashed:
      _strokeStyle = .Dashed
    case .Dotted:
      _strokeStyle = .Dotted
    case .Solid:
      _strokeStyle = .Solid
    default:
      break
    }
  }

  private func refreshContainer() {
    // Refresh the container, e.g., invalidate the chart view if visible
    if _visible {
      _container.refresh() // Implement this method to update the chart view
    }
  }

  private func convertToDoubleDictionary(from dictionary: [String: Any]) -> [String: Double] {
    var doubleDictionary = [String: Double]()
    for (key, value) in dictionary {
      if let doubleValue = value as? Double {
        doubleDictionary[key] = doubleValue
      }
      // Optionally handle string conversion to double
      else if let stringValue = value as? String, let doubleValue = Double(stringValue) {
        doubleDictionary[key] = doubleValue
      }
    }
    return doubleDictionary
  }

  // Assuming a method to compute points for the trendline
  private func getPoints(xMin: CGFloat, xMax: CGFloat, viewWidth: Int) -> [CGPoint] {
    guard initialized, let currentModel = currentModel else {
      return []
    }

    let strokeStep: Int
    switch strokeStyle {
    case .Dashed:
      strokeStep = 20
    case .Dotted:
      strokeStep = 12
    case .Solid:
      strokeStep = 1
    default:
      strokeStep = 1
    }

    let steps = Int(ceil(Double(viewWidth) / Double(strokeStep)))

    let temp = convertToDoubleDictionary(from: lastResults)

    let results = currentModel.computePoints(results: temp, xMin: Double(xMin), xMax: Double(xMax), viewWidth: viewWidth, steps: steps)

    return results
  }

  private func resultOrNan(_ value: Double?) -> Double {
    return value ?? Double.nan
  }

  private func resultOrZero(_ value: Double?) -> Double {
    return value ?? 0.0
  }

  class ScatterChartBestFitDataSet: ScatterChartDataSet, HasTrendline {

    unowned var trendline: Trendline

    init(trendline: Trendline) {
      self.trendline = trendline
      super.init(entries: [], label: "Best Fit")
    }

    required init() {
      fatalError("init() has not been implemented")
    }

    func getPoints(xMin: CGFloat, xMax: CGFloat, viewWidth: Int) -> [CGPoint] {
      return trendline.getPoints(xMin: xMin, xMax: xMax, viewWidth: viewWidth)
    }

    func getColor() -> UIColor {
      return argbToColor(trendline.Color)
    }

    func getDashPattern() -> [CGFloat]? {
      switch trendline.strokeStyle {
      case .Dashed:
        return trendline._dashed
      case .Dotted:
        return trendline._dotted
      case .Solid:
        return nil
      default:
        return nil// Solid lines have no dash pattern
      }
    }

    func getLineWidth() -> CGFloat {
      return trendline.strokeWidth * CGFloat(trendline._density)
    }

    func isVisible() -> Bool {
      return trendline._visible
    }

    override var description: String {
      if let chartData = trendline._chartData {
        return (chartData.chartDataModel?.dataset!.description)!
      } else {
        return super.description
      }
    }
  }

  class ScatterChartBestFitModel: ScatterChartDataModel {
    unowned var container: Chart
    unowned var trendline: Trendline

    init(container: Chart, trendline: Trendline) {
      self.container = container
      self.trendline = trendline

      guard let chartView = container.chartView as? ScatterChartView,
            let data = chartView.data as? ScatterChartData else {
        fatalError("Initialization failed due to incorrect chart type or data")
      }

      let dataSet = ScatterChartBestFitDataSet(trendline: trendline)

      super.init(data: data, view: chartView, dataset: dataSet)
    }
  }

  class LineChartBestFitDataSet: LineChartDataSet, HasTrendline {

    unowned var trendline: Trendline

    init(trendline: Trendline) {
      self.trendline = trendline
      super.init(entries: [], label: "Best Fit")
    }
    
    required init() {
      fatalError("init() has not been implemented")
    }
    
    func getPoints(xMin: CGFloat, xMax: CGFloat, viewWidth: Int) -> [CGPoint] {
      return trendline.getPoints(xMin: xMin, xMax: xMax, viewWidth: viewWidth)
    }

    func getColor() -> UIColor {
      return argbToColor(trendline.Color)
    }

    func getDashPattern() -> [CGFloat]? {
      switch trendline.strokeStyle {
      case .Dashed:
        return trendline._dashed
      case .Dotted:
        return trendline._dotted
      case .Solid:
        return nil // Solid lines have no dash pattern
      default:
        return nil
      }
    }

    func getLineWidth() -> CGFloat {
      return trendline.strokeWidth * CGFloat(trendline._density)
    }

    func isVisible() -> Bool {
      return trendline._visible
    }

    override var description: String {
      if let chartData = trendline._chartData {
        return chartData.chartDataModel?.dataset?.description ?? super.description
      } else {
        return super.description
      }
    }
  }

  class LineChartBestFitModel: LineChartDataModel {
    unowned var container: Chart
    unowned var trendline: Trendline

    init(container: Chart, trendline: Trendline) {
      self.container = container
      self.trendline = trendline

      guard let chartView = container.chartView as? LineChartView,
            let data = chartView.data as? LineChartData else {
        fatalError("Initialization failed due to incorrect chart type or data")
      }

      let dataSet = LineChartBestFitDataSet(trendline: trendline)

      super.init(data: data, view: chartView, dataset: dataSet)
    }
  }

}
