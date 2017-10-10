// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

fileprivate class TextBoxAdapter: AbstractMethodsForTextBox {
  fileprivate let _field = UITextField(frame: CGRect.zero)
  fileprivate let _view = UITextView(frame: CGRect.zero)
  fileprivate let _wrapper = UIView(frame: CGRect.zero)
  private var _multiLine = false

  fileprivate init() {
    _field.translatesAutoresizingMaskIntoConstraints = false
    _view.translatesAutoresizingMaskIntoConstraints = false
    _wrapper.translatesAutoresizingMaskIntoConstraints = false

    // Set up the minimum size constraint for the UITextView
    let heightConstraint = _view.heightAnchor.constraint(greaterThanOrEqualToConstant: 26.5)
    heightConstraint.priority = UILayoutPriorityDefaultHigh
    _view.addConstraint(heightConstraint)

    // We are single line by default
    makeSingleLine()
  }

  open var view: UIView {
    get {
      return _wrapper
    }
  }

  open var alignment: NSTextAlignment {
    get {
      return _field.textAlignment
    }
    set(alignment) {
      _field.textAlignment = alignment
      _view.textAlignment = alignment
    }
  }

  open var backgroundColor: UIColor? {
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

  open var textColor: UIColor? {
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

  open var font: UIFont {
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

  open var placeholderText: String? {
    get {
      return _field.placeholder
    }
    set(text) {
      // TODO(ewpatton): Implement for UITextView
      _field.placeholder = text
    }
  }

  open var text: String? {
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

  var multiLine: Bool {
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
}

open class TextBox: TextBoxBase {
  fileprivate let _adapter = TextBoxAdapter()
  fileprivate var _acceptsNumbersOnly = false

  public init(_ parent: ComponentContainer) {
    super.init(parent, _adapter)
    MultiLine = false
  }

  // MARK: TextBox Properties
  open var NumbersOnly: Bool {
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
      return _adapter.multiLine
    }
    set(multiLine) {
      _adapter.multiLine = multiLine
    }
  }

  // MARK: TextBox Methods
  public func HideKeyboard() {
    _adapter._view.resignFirstResponder()
  }
}
