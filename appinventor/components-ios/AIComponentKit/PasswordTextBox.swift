//
//  PasswordTextBox.swift
//  AIComponentKit
//
//  Created by Evan Patton on 12/1/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

fileprivate class TextBoxAdapter: NSObject, AbstractMethodsForTextBox, UITextFieldDelegate {
  fileprivate let _field = UITextField(frame: CGRect.zero)

  fileprivate override init() {
    _field.translatesAutoresizingMaskIntoConstraints = false
    _field.isSecureTextEntry = true
    super.init()
    _field.inputAccessoryView = getAccesoryView(#selector(dismissKeyboard))
    _field.delegate = self
  }

  open var view: UIView {
    get {
      return _field
    }
  }

  open var alignment: NSTextAlignment {
    get {
      return _field.textAlignment
    }
    set(alignment) {
      _field.textAlignment = alignment
    }
  }

  open var backgroundColor: UIColor? {
    get {
      return _field.backgroundColor
    }
    set(color) {
      _field.backgroundColor = color
    }
  }

  open var textColor: UIColor? {
    get {
      return _field.textColor
    }
    set(color) {
      _field.textColor = color
    }
  }

  open var font: UIFont {
    get {
      return _field.font!
    }
    set(font) {
      _field.font = font
    }
  }

  open var placeholderText: String? {
    get {
      return _field.placeholder
    }
    set(text) {
      _field.placeholder = text
    }
  }

  open var text: String? {
    get {
      return _field.text
    }
    set(text) {
      _field.text = text
    }
  }

  @objc func dismissKeyboard() {
    _field.endEditing(true)
  }

  // prevents clearing of password field when changing 
  func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
    if let range = Range(range, in: textField.text ?? "") {
      textField.text?.replaceSubrange(range, with: string)
    }
    return false
  }

  func togggleVisible(_ visible: Bool) {
    _field.isSecureTextEntry = !visible
    // fix cursor position
    let text = _field.text ?? ""
    _field.text = ""
    _field.text = text
  }
}

open class PasswordTextBox: TextBoxBase {
  fileprivate let _adapter = TextBoxAdapter()

  @objc public init(_ parent: ComponentContainer) {
    super.init(parent, _adapter)
  }

  @objc open var PasswordVisible: Bool = false {
    didSet {
      _adapter.togggleVisible(PasswordVisible)
      // maintain proper type face
      FontTypeface = FontTypeface + 0
    }
  }
}
