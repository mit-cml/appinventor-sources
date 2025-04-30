// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public class Slider: ViewComponent, AbstractMethodsForViewComponent {
  private var _view: UISlider
  private var _minValue: Float32 = kSliderMinValue
  private var _maxValue: Float32 = kSliderMaxValue
  private var _numberOfStepsInt: Int32 = 100
  private var _numberOfSteps: Float32 = 100.0
  private var _notice: Bool = true
  private var _thumbPosition: Float32 = kSliderThumbValue
  private var _leftColor: UIColor = UIColor.orange
  private var _rightColor: UIColor = UIColor.gray
  private var _thumbColor: UIColor = UIColor.darkGray
  
  public override init(_ parent: ComponentContainer) {
    _view = UISlider()
    super.init(parent)
    super.setDelegate(self)
    
    setupSliderView()
    parent.add(self)
    
    ThumbPosition = kSliderThumbValue
    MinValue = kSliderMinValue
    MaxValue = kSliderMaxValue
    NumberOfSteps = _numberOfStepsInt;
    Width = 50
  }
  
  private func setupSliderView() {
    _view.isContinuous = true
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.minimumTrackTintColor = _leftColor
    _view.maximumTrackTintColor = _rightColor
    _view.thumbTintColor = _thumbColor
    _view.maximumValue = _numberOfSteps
    _view.minimumValue = 0.0
    _view.isEnabled = true
    _view.addTarget(self, action: #selector(self.positionChanged(sender:)), for: .valueChanged)
    _view.addTarget(self, action: #selector(self.handleTouchDown), for: .touchDown)
    _view.addTarget(self, action: #selector(self.handleTouchUp), for: [.touchUpInside, .touchUpOutside, .touchCancel])
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
      _thumbPosition = ((_maxValue - _minValue) * _view.value / _numberOfSteps) + _minValue;
    }
  }
  
  @objc public var MaxValue: Float32 {
    get {
      return _maxValue
    }
    set(value) {
      _maxValue = value
      _minValue = min(value, _minValue)
      _thumbPosition = ((_maxValue - _minValue) * _view.value / _numberOfSteps) + _minValue;
    }
  }

  @objc public var NumberOfSteps: Int32 {
    get {
      return Int32(_numberOfStepsInt)
    }
    set(value) {
      _numberOfStepsInt = value
      _numberOfSteps = Float(value)
      let oldPosition: Float = _thumbPosition
      // We set the notice flag to false so that the user is not informed in any way about the change of this property
      _notice = false
      _view.maximumValue = _numberOfSteps
      // restore the original position
      _thumbPosition = oldPosition
      setSliderPosition()
      _notice = true;
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

  @objc public var ThumbColor: Int32 {
    get {
      return colorToArgb(_thumbColor)
    }
    set(argb) {
      _thumbColor = argbToColor(argb)
      _view.thumbTintColor = _thumbColor
    }
  }
  
  // Set the slider position based on _minValue, _maxValue, and _thumbPosition
  // Slider position is a float in the range [0,_numberOfSteps] and is determined by _minValue,
  // _maxValue and _thumbPosition
  private func setSliderPosition() {
    let thumbPosition: Float = (_thumbPosition - _minValue) / (_maxValue - _minValue) * _numberOfSteps
    thumbPosition.isNaN ? _view.setValue(50.0, animated: _notice) : _view.setValue(thumbPosition, animated: _notice)
  }
  
  @objc func positionChanged(sender: UISlider) {
    if (_notice) {
      _thumbPosition = (_maxValue - _minValue) * sender.value / _numberOfSteps + _minValue
      PositionChanged(_thumbPosition)
    }
  }

  @objc func handleTouchDown() {
    TouchDown();
  }

  @objc func handleTouchUp() {
    TouchUp();
  }
  
  @objc open func PositionChanged(_ thumbPosition: Float) {
    EventDispatcher.dispatchEvent(of: self, called: "PositionChanged", arguments: thumbPosition as NSNumber)
  }

  @objc open func TouchDown() {
    EventDispatcher.dispatchEvent(of: self, called: "TouchDown")
  }

  @objc open func TouchUp() {
    EventDispatcher.dispatchEvent(of: self, called: "TouchUp")
  }
}
