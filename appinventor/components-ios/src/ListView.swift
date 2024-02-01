// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


import Foundation

fileprivate let kListViewDefaultBackgroundColor = Color.black
fileprivate let kListViewDefaultSelectionColor = Color.lightGray
fileprivate let kListViewDefaultTextColor = Color.white
fileprivate let kDefaultTableCell = "UITableViewCell"
fileprivate let kDefaultTableCellHeight = CGFloat(44.0)

open class ListView: ViewComponent, AbstractMethodsForViewComponent,
    UITableViewDataSource, UITableViewDelegate, UISearchBarDelegate {
  fileprivate final var _view: UITableView
  fileprivate var _horizontalTableView: UICollectionView?
  fileprivate var _backgroundColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _elements = [String]()
  fileprivate var _selection = ""
  fileprivate var _selectionDetailText = ""
  fileprivate var _selectionColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _selectionIndex = Int32(0)
  fileprivate var _showFilter = false
  fileprivate var _textColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _textColorDetail = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _textSize = Int32(22)
  fileprivate var _automaticHeightConstraint: NSLayoutConstraint!
  fileprivate var _results: [String]? = nil
  fileprivate var _fontSizeDetail = Int32(16)
  fileprivate var _listData: [[String: String]] = []   //ListData
  fileprivate var _listViewLayoutMode = Int32(0)
  fileprivate var _fontTypeface: String = ""
  fileprivate var _fontTypefaceDetail: String = ""
  fileprivate var _orientation = Int32(1)


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
    _view.backgroundColor = preferredTextColor(parent.form)

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
      Elements = elements.split(",") as [AnyObject]
    }
  }

  @objc open var Elements: [AnyObject] {
    get {
      if _listData.count > 0 {
        return _listData as [AnyObject]
      } else {
        return _elements as [AnyObject]
      }
    }
    set(elements) {
      _elements = []
      _listData = []
      guard !elements.isEmpty else {
        _view.reloadData()
        return
      }
      if elements.first is YailDictionary {
        for item in elements {
          if let row = item as? YailDictionary {
            if let rowDict = row as? [String:String] {
              _listData.append(rowDict)
            }
          } else if let row = item as? String {
            _listData.append(["Text1": row, "Text2": "", "Image": ""])
          } else {
            // Hmm...
          }
        }
      } else {
        _elements = elements.toStringArray()
      }
      let rows = max(_elements.count, _listData.count)
      _automaticHeightConstraint.constant = rows == 0 ? kDefaultTableCellHeight : kDefaultTableCellHeight * CGFloat(rows)
      if let searchBar = _view.tableHeaderView as? UISearchBar {
        self.searchBar(searchBar, textDidChange: searchBar.text ?? "")
      } else {
        _view.reloadData()
      }
    }
  }

  @objc open var FontTypeface: String {
    get {
      return _fontTypeface
    }
    set(fontTypeface) {
      _fontTypeface = fontTypeface
      _view.reloadData()
    }
  }

  @objc open var FontTypefaceDetail: String {
    get {
      return _fontTypeface
    }
    set(FontTypefaceDetail) {
      _fontTypefaceDetail = FontTypefaceDetail
      _view.reloadData()
    }
  }

  //ListData
  @objc open var ListData: String {
    get {
      do {
        let jsonString = try getJsonRepresentation(_listData as AnyObject)
        return jsonString
      } catch {
        print("Error serializing JSON: \(error)")
        return ""
      }
    }
    set(jsonString) {
      do {
        if let dictionaries = try getObjectFromJson(jsonString) as? [[String: Any]] {
          _listData = dictionaries.compactMap { dictionary in
            var item: [String: String] = [:]

            if let text1 = dictionary["Text1"] as? String {
              item["Text1"] = text1
            }

            if let text2 = dictionary["Text2"] as? String {
              item["Text2"] = text2
            }

            if let image = dictionary["Image"] as? String {
              item["Image"] = image
            }

            // Check if any of the required values is missing and skip the entry if needed
            if item["Text1"] != nil || item["Text2"] != nil || item["Image"] != nil {
              return item
            }

            return nil
          }
          _view.reloadData()
        }
      } catch {
        print("Error parsing JSON: \(error)")
      }
    }
  }

  //ListLayout
  @objc open var ListViewLayout: Int32 {
    get {
      return _listViewLayoutMode
    }
    set(listViewLayoutMode) {
      _listViewLayoutMode = listViewLayoutMode
      _view.reloadData()
    }
  }

  @objc open var Orientation: Int32 {
    get {
      return _orientation
    }
    set(orientation) {
      _orientation = orientation
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
      } else if let index = _listData.firstIndex(where: { $0["Text1"] == selection }) {
        _selectionIndex = Int32(index) + 1
        _selection = selection
        _view.selectRow(at: IndexPath(item: index, section: 0), animated: true, scrollPosition: .none)
      } else {
        _selectionIndex = 0
        _selection = ""
      }
    }
  }

  @objc open var SelectionDetailText: String {
    get {
      return _selectionDetailText
    }
    set(selectionDetailText) {
      if let selectedRow = _view.indexPathForSelectedRow {
        _view.deselectRow(at: selectedRow, animated: false)
      }
      if let index = _listData.firstIndex(where: { $0["Text2"] == selectionDetailText }) {
        _selectionIndex = Int32(index) + 1
        _selectionDetailText = selectionDetailText
        _view.selectRow(at: IndexPath(item: index, section: 0), animated: true, scrollPosition: .none)
      } else {
        _selectionIndex = 0
        _selectionDetailText = ""
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
      return _selectionIndex
    }
    set(selectionIndex) {
      if selectionIndex > 0 && selectionIndex <= Int32(_elements.count) {
        _selectionIndex = selectionIndex
        _selection = _elements[Int(selectionIndex) - 1]
        _selectionDetailText = _elements[Int(selectionIndex) - 1]
      } else {
        _selectionIndex = 0
        _selection = ""
        _selectionDetailText = ""
      }
      if _selectionIndex == 0 {
        if let path = _view.indexPathForSelectedRow {
          _view.deselectRow(at: path, animated: true)
        }
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
      if _showFilter && _view.tableHeaderView == nil {
        let filter = UISearchBar()
        _view.tableHeaderView = filter
        filter.sizeToFit()
        filter.delegate = self
      } else if !_showFilter && _view.tableHeaderView != nil {
        _view.tableHeaderView = nil
      }
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

  @objc open var TextColorDetail: Int32 {
    get {
      return _textColorDetail
    }
    set(textColorDetail) {
      _textColorDetail = textColorDetail
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

  //FontSizeDetail
  @objc open var FontSizeDetail: Int32 {
    get {
      return _fontSizeDetail
    }
    set(fontSizeDetail) {
      _fontSizeDetail = fontSizeDetail < 0 ? Int32(7) : fontSizeDetail
      _view.reloadData()
    }
  }

  // MARK: Methods

  @objc open func CreateElement(_ mainText: String, _ detailText: String, _ imageName: String) -> YailDictionary {
    return [
      "Text1": mainText,
      "Text2": detailText,
      "Image": imageName
    ] as YailDictionary
  }

  @objc open func GetDetailText(_ listElement: YailDictionary) -> String {
    return listElement["Text2"] as? String ?? ""
  }

  @objc open func GetImageName(_ listElement: YailDictionary) -> String {
    return listElement["Image"] as? String ?? ""
  }

  @objc open func GetMainText(_ listElement: YailDictionary) -> String {
    return listElement["Text1"] as? String ?? ""
  }

  @objc open func Refresh() {
    _view.reloadData()
  }

  @objc open func RemoveItemAtIndex(_ index: Int32) {
    if index < 1 || index > max(_listData.count, _elements.count) {
      _container?.form?.dispatchErrorOccurredEvent(self, "RemoveItemAtIndex",
           ErrorMessage.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, index)
      return
    }
    if _listData.count >= index {
      _listData.remove(at: Int(index - 1))
    }
    if _elements.count >= index {
      _elements.remove(at: Int(index - 1))
    }
    _view.reloadData()
  }

  // MARK: Events

  @objc open func AfterPicking() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterPicking")
  }

  // MARK: UITableViewDataSource

  open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(withIdentifier: kDefaultTableCell) ??
      UITableViewCell(style: .subtitle, reuseIdentifier: kDefaultTableCell)

    if indexPath.row < _elements.count {
      cell.textLabel?.text = _elements[indexPath.row]
      cell.textLabel?.numberOfLines = 0
      cell.textLabel?.lineBreakMode = .byWordWrapping
    } else {
      let listDataIndex = indexPath.row - _elements.count
      if _listViewLayoutMode == 1{
        cell.textLabel?.text = _listData[listDataIndex]["Text1"]
        cell.detailTextLabel?.text = _listData[listDataIndex]["Text2"]
      } else if _listViewLayoutMode == 2 {
        tableView.rowHeight = 60
        cell.textLabel?.text = _listData[listDataIndex]["Text1"]
        cell.detailTextLabel?.text = _listData[listDataIndex]["Text2"]

        // Configure the layout
        cell.layoutMargins = UIEdgeInsets.zero
        cell.separatorInset = UIEdgeInsets.zero
        cell.preservesSuperviewLayoutMargins = true

        // Create a stack view to hold the labels horizontally
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill

        // Add the labels to the stack view
        stackView.addArrangedSubview(cell.textLabel!)
        stackView.addArrangedSubview(cell.detailTextLabel!)

        // Add the stack view to the cell's content view
        cell.contentView.addSubview(stackView)

        // Set up constraints
        stackView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            stackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor),
            stackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor),
            stackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor)
        ])
      } else if _listViewLayoutMode == 3 {
        tableView.rowHeight = 60
        cell.textLabel?.text = _listData[listDataIndex]["Text1"]
        if let imagePath = _listData[listDataIndex]["Image"],
           let image = AssetManager.shared.imageFromPath(path: imagePath) {
          cell.imageView?.image = image

          // Configure the layout
          cell.layoutMargins = UIEdgeInsets.zero
          cell.separatorInset = UIEdgeInsets.zero
          cell.preservesSuperviewLayoutMargins = true

          // Create a stack view to hold the labels horizontally
          let stackView = UIStackView()
          stackView.axis = .horizontal
          stackView.alignment = .leading
          stackView.distribution = .fill
          stackView.spacing = 8.0

          // Add the labels to the stack view
          stackView.addArrangedSubview(cell.imageView!)
          stackView.addArrangedSubview(cell.textLabel!)

          // Add the stack view to the cell's content view
          cell.contentView.addSubview(stackView)

          // Set up constraints
          stackView.translatesAutoresizingMaskIntoConstraints = false
          NSLayoutConstraint.activate([
              stackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
              stackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
              stackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor, constant: 8.0),
              stackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor, constant: -8.0),
              cell.imageView!.widthAnchor.constraint(equalToConstant: 50.0)
          ])
        }
      } else if _listViewLayoutMode == 4 {
        tableView.rowHeight = 60
        cell.textLabel?.text = _listData[listDataIndex]["Text1"]
        cell.detailTextLabel?.text = _listData[listDataIndex]["Text2"]
        if let imagePath = _listData[listDataIndex]["Image"],
           let image = AssetManager.shared.imageFromPath(path: imagePath) {
          cell.imageView?.image = image

          // Configure the layout
          cell.layoutMargins = UIEdgeInsets.zero
          cell.separatorInset = UIEdgeInsets.zero
          cell.preservesSuperviewLayoutMargins = true

          // Create a horizontal stack view to hold the imageView and a nested vertical stack view
          let horizontalStackView = UIStackView()
          horizontalStackView.axis = .horizontal
          horizontalStackView.alignment = .center
          horizontalStackView.distribution = .fill
          horizontalStackView.spacing = 8.0

          // Create a vertical stack view to hold the textLabel and detailTextLabel
          let verticalStackView = UIStackView()
          verticalStackView.axis = .vertical
          verticalStackView.alignment = .leading
          verticalStackView.distribution = .fill
          verticalStackView.spacing = 8.0

          // Add the imageView and nested vertical stack view to the horizontal stack view
          horizontalStackView.addArrangedSubview(cell.imageView!)
          horizontalStackView.addArrangedSubview(verticalStackView)

          // Add the textLabel and detailTextLabel to the vertical stack view
          verticalStackView.addArrangedSubview(cell.textLabel!)
          verticalStackView.addArrangedSubview(cell.detailTextLabel!)

          // Add the horizontal stack view to the cell's content view
          cell.contentView.addSubview(horizontalStackView)

          // Set up constraints
          horizontalStackView.translatesAutoresizingMaskIntoConstraints = false
          NSLayoutConstraint.activate([
              horizontalStackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
              horizontalStackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
              horizontalStackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor, constant: 8.0),
              horizontalStackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor, constant: -8.0),
              cell.imageView!.widthAnchor.constraint(equalToConstant: 50.0)
          ])
        }
      } else {
        cell.textLabel?.text = _listData[listDataIndex]["Text1"]
      }

      cell.textLabel?.numberOfLines = 0
      cell.textLabel?.lineBreakMode = .byWordWrapping
    }

    cell.textLabel?.font = cell.textLabel?.font.withSize(CGFloat(_textSize))
    cell.detailTextLabel?.font = cell.textLabel?.font.withSize(CGFloat(_fontSizeDetail))

    guard let form = _container?.form else {
      return cell
    }

    if _backgroundColor == Color.default.int32 {
      cell.backgroundColor = preferredTextColor(form)
    } else {
      cell.backgroundColor = argbToColor(_backgroundColor)
    }

    //maintext
    if _textColor == Color.default.int32 {
      cell.textLabel?.textColor = preferredBackgroundColor(form)
    } else {
      cell.textLabel?.textColor = argbToColor(_textColor)
    }

    //detailtext
    if _textColorDetail == Color.default.int32 {
      cell.detailTextLabel?.textColor = preferredBackgroundColor(form)
    } else {
      cell.detailTextLabel?.textColor = argbToColor(_textColorDetail)
    }

    if _fontTypeface == "1" {
      cell.textLabel?.font = UIFont(name: "Helvetica", size: CGFloat(_textSize))
    } else if _fontTypeface == "2" {
      cell.textLabel?.font = UIFont(name: "Times New Roman", size: CGFloat(_textSize))
    } else if _fontTypeface == "3" {
      cell.textLabel?.font = UIFont(name: "Courier", size: CGFloat(_textSize))
    }

    if _fontTypefaceDetail == "1" {
      cell.detailTextLabel?.font = UIFont(name: "Helvetica", size: CGFloat(_fontSizeDetail))
    } else if _fontTypefaceDetail == "2" {
      cell.detailTextLabel?.font = UIFont(name: "Times New Roman", size: CGFloat(_fontSizeDetail))
    } else if _fontTypefaceDetail == "3" {
      cell.detailTextLabel?.font = UIFont(name: "Courier", size: CGFloat(_fontSizeDetail))
    }

    if cell.selectedBackgroundView == nil {
      cell.selectedBackgroundView = UIView()
    }
    cell.selectedBackgroundView?.backgroundColor =
        argbToColor(_selectionColor == Int32(bitPattern: Color.default.rawValue)
        ? Int32(bitPattern: kListViewDefaultSelectionColor.rawValue) : _selectionColor)
    return cell
  }

  open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return _listData.isEmpty ? _elements.count : _listData.count
  }

  // MARK: UITableViewDelegate

  open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    if indexPath.row < _elements.count {
      _selectionIndex = Int32(indexPath.row) + 1
      _selection = _elements[indexPath.row]
      _selectionDetailText = ""
    } else if indexPath.row < _elements.count + _listData.count {
      let listDataIndex = indexPath.row - _elements.count
      _selectionIndex = Int32(indexPath.row) + 1
      _selection = _listData[listDataIndex]["Text1"] ?? ""
      _selectionDetailText = _listData[listDataIndex]["Text2"] ?? ""
    }
    AfterPicking()
  }

  // MARK: UISearchBarDelegate

  open func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    _results = nil
    if !searchText.isEmpty  {
      _results = [String]()
      for item in _elements {
        if item.starts(with: searchText) {
          _results?.append(item)
        }
      }
    }
    _view.reloadData()
  }

  open func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
    searchBar.endEditing(true)
  }

  // MARK: Private implementation

  var elements: [String] {
    return _results ?? _elements
  }
}
