//
//  Slider.swift
//  AIComponentKit
//
//  Created by Andrew McKinney on 1/6/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class Slider: ViewComponent, AbstractMethodsForViewComponent {
    private var _view: UISlider
    private var _alignment: Int32 = Alignment.normal.rawValue
    private var _typeface: Int32 = Typeface.normal.rawValue
    private var _bold = false
    private var _italic = false
    private var _hasMargins = false
    private var _minValue: Float = 0.0
    private var _maxValue: Float = 0.0
    private var _thumbValue: Float = 0.0
    private var _leftColor: UIColor = UIColor.orange
    private var _rightColor: UIColor = UIColor.gray

    public override init(_ parent: ComponentContainer) {
        _view = UISlider()
        _view.maximumValue = Float(kSliderMaxValue)
        _view.minimumValue = Float(kSliderMinValue)
        _view.minimumTrackTintColor = _leftColor
        _view.maximumTrackTintColor = _rightColor
        _view.isEnabled = true
        super.init(parent)
        super.setDelegate(self)
        parent.add(self)
    }

    public override var view: UIView {
        get {
            return _view
        }
    }

    public var ThumbEnabled: Bool {
        get {
            return _view.isEnabled
        }
        set(thumbEnable) {
            _view.isEnabled = thumbEnable
        }
    }

    public func ThumbPosition(_thumbValue: Float) {
            _view.setValue(_thumbValue, animated: true)
    }

    public var MinValue: Float {
        get {
            return _view.minimumValue
        }
        set(value) {
            let maxValue = max(value, _view.maximumValue)
            ThumbPosition(_thumbValue: ((maxValue + _minValue) / 2))
        }
    }

    public var MaxValue: Float {
        get {
            return _view.maximumValue
        }
        set(value) {
            let minValue = min(value, _view.minimumValue)
            ThumbPosition(_thumbValue: ((minValue + _maxValue) / 2))
        }
    }

    public var ColorLeft: Int32 {
        get {
            return colorToArgb(_leftColor)
        }
        set(argb) {
            _leftColor = argbToColor(argb)
            _view.minimumTrackTintColor = _leftColor
        }
    }

    public var ColorRight: Int32 {
        get {
            return colorToArgb(_rightColor)
        }
        set(argb) {
            _rightColor = argbToColor(argb)
            _view.maximumTrackTintColor = _rightColor
        }
    }

//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//    //progress has been changed. Set the sliderThumbPosition and then trigger the event
//
//    //Now convert this progress value (which is between 0-100), back to a value between the
//    //range that user has set within minValue, maxValue
//    thumbPosition = ((maxValue - minValue) * (float) progress / 100)
//    + minValue;
//
//    if (DEBUG) {
//    Log.d(LOG_TAG, "onProgressChanged progress value [0-100]: " + progress
//    + ", reporting to user as: " + thumbPosition);
//    }
//
//    //Trigger the event, reporting this new value
//    PositionChanged(thumbPosition);
//    EventDispatcher.dispatchEvent(this, "PositionChanged", thumbPosition);
//    }
}
