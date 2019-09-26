// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

fileprivate let kListViewDefaultBackgroundColor = Color.black
fileprivate let kListViewDefaultSelectionColor = Color.lightGray
fileprivate let kListViewDefaultTextColor = Color.white
fileprivate let kDefaultTableCell = "UITableViewCell"
fileprivate let kDefaultTableCellHeight = CGFloat(44.0)

open class ListView: ViewComponent, AbstractMethodsForViewComponent, UITableViewDataSource, UITableViewDelegate {
  fileprivate final var _view: UITableView
  fileprivate var _backgroundColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _elements = [String]()
  fileprivate var _selection = ""
  fileprivate var _selectionColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _selectionIndex = Int32(0)
  fileprivate var _showFilter = false
  fileprivate var _textColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _textSize = Int32(22)
  fileprivate var _automaticHeightConstraint: NSLayoutConstraint!

  public override init(_ parent: ComponentContainer) {
    _view = UITableView(frame: CGRect.zero, style: .plain)
    super.init(parent)
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.delegate = self
    _view.dataSource = self
    self.setDelegate(self)
    parent.add(self)
    Width = kLengthFillParent
    _view.tableFooterView = UIView()
    _view.backgroundView = nil
    _view.backgroundColor = UIColor.black

    // The intrinsic height of the ListView needs to be explicit because UITableView does not
    // provide a value. We use a low priority constraint to configure the height, and size it based
    // on the number of rows (min 1). Updates to the constant in the constraint are done in the
    // Elements property setter.
    _automaticHeightConstraint = _view.heightAnchor.constraint(equalToConstant: kDefaultTableCellHeight)
    _automaticHeightConstraint.priority = UILayoutPriority(1.0)
    _automaticHeightConstraint.isActive = true
  }

  open override var view: UIView {
    get {
      return _view
    }
  }

  // MARK: Properties
  @objc open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(backgroundColor) {
      if (backgroundColor == Int32(bitPattern: Color.default.rawValue)) {
        _backgroundColor = Int32(bitPattern: kListViewDefaultBackgroundColor.rawValue)
      } else {
        _backgroundColor = backgroundColor
      }
      _view.backgroundColor = argbToColor(_backgroundColor)
    }
  }

  @objc open var ElementsFromString: String {
    get {
      return ""
    }
    set(elements) {
      Elements = elements.split(",")
    }
  }

  @objc open var Elements: [String] {
    get {
      return _elements
    }
    set(elements) {
      _elements = elements
      _automaticHeightConstraint.constant = _elements.isEmpty ? kDefaultTableCellHeight : kDefaultTableCellHeight * CGFloat(_elements.count)
      _view.reloadData()
    }
  }

  @objc open var Selection: String {
    get {
      return _selection
    }
    set(selection) {
      if let selectedRow = _view.indexPathForSelectedRow {
        _view.deselectRow(at: selectedRow, animated: false)
      }
      if let index = _elements.firstIndex(of: selection) {
        _selectionIndex = Int32(index) + 1
        _selection = selection
        _view.selectRow(at: IndexPath(item: index, section: 0), animated: true, scrollPosition: .none)
      } else {
        _selectionIndex = 0
        _selection = ""
      }
    }
  }

  @objc open var SelectionColor: Int32 {
    get {
      return _selectionColor
    }
    set(selectionColor) {
      _selectionColor = selectionColor
      _view.reloadData()
    }
  }

  @objc open var SelectionIndex: Int32 {
    get {
      return _selectionIndex + 1
    }
    set(selectionIndex) {
      _selectionIndex = selectionIndex - 1
      if _selectionIndex < 0 {
        _view.deselectRow(at: _view.indexPathForSelectedRow!, animated: true)
      } else {
        _view.selectRow(at: IndexPath(row: Int(_selectionIndex), section: 0), animated: true, scrollPosition: UITableView.ScrollPosition.middle)
      }
    }
  }

  @objc open var ShowFilterBar: Bool {
    get {
      return _showFilter
    }
    set(filterBar) {
      _showFilter = filterBar
      _view.tableHeaderView = _showFilter ? UISearchBar() : nil
    }
  }

  @objc open var TextColor: Int32 {
    get {
      return _textColor
    }
    set(textColor) {
      _textColor = textColor
      _view.reloadData()
    }
  }

  @objc open var TextSize: Int32 {
    get {
      return _textSize
    }
    set(textSize) {
      _textSize = textSize < 0 ? Int32(7) : textSize
      _view.reloadData()
    }
  }

  // MARK: Events

  @objc open func AfterPicking() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterPicking")
  }

  // MARK: UITableViewDataSource

  open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    if let cell = tableView.dequeueReusableCell(withIdentifier: kDefaultTableCell) {
      cell.textLabel?.text = _elements[indexPath.row]
      cell.textLabel?.font = cell.textLabel?.font.withSize(CGFloat(_textSize))
      cell.backgroundColor = argbToColor(_backgroundColor == Int32(bitPattern: Color.default.rawValue) ? Int32(bitPattern: kListViewDefaultBackgroundColor.rawValue) : _backgroundColor)
      cell.textLabel?.textColor = argbToColor(_textColor == Int32(bitPattern: Color.default.rawValue) ? Int32(bitPattern: kListViewDefaultTextColor.rawValue) : _textColor)
      cell.selectedBackgroundView?.backgroundColor = argbToColor(_selectionColor == Int32(bitPattern: Color.default.rawValue) ? Int32(bitPattern: kListViewDefaultSelectionColor.rawValue) : _selectionColor)
      return cell
    } else {
      let cell = UITableViewCell(style: .default, reuseIdentifier: kDefaultTableCell)
      cell.textLabel?.text = _elements[indexPath.row]
      cell.textLabel?.font = cell.textLabel?.font.withSize(CGFloat(_textSize))
      cell.backgroundColor = argbToColor(_backgroundColor == Int32(bitPattern: Color.default.rawValue) ? Int32(bitPattern: kListViewDefaultBackgroundColor.rawValue) : _backgroundColor)
      cell.textLabel?.textColor = argbToColor(_textColor == Int32(bitPattern: Color.default.rawValue) ? Int32(bitPattern: kListViewDefaultTextColor.rawValue) : _textColor)
      let cellSelection = UIView()
      cellSelection.backgroundColor = argbToColor(_selectionColor == Int32(bitPattern: Color.default.rawValue) ? Int32(bitPattern: kListViewDefaultSelectionColor.rawValue) : _selectionColor)
      cell.selectedBackgroundView = cellSelection
      return cell
    }
  }

  open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return _elements.count
  }

  // MARK: UITableViewDelegate

  open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    _selectionIndex = Int32(indexPath.row) + 1
    _selection = _elements[indexPath.row]
    AfterPicking()
  }
}
