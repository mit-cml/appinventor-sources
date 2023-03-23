// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

let kDefaultPlaceholderColor = UIColor(red: 0, green: 0, blue: 25/255, alpha: 0.22)

fileprivate protocol TextBoxDelegate: AbstractMethodsForTextBox, UITextFieldDelegate, UITextViewDelegate {
}

class TextBoxAdapter: NSObject, TextBoxDelegate {
  private let _field = UITextField(frame: CGRect.zero)
  fileprivate let _view = UITextView(frame: CGRect.zero)
  private let _wrapper = UIView(frame: CGRect.zero)
  private var _numbersOnly = false

  private var _multiLine = false
  private var _empty = true
  private var _readOnly = false
  private weak var _base: TextBoxBase? = nil
  
  override init() {
    super.init()
    _field.translatesAutoresizingMaskIntoConstraints = false
    _view.translatesAutoresizingMaskIntoConstraints = false
    _wrapper.translatesAutoresizingMaskIntoConstraints = false
    _view.textContainerInset = .zero
    _view.textContainer.lineFragmentPadding = 0
    _view.isSelectable = true
    _view.isEditable = true
    _view.delegate = self
    _field.delegate = self
    setupView()
    
    // We are single line by default
    makeSingleLine()
    textColor = UIColor.black

    // we want to be able to force unwrap
    text = ""
  }
  
  private func setupView() {
    // Set up the minimum size constraint for the UITextView
    let heightConstraint = _view.heightAnchor.constraint(greaterThanOrEqualToConstant: 20)
    heightConstraint.priority = UILayoutPriority.defaultHigh
    _view.addConstraint(heightConstraint)
    _view.isScrollEnabled = false
    let selector = #selector(dismissKeyboard)
    _view.inputAccessoryView = getAccesoryView(selector)
    _field.inputAccessoryView = getAccesoryView(selector)
  }

  open var view: UIView {
    get {
      return _wrapper
    }
  }

  @objc open var alignment: NSTextAlignment {
    get {
      return _field.textAlignment
    }
    set(alignment) {
      _field.textAlignment = alignment
      _view.textAlignment = alignment
    }
  }

  @objc open var backgroundColor: UIColor? {
    get {
      return _field.backgroundColor
    }
    set(color) {
      _view.backgroundColor = color
      _field.backgroundColor = color
    }
  }

  @objc open var textColor: UIColor? {
    get {
      return _field.textColor
    }
    set(color) {
      _field.textColor = color
      _view.textColor = _empty ? kDefaultPlaceholderColor : color
    }
  }

  @objc open var font: UIFont {
    get {
      return _field.font!
    }
    set(font) {
      _view.font = font
      _field.font = font
    }
  }

  @objc open var placeholderText: String? {
    get {
      return _field.placeholder
    }
    set(text) {
      _field.placeholder = text
      if _empty {
        _view.text = text
      }
    }
  }

  @objc open var readOnly: Bool {
    get {
      return _readOnly
    }
    set (ro) {
      _readOnly = ro
      _view.isEditable = !ro
    }
  }

  @objc open var text: String? {
    get {
      return _multiLine ? _view.text: _field.text
    }
    set(text) {
      _field.text = text
      _view.text = text
    }
  }

  @objc var multiLine: Bool {
    get {
      return _multiLine
    }
    set(multiLine) {
      if _multiLine == multiLine {
        return  // nothing to do
      }
      if multiLine {
        makeMultiLine()
      } else {
        makeSingleLine()
      }
    }
  }

  @objc var numbersOnly: Bool {
    get {
      return _numbersOnly
    }
    set(acceptsNumbersOnly) {
      if acceptsNumbersOnly != _numbersOnly {
        _numbersOnly = acceptsNumbersOnly
        let keyboardType: UIKeyboardType = acceptsNumbersOnly ? .decimalPad : .default
        _field.keyboardType = keyboardType
        _view.keyboardType = keyboardType
        _field.reloadInputViews()
        _view.reloadInputViews()
      }
    }
  }

  fileprivate func setEmpty(_ shouldEmpty: Bool) {
    _empty = shouldEmpty
    _view.text = _empty ? _field.placeholder: nil
    _view.textColor = _empty ? kDefaultPlaceholderColor : _field.textColor
  }

  private func makeMultiLine() {
    _field.removeFromSuperview()
    _wrapper.addSubview(_view)
    _wrapper.addConstraint(_view.heightAnchor.constraint(equalTo: _wrapper.heightAnchor))
    _wrapper.addConstraint(_view.widthAnchor.constraint(equalTo: _wrapper.widthAnchor))
    _wrapper.addConstraint(_view.topAnchor.constraint(equalTo: _wrapper.topAnchor))
    _wrapper.addConstraint(_view.leadingAnchor.constraint(equalTo: _wrapper.leadingAnchor))
    _multiLine = true
  }

  private func makeSingleLine() {
    _view.removeFromSuperview()
    _wrapper.addSubview(_field)
    _wrapper.addConstraint(_field.heightAnchor.constraint(equalTo: _wrapper.heightAnchor))
    _wrapper.addConstraint(_field.widthAnchor.constraint(equalTo: _wrapper.widthAnchor))
    _wrapper.addConstraint(_field.topAnchor.constraint(equalTo: _wrapper.topAnchor))
    _wrapper.addConstraint(_field.leadingAnchor.constraint(equalTo: _wrapper.leadingAnchor))
    _multiLine = false
  }

  func textViewDidBeginEditing(_ textView: UITextView) {
    if _empty {
      setEmpty(false)
    }
  }

  func textViewDidEndEditing(_ textView: UITextView) {
    _field.text = textView.text
    if textView.text.isEmpty {
      setEmpty(true)
    }
    _base?.LostFocus()
  }

  func textFieldDidBeginEditing(_ textField: UITextField) {
    if _empty {
      setEmpty(false)
    }
    _base?.GotFocus()
  }

  func textFieldDidEndEditing(_ textField: UITextField) {
    _view.text = textField.text
    if textField.text?.isEmpty ?? true {
      setEmpty(true)
    }
    _base?.LostFocus()
  }

  func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
    guard !_readOnly else {
      return false
    }
    if _numbersOnly {
      let decimalSeparator = Locale.current.decimalSeparator ?? "."
      let escapedDecimalSeparator = decimalSeparator == "." ? "\\." : ","
      var copyOfText = String(textField.text ?? "")
      copyOfText = copyOfText.replacingCharacters(in: Range(range, in: copyOfText)!, with: string)
      let express = try! NSRegularExpression(pattern: "^[-+]?([0-9]*\(escapedDecimalSeparator))?[0-9]*$", options: [])
      return express.numberOfMatches(in: copyOfText, options: [], range: NSRange(0..<copyOfText.count)) == 1
    }
    return true
  }

  func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
    if _numbersOnly {
      let decimalSeparator = Locale.current.decimalSeparator ?? "."
      let escapedDecimalSeparator = decimalSeparator == "." ? "\\." : ","
      var copyOfText = String(textView.text ?? "")
      copyOfText = copyOfText.replacingCharacters(in: Range(range, in: copyOfText)!, with: text)
      let express = try! NSRegularExpression(pattern: "^[-+]?([0-9]*\(escapedDecimalSeparator))?[0-9]*$", options: [])
      return express.numberOfMatches(in: copyOfText, options: [], range: NSRange(0..<copyOfText.count)) == 1
    }
    return true
  }

  @objc func dismissKeyboard() {
    _view.endEditing(true)
    _field.endEditing(true)
  }
  
  func setTextbase(_ base: TextBoxBase) {
    _base = base
  }
}

open class TextBox: TextBoxBase {
  fileprivate let _adapter = TextBoxAdapter()
  fileprivate var _colorSet = false
  fileprivate var _empty = true

  @objc public init(_ parent: ComponentContainer) {
    super.init(parent, _adapter)
    MultiLine = false
    _adapter.setTextbase(self)
  }

  // MARK: TextBox Properties
  @objc open var NumbersOnly: Bool {
    get {
      return _adapter.numbersOnly
    }
    set(acceptsNumbersOnly) {
      _adapter.numbersOnly = acceptsNumbersOnly
    }
  }

  @objc open override var Height: Int32 {
    didSet {
      _adapter._view.isScrollEnabled = Height != kLengthPreferred
    }
  }

  @objc public var MultiLine: Bool {
    get {
      return _adapter.multiLine
    }
    set(multiLine) {
      _adapter.multiLine = multiLine
    }
  }

  // MARK: TextBox Methods
  @objc public func HideKeyboard() {
    _adapter._view.resignFirstResponder()
  }
}
