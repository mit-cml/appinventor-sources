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
  private var _backgroundColor = Int32(bitPattern: Color.DEFAULT.rawValue)
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
    self._view.backgroundColor = argbToColor(_backgroundColor)
//    self._view.translatesAutoresizingMaskIntoConstraints = false
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
  
  public var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
      self._view.backgroundColor = argbToColor(argb)
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
  
  public var FontSize: Float32 {
    get {
      return Float32((_view.titleLabel?.font.pointSize)!)
    }
    set(size) {
      _view.titleLabel?.font = _view.titleLabel?.font.withSize(CGFloat(size))
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
          NSLog("Width: \((image?.size.width)!) Height: \((image?.size.height)!)")
          _view.frame.size = (image?.size)!
          let constraints = [
            _view.widthAnchor.constraint(equalToConstant: (image?.size.width)!),
            _view.heightAnchor.constraint(equalToConstant: (image?.size.height)!)
          ]
          _view.addConstraints(constraints)
          NSLayoutConstraint.activate(constraints)
          _view.invalidateIntrinsicContentSize()
          _view.sizeToFit()
          _view.setNeedsLayout()
        } else {
          NSLog("Image is nil");
        }
      }
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
      return colorToArgb((_view.titleLabel?.textColor)!)
    }
    set(color) {
      _view.titleLabel?.textColor = argbToColor(color)
    }
  }

  public func longClick() -> Bool {
    return false;
  }
}
