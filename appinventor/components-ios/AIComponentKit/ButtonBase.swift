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

open class ButtonBase: ViewComponent {
  final var _view: UIButton
  fileprivate weak var _delegate: AbstractMethodsForButton?
  fileprivate var _textAlignment = Alignment.center
  fileprivate var _backgroundColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _fontTypeface = Typeface.normal
  fileprivate var _bold = false
  fileprivate var _showFeedback = true
  fileprivate var _italic = false
  fileprivate var _textColor = Color.default
  fileprivate var _shape = ButtonShape.normal
  fileprivate var _imagePath: String?

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

  open override var view: UIView {
    get {
      return _view
    }
  }
  
  open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
      self._view.backgroundColor = argbToColor(argb)
    }
  }

  open var Enabled: Bool {
    get {
      return _view.isEnabled
    }
    set(enabled) {
      _view.isEnabled = enabled
    }
  }
  
  open var FontSize: Float32 {
    get {
      return Float32((_view.titleLabel?.font.pointSize)!)
    }
    set(size) {
      _view.titleLabel?.font = _view.titleLabel?.font.withSize(CGFloat(size))
    }
  }

  open var Image: String? {
    get {
      return _imagePath
    }
    set(path) {
      if (path == nil || path == "") {
        _view.setImage(nil, for: UIControlState.normal)
      } else {
        NSLog("Path: \(path)")
        var image = UIImage(named: path!);
        if (image == nil) {
          image = UIImage(contentsOfFile: AssetManager.shared.pathForAssetInBundle(filename: path!))
        }
        if (image != nil) {
          NSLog("Image is not nil");
          _imagePath = path
          _view.setBackgroundImage(image, for: UIControlState.normal)
          NSLog("Width: \((image?.size.width)!) Height: \((image?.size.height)!)")
          _view.frame.size = (image?.size)!
//          _view.sizeToFit()
//          let constraints = [
//            _view.widthAnchor.constraint(equalToConstant: (image?.size.width)!),
//            _view.heightAnchor.constraint(equalToConstant: (image?.size.height)!)
//          ]
//          _view.addConstraints(constraints)
//          NSLayoutConstraint.activate(constraints)
          _view.invalidateIntrinsicContentSize()
          _view.setNeedsLayout()
          NSLog("Button frame size: \(_view.frame)")
          _container.form?.layoutSubviews()
        } else {
          NSLog("Image is nil");
        }
      }
    }
  }

  open var Text: String? {
    get {
      return _view.title(for: UIControlState.normal)
    }
    set(text) {
      _view.setTitle(text, for: UIControlState.normal)
      _view.frame.size = _view.intrinsicContentSize
    }
  }

  open var TextColor: Int32 {
    get {
      return colorToArgb((_view.titleLabel?.textColor)!)
    }
    set(color) {
      _view.titleLabel?.textColor = argbToColor(color)
    }
  }

  open func longClick() -> Bool {
    return false;
  }
}
