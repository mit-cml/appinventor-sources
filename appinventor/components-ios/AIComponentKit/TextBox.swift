//
//  TextBox.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

fileprivate class TextBoxAdapter: AbstractMethodsForTextBox {
  fileprivate let _field = UITextField(frame: CGRect.zero)
  fileprivate let _view = UITextView(frame: CGRect.zero)
  fileprivate let _wrapper = UIView(frame: CGRect.zero)
  fileprivate var _multiLine = false

  public var view: UIView {
    get {
      return _wrapper
    }
  }

  public var alignment: NSTextAlignment {
    get {
      return _field.textAlignment
    }
    set(alignment) {
      _field.textAlignment = alignment
      _view.textAlignment = alignment
    }
  }

  public var backgroundColor: UIColor? {
    get {
      if _multiLine {
        return _view.backgroundColor
      } else {
        return _field.backgroundColor
      }
    }
    set(color) {
      _view.backgroundColor = color
      _field.backgroundColor = color
    }
  }

  public var textColor: UIColor? {
    get {
      if _multiLine {
        return _view.textColor
      } else {
        return _field.textColor
      }
    }
    set(color) {
      _view.textColor = color
      _field.textColor = color
    }
  }

  public var font: UIFont {
    get {
      if _multiLine {
        return _view.font!
      } else {
        return _field.font!
      }
    }
    set(font) {
      _view.font = font
      _field.font = font
    }
  }

  public var placeholderText: String? {
    get {
      return _field.placeholder
    }
    set(text) {
      // TODO(ewpatton): Implement for UITextView
      _field.placeholder = text
    }
  }

  public var text: String? {
    get {
      if _multiLine {
        return _view.text
      } else {
        return _field.text
      }
    }
    set(text) {
      _field.text = text
      _view.text = text
    }
  }
}

public class TextBox: TextBoxBase {
  private let _adapter = TextBoxAdapter()
  private var _acceptsNumbersOnly = false

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    self.setDelegate(_adapter)
  }

  // MARK: TextBox Properties
  public var NumbersOnly: Bool {
    get {
      return _acceptsNumbersOnly
    }
    set(acceptsNumbersOnly) {
      _acceptsNumbersOnly = acceptsNumbersOnly
      if acceptsNumbersOnly {
        _adapter._view.keyboardType = .numberPad
      } else {
        _adapter._view.keyboardType = .default
      }
    }
  }

  public var MultiLine: Bool {
    get {
      return _adapter._multiLine
    }
    set(multiLine) {
      if _adapter._multiLine == multiLine {
        return  // nothing to do
      }
      if _adapter._multiLine {
        _adapter._view.removeFromSuperview()
        _adapter._wrapper.addSubview(_adapter._field)
      } else {
        _adapter._field.removeFromSuperview()
        _adapter._wrapper.addSubview(_adapter._view)
      }
      _adapter._multiLine = multiLine
    }
  }

  // MARK: TextBox Methods
  public func HideKeyboard() {
    _adapter._view.resignFirstResponder()
  }
}
