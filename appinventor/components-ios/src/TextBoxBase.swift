// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public protocol AbstractMethodsForTextBox: AbstractMethodsForViewComponent {
  var alignment: NSTextAlignment { get set }
  var backgroundColor: UIColor? { get set }
  var textColor: UIColor? { get set }
  var font: UIFont { get set }
  var placeholderText: String? { get set }
  var text: String? { get set }
  var readOnly: Bool { get set }
  func textFieldDidBeginEditing(_ textField: UITextField)
  func textFieldDidEndEditing(_ textfield: UITextField)
  func setTextbase(_ base: TextBoxBase)
}

// method for creating toolbar to allow users to dismiss keyboard
extension AbstractMethodsForTextBox {
  func getAccesoryView(_ selector: Selector) -> UIView {
    let keyboardToolbar = UIToolbar()
    keyboardToolbar.sizeToFit()
    let flexBarButton = UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: nil, action: nil)
    let doneBarButton = UIBarButtonItem(barButtonSystemItem: .done, target: self, action: selector)
    keyboardToolbar.items = [flexBarButton, doneBarButton]
    return keyboardToolbar
  }
}

open class TextBoxBase: ViewComponent, UITextViewDelegate {
  fileprivate weak var _delegate: AbstractMethodsForTextBox!
  
  fileprivate var _textAlignment: Int32 = 0
  fileprivate var _backgroundColor: Int32 = 0
  fileprivate var _fontTypeface = Typeface.normal
  fileprivate var _bold: Bool = false
  fileprivate var _italic: Bool = false
  fileprivate var _hint: String = ""
  fileprivate var _textColor: Int32 = 0

  public init(_ parent: ComponentContainer, _ delegate: AbstractMethodsForTextBox) {
    super.init(parent)
    _delegate = delegate
    super.setDelegate(delegate)
    parent.add(self)
    let widthConstraint = _delegate.view.widthAnchor.constraint(greaterThanOrEqualToConstant: CGFloat(kTextboxPreferredWidth))
    widthConstraint.priority = UILayoutPriority.defaultHigh
    widthConstraint.isActive = true
    FontSize = 14.0
    TextColor = Color.default.int32
  }

  internal func setDelegate(_ delegate: AbstractMethodsForTextBox) {
    _delegate = delegate
    super.setDelegate(delegate)
  }

  // MARK: TextboxBase Properties
  @objc open var TextAlignment: Int32 {
    get {
      return _textAlignment
    }
    set(alignment) {
      if let alignment = Alignment(rawValue: alignment) {
        _textAlignment = alignment.rawValue
        let isRTL = UIView.userInterfaceLayoutDirection(for: (_delegate.view.semanticContentAttribute)) == UIUserInterfaceLayoutDirection.rightToLeft
        switch alignment {
        case .normal:
          _delegate.alignment = isRTL ? .right : .left
        case .center:
          _delegate.alignment = .center
        case .opposite:
          _delegate.alignment = isRTL ? .left : .right
        }
      }
    }
  }
  
  @objc open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
      _delegate?.backgroundColor = argbToColor(argb)
    }
  }
  
  @objc open var Enabled: Bool {
    get {
      return (_delegate?.view.isUserInteractionEnabled)!
    }
    set(enabled) {
      _delegate?.view.isUserInteractionEnabled = enabled
    }
  }

  @objc open var FontBold: Bool {
    get {
      return _bold
    }
    set(bold) {
      _bold = bold
      _delegate.font = getFontTrait(font: _delegate.font, trait: .traitBold, shouldSet: bold)!
    }
  }
  
  @objc open var FontItalic: Bool {
    get {
      return _italic
    }
    set(italic) {
      _italic = italic
      _delegate.font = getFontTrait(font: _delegate.font, trait: .traitItalic, shouldSet: italic)!
    }
  }

  @objc open var FontSize: Float32 {
    get {
      if let size = _delegate?.font.pointSize {
        return Float32(size)
      } else {
        return 17.0  // default size
      }
    }
    set(size) {
      _delegate.font = getFontSize(font: _delegate?.font, size: size) ?? _delegate.font
    }
  }
  
  @objc open var FontTypeface: Int32 {
    get {
      return _fontTypeface.rawValue
    }
    set(typeface) {
      if typeface != _fontTypeface.rawValue {
        if let type = Typeface(rawValue: typeface) {
          _fontTypeface = type
          if let delegate = _delegate {
            delegate.font = getFontTypeface(font: delegate.font, typeFace: type) ?? delegate.font
          }
        }
      }
    }
  }
  
  @objc open var Hint: String {
    get {
      return _hint
    }
    set(hint) {
      _hint = hint
      _delegate?.placeholderText = hint
    }
  }

  @objc open var ReadOnly: Bool {
    get {
      return _delegate?.readOnly ?? false
    }
    set (ro) {
      _delegate?.readOnly = ro
    }
  }
  
  @objc open var Text: String {
    get {
      if let delegate = _delegate {
        if let text = delegate.text {
          return text
        }
      }
      return ""
    }
    set(text) {
      _delegate?.text = text
    }
  }
  
  @objc open var TextColor: Int32 {
    get {
      return _textColor
    }
    set(argb) {
      _textColor = argb
      if _textColor == Color.default.int32 {
        _delegate?.textColor = preferredTextColor(_container?.form)
      } else {
        _delegate?.textColor = argbToColor(argb)
      }
    }
  }
  
  // MARK: TextboxBase Methods
  @objc open func RequestFocus() {
    _delegate?.view.becomeFirstResponder()
  }
  
  // MARK: TextboxBase Events
  @objc open func GotFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "GotFocus")
  }
  
  @objc open func LostFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "LostFocus")
  }

  // MARK: UITextViewDelegate implementation
  open func textViewDidBeginEditing(_ textView: UITextView) {
    GotFocus()
  }

  open func textViewDidEndEditing(_ textView: UITextView) {
    LostFocus()
  }
}
