// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public final class Label: ViewComponent, AbstractMethodsForViewComponent, AccessibleComponent {
  fileprivate var _view: UILabel
  fileprivate var _alignment: Int32 = Alignment.normal.rawValue
  fileprivate var _typeface = Typeface.normal
  fileprivate var _bold = false
  fileprivate var _italic = false
  fileprivate var _hasMargins = false
  fileprivate var _htmlContent: String = ""
  fileprivate var _htmlFormat = false
  fileprivate var _fontSize = kFontSizeDefault
  fileprivate var _isBigText = false
  fileprivate var _textColor = Int32(bitPattern: Color.default.rawValue)
  public var HighContrast: Bool = false
  
  public override init(_ parent: ComponentContainer) {
    _view = UILabel()
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.text = ""
    _view.numberOfLines = 0
    _view.lineBreakMode = .byWordWrapping
    _view.font = _view.font.withSize(14.0)
    _view.textColor = preferredTextColor(parent.form)
    super.init(parent)
    super.setDelegate(self)
    parent.add(self)
    Height = kLengthPreferred
    Width = kLengthPreferred
    FontSize = kFontSizeDefault
  }

  func updateFontSize() {
    if _htmlFormat {
      updateFormattedContent()
    } else {
      if form?.BigDefaultText == true {
        if _fontSize == kFontSizeDefault {
          _view.font = _view.font.withSize(CGFloat(kFontSizeLargeDefault))
        } else {
          _view.font = _view.font.withSize(CGFloat(_fontSize))
        }
      } else {
        _view.font = _view.font.withSize(CGFloat(_fontSize))
      }
    }
    _view.sizeToFit()
    updateFormattedContent()
  }
  
  public override var view: UIView {
    get {
      return _view
    }
  }

  @objc public var TextAlignment: Int32 {
    get {
      return _alignment
    }
    set(alignment) {
      if let align = Alignment(rawValue: alignment) {
        _alignment = alignment
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
        switch(align) {
          case .normal:
            _view.textAlignment = rtl ? NSTextAlignment.right : NSTextAlignment.left
          case .center:
            _view.textAlignment = NSTextAlignment.center
          case .opposite:
            _view.textAlignment = rtl ? NSTextAlignment.left : NSTextAlignment.right
        }
      }
    }
  }
  
  @objc public var BackgroundColor: Int32 {
    get {
      if let color = _view.backgroundColor {
        return colorToArgb(color)
      }
      return Int32(bitPattern: Color.default.rawValue)
    }
    set(argb) {
      _view.backgroundColor = argbToColor(argb)
    }
  }
  
  @objc public var FontBold: Bool {
    get {
      return _bold
    }
    set(bold) {
      _bold = bold
      _view.font = getFontTrait(font: _view.font, trait: .traitBold, shouldSet: bold)
      if _htmlFormat {
        updateFormattedContent()
      }
    }
  }
  
  @objc public var FontItalic: Bool {
    get {
      return _italic
    }
    set(italic) {
      _italic = italic
      _view.font = getFontTrait(font: _view.font, trait: .traitItalic, shouldSet: italic)
      if _htmlFormat {
        updateFormattedContent()
      }
    }
  }
  
  @objc public var HasMargins: Bool {
    get {
      return _hasMargins
    }
    set(hasMargins) {
      _hasMargins = hasMargins
      _view.layoutMargins = hasMargins ?
        UIEdgeInsets(top: 4.0, left: 4.0, bottom: 4.0, right: 4.0) :
        UIEdgeInsets()
    }
  }
  
  @objc public var FontSize: Float {
    get {
      return Float(_view.font.pointSize)
    }
    set(size) {
      _fontSize = size
      updateFontSize()
    }
  }
  
  @objc public var FontTypeface: Int32 {
    get {
      return _typeface.rawValue
    }
    set(typeface) {
      if typeface != _typeface.rawValue {
        if let type = Typeface(rawValue: Int32(typeface)) {
          _typeface = type
          _view.font = getFontTypeface(font: _view.font, typeFace: type)
        }
        if _htmlFormat {
          updateFormattedContent()
        }
      }
    }
  }
  
  @objc public var LargeFont: Bool {
    get {
      return _isBigText
    }
    set (isLargeFont){
      _isBigText = isLargeFont
      updateFontSize()
    }
  }

  @objc public var Text: String {
    get {
      return _view.text ?? ""
    }
    set(text) {
      _htmlContent = text
      updateFormattedContent()
      _view.setNeedsUpdateConstraints()
      _view.setNeedsLayout()
      _container?.form?.view.setNeedsLayout()
    }
  }

  @objc public var HTMLContent: String {
    get {
      return _htmlContent
    }
  }

  @objc public var HTMLFormat: Bool {
    get {
      return _htmlFormat
    }
    set(fmt) {
      _htmlFormat = fmt
      updateFormattedContent()
    }
  }
  
  @objc public var TextColor: Int32 {
    get {
      return colorToArgb(_view.textColor)
    }
    set(argb) {
      _view.textColor = argbToColor(argb)
    }
  }

  // MARK: Private methods

  private func updateFormattedContent() {
    if _htmlFormat {
      var style = "font-size: \(_fontSize)pt;"
      switch _typeface {
      case .normal, .sansSerif:
        style += "font-family: sans-serif;"
      case .serif:
        style += "font-family: serif;"
      case .monospace:
        style += "font-family: monospace;"
      }
      if _bold {
        style += "font-weight: bold;"
      }
      if _italic {
        style += "font-style: italic;"
      }
      let data = ("<div style=\"\(style)\">" + _htmlContent + "</div>").data(using: .utf8) ?? Data()
      var options = [NSAttributedString.DocumentReadingOptionKey:Any]()
      options[NSAttributedString.DocumentReadingOptionKey.documentType] =
          NSAttributedString.DocumentType.html
      options[NSAttributedString.DocumentReadingOptionKey.characterEncoding] =
          NSUTF8StringEncoding
      _view.attributedText = try? NSAttributedString(data: data,
                                                     options: options,
                                                     documentAttributes: nil)
    } else {
      _view.text = _htmlContent
    }
  }
}
