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

public class TextBoxBase: ViewComponent, UITextViewDelegate {
  private weak var _delegate: AbstractMethodsForTextBox?
  
  private var _textAlignment: Int32 = 0
  private var _backgroundColor: Int32 = 0
  private var _fontTypeface: Int32 = 0
  private var _bold: Bool = false
  private var _italic: Bool = false
  private var _hint: String = ""
  private var _textColor: Int32 = 0

  public init(_ parent: ComponentContainer, _ delegate: AbstractMethodsForTextBox) {
    super.init(parent)
    super.setDelegate(delegate)
    parent.add(self)
    parent.setChildWidth(of: self, width: kTextboxPreferredWidth)
  }

  internal func setDelegate(_ delegate: AbstractMethodsForTextBox) {
    _delegate = delegate
    super.setDelegate(delegate)
  }

  // MARK: TextboxBase Properties
  public var TextAlignment: Int32 {
    get {
      return _textAlignment
    }
    set(alignment) {
      if let alignment = Alignment(rawValue: alignment) {
        _textAlignment = alignment.rawValue
        let isRTL = UIView.userInterfaceLayoutDirection(for: (_delegate?.view.semanticContentAttribute)!) == UIUserInterfaceLayoutDirection.rightToLeft
        switch alignment {
        case .normal:
          _delegate?.alignment = isRTL ? .right : .left
          break
        case .center:
          _delegate?.alignment = .center
          break
        case .opposite:
          _delegate?.alignment = isRTL ? .left : .right
          break
        }
      }
    }
  }
  
  public var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
      _delegate?.backgroundColor = argbToColor(argb)
    }
  }
  
  public var Enabled: Bool {
    get {
      return (_delegate?.view.isUserInteractionEnabled)!
    }
    set(enabled) {
      _delegate?.view.isUserInteractionEnabled = enabled
    }
  }
  
  public var FontBold: Bool {
    get {
      return _bold
    }
    set(bold) {
      _bold = bold
      // TODO(ewpatton): Update with bolded font
    }
  }
  
  public var FontItalic: Bool {
    get {
      return _italic
    }
    set(italic) {
      _italic = italic
      // TODO(ewpatton): Update with italicized font
    }
  }
  
  public var FontSize: Float32 {
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
  
  public var FontTypeface: Int32 {
    get {
      return _fontTypeface
    }
    set(typeface) {
      _fontTypeface = typeface
      // TODO(ewpatton): Replace font with new typeface
    }
  }
  
  public var Hint: String {
    get {
      return _hint
    }
    set(hint) {
      _hint = hint
      _delegate?.placeholderText = hint
    }
  }
  
  public var Text: String {
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
  
  public var TextColor: Int32 {
    get {
      return _textColor
    }
    set(argb) {
      _textColor = argb
      _delegate?.textColor = argbToColor(argb)
    }
  }
  
  // MARK: TextboxBase Methods
  public func RequestFocus() {
    _delegate?.view.becomeFirstResponder()
  }
  
  // MARK: TextboxBase Events
  public func GotFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "GotFocus")
  }
  
  public func LostFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "LostFocus")
  }

  // MARK: UITextViewDelegate implementation
  public func textViewDidBeginEditing(_ textView: UITextView) {
    GotFocus()
  }

  public func textViewDidEndEditing(_ textView: UITextView) {
    LostFocus()
  }
}
