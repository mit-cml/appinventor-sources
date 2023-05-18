// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import Contacts

class EmailPickerAdapter: NSObject, AbstractMethodsForTextBox, UITextFieldDelegate {
  fileprivate let _field = UITextField(frame: CGRect.zero)
  fileprivate let _wrapper = UIView(frame: CGRect.zero)
  private var _readOnly = false
  private weak var _base: TextBoxBase?

  override init() {
    super.init()
    _field.translatesAutoresizingMaskIntoConstraints = false
    _field.delegate = self
    _wrapper.translatesAutoresizingMaskIntoConstraints = false
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

  open var readOnly: Bool {
    get {
      return _readOnly
    }
    set (ro) {
      _readOnly = ro
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

  open func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
    return !_readOnly
  }

  private func makeSingleLine() {
    _wrapper.addSubview(_field)
    _wrapper.addConstraint(_field.heightAnchor.constraint(equalTo: _wrapper.heightAnchor))
    _wrapper.addConstraint(_field.widthAnchor.constraint(equalTo: _wrapper.widthAnchor))
    _wrapper.addConstraint(_field.topAnchor.constraint(equalTo: _wrapper.topAnchor))
    _wrapper.addConstraint(_field.leadingAnchor.constraint(equalTo: _wrapper.leadingAnchor))
  }
  
  func setTextbase(_ base: TextBoxBase) {
    _base = base
  }
  
  func textViewDidBeginEditing(_ textView: UITextView) {
    _base?.GotFocus()
  }
  
  func textFieldDidBeginEditing(_ textField: UITextField) {
    _base?.GotFocus()
  }
  
  func textFieldDidEndEditing(_ textField: UITextField) {
    _base?.LostFocus()
  }
}

protocol EmailPickerProtocol: UITextFieldDelegate, UITableViewDataSource, UITableViewDelegate {
}

fileprivate class Node {
  var value: Character
  var children: [Node] = []

  init(_ value: Character) {
    self.value = value
  }

  func addChild(_ char: Character) -> Node  {
    if children.count == 0 {
      children.append(Node(char))
      return children[0]
    }
    else {
      let index = binSearch(char)
      if children[index].value != char {
        let offset = children[index].value > char ? 0: 1
        children.insert(Node(char), at: index + offset)
        return children[index + offset]
      }
      return children[index]
    }
  }

  func findChild(_ char: Character) -> Node? {
    if children.count == 0 {
      return nil
    }
    let index = binSearch(char)
    if children[index].value == char {
      return children[index]
    } else {
      return nil
    }
  }

  func binSearch(_ char: Character) -> Int {
    var start = 0
    var end = children.count - 1
    while start <= end {
      let middle = (start + end) / 2
      if children[middle].value > char {
        end = middle - 1
      } else if children[middle].value < char {
        start = middle + 1
      } else {
        return middle
      }
    }
    return (start + end) / 2
  }
}

//MARK: A binary search trie (chosen instead of a full alphabet to reduce size); there should only be one instance for all EmailPickers
fileprivate struct Tree {
  fileprivate var base : Node
  fileprivate let lock = DispatchSemaphore(value: 1)

  init(){
    let baseChar : Character = " "
    base = Node(baseChar)
  }

  func isEmpty() -> Bool {
    lock.wait()
    defer {lock.signal()}
    return base.children.count == 0
  }

  mutating func add(_ word: String){
    lock.wait()
    var next = base
    for char in word {
      next = next.addChild(char)
    }
    lock.signal()
  }

  func getAll(_ partial: String) -> [String] {
    var next: Node? = self.base
    var base = ""
    for char in partial {
      next = next?.findChild(char)
      if next == nil {
        return []
      }
      base += "\(char)"
    }
    return recurse(node: next!, base: String(base.dropLast()))
  }

  private func recurse(node: Node, base: String) -> [String] {
    if node.children.count == 0 {
      return ["\(base)\(node.value)"]
    } else {
      var strings: [String] = []
      for child in node.children {
        strings += recurse(node: child, base: "\(base)\(node.value)")
      }
      return strings
    }
  }

  func getClosest(_ word: String) -> String{
    var next: Node? = base
    var combined = ""
    for char in word {
      next = next?.findChild(char)
      if next == nil {
        return combined
      }
      combined += "\(char)"
    }
    while next!.children.count > 0 {
      next = next!.children[0]
      combined += "\(next!.value)"
    }
    return combined
  }
}

//MARK: Has one single trie for all emails, not updated if contacts change while the app is open
open class EmailPicker: TextBoxBase, EmailPickerProtocol {
  fileprivate let _adapter = EmailPickerAdapter()
  fileprivate static var tree = Tree()
  fileprivate var _accView = UIInputView(frame: CGRect(x: 0, y: 0, width: 10, height: 80), inputViewStyle: .keyboard)
  fileprivate var _toolBar = UIToolbar()
  fileprivate var _tableView = UITableView()
  fileprivate var options: [String] = []
  fileprivate let separator = " " //MARK: Left here because unsure whether to automatically separate autocompleted emails or not
  fileprivate var toolBarConstraint : NSLayoutConstraint?

  @objc public init(_ parent: ComponentContainer) {
    super.init(parent, _adapter)
    _adapter.setTextbase(self)
    if EmailPicker.tree.isEmpty() {
      let store = CNContactStore()
      store.requestAccess(for: .contacts) { granted, error in
        guard granted else {
          return
        }
        let request = CNContactFetchRequest(keysToFetch: [CNContactEmailAddressesKey as CNKeyDescriptor])
        do {
          try store.enumerateContacts(with: request) { contact, stop in
            for email in contact.emailAddresses {
              EmailPicker.tree.add(email.value as String)
            }
          }
        } catch {}
      }
    }
    _adapter._field.keyboardType = .emailAddress
    _adapter._field.autocapitalizationType = .none
    _adapter._field.autocorrectionType = .no
    _adapter._field.spellCheckingType = .no
    _adapter._field.inputView?.addSubview(_accView)
    _adapter._field.inputAccessoryView = _accView

    _accView.backgroundColor = UIColor.white

    _accView.addSubview(_toolBar)
    _accView.addSubview(_tableView)

    _toolBar.barStyle = UIBarStyle.default
    let doneButton = UIBarButtonItem(title: "Done", style: UIBarButtonItem.Style.plain, target: self, action: #selector(dismissAcc))
    _toolBar.setItems([doneButton], animated: true)
    _toolBar.isUserInteractionEnabled = true
    _toolBar.translatesAutoresizingMaskIntoConstraints = false
    _toolBar.leadingAnchor.constraint(equalTo: _accView.leadingAnchor).isActive = true
    _toolBar.trailingAnchor.constraint(equalTo: _accView.trailingAnchor).isActive = true
    toolBarConstraint = _toolBar.topAnchor.constraint(equalTo: _accView.topAnchor, constant: 50)
    toolBarConstraint?.isActive = true
    _toolBar.heightAnchor.constraint(equalToConstant: 30).isActive = true

    _tableView.translatesAutoresizingMaskIntoConstraints = false
    _tableView.isHidden = true
    _tableView.heightAnchor.constraint(equalToConstant: 50).isActive = true
    _tableView.leadingAnchor.constraint(equalTo: _accView.leadingAnchor).isActive = true
    _tableView.trailingAnchor.constraint(equalTo: _accView.trailingAnchor).isActive = true
    _tableView.bottomAnchor.constraint(equalTo: _accView.bottomAnchor).isActive = true
    _tableView.delegate = self
    _tableView.dataSource = self
    _tableView.separatorStyle = .none
  }

  @objc open func dismissAcc() {
    _adapter._field.resignFirstResponder()
  }

  open override func RequestFocus() {
    _adapter._field.becomeFirstResponder()
  }

  open func numberOfSections(in tableView: UITableView) -> Int {
    return 1
  }

  open func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    return 25
  }

  open func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
    _adapter._field.becomeFirstResponder()
    if let aString = _adapter.text {
      let newString = aString.replacingCharacters(in: Range(range, in: aString)!, with: string)
      let strings = newString.split(separator: " ")
      if newString.count > 0 && newString.last != " " {
        options = EmailPicker.tree.getAll(String(strings[strings.count - 1]))
        toolBarConstraint?.constant = 0
        _tableView.isHidden = false
      } else {
        toolBarConstraint?.constant = 50

        _tableView.isHidden = true
        options = []
      }
      _tableView.reloadData()
    }
    return true
  }

  open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    if let index = _adapter.text?.range(of: " ", options: .backwards)?.lowerBound {
      _adapter.text = _adapter.text![..<index] + " " + options[indexPath.row] + separator
    } else {
      _adapter.text = options[indexPath.row] + separator
    }
    options = []
    _tableView.reloadData()
  }

  open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return options.count
  }

  open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    var cell = tableView.dequeueReusableCell(withIdentifier: "emailpicker")
    if cell == nil {
      cell = UITableViewCell(style: .default, reuseIdentifier: "emailpicker")
    }
    cell?.textLabel?.text = options[indexPath.row]
    cell?.textLabel?.textAlignment = NSTextAlignment.center
    return cell!
  }
}

