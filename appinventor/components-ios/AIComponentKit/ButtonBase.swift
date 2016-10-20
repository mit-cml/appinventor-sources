//
//  ButtonBase.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol AbstractMethodsForButton: AbstractMethodsForViewComponent {
  func click()
}

let kRoundedCornersRadius: Float = 10.0
let kRoundedCornersArray = [kRoundedCornersRadius, kRoundedCornersRadius]
let kShapedDefaultBackgroundColor = Color.lightGray

public class ButtonBase: ViewComponent {
  final var _view: UIButton
  private weak var _delegate: AbstractMethodsForButton?
  private var _textAlignment = Alignment.center
  private var _backgroundColor = Color.DEFAULT
  private var _fontTypeface = Typeface.normal
  private var _bold = false
  private var _showFeedback = true
  private var _italic = false
  private var _textColor = Color.DEFAULT
  private var _shape = ButtonShape.normal
  private var _imagePath: String?

  public override init(_ parent: ComponentContainer) {
    self._view = UIButton(type: UIButtonType.system)
    super.init(parent)
  }

  internal func setDelegate(_ delegate: AbstractMethodsForButton) {
    self._delegate = delegate
    super.setDelegate(delegate)
  }

  public override var view: UIView {
    get {
      return _view
    }
  }

  public var Enabled: Bool {
    get {
      return _view.isEnabled
    }
    set(enabled) {
      _view.isEnabled = enabled
    }
  }

  public var Text: String? {
    get {
      return _view.title(for: UIControlState.normal)
    }
    set(text) {
      _view.setTitle(text, for: UIControlState.normal)
    }
  }

  public var TextColor: Int32 {
    get {
      var r: CGFloat = 0.0, g: CGFloat = 0.0, b: CGFloat = 0.0, a: CGFloat = 0.0
      let color = _view.titleLabel?.textColor
      color?.getRed(&r, green: &g, blue: &b, alpha: &a)
      var intColor: Int32 = 0
      intColor |= ((Int32)(255 * a))
      intColor <<= 8
      intColor |= ((Int32)(255 * r))
      intColor <<= 8
      intColor |= ((Int32)(255 * g))
      intColor <<= 8
      intColor |= ((Int32)(255 * b))
      return intColor
    }
    set(color) {
      var r: Float = 0.0, g: Float = 0.0, b: Float = 0.0, a: Float = 0.0
      var mutableColor = color
      b = Float(mutableColor & 0xFF) / 255.0
      mutableColor >>= 8
      g = Float(mutableColor & 0xFF) / 255.0
      mutableColor >>= 8
      r = Float(mutableColor & 0xFF) / 255.0
      mutableColor >>= 8
      a = Float(mutableColor & 0xFF) / 255.0
      _view.titleLabel?.textColor = UIColor(colorLiteralRed: r, green: g, blue: b, alpha: a)
    }
  }

  public var Image: String? {
    get {
      return _imagePath
    }
    set(path) {
      if (path == nil || path == "") {
        _view.setImage(nil, for: UIControlState.normal)
      } else {
        NSLog("Path: \(path)")
        let image = UIImage(named: path!);
        if (image != nil) {
          NSLog("Image is not nil");
          _imagePath = path
          _view.setBackgroundImage(image, for: UIControlState.normal)
        } else {
          NSLog("Image is nil");
        }
      }
    }
  }

  public func longClick() -> Bool {
    return false;
  }
}
