//
//  TextBox.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class TextBox: TextBoxBase, AbstractMethodsForTextBox {
  private let _field = UITextField(frame: CGRect.zero)
  private let _view = UITextView(frame: CGRect.zero)
  private let _wrapper = UIView(frame: CGRect.zero)
  private var _acceptsNumbersOnly = false
  private var _multiLine = false
  
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
  }
  
  // MARK: TextBox Properties
  public var NumbersOnly: Bool {
    get {
      return _acceptsNumbersOnly
    }
    set(acceptsNumbersOnly) {
      _acceptsNumbersOnly = acceptsNumbersOnly
      if acceptsNumbersOnly {
        _view.keyboardType = .numberPad
      } else {
        _view.keyboardType = .default
      }
    }
  }
  
  public var MultiLine: Bool {
    get {
      return _multiLine
    }
    set(multiLine) {
      if _multiLine == multiLine {
        return  // nothing to do
      }
      if _multiLine {
        _view.removeFromSuperview()
        _wrapper.addSubview(_field)
      } else {
        _field.removeFromSuperview()
        _wrapper.addSubview(_view)
      }
      _multiLine = multiLine
    }
  }

  // MARK: TextBox Methods
  public func HideKeyboard() {
    _view.resignFirstResponder()
  }
  
  // MARK: AbstractMethodsForTextBox implementation
  public override var view: UIView {
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
      
    }
    set(color) {
      
    }
  }
  public var textColor: UIColor? {
    get {
      
    }
    set(color) {
      
    }
  }

  public var font: UIFont {
    get {
      
    }
    set(font) {
      
    }
  }

  public var placeholderText: String? {
    get {
      return _field.placeholder
    }
    set(text) {
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
