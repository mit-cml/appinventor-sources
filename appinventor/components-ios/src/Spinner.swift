// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

protocol SpinnerDelegate: UITableViewDelegate, UITableViewDataSource, UIPickerViewDelegate, UIPickerViewDataSource, UIPopoverPresentationControllerDelegate {
  func AfterSelecting()
  func CancelSelection(for pickerView: UIPickerView)
}

protocol SpinnerController {
  func selectItem(_ row: Int)
  func reloadComponents()
  var menuView: UIView { get }
}

fileprivate class SpinnerPadController: PickerPadController, SpinnerController {
  fileprivate var _menuView: UITableView
  fileprivate var _delegate: SpinnerDelegate

  public init(_ delegateDataSource: SpinnerDelegate) {
    _menuView = UITableView()
    _delegate = delegateDataSource
    super.init()
    _menuView.delegate = delegateDataSource
    _menuView.dataSource = delegateDataSource
  }

  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  @objc public func reloadComponents(){
    _menuView.reloadData()
  }

  @objc public func selectItem(_ row: Int){
    _menuView.selectRow(at: IndexPath(row: row, section: 0), animated: true, scrollPosition: UITableView.ScrollPosition.middle)
  }

  open override func viewDidLoad() {
    super.viewDidLoad()
  }

  override func setupViews() {
    view.addSubview(_menuView)
    _menuView.translatesAutoresizingMaskIntoConstraints = false
  }

  override func addLayoutConstraints() {
    _menuView.heightAnchor.constraint(equalTo: view.heightAnchor).isActive = true
    _menuView.widthAnchor.constraint(equalTo: view.widthAnchor).isActive = true
    _menuView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
    _menuView.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
    _menuView.isScrollEnabled = true
  }
  
  open var menuView: UIView {
    get {
      return _menuView
    }
  }
}

fileprivate class SpinnerPhoneController: PickerPhoneController, SpinnerController {
  fileprivate var _pickerView: UIPickerView
  fileprivate var _delegate: SpinnerDelegate

  public init(_ delegateDataSource: SpinnerDelegate, screen form: Form) {
    _pickerView = UIPickerView()
    _delegate = delegateDataSource
    super.init(contentView: _pickerView, screen: form)
    _pickerView.delegate = delegateDataSource
    _pickerView.dataSource = delegateDataSource
  }

  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  @objc public func reloadComponents(){
    _pickerView.reloadAllComponents()
  }

  @objc public func selectItem(_ row: Int){
    _pickerView.selectRow(row, inComponent: 0, animated: true)
  }

  override func doCancelPicker() {
    self._delegate.CancelSelection(for: _pickerView)
  }

  override func doDismissPicker(){
    if _pickerView.numberOfRows(inComponent: 0) > 0 {
      self._delegate.AfterSelecting()
    }
  }
  
  open var menuView: UIView {
    get {
      return _pickerView
    }
  }
}

open class Spinner: ButtonBase, AbstractMethodsForButton, SpinnerDelegate  {
  fileprivate var _items = [String]()
  fileprivate var _viewController: SpinnerController?
  fileprivate var _prompt: String = "add items..."
  fileprivate var _selectionIndex : Int32 = 0
  fileprivate var _selection: String = ""
  fileprivate let _isPhone = UIDevice.current.userInterfaceIdiom == .phone
  fileprivate var _currSelection: String = "" // saves selection state for cancel
  fileprivate var _currSelectionIndex: Int32 = 0 // saves selection state for cancel

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    if _isPhone {
      _viewController = SpinnerPhoneController(self, screen: parent.form!)
    } else {
      _viewController = SpinnerPadController(self)
    }
    _view.addTarget(self, action: #selector(click), for: UIControl.Event.primaryActionTriggered)
    Prompt = ""
    _currSelectionIndex = SelectionIndex
  }

  @objc open func AfterSelecting() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterSelecting", arguments: _selection as AnyObject)
    self.Text = _selection
  }

  @objc open func click() {
    if !_isPhone {
      if let popover = (_viewController as! UIViewController).popoverPresentationController {
        popover.delegate = self
        popover.sourceView = _view
        popover.sourceRect = _view.frame
        popover.canOverlapSourceViewRect = true
      }
    }
    _currSelection = _selection
    _currSelectionIndex = _selectionIndex
    _container?.form?.present(_viewController as! UIViewController, animated: true)

  }
  
  @objc open func CancelSelection(for pickerView: UIPickerView) {
    _selection = _currSelection
    _selectionIndex = _currSelectionIndex
    if _selectionIndex > 0 {
      pickerView.selectRow(Int(_selectionIndex) - 1, inComponent: 0, animated: true)
    }
  }

  @objc open func DisplayDropdown() {
    click()
  }

  @objc open var Elements: YailList<SCMValueProtocol> {
    get {
      return YailList<SCMValueProtocol>(array: _items as [AnyObject])
    }
    set(items) {
      var pendingSelectionIndex = SelectionIndex
      if (items.length == 0) {
        pendingSelectionIndex = 0
      } else if (items.length < _items.count && SelectionIndex > items.length) {
        pendingSelectionIndex = Int32(items.length)
      } else if (SelectionIndex == 0 && items.length > 0) {
        pendingSelectionIndex = 1
      }
      _items.removeAll()
      _items = (items as [AnyObject]).toStringArray()
      _viewController!.reloadComponents()
      SelectionIndex = pendingSelectionIndex
    }
  }

  @objc open var ElementsFromString: String {
    get {
      return ""
    }
    set(itemstring) {
      Elements = YailList<SCMValueProtocol>(array: elementsFromString(itemstring) as [AnyObject])
    }
  }

  open func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
    if _items.isEmpty {
      return
    }
    _selection = _items[row]
    _selectionIndex = Int32(row) + 1
  }

  open func numberOfComponents(in pickerView: UIPickerView) -> Int {
    return 1
  }

  open func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
    return _items.count
  }

  open func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
    return _items[row]
  }

  @objc open var Prompt : String {
    get {
      return _prompt
    }
    set(prompt) {
      _prompt = prompt
    }
  }

  @objc open var Selection: String {
    get {
      return _selection
    }
    set(value) {
      _selection = value
      if let i = _items.firstIndex(of: value) {
        self.Text = _selection
        _viewController!.selectItem(i)
        _selectionIndex = Int32(i) + 1
      } else {
        self.Text = ""
        _selectionIndex = 0
      }
    }
  }

  @objc open var SelectionIndex: Int32 {
    get {
      return _selectionIndex
    }
    set(index) {
      _selectionIndex = index
      if index > 0 && index <= Int32(_items.count) {
        _viewController!.selectItem(Int(index) - 1)
        _selection = _items[Int(index) - 1]
        self.Text = _selection
      } else {
        _selection = ""
        self.Text = ""
      }
    }
  }

  open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    var cell = tableView.dequeueReusableCell(withIdentifier: "spinner")
    if cell == nil {
      cell = UITableViewCell(style: .default, reuseIdentifier: "spinner")
    }
    cell?.textLabel?.text = _items[indexPath.row]
    cell?.textLabel?.textAlignment = NSTextAlignment.center
    return cell!
  }

  open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return _items.count
  }

  open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    (_viewController as! UIViewController).dismiss(animated: true, completion: {
      self._selection = self._items[indexPath.row]
      self._selectionIndex = Int32(indexPath.row) + 1
      self.AfterSelecting()
    })
  }

  open func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
    let view = UITextView()
    view.text = _prompt == "add items..." ? "Plese select an item": _prompt
    view.isEditable = false
    view.font = view.font?.withSize(30)
    view.textContainerInset = UIEdgeInsets(top: 0, left: 10, bottom: 0, right: 0)
    return view
  }
}

