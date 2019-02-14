// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

public protocol AbstractMethodsForButton: AbstractMethodsForViewComponent {
  func click()
}

let kRoundedCornersRadius: Float = 10.0
let kRoundedCornersArray = [kRoundedCornersRadius, kRoundedCornersRadius]
let kShapedDefaultBackgroundColor = Color.lightGray

open class ButtonBase: ViewComponent {
  @objc final var _view: UIButton
  fileprivate weak var _delegate: AbstractMethodsForButton?
  fileprivate var _textAlignment = Alignment.center
  fileprivate var _backgroundColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _fontTypeface = Typeface.normal
  fileprivate var _bold = false
  fileprivate var _showFeedback = true
  fileprivate var _italic = false
  fileprivate var _textColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _shape = ButtonShape.normal
  fileprivate var _imagePath: String?
  fileprivate var _defaultTextColor: UIColor

  public override init(_ parent: ComponentContainer) {
    self._view = UIButton(type: UIButton.ButtonType.system)
    _defaultTextColor = self._view.tintColor
    super.init(parent)
    self._view.backgroundColor = argbToColor(_backgroundColor)
    self._view.translatesAutoresizingMaskIntoConstraints = false
    self._view.addTarget(self, action: #selector(TouchDown), for: UIControl.Event.touchDown)
    self._view.addTarget(self, action: #selector(TouchUp), for: UIControl.Event.touchUpInside)
    BackgroundColor = Int32(Color.default.rawValue)
    Enabled = true
    FontSize = kFontSizeDefault
    FontBold = _bold
    FontItalic = _italic
    Image = ""
    Shape = ButtonShape.normal.rawValue
    Text = ""
    TextAlignment = Alignment.center.rawValue
    TextColor = Int32(Color.default.rawValue)
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
  
  @objc open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
      self._view.backgroundColor = argbToColor(argb)
    }
  }

  @objc open var Enabled: Bool {
    get {
      return _view.isEnabled
    }
    set(enabled) {
      _view.isEnabled = enabled
    }
  }
  
  @objc open var FontBold: Bool {
    get {
      return _bold
    }
    set(shouldBold){
      _bold = shouldBold
      _view.titleLabel?.font = getFontTrait(font: _view.titleLabel?.font, trait: .traitBold, shouldSet: shouldBold)
    }
  }

  @objc open var FontItalic: Bool {
    get {
      return _italic
    }
    set(shouldItalic){
      _italic = shouldItalic
      _view.titleLabel?.font = getFontTrait(font: _view.titleLabel?.font, trait: .traitItalic, shouldSet: shouldItalic)
    }
  }

  @objc open var FontTypeface: Int32 {
    get {
      return _fontTypeface.rawValue
    }
    set(newTypeFace) {
      if newTypeFace != _fontTypeface.rawValue {
        if let type = Typeface(rawValue: Int32(newTypeFace)) {
          _fontTypeface = type
          _view.titleLabel?.font = getFontTypeface(font: _view.titleLabel?.font, typeFace: type)
        }
      }
    }
  }


  @objc open var FontSize: Float32 {
    get {
      return Float32((_view.titleLabel?.font.pointSize)!)
    }
    set(size) {
      _view.titleLabel?.font = getFontSize(font: _view.titleLabel?.font, size: size)
    }
  }

  @objc open var Image: String? {
    get {
      return _imagePath
    }
    set(path) {
      if (path == nil || path == "") {
        _view.setImage(nil, for: UIControl.State.normal)
      } else {
        NSLog("Path: \(String(describing: path))")
        var image = UIImage(named: path!);
        if (image == nil) {
          image = UIImage(contentsOfFile: AssetManager.shared.pathForExistingFileAsset(path!))
        }
        if (image != nil) {
          NSLog("Image is not nil");
          _imagePath = path
          _view.setBackgroundImage(image, for: UIControl.State.normal)
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
          _container.form.view.setNeedsLayout()
        } else {
          NSLog("Image is nil");
        }
      }
    }
  }

  @objc open var Shape: Int32 {
    get {
      return _shape.rawValue
    }
    set(shape) {
      if let shape = ButtonShape(rawValue: shape) {
        _shape = shape
        // TODO(ewpatton): Adjust button shape
      }
    }
  }

  @objc open var ShowFeedback: Bool {
    get {
      return _showFeedback
    }
    set(feedback) {
      if _showFeedback != feedback {
        _showFeedback = feedback
        _view.showsTouchWhenHighlighted = feedback
      }
    }
  }

  @objc open var Text: String? {
    get {
      return _view.title(for: UIControl.State.normal)
    }
    set(text) {
      _view.setTitle(text, for: UIControl.State.normal)
      _view.frame.size = _view.intrinsicContentSize
    }
  }

  @objc open var TextAlignment: Int32 {
    get {
      return _textAlignment.rawValue
    }
    set(alignment) {
      if alignment >= 0 && alignment <= 2 {
        _textAlignment = Alignment(rawValue: alignment)!
        var rtl = false
        if #available(iOS 9.0, *) {
          if UIView.userInterfaceLayoutDirection(for: _view.semanticContentAttribute) == .rightToLeft {
            rtl = true
          }
        } else {
          if UIApplication.shared.userInterfaceLayoutDirection == UIUserInterfaceLayoutDirection.rightToLeft {
            rtl = true
          }
        }
        switch _textAlignment {
          case .normal:
            _view.contentHorizontalAlignment = rtl ? .right : .left
          case .center:
            _view.contentHorizontalAlignment = .center
          case .opposite:
            _view.contentHorizontalAlignment = rtl ? .left : .right
        }
      }
    }
  }

  @objc open var TextColor: Int32 {
    get {
      return colorToArgb((_view.titleLabel?.textColor)!)
    }
    set(color) {
      _textColor = color
      if (color == Int32(bitPattern: Color.default.rawValue)) {
        _view.setTitleColor(_defaultTextColor, for: .normal)
      } else {
        _view.setTitleColor(argbToColor(color), for: .normal)
      }
    }
  }

  // MARK: Events
  @objc open func GotFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "GotFocus")
  }

  @objc open func LostFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "LostFocus")
  }

  @objc open func TouchDown() {
    EventDispatcher.dispatchEvent(of: self, called: "TouchDown")
  }

  @objc open func TouchUp() {
    EventDispatcher.dispatchEvent(of: self, called: "TouchUp")
  }

  @objc open func longClick() -> Bool {
    return false;
  }
}
