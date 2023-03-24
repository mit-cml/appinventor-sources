// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public class Slider: ViewComponent, AbstractMethodsForViewComponent {
  private var _view: UISlider
  private var _minValue: Float32 = kSliderMinValue
  private var _maxValue: Float32 = kSliderMaxValue
  private var _thumbPosition: Float32 = kSliderThumbValue
  private var _leftColor: UIColor = UIColor.orange
  private var _rightColor: UIColor = UIColor.gray
  
  public override init(_ parent: ComponentContainer) {
    _view = UISlider()
    super.init(parent)
    super.setDelegate(self)
    
    setupSliderView()
    parent.add(self)
    
    ThumbPosition = kSliderThumbValue
    MinValue = kSliderMinValue
    MaxValue = kSliderMaxValue
    Width = 50
  }
  
  private func setupSliderView() {
    _view.isContinuous = true
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.minimumTrackTintColor = _leftColor
    _view.maximumTrackTintColor = _rightColor
    _view.maximumValue = 100.0
    _view.minimumValue = 0.0
    _view.isEnabled = true
    _view.addTarget(self, action: #selector(self.positionChanged(sender:)), for: .valueChanged)
  }
  
  public override var view: UIView {
    get {
      return _view
    }
  }
  
  @objc public var ThumbEnabled: Bool {
    get {
      return _view.isEnabled
    }
    set(thumbEnable) {
      _view.isEnabled = thumbEnable
    }
  }
  
  @objc public var ThumbPosition: Float32 {
    get {
      return _thumbPosition
    }
    set(position) {
      _thumbPosition = min(max(position, _minValue), _maxValue)
      setSliderPosition()
    }
  }
  
  @objc public var MinValue: Float32 {
    get {
      return _minValue
    }
    set(value) {
      _minValue = value
      _maxValue = max(value, _maxValue)
      ThumbPosition = (_maxValue + _minValue) / 2.0
    }
  }
  
  @objc public var MaxValue: Float32 {
    get {
      return _maxValue
    }
    set(value) {
      _maxValue = value
      _minValue = min(value, _minValue)
      ThumbPosition = (_minValue + _maxValue) / 2.0
    }
  }
  
  @objc public var ColorLeft: Int32 {
    get {
      return colorToArgb(_leftColor)
    }
    set(argb) {
      _leftColor = argbToColor(argb)
      _view.minimumTrackTintColor = _leftColor
    }
  }
  
  @objc public var ColorRight: Int32 {
    get {
      return colorToArgb(_rightColor)
    }
    set(argb) {
      _rightColor = argbToColor(argb)
      _view.maximumTrackTintColor = _rightColor
    }
  }
  
  // Set the slider position based on _minValue, _maxValue, and _thumbPosition
  // Slider position is a float in the range [0,100] and is determined by _minValue,
  // _maxValue and _thumbPosition
  private func setSliderPosition() {
    let thumbPosition: Float = (_thumbPosition - _minValue) / (_maxValue - _minValue) * 100.0
    thumbPosition.isNaN ? _view.setValue(50.0, animated: true) : _view.setValue(thumbPosition, animated: true)
  }
  
  @objc func positionChanged(sender: UISlider) {
    _thumbPosition = (_maxValue - _minValue) * sender.value / 100 + _minValue
    PositionChanged(_thumbPosition)
  }
  
  @objc open func PositionChanged(_ thumbPosition: Float) {
    EventDispatcher.dispatchEvent(of: self, called: "PositionChanged", arguments: thumbPosition as NSNumber)
  }
}
