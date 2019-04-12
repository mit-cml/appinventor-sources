// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

public protocol AbstractMethodsForButton: AbstractMethodsForViewComponent {
  func click()
}

let kRoundedCornersRadius: Float = 10.0
let kRoundedCornersArray = [kRoundedCornersRadius, kRoundedCornersRadius]
let kShapedDefaultBackgroundColor = Color.lightGray
fileprivate let DEFAULT_BUTTON_INSET = UIEdgeInsets(top: 4.0, left: 5.0, bottom: 8.0, right: 5.0)
fileprivate let CLASSIC_DEFAULT_BUTTON_TEXTCOLOR = argbToColor(-16777216)
fileprivate let CLASSIC_DEFAULT_BUTTON_DISABLED_TEXTCOLOR = argbToColor(-2147483648)

open class ButtonBase: ViewComponent {
  @objc final var _view: UIButton
  fileprivate weak var _delegate: AbstractMethodsForButton?
  fileprivate var _textAlignment = Alignment.center
  fileprivate var _backgroundColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _backgroundImage: UIImage?
  fileprivate var _fontTypeface = Typeface.normal
  fileprivate var _bold = false
  fileprivate var _showFeedback = true
  fileprivate var _italic = false
  fileprivate var _textColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _shape = ButtonShape.normal
  fileprivate var _imagePath: String?
  fileprivate var _defaultTextColor: UIColor
  fileprivate var _defaultHighlightColor: UIColor?
  fileprivate static var _classicButton: UIImage!
  fileprivate static var _classicButtonDisabled: UIImage!
  fileprivate static var _classicButtonPressed: UIImage!

  static func loadClassicButton() {
    let bundle = Bundle(for: ButtonBase.self)
    _classicButton = UIImage(contentsOfFile: bundle.path(forResource: "classic_button", ofType: "png")!)
    _classicButton = _classicButton?.resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
    _classicButtonPressed = UIImage(contentsOfFile: bundle.path(forResource: "classic_button.pressed", ofType: "png")!)
    _classicButtonPressed = _classicButtonPressed?.resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
    _classicButtonDisabled = UIImage(contentsOfFile: bundle.path(forResource: "classic_button.disabled", ofType: "png")!)
    _classicButtonDisabled = _classicButtonDisabled?.resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
  }

  public override init(_ parent: ComponentContainer) {
    if ButtonBase._classicButton == nil {
      ButtonBase.loadClassicButton()
    }
    self._view = UIButton(type: UIButton.ButtonType.custom)
    _defaultTextColor = self._view.tintColor
    _defaultHighlightColor = self._view.titleColor(for: .highlighted)
    super.init(parent)
    self._view.translatesAutoresizingMaskIntoConstraints = false
    self._view.addTarget(self, action: #selector(TouchDown), for: UIControl.Event.touchDown)
    self._view.addTarget(self, action: #selector(TouchUp), for: UIControl.Event.touchUpInside)
    BackgroundColor = Int32(Color.default.rawValue)
    Enabled = true
    ShowFeedback = true
    FontSize = kFontSizeDefault
    FontBold = _bold
    FontItalic = _italic
    Image = ""
    Shape = ButtonShape.normal.rawValue
    Text = ""
    TextAlignment = Alignment.center.rawValue
    TextColor = Int32(Color.default.rawValue)
    applyTheme()
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
      applyTheme()
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
        _backgroundImage = nil
      } else {
        NSLog("Path: \(String(describing: path))")
        var image = UIImage(named: path!);
        if (image == nil) {
          image = UIImage(contentsOfFile: AssetManager.shared.pathForExistingFileAsset(path!))
        }
        if (image != nil) {
          NSLog("Image is not nil");
          _imagePath = path
          _backgroundImage = image
          NSLog("Width: \((image?.size.width)!) Height: \((image?.size.height)!)")
          _view.frame.size = (image?.size)!
          _view.invalidateIntrinsicContentSize()
          NSLog("Button frame size: \(_view.frame)")
        } else {
          NSLog("Image is nil");
        }
      }
      applyTheme()
      _container.form.view.setNeedsLayout()
      _view.setNeedsLayout()
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
      _showFeedback = feedback
      applyTheme()
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

  func applyTheme() {
    switch _container.form.Theme {
    case "Classic":
      self._view.imageView?.tintColor = nil
      if let backgroundImage = _backgroundImage {
        self._view.setBackgroundImage(backgroundImage, for: .normal)
        self._view.setBackgroundImage(nil, for: [.disabled, .highlighted])
        self._view.contentEdgeInsets = .zero
      } else {
        if _backgroundColor != Int32(bitPattern: Color.default.rawValue) {
          let tint = argbToColor(_backgroundColor)
          let bg = ButtonBase._classicButton.imageWithTint(tint: tint)?.resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
          let bgd = ButtonBase._classicButtonDisabled.imageWithTint(tint: tint)?.resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
          let bgh = ButtonBase._classicButtonPressed.imageWithTint(tint: tint)?.resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
          self._view.setBackgroundImage(bg, for: .normal)
          self._view.setBackgroundImage(ShowFeedback ? bgh : bg, for: .highlighted)
          self._view.setBackgroundImage(bgd, for: .disabled)
        } else {
          self._view.setBackgroundImage(ButtonBase._classicButton, for: .normal)
          self._view.setBackgroundImage(ButtonBase._classicButtonDisabled, for: .disabled)
          self._view.setBackgroundImage(ShowFeedback ? ButtonBase._classicButtonPressed : ButtonBase._classicButton, for: .highlighted)
        }
        self._view.contentEdgeInsets = UIEdgeInsets(top: 7.0, left: 11.0, bottom: 11.0, right: 11.0)
      }
      if _textColor == Int32(bitPattern: Color.default.rawValue) {
        self._view.setTitleColor(CLASSIC_DEFAULT_BUTTON_TEXTCOLOR, for: .normal)
      }
      self._view.setTitleColor(CLASSIC_DEFAULT_BUTTON_DISABLED_TEXTCOLOR, for: .disabled)
      self._view.showsTouchWhenHighlighted = false
      self._view.adjustsImageWhenHighlighted = ShowFeedback
      break
    default:
      if _backgroundImage == nil && BackgroundColor != Int32(bitPattern: Color.default.rawValue) {
        self._view.setBackgroundImage(nil, for: [.normal, .disabled, .highlighted])
      } else {
        self._view.setBackgroundImage(_backgroundImage, for: .normal)
        self._view.setBackgroundImage(nil, for: .disabled)
        self._view.setBackgroundImage(ShowFeedback ? nil : _backgroundImage, for: .highlighted)
      }
      if _textColor == Int32(bitPattern: Color.default.rawValue) {
        self._view.setTitleColor(_defaultTextColor, for: .normal)
      } else {
        self._view.setTitleColor(argbToColor(_textColor), for: .normal)
      }
      self._view.showsTouchWhenHighlighted = ShowFeedback
      self._view.adjustsImageWhenHighlighted = ShowFeedback
      self._view.setTitleColor(_defaultHighlightColor, for: .highlighted)
      self._view.setTitleColor(.gray, for: .disabled)
      break
    }
  }
}
