// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

public final class Label: ViewComponent, AbstractMethodsForViewComponent {
  fileprivate var _view: UILabel
  fileprivate var _alignment: Int32 = Alignment.normal.rawValue
  fileprivate var _typeface: Int32 = Typeface.normal.rawValue
  fileprivate var _bold = false
  fileprivate var _italic = false
  fileprivate var _hasMargins = false
  
  public override init(_ parent: ComponentContainer) {
    _view = UILabel()
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.text = ""
    _view.numberOfLines = 0
    _view.lineBreakMode = .byWordWrapping
    _view.font = _view.font.withSize(14.0)
    super.init(parent)
    super.setDelegate(self)
    parent.add(self)
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
      return false
    }
    set(bold) {
      
    }
  }
  
  @objc public var FontItalic: Bool {
    get {
      return false
    }
    set(italic) {
      
    }
  }
  
  @objc public var HasMargins: Bool {
    get {
      return false
    }
    set(hasMargins) {
      
    }
  }
  
  @objc public var FontSize: Float64 {
    get {
      return Float64(_view.font.pointSize)
    }
    set(size) {
      _view.font = _view.font.withSize(CGFloat(size))
      _view.sizeToFit()
    }
  }
  
  @objc public var FontTypeface: Int32 {
    get {
      return Typeface.normal.rawValue
    }
    set(typeface) {
      
      _view.sizeToFit()
    }
  }
  
  @objc public var Text: String {
    get {
      if let text = _view.text {
        return text
      } else {
        return ""
      }
    }
    set(text) {
      _view.text = text
      _view.widthAnchor.constraint(equalToConstant:_view.intrinsicContentSize.width).isActive = true
      _view.setNeedsUpdateConstraints()
      _view.setNeedsLayout()
      _container.form.view.setNeedsLayout()
    }
  }
  
  @objc public var HTMLFormat: Bool {
    get {
      return false
    }
    set(fmt) {
      
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
}
