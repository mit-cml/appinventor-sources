// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public protocol AbstractMethodsForButton: AbstractMethodsForViewComponent {
  func click()
}

let kRoundedCornersRadius: Float = 10.0
let kRoundedCornersArray = [kRoundedCornersRadius, kRoundedCornersRadius]
let kShapedDefaultBackgroundColor = Color.lightGray
fileprivate let kDefaultColor = Int32(bitPattern: Color.default.rawValue)
fileprivate let DEFAULT_BUTTON_INSET = UIEdgeInsets(top: 4.0, left: 5.0, bottom: 8.0, right: 5.0)
fileprivate let CLASSIC_DEFAULT_BUTTON_TEXTCOLOR = argbToColor(Int32(-16777216))
fileprivate let CLASSIC_DEFAULT_BUTTON_DISABLED_TEXTCOLOR = argbToColor(Int32(-2147483648))

@objc public class MAIButton: UIButton {
  private var ovalLayer: CAShapeLayer?
  private var _bgColor: UIColor?

  override open var backgroundColor: UIColor? {
    get {
      return _bgColor ?? super.backgroundColor
    }
    set(backgroundColor) {
      if isOval {
        ovalLayer?.fillColor = backgroundColor?.cgColor
      } else {
        super.backgroundColor = backgroundColor
      }
      _bgColor = backgroundColor
    }
  }

  var isOval: Bool {
    get {
      return ovalLayer != nil
    }
    set(oval) {
      ovalLayer?.removeFromSuperlayer()
      if oval {
        ovalLayer = CAShapeLayer()
        layer.insertSublayer(ovalLayer!, at: 0)
        ovalLayer?.backgroundColor = _bgColor?.cgColor
        super.backgroundColor = nil
      } else {
        super.backgroundColor = _bgColor
      }
    }
  }

  override public func layoutSubviews() {
    super.layoutSubviews()
    if let ovalLayer = ovalLayer {
      ovalLayer.path = UIBezierPath(ovalIn: CGRect(x: 0, y: 0, width: frame.width, height: frame.height)).cgPath
    }
  }

  override public var intrinsicContentSize: CGSize {
    // The intrinsic content size for UIButton doesn't account for line wrapping in the title label
    // We perform this here so that automatic sizing works when the label needs to wrap.
    var result = super.intrinsicContentSize
    if let image = backgroundImage(for: .normal) {
      result = image.size
    }
    if let titleFrame = titleLabel?.frame,
       let labelSize = titleLabel?.sizeThatFits(CGSize(width: frame.width, height: frame.height)) {
      let insetWidth = titleEdgeInsets.left + titleEdgeInsets.right + contentEdgeInsets.left +
        contentEdgeInsets.right
      let insetHeight = titleEdgeInsets.top + titleEdgeInsets.bottom + contentEdgeInsets.top +
        contentEdgeInsets.bottom
      result.width = max(result.width, max(titleFrame.width, labelSize.width) + insetWidth)
      result.height = max(result.height, max(titleFrame.height, labelSize.height) + insetHeight)
    }
    return result
  }
}

enum ButtonStylePipeline: Int {
  case Base
  case FontColor
  case Highlight
  case BackgroundImage
  case Shape
  case DefaultShape
  case BackgroundColor
}

typealias PipelineStep = (ButtonBase)->Bool

open class ButtonBase: ViewComponent, AccessibleComponent {
  @objc final var _view: MAIButton
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
  fileprivate static var CLASSIC_BUTTON: UIImage!
  fileprivate static var CLASSIC_BUTTON_DISABLED: UIImage!
  fileprivate static var CLASSIC_BUTTON_PRESSED: UIImage!
  fileprivate static var _classicButton: UIImage!
  fileprivate static var _classicButtonDisabled: UIImage!
  fileprivate static var _classicButtonPressed: UIImage!
  fileprivate static var CLASSIC_DEFAULT_PIPELINE = [ButtonStylePipeline:PipelineStep]()
  fileprivate static var MODERN_DEFAULT_PIPELINE = [ButtonStylePipeline:PipelineStep]()
  fileprivate var _stylePipeline: [ButtonStylePipeline:PipelineStep]
  fileprivate var _needsStyleApplied = true
  fileprivate var _isHighContrast = false
  fileprivate var _isBigText = false
  fileprivate var _userFontSize = kFontSizeDefault
  fileprivate var _userBackgroundColor: Int32 = Color.default.int32
  fileprivate var _userTextColor: Int32 = Color.default.int32
  fileprivate var _hintColorDefault: Int32 = Color.default.int32

  static func loadClassicButton() {
    let bundle = Bundle(for: ButtonBase.self)
    CLASSIC_BUTTON = UIImage(contentsOfFile: bundle.path(forResource: "classic_button", ofType: "png")!)
    _classicButton = CLASSIC_BUTTON.resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
    CLASSIC_BUTTON_PRESSED = UIImage(contentsOfFile: bundle.path(forResource: "classic_button.pressed", ofType: "png")!)
    _classicButtonPressed = CLASSIC_BUTTON_PRESSED.resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
    CLASSIC_BUTTON_DISABLED = UIImage(contentsOfFile: bundle.path(forResource: "classic_button.disabled", ofType: "png")!)
    _classicButtonDisabled = CLASSIC_BUTTON_DISABLED.resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)

    // Classic Android theme
    CLASSIC_DEFAULT_PIPELINE[.Base] = { $0._view.imageView?.tintColor = nil; return true }
    CLASSIC_DEFAULT_PIPELINE[.BackgroundImage] = {
      if let backgroundImage = $0._backgroundImage {
        $0._view.isOval = false  // always false when an image is supplied
        $0._view.layer.cornerRadius = CGFloat(0.0)
        $0._view.setBackgroundImage(backgroundImage, for: .normal)
        $0._view.setBackgroundImage(backgroundImage, for: .disabled)
        $0._view.setBackgroundImage(backgroundImage, for: .highlighted)
        $0._view.contentEdgeInsets = .zero
        $0._view.invalidateIntrinsicContentSize()
        return false
      }
      return true
    }
    CLASSIC_DEFAULT_PIPELINE[.Shape] = {
      $0._view.isOval = $0._shape == .oval
      $0._view.layer.cornerRadius = CGFloat($0._shape == .rounded ? kRoundedCornersRadius : 0.0)
      return true
    }
    CLASSIC_DEFAULT_PIPELINE[.DefaultShape] = {
      $0._view.contentEdgeInsets = UIEdgeInsets(top: 7.0, left: 11.0, bottom: 11.0, right: 11.0)
      $0._view.titleEdgeInsets = UIEdgeInsets(top: 7.0, left: 0.0, bottom: 6.0, right: 0.0)
      if $0._shape == .normal {
        var bg = ButtonBase._classicButton, bgd = ButtonBase._classicButtonDisabled, bgh = ButtonBase._classicButtonPressed
        if $0._backgroundColor != kDefaultColor {
          let tint = argbToColor($0._backgroundColor)
          bg = CLASSIC_BUTTON.imageWithTint(tint: tint)?
            .resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
          bgd = CLASSIC_BUTTON_DISABLED.imageWithTint(tint: tint)?
            .resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
          if $0.ShowFeedback {
            bgh = CLASSIC_BUTTON_PRESSED.imageWithTint(tint: tint)?
              .resizableImage(withCapInsets: DEFAULT_BUTTON_INSET, resizingMode: .stretch)
          }
        }
        $0._view.setBackgroundImage(bg, for: .normal)
        $0._view.setBackgroundImage(bgd, for: .disabled)
        $0._view.setBackgroundImage($0.ShowFeedback ? bgh : bg, for: .highlighted)
        $0._view.invalidateIntrinsicContentSize()
        return false
      }
      return true
    }
    CLASSIC_DEFAULT_PIPELINE[.BackgroundColor] = {
      $0._view.backgroundColor = argbToColor($0._backgroundColor == kDefaultColor ? Int32(bitPattern: kShapedDefaultBackgroundColor.rawValue) : $0._backgroundColor)
      return true
    }
    CLASSIC_DEFAULT_PIPELINE[.FontColor] = {
      $0._view.setTitleColor($0._textColor == kDefaultColor ? CLASSIC_DEFAULT_BUTTON_TEXTCOLOR : argbToColor($0._textColor), for: .normal)
      $0._view.setTitleColor(CLASSIC_DEFAULT_BUTTON_DISABLED_TEXTCOLOR, for: .disabled)
      return true
    }
    CLASSIC_DEFAULT_PIPELINE[.Highlight] = {
      $0._view.showsTouchWhenHighlighted =
        ($0._container?.form?.Theme != "Classic" || $0._backgroundImage != nil) && $0.ShowFeedback
      $0._view.adjustsImageWhenHighlighted = $0.ShowFeedback
      return true
    }

    // Modern iOS theme
    let defaultbuttoncolor = UIButton().tintColor
    MODERN_DEFAULT_PIPELINE = CLASSIC_DEFAULT_PIPELINE
    MODERN_DEFAULT_PIPELINE[.FontColor] = {
      let color = $0._textColor == kDefaultColor ? defaultbuttoncolor : argbToColor($0._textColor)
      let highlightColor = color?.withAlphaComponent(0.3)
      $0._view.setTitleColor(color, for: .normal)
      $0._view.setTitleColor($0.ShowFeedback ? highlightColor : nil, for: .highlighted)
      return true
    }
    MODERN_DEFAULT_PIPELINE[.DefaultShape] = { (MAIButton) in return true }  // Cancels Classic button style
    MODERN_DEFAULT_PIPELINE[.BackgroundColor] = {
      $0._view.backgroundColor = $0._backgroundColor == kDefaultColor ? nil : argbToColor($0._backgroundColor)
      return true
    }
  }

  public override init(_ parent: ComponentContainer) {
    if ButtonBase._classicButton == nil {
      ButtonBase.loadClassicButton()
    }
    self._view = MAIButton(type: UIButton.ButtonType.custom)
    _defaultTextColor = self._view.tintColor
    _defaultHighlightColor = self._view.titleColor(for: .highlighted)
    _stylePipeline = ButtonBase.CLASSIC_DEFAULT_PIPELINE
    super.init(parent)
    self._view.titleLabel?.lineBreakMode = .byWordWrapping
    self._view.titleLabel?.textAlignment = .center
    self._view.translatesAutoresizingMaskIntoConstraints = false
    self._view.addTarget(self, action: #selector(TouchDown), for: UIControl.Event.touchDown)
    self._view.addTarget(self, action: #selector(TouchUp), for: UIControl.Event.touchUpInside)
    parent.add(self)
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
    Height = kLengthPreferred
    Width = kLengthPreferred
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

  @objc func Initialize() {
    if _container?.form?.Theme != "Classic" {
      _stylePipeline = ButtonBase.MODERN_DEFAULT_PIPELINE
    }
    applyStyle()
  }

  func updateFontSize() {
    guard let titleLabel = _view.titleLabel else {
      return
    }

    if form?.BigDefaultText == true {
      if _userFontSize == kFontSizeDefault {
        titleLabel.font = getFontSize(font: titleLabel.font, size: kFontSizeLargeDefault)
      } else {
        titleLabel.font = getFontSize(font: titleLabel.font, size: _userFontSize)
      }
    } else {
      titleLabel.font = getFontSize(font: titleLabel.font, size: _userFontSize)
    }
    setNeedsStyleApplied()
  }

  func updateColor() {
    if form?.HighContrast == true {
      if _userTextColor == Color.default.int32  {
        _textColor = Int32(bitPattern: Color.white.rawValue)
      } else {
        _textColor = _userTextColor
      }

      if _userBackgroundColor == Color.default.int32 {
        _backgroundColor = Int32(bitPattern: Color.black.rawValue)
      } else {
        _backgroundColor = _userBackgroundColor
      }

    } else {
      _textColor = _userTextColor
      _backgroundColor = _userBackgroundColor
    }
    setNeedsStyleApplied()
  }

  @objc open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _userBackgroundColor = argb
      updateColor()
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
      _userFontSize = size
      updateFontSize()
    }
  }

  @objc open var HighContrast: Bool {
    get {
      return _isHighContrast
    }
    set(isHighContrast) {
      _isHighContrast = isHighContrast
      updateColor()
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
        var image = UIImage(named: path!);
        if (image == nil) {
          image = UIImage(contentsOfFile: AssetManager.shared.pathForExistingFileAsset(path!))
        }
        if (image != nil) {
          _imagePath = path
          _backgroundImage = image
          _view.frame.size = (image?.size)!
          _view.invalidateIntrinsicContentSize()
        }
      }
      setNeedsStyleApplied()
    }
  }

  @objc open var LargeFont: Bool {
    get {
      return _isBigText
    }
    set (isLargeFont){
      _isBigText = isLargeFont
      updateFontSize()
    }
  }

  @objc open var Shape: Int32 {
    get {
      return _shape.rawValue
    }
    set(shape) {
      if let shape = ButtonShape(rawValue: shape) {
        _shape = shape
        setNeedsStyleApplied()
      }
    }
  }

  @objc open var ShowFeedback: Bool {
    get {
      return _showFeedback
    }
    set(feedback) {
      _showFeedback = feedback
      setNeedsStyleApplied()
    }
  }

  @objc open var Text: String? {
    get {
      return _view.title(for: UIControl.State.normal)
    }
    set(text) {
      _view.setTitle(text, for: UIControl.State.normal)
      _view.setNeedsLayout()
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
      _userTextColor = color
      updateColor()
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

  func setNeedsStyleApplied() {
    if !_needsStyleApplied {
      _needsStyleApplied = true
      DispatchQueue.main.async {
        self.applyStyle()
      }
    }
  }

  func applyStyle() {
    _needsStyleApplied = false
    for step in _stylePipeline.sorted(by: { $0.key.rawValue < $1.key.rawValue }) {
      if !step.value(self) { break }
    }
    _container?.form?.view.setNeedsLayout()
    _view.setNeedsLayout()
  }
}
