// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

private let kListViewCellIdentifier = "listview"

open class ListPickerActivity: UINavigationController {
  fileprivate var _tableViewController: UITableViewController!

  public required override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
    super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
  }

  public init(delegate: UITableViewDelegate, dataSource: UITableViewDataSource, cancelTarget: Any?, cancelAction: Selector?) {
    let tableVC = UITableViewController()
    super.init(rootViewController: tableVC)
    _tableViewController = tableVC
    _tableViewController.tableView.delegate = delegate
    _tableViewController.tableView.dataSource = dataSource
    let cancelButton = UIBarButtonItem(barButtonSystemItem: .cancel, target: cancelTarget, action: cancelAction)
    _tableViewController.navigationItem.leftBarButtonItem = cancelButton
  }

  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}

open class ListPicker: Picker, AbstractMethodsForPicker, UITableViewDataSource, UITableViewDelegate, UISearchBarDelegate {
  fileprivate var _items: [String] = []
  fileprivate var _viewController: ListPickerActivity?
  fileprivate var _selection: String = ""
  fileprivate var _selectionIndex: Int32 = 0
  fileprivate var _itemBackgroundColor: UIColor = UIColor.white
  fileprivate var _itemTextColor: UIColor = UIColor.black
  fileprivate var _needsReload = true
  fileprivate var _showSearch = false
  fileprivate var _searchBar = UISearchBar()
  fileprivate var _results: [String]? = nil

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    _searchBar.autocapitalizationType = .none
    _searchBar.searchBarStyle = .minimal
    _searchBar.barTintColor = UIColor.clear
    _searchBar.placeholder = "Search list..."
    _searchBar.delegate = self
    _searchBar.returnKeyType = .done
    super.setDelegate(self)
    _view.addTarget(self, action: #selector(click), for: UIControlEvents.primaryActionTriggered)
    _viewController = ListPickerActivity(delegate: self, dataSource: self, cancelTarget: self, cancelAction: #selector(cancelPicking(_:)))
    parent.add(self)
  }

  // MARK: ListPicker Properties
  open var Elements: [String] {
    get {
      return _items
    }
    set(items) {
      setItems(items: items)
    }
  }

  open var ElementsFromString: String {
    get {
      return ""
    }
    set(itemstring) {
      setItems(items: elementsFromString(itemstring))
    }
  }

  open var ItemBackgroundColor: Int32 {
    get {
      return colorToArgb(_itemBackgroundColor)
    }
    set(argb) {
      let newColor = argbToColor(argb)
      if _itemBackgroundColor != newColor {
        _itemBackgroundColor = argbToColor(argb)
        _viewController?._tableViewController.tableView.backgroundColor = _itemBackgroundColor
        _needsReload = true
      }
    }
  }

  open var ItemTextColor: Int32 {
    get {
      return colorToArgb(_itemTextColor)
    }
    set(argb) {
      let newColor = argbToColor(argb)
      if _itemTextColor != newColor {
        _itemTextColor = argbToColor(argb)
        _needsReload = true
      }
    }
  }

  open var ShowFilterBar: Bool {
    get {
      return _showSearch
    }
    set(showFilterBar) {
      _showSearch = showFilterBar
      _needsReload = true
    }
  }

  open var Selection: String {
    get {
      return _selection
    }
    set(value) {
      _selection = value
      if let i = _items.index(of: value) {
        _selectionIndex = Int32(i) + 1
      } else {
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
        _selection = _items[Int(index) - 1]
      } else {
        _selection = ""
      }
    }
  }

  open var Title: String {
    get {
      if let title = _viewController?._tableViewController.navigationItem.title {
        return title
      } else {
        return ""
      }
    }
    set(title) {
      _viewController?._tableViewController.navigationItem.title = title
    }
  }

  @objc fileprivate func cancelPicking(_ sender: Any?) {
    _viewController?.dismiss(animated: true, completion: {
      self.resetSearch()
      self._selection = ""
      self._selectionIndex = 0
      self.AfterPicking()
    })
  }

  private func setItems(items: [String]) {
    _items = items
    _needsReload = true
  }

  // MARK: AbstractMethodsForPicker
  open func open() {
    if _needsReload, let tableVC = _viewController?._tableViewController {
      tableVC.tableView.reloadData()
      _needsReload = false
    }
    _container.form.present(_viewController!, animated: true, completion: {})
  }

  // MARK: UITableViewDataSource
  open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    var cell = tableView.dequeueReusableCell(withIdentifier: kListViewCellIdentifier)
    if cell == nil {
      cell = UITableViewCell(style: .default, reuseIdentifier: kListViewCellIdentifier)
    }
    cell?.backgroundColor = _itemBackgroundColor
    let dataArr = _results ?? _items
    cell?.textLabel?.text = dataArr[indexPath.row]
    cell?.textLabel?.textColor = _itemTextColor
    return cell!
  }

  open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return _results?.count ?? _items.count
  }

  open func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
    return _showSearch ? _searchBar: nil
  }

  open func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
    return _showSearch ? 40: 0
  }

  // MARK: UITableViewDelegate
  open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    _viewController?.dismiss(animated: true, completion: {
      self.resetSearch()
      self._selection = self._items[indexPath.row]
      self._selectionIndex = Int32(indexPath.row) + 1
      self.AfterPicking()
    })
  }
  
  // MARK: UISearchBarDelegate
  open func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    _results = nil
    if !searchText.isEmpty  {
      _results = [String]()
      for item in _items {
        if item.starts(with: searchText) {
          _results!.append(item)
        }
      }
    }
    _viewController?._tableViewController.tableView.reloadData()
  }

  open func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
    searchBar.endEditing(true)
  }

  fileprivate func resetSearch() {
    _searchBar.text = ""
    _results = nil
    _viewController?._tableViewController.tableView.reloadData()
  }
}
