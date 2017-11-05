// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

protocol SpinnerDelegate: UITableViewDelegate, UITableViewDataSource, UIPickerViewDelegate, UIPickerViewDataSource, UIPopoverPresentationControllerDelegate {
    func AfterSelecting()
}

protocol SpinnerController {
    func selectItem(_ row: Int)
    func reloadComponents()
}

fileprivate class SpinnerPadController: UIViewController, SpinnerController {
    fileprivate var _menuView: UITableView
    fileprivate var _delegate: SpinnerDelegate

    
    public init(_ delegateDataSource: SpinnerDelegate) {
        _menuView = UITableView()
        _delegate = delegateDataSource
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .popover
        _menuView.delegate = delegateDataSource
        _menuView.dataSource = delegateDataSource

    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func reloadComponents(){
        _menuView.reloadData()
    }
    
    public func selectItem(_ row: Int){
        _menuView.selectRow(at: IndexPath(row: row, section: 0), animated: true, scrollPosition: UITableViewScrollPosition.middle)
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
        view.addSubview(_menuView)
        _menuView.translatesAutoresizingMaskIntoConstraints = false
        _menuView.heightAnchor.constraint(equalTo: view.heightAnchor).isActive = true
        _menuView.widthAnchor.constraint(equalTo: view.widthAnchor).isActive = true
        _menuView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        _menuView.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
        _menuView.isScrollEnabled = true
    }
}

fileprivate class SpinnerPhoneController: UIViewController, SpinnerController {
    fileprivate var _pickerView: UIPickerView
    fileprivate var _toolBar: UIToolbar
    fileprivate var _delegate: SpinnerDelegate
    
    public init(_ delegateDataSource: SpinnerDelegate) {
        _pickerView = UIPickerView()
        _toolBar = UIToolbar()
        _delegate = delegateDataSource
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .custom
        _pickerView.delegate = delegateDataSource
        _pickerView.dataSource = delegateDataSource
        _toolBar.barStyle = UIBarStyle.default
        let doneButton = UIBarButtonItem(title: "Done", style: UIBarButtonItemStyle.plain, target: self, action: #selector(self.dismissPicker))
        _toolBar.setItems([doneButton], animated: true)
        _toolBar.isUserInteractionEnabled = true
        _toolBar.sizeToFit()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func reloadComponents(){
        _pickerView.reloadAllComponents()
    }
    
    public func selectItem(_ row: Int){
        _pickerView.selectRow(row, inComponent: 0, animated: true)
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
        view.addSubview(_toolBar)
        view.addSubview(_pickerView)
        _pickerView.heightAnchor.constraint(equalTo: view.heightAnchor, multiplier: 1.5 / 7).isActive = true
        _toolBar.bottomAnchor.constraint(equalTo: _pickerView.topAnchor).isActive = true
        _toolBar.heightAnchor.constraint(equalToConstant: 50)
        _toolBar.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        _toolBar.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        _toolBar.translatesAutoresizingMaskIntoConstraints = false
        _pickerView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
        _pickerView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        _pickerView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        _pickerView.translatesAutoresizingMaskIntoConstraints = false
    }
    
    public func dismissPicker(){
        self._delegate.AfterSelecting()
        self.dismiss(animated: true)
    }
}

open class Spinner: ButtonBase, AbstractMethodsForButton, SpinnerDelegate  {
    fileprivate var _items: [String] = []
    fileprivate var _viewController: SpinnerController?
    fileprivate var _prompt: String = "add items..."
    fileprivate var _selectionIndex : Int32 = 0
    fileprivate var _selection: String = ""
    fileprivate let _isPhone = UIDevice.current.userInterfaceIdiom == .phone
    
    public override init(_ parent: ComponentContainer) {
        super.init(parent)
        super.setDelegate(self)
        if _isPhone {
            _viewController = SpinnerPhoneController(self)
        } else {
            _viewController = SpinnerPadController(self)
        }
        _view.addTarget(self, action: #selector(click), for: UIControlEvents.primaryActionTriggered)
        parent.add(self)
        self.Text = _prompt
    }
    
    open func AfterSelecting() {
        EventDispatcher.dispatchEvent(of: self, called: "AfterSelecting", arguments: _selection as AnyObject)
        self.Text = _selection
    }
    
    open func click() {
        if !_isPhone {
            if let popover = (_viewController as! UIViewController).popoverPresentationController {
                popover.delegate = self
                popover.sourceView = _view
                popover.sourceRect = _view.frame
            }
        }
        _container.form.present(_viewController as! UIViewController, animated: true)
    }
    
    open func DisplayDropdown() {
        click()
    }
    
    open var Elements: [String] {
        get {
            return _items
        }
        set(items) {
            _items = items
            _viewController!.reloadComponents()
            self.Selection = ""
        }
    }
    
    open var ElementsFromString: String {
        get {
            return ""
        }
        set(itemstring) {
            _items = elementsFromString(itemstring)
            _viewController!.reloadComponents()
            self.Selection = ""
        }
    }
    
    open func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
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

    open var Prompt : String {
        get {
            return _prompt
        }
        set(prompt) {
            _prompt = prompt
            if _selection == "" {
                self.Text = _prompt
            }
        }
    }
    
    open var Selection: String {
        get {
            return _selection
        }
        set(value) {
            _selection = value
            if let i = _items.index(of: value) {
                self.Text = _selection
                _viewController!.selectItem(i)
                _selectionIndex = Int32(i) + 1
            } else {
                self.Text = _prompt
                _selectionIndex = 0
            }
        }
    }
    
    open var SelectionIndex: Int32 {
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
                self.Text = _prompt
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
        return view
    }
}

