//
//  ListPicker.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

private let kListViewCellIdentifier = "listview"

public class ListPickerActivity: UINavigationController {
  fileprivate var _tableViewController: UITableViewController!

  public required override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
    super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
  }

  public init(delegate: UITableViewDelegate, dataSource: UITableViewDataSource, cancelTarget: Any?, cancelAction: Selector?) {
    _tableViewController = UITableViewController()
    _tableViewController.tableView.delegate = delegate
    _tableViewController.tableView.dataSource = dataSource
    let cancelButton = UIBarButtonItem(barButtonSystemItem: .cancel, target: cancelTarget, action: cancelAction)
    _tableViewController.navigationItem.leftBarButtonItem = cancelButton
    super.init(rootViewController: _tableViewController)
  }

  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}

public class ListPicker: Picker, AbstractMethodsForPicker, UITableViewDataSource, UITableViewDelegate {
  private var _items: [String] = []
  private var _viewController: ListPickerActivity?
  private var _selection: String = ""
  private var _selectionIndex: Int32 = 0
  private var _itemBackgroundColor: UIColor = UIColor.white
  private var _itemTextColor: UIColor = UIColor.black

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    _view.addTarget(self, action: #selector(click), for: UIControlEvents.primaryActionTriggered)
    _viewController = ListPickerActivity(delegate: self, dataSource: self, cancelTarget: self, cancelAction: #selector(cancelPicking(_:)))
    parent.add(self)
  }

  // MARK: ListPicker Properties
  public var ElementsFromString: String {
    get {
      return ""
    }
    set(itemstring) {
      _items = elementsFromString(itemstring)
    }
  }

  public var ItemBackgroundColor: Int32 {
    get {
      return colorToArgb(_itemBackgroundColor)
    }
    set(argb) {
      _itemBackgroundColor = argbToColor(argb)
      _viewController?._tableViewController.tableView.reloadData()
    }
  }

  public var ItemTextColor: Int32 {
    get {
      return colorToArgb(_itemTextColor)
    }
    set(argb) {
      _itemTextColor = argbToColor(argb)
      _viewController?._tableViewController.tableView.reloadData()
    }
  }

  public var Selection: String {
    get {
      return _selection
    }
    set(value) {
      _selection = value
      if let i = _items.index(of: value) {
        _selectionIndex = i + 1
      } else {
        _selectionIndex = 0
      }
    }
  }

  public var SelectionIndex: Int32 {
    get {
      return _selectionIndex
    }
    set(index) {
      _selectionIndex = index
      if index > 0 && index <= Int32(_items.count) {
        _selection = _items[index - 1]
      } else {
        _selection = ""
      }
    }
  }

  public var Title: String {
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
      self._selection = ""
      self._selectionIndex = 0
      self.AfterPicking()
    })
  }

  // MARK: AbstractMethodsForPicker
  public func open() {
    _container.form?.present(_viewController!, animated: true, completion: {})
  }

  // MARK: UITableViewDataSource
  public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    var cell = tableView.dequeueReusableCell(withIdentifier: kListViewCellIdentifier)
    if cell == nil {
      cell = UITableViewCell(style: .default, reuseIdentifier: kListViewCellIdentifier)
    }
    cell?.textLabel?.text = _items[indexPath.row]
    return cell!
  }

  public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return _items.count
  }

  // MARK: UITableViewDelegate
  public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    _viewController?.dismiss(animated: true, completion: {
      self._selection = self._items[indexPath.row]
      self._selectionIndex = indexPath.row + 1
      self.AfterPicking()
    })
  }
}
