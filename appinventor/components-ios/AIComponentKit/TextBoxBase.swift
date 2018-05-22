//
//  TextBoxBase.swift
//  AIComponentKit
//
//  Created by Evan Patton on 11/30/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol AbstractMethodsForTextBox: AbstractMethodsForViewComponent {
  var alignment: NSTextAlignment { get set }
  var backgroundColor: UIColor? { get set }
  var textColor: UIColor? { get set }
  var font: UIFont { get set }
  var placeholderText: String? { get set }
  var text: String? { get set }
}

open class TextBoxBase: ViewComponent, UITextViewDelegate {
  fileprivate weak var _delegate: AbstractMethodsForTextBox!
  
  fileprivate var _textAlignment: Int32 = 0
  fileprivate var _backgroundColor: Int32 = 0
  fileprivate var _fontTypeface: Int32 = 0
  fileprivate var _bold: Bool = false
  fileprivate var _italic: Bool = false
  fileprivate var _hint: String = ""
  fileprivate var _textColor: Int32 = 0

  public init(_ parent: ComponentContainer, _ delegate: AbstractMethodsForTextBox) {
    super.init(parent)
    _delegate = delegate
    super.setDelegate(delegate)
    parent.add(self)
    parent.setChildWidth(of: self, width: kTextboxPreferredWidth)
    FontSize = 14.0
  }

  internal func setDelegate(_ delegate: AbstractMethodsForTextBox) {
    _delegate = delegate
    super.setDelegate(delegate)
  }

  // MARK: TextboxBase Properties
  open var TextAlignment: Int32 {
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
  
  open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
      _delegate?.backgroundColor = argbToColor(argb)
    }
  }
  
  open var Enabled: Bool {
    get {
      return (_delegate?.view.isUserInteractionEnabled)!
    }
    set(enabled) {
      _delegate?.view.isUserInteractionEnabled = enabled
    }
  }

  open var FontBold: Bool {
    get {
      return _bold
    }
    set(bold) {
      _bold = bold
      _delegate.font = getFontTrait(font: _delegate.font, trait: .traitBold, shouldSet: bold)!
    }
  }
  
  open var FontItalic: Bool {
    get {
      return _italic
    }
    set(italic) {
      _italic = italic
      _delegate.font = getFontTrait(font: _delegate.font, trait: .traitItalic, shouldSet: italic)!
    }
  }

  open var FontSize: Float32 {
    get {
      if let size = _delegate?.font.pointSize {
        return Float32(size)
      } else {
        return 17.0  // default size
      }
    }
    set(size) {
      if let delegate = _delegate {
        delegate.font = delegate.font.withSize(CGFloat(size))
      }
    }
  }
  
  open var FontTypeface: Int32 {
    get {
      return _fontTypeface
    }
    set(typeface) {
      _fontTypeface = typeface
      // TODO(ewpatton): Replace font with new typeface
    }
  }
  
  open var Hint: String {
    get {
      return _hint
    }
    set(hint) {
      _hint = hint
      _delegate?.placeholderText = hint
    }
  }
  
  open var Text: String {
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
  
  open var TextColor: Int32 {
    get {
      return _textColor
    }
    set(argb) {
      _textColor = argb
      _delegate?.textColor = argbToColor(argb)
    }
  }
  
  // MARK: TextboxBase Methods
  open func RequestFocus() {
    _delegate?.view.becomeFirstResponder()
  }
  
  // MARK: TextboxBase Events
  open func GotFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "GotFocus")
  }
  
  open func LostFocus() {
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
