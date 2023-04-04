// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public class Switch: ViewComponent, AbstractMethodsForViewComponent, AccessibleComponent {
  public var HighContrast: Bool = false
  fileprivate var _view = UIView()
  fileprivate var _text = UILabel()
  fileprivate var _switch = UISwitch()
  fileprivate var _alignment: Int32 = Alignment.normal.rawValue
  fileprivate var _typeface: Int32 = Typeface.normal.rawValue
  fileprivate var _backgroundColor = Color.default.int32
  fileprivate var _trackColorActive = Color.default.int32
  fileprivate var _thumbColorActive = Color.default.int32
  fileprivate var _trackColorInactive = Color.default.int32
  fileprivate var _thumbColorInactive = Color.default.int32
  fileprivate var _fontTypeface = Typeface.normal
  fileprivate var _bold = false
  fileprivate var _italic = false
  fileprivate var _hasMargins = false
  fileprivate var _userFontSize = kFontSizeDefault
  fileprivate var _isBigText = false
  
  public override init(_ parent: ComponentContainer) {
    _switch.isOn = false
    self.On = false
    super.init(parent)
    super.setDelegate(self)
    _switch.addTarget(self, action: #selector(changeSwitch), for: .touchUpInside)
    _view.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(changeSwitch)))
    _text.textColor = preferredTextColor(parent.form)
    FontSize = 14.0
    parent.add(self)
    
    setupViews()
  }

  private func setupViews() {
    _view.translatesAutoresizingMaskIntoConstraints = false
    _switch.translatesAutoresizingMaskIntoConstraints = false
    _text.translatesAutoresizingMaskIntoConstraints = false
    _switch.transform = CGAffineTransform(scaleX: 0.75, y: 0.75)
    _text.numberOfLines = 0
    
    _view.addSubview(_switch)
    _view.addSubview(_text)
    
    _switch.trailingAnchor.constraint(equalTo: _view.trailingAnchor, constant: -5).isActive = true
    _text.leadingAnchor.constraint(equalTo: _view.leadingAnchor, constant: 5).isActive = true
    _switch.centerYAnchor.constraint(equalTo: _view.centerYAnchor).isActive = true
    _text.centerYAnchor.constraint(equalTo: _view.centerYAnchor).isActive = true
    _switch.leadingAnchor.constraint(equalTo: _text.trailingAnchor, constant: 5).isActive = true
    _view.heightAnchor.constraint(greaterThanOrEqualTo: _switch.heightAnchor).isActive = true
    _text.heightAnchor.constraint(greaterThanOrEqualTo: _view.heightAnchor).isActive = true
    _view.clipsToBounds = true
  }

  func updateFontSize() {
    if form?.BigDefaultText == true {
      if _userFontSize == kFontSizeDefault {
        _text.font = UIFont(descriptor: _text.font.fontDescriptor, size: CGFloat(kFontSizeLargeDefault))
      } else {
        _text.font = UIFont(descriptor: _text.font.fontDescriptor, size: CGFloat(_userFontSize))
      }
    } else {
      _text.font = UIFont(descriptor: _text.font.fontDescriptor, size: CGFloat(_userFontSize))
    }
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
      _view.backgroundColor = argbToColor(argb)
    }
  }
  
  @objc open var TrackColorActive: Int32 {
    get {
      return _trackColorActive
    }
    set(argb) {
      _trackColorActive = argb
      _switch.onTintColor = argbToColor(argb)
    }
  }
  
  @objc open var ThumbColorActive: Int32 {
    get {
      return _thumbColorActive
    }
    set(argb) {
      _thumbColorActive = argb
      if _switch.isOn {
        _switch.thumbTintColor = argbToColor(argb)
      }
    }
  }

  @objc open var TrackColorInactive: Int32 {
    get {
      return _trackColorInactive
    }
    set(argb) {
      _trackColorInactive = argb
      _switch.layer.cornerRadius = _switch.frame.height / 1.5
      _switch.backgroundColor = argbToColor(argb)
    }
  }
  
  @objc open var ThumbColorInactive: Int32 {
    get {
      return _thumbColorInactive
    }
    set(argb) {
      _thumbColorInactive = argb
      if !_switch.isOn {
        _switch.thumbTintColor = argbToColor(argb)
      }
    }
  }

  @objc open var On: Bool {
    didSet {
      if oldValue != On {
        _switch.setOn(On, animated: true)
        Changed()
      }
    }
  }

  @objc open var Enabled: Bool {
    get {
      return _switch.isEnabled
    }
    set(enabled) {
      if _switch.isEnabled != enabled {
        _switch.isEnabled = enabled
      }
    }
  }
  

  @objc open var FontBold: Bool {
    get {
      return _bold
    }
    set(shouldBold) {
      _bold = shouldBold
      _text.font = getFontTrait(font: _text.font, trait: .traitBold, shouldSet: shouldBold)
    }
  }
  
  @objc open var FontItalic: Bool {
    get {
      return _italic
    }
    set(shouldItalic) {
      _italic = shouldItalic
      _text.font = getFontTrait(font: _text.font, trait: .traitItalic, shouldSet: shouldItalic)
    }
  }
  
  @objc open var FontSize: Float32 {
    get {
      return Float32(_text.font.pointSize)
    }
    set(size) {
      _userFontSize = size
      updateFontSize()
    }
  }
  
  @objc open var FontTypeface: Int32 {
    get {
      return _fontTypeface.rawValue
    }
    set(newTypeface) {
      
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
  
  @objc open var Text: String {
    get {
      return _text.text ?? ""
    }
    set(newText) {
      _text.text = newText
    }
  }
  
  @objc open var TextColor: Int32 {
    get {
      return colorToArgb(_text.textColor)
    }
    set(color) {
      _text.textColor = argbToColor(color)
    }
  }
  
  @objc fileprivate func changeSwitch(gesture: UITapGestureRecognizer) {
    if _switch.isEnabled {
      On = !On
      if _switch.isOn {
        _switch.thumbTintColor = _thumbColorActive == Color.default.int32 ? nil : argbToColor(_thumbColorActive)
      } else {
        _switch.thumbTintColor = _thumbColorActive == Color.default.int32 ? nil : argbToColor(_thumbColorInactive)
      }
    }
  }
  
  @objc open func Changed() {
    EventDispatcher.dispatchEvent(of: self, called: "Changed")
  }
  
  @objc open func GotFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "GotFocus")
  }
  
  @objc open func LostFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "LostFocus")
  }}
