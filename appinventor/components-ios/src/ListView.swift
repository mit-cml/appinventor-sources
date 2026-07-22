// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

fileprivate let kListViewDefaultBackgroundColor = Color.black.int32
fileprivate let kListViewDefaultElementColor = Color.none.int32
fileprivate let kListViewDefaultSelectionColor = Color.lightGray.int32
fileprivate let kListViewDefaultTextColor = Color.white.int32
fileprivate let kListViewDefaultDividerColor = Color.none.int32
fileprivate let kDefaultTableCell = "UITableViewCell"
fileprivate let kDefaultTableCellHeight = CGFloat(44.0)
fileprivate let kDefaultTableCellVerticalPadding = CGFloat(30.0)

let VERTICAL_LAYOUT = 0
let HORIZONTAL_LAYOUT = 1

fileprivate final class ListViewRootView: UIView {
  var preferredSizeProvider: (() -> CGSize)?

  override var intrinsicContentSize: CGSize {
    return preferredSizeProvider?() ?? super.intrinsicContentSize
  }
}

  open class ListView: ViewComponent, AbstractMethodsForViewComponent,
    UITableViewDataSource, UITableViewDelegate, UISearchBarDelegate,
    UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
  fileprivate final var _view: UITableView
  fileprivate let _rootView = ListViewRootView()
  fileprivate var _collectionView: UICollectionView
  fileprivate let kDefaultItemSize = CGSize(width: 160, height: 56)
    
  fileprivate var _backgroundColor = kListViewDefaultBackgroundColor
  fileprivate var _elements = [String]()
  fileprivate var _items: [[String: AnyObject]] = []
  fileprivate var _selection = ""
  fileprivate var _selectionDetailText = ""
  fileprivate var _selectionColor = kListViewDefaultSelectionColor
  fileprivate var _selectionIndex = Int32(0)
  fileprivate var _showFilter = false
  fileprivate var _textColor = kListViewDefaultTextColor
  fileprivate var _textColorDetail = kListViewDefaultTextColor
  fileprivate var _fontSize = Int32(22)
  fileprivate var _automaticHeightConstraint: NSLayoutConstraint?
  fileprivate var _results: [String]? = nil
  fileprivate var _fontSizeDetail = Int32(16)
  //ListData
  fileprivate var _listViewLayoutMode = Int32(0)
  fileprivate var _fontTypeface: String = ""
  fileprivate var _fontTypefaceDetail: String = ""
    
  fileprivate var _orientation = Int32(VERTICAL_LAYOUT)
  fileprivate let _horizontalLayout = UICollectionViewFlowLayout()
  fileprivate let filter = UISearchBar()
  fileprivate var _hint = "Search list..."
  fileprivate var _dividerColor = kListViewDefaultDividerColor
  fileprivate var _dividerThickness = Int32(0)
  fileprivate var _elementColor = kListViewDefaultElementColor
  fileprivate var _elementCornerRadius = Int32(0)
  fileprivate var _elementMarginsWidth = Int32(0)
  fileprivate var _imageHeight = Int32(200)
  fileprivate var _imageWidth = Int32(200)
  fileprivate var _textAlignmentMain = Alignment.normal.rawValue
  fileprivate var _textAlignmentDetail = Alignment.normal.rawValue

  let COMPANION_CORRECTION = 5

  public override init(_ parent: ComponentContainer) {
    _view = UITableView(frame: .zero, style: .plain)
    _collectionView = UICollectionView(frame: .zero, collectionViewLayout: _horizontalLayout)
    super.init(parent)

    // Root container
    _rootView.translatesAutoresizingMaskIntoConstraints = false

    // Table setup (existing)
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.delegate = self
    _view.dataSource = self
    self.setDelegate(self)
    parent.add(self)
    Width = kLengthFillParent
    _view.tableFooterView = UIView()
    _view.backgroundView = nil
    _view.backgroundColor = argbToColor(_backgroundColor)

    // Auto height for the table (existing)
    _automaticHeightConstraint = _view.heightAnchor.constraint(equalToConstant: kDefaultTableCellHeight)
    _automaticHeightConstraint?.priority = UILayoutPriority(1.0)
    _automaticHeightConstraint?.isActive = true
    _rootView.preferredSizeProvider = { [weak self] in
      return self?.preferredListViewSize ?? CGSize(width: 320, height: kDefaultTableCellHeight)
    }

    // Create horizontal collection view
    let layout = UICollectionViewFlowLayout()
   
    _horizontalLayout.scrollDirection = .horizontal
    _horizontalLayout.minimumLineSpacing = 8
    _horizontalLayout.minimumInteritemSpacing = 0
    _horizontalLayout.sectionInset = .zero
    _horizontalLayout.estimatedItemSize = .zero

    _collectionView.backgroundColor = .clear
    _collectionView.translatesAutoresizingMaskIntoConstraints = false
    _collectionView.showsHorizontalScrollIndicator = true
    _collectionView.showsVerticalScrollIndicator = false
    _collectionView.alwaysBounceVertical = false
    _collectionView.alwaysBounceHorizontal = true
    _collectionView.dataSource = self
    _collectionView.delegate = self
    _collectionView.heightAnchor.constraint(equalToConstant: 60)
  
    _collectionView.register(HListCell.self, forCellWithReuseIdentifier: HListCell.reuseId)

    // Assemble root: keep both, toggle visibility later
    _rootView.addSubview(_view)
    _rootView.addSubview(_collectionView)

    
    NSLayoutConstraint.activate([
      _view.leadingAnchor.constraint(equalTo: _rootView.leadingAnchor),
      _view.trailingAnchor.constraint(equalTo: _rootView.trailingAnchor),
      _view.topAnchor.constraint(equalTo: _rootView.topAnchor),
      _view.bottomAnchor.constraint(equalTo: _rootView.bottomAnchor),

      _collectionView.leadingAnchor.constraint(equalTo: _rootView.leadingAnchor),
      _collectionView.trailingAnchor.constraint(equalTo: _rootView.trailingAnchor),
      _collectionView.topAnchor.constraint(equalTo: _rootView.topAnchor),
      _collectionView.bottomAnchor.constraint(equalTo: _rootView.bottomAnchor),
      // Give the horizontal list a reasonable intrinsic height like the table
    
    ])

    updateOrientationUI()
  }

 
  open override var view: UIView { _rootView }

  @objc open override var Width: Int32 {
    get {
      return super.Width
    }
    set(width) {
      super.Width = width == kLengthPreferred ? kLengthFillParent : width
    }
  }

  // MARK: Properties
  @objc open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(backgroundColor) {
      _backgroundColor = backgroundColor
      _view.backgroundColor = argbToColor(_backgroundColor)
      _collectionView.backgroundColor = argbToColor(_backgroundColor)
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
      if _items.count > 0 {
        return _items as [AnyObject]
      } else {
        return _elements as [AnyObject]
      }
    }
    set(elements) {
      _items = []
      _elements = []
      guard !elements.isEmpty else {
        elementsCount()
        return
      }
      addElements(elements)
    }
  }
    
  private func makeListItem(text1: String = "", text2: String = "", image: String = "") -> [String: AnyObject] {
    return [
      "Text1": text1 as AnyObject,
      "Text2": text2 as AnyObject,
      "Image": image as AnyObject
    ]
  }

  private func listItem(at index: Int) -> [String: AnyObject]? {
    if !_items.isEmpty {
      guard index >= 0 && index < _items.count else {
        return nil
      }
      return _items[index]
    }
    let source = _results ?? _elements
    guard index >= 0 && index < source.count else {
      return nil
    }
    return makeListItem(text1: source[index])
  }

  private func promoteElementsToItemsIfNeeded() {
    if _items.isEmpty && !_elements.isEmpty {
      _items = _elements.map { makeListItem(text1: $0) }
      _elements.removeAll()
    }
  }

  private func normalizedListItem(from item: AnyObject) -> [String: AnyObject] {
    if let dictionary = item as? [String: AnyObject] {
      return dictionary
    }
    if let dictionary = item as? NSDictionary {
      return [
        "Text1": (dictionary["Text1"] as? String ?? "") as AnyObject,
        "Text2": (dictionary["Text2"] as? String ?? "") as AnyObject,
        "Image": (dictionary["Image"] as? String ?? "") as AnyObject
      ]
    }
    return makeListItem(text1: toString(item))
  }

  private func normalizedElements(_ elements: [AnyObject]) -> [AnyObject] {
    if elements.first is SCMSymbol {
      return Array(elements.dropFirst())
    }
    return elements
  }
  
    private func addElements(_ elements: [AnyObject]) {
      let elements = normalizedElements(elements)
      guard !elements.isEmpty else {
        return
      }
      let useDictFormat = !_items.isEmpty || elements.contains { $0 is NSDictionary }
      if useDictFormat {
        promoteElementsToItemsIfNeeded()
        _items.append(contentsOf: elements.map { normalizedListItem(from: $0) })
      } else {
        _elements.append(contentsOf: elements.toStringArray())
      }
      elementsCount()
    }
  func elementsCount() {
    _automaticHeightConstraint?.constant = preferredTableHeight
    if let searchBar = _view.tableHeaderView as? UISearchBar {
      self.searchBar(searchBar, textDidChange: searchBar.text ?? "")
    } else {
      _view.reloadData()
    }
    _collectionView.reloadData()
    _collectionView.collectionViewLayout.invalidateLayout()
    invalidateListViewSize()
  }

  @objc open var FontTypeface: String {
    get {
      return _fontTypeface
    }
    set(fontTypeface) {
      _fontTypeface = fontTypeface
      elementsCount()
    }
  }

  @objc open var FontTypefaceDetail: String {
    get {
      return _fontTypefaceDetail
    }
    set(FontTypefaceDetail) {
      _fontTypefaceDetail = FontTypefaceDetail
      elementsCount()
    }
  }

  // This property is not supported in iOS
  @objc open var BounceEdgeEffect: Bool {
    get {
      return false;
    }
    set(addEffect) {
    }
  }

  // This property is not fully implemented in iOS
  @objc open var DividerColor: Int32 {
    get {
      return _dividerColor
    }
    set(dividerColor) {
      _dividerColor = dividerColor
    }
  }

  // This property is not fully implemented in iOS
  @objc open var DividerThickness: Int32 {
    get {
      return _dividerThickness
    }
    set(dividerThickness) {
      _dividerThickness = dividerThickness
      _view.reloadData()
    }
  }

  @objc open var ElementColor: Int32 {
    get {
      return _elementColor
    }
    set(elementColor) {
      _elementColor = elementColor
    }
  }

  // This property is not fully implemented in iOS
  @objc open var ElementCornerRadius: Int32 {
    get {
      return _elementCornerRadius
    }
    set(elementCornerRadius) {
      _elementCornerRadius = elementCornerRadius
      _view.reloadData()
    }
  }

  // This property is not fully implemented in iOS
  @objc open var ElementMarginsWidth: Int32 {
    get {
      return _elementMarginsWidth
    }
    set(elementMarginsWidth) {
      _elementMarginsWidth = elementMarginsWidth
      _view.reloadData()
    }
  }

  @objc open var ImageHeight: Int32 {
    get {
      return _imageHeight
    }
    set(height) {
      _imageHeight = height
      _view.reloadData()
    }
}

  @objc open var ImageWidth: Int32 {
    get {
      return _imageWidth
    }
    set(width) {
      _imageWidth = width
      _view.reloadData()
    }
  }

  //ListData
  @objc open var ListData: String {
    get {
      do {
        let jsonString = try getJsonRepresentation(_items as AnyObject)
        return jsonString
      } catch {
        print("Error serializing JSON: \(error)")
        return ""
      }
    }
    set(jsonString) {
      do {
        if let dictionaries = try getObjectFromJson(jsonString) as? [[String: Any]] {
          _elements.removeAll()
          _items = dictionaries.compactMap { dictionary in
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
              return item as [String: AnyObject]
            }

            return nil
          }
          elementsCount()
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
      invalidateListViewSize()
    }
  }

  @objc open var Orientation: Int32 {
    get { return _orientation }
    set(orientation) {
      _orientation = orientation
      updateOrientationUI()
    }
  }

  @objc open override var Height: Int32 {
    get {
      return super.Height
    }
    set(height) {
      super.Height = height
      _automaticHeightConstraint?.constant = preferredTableHeight
      invalidateListViewSize()
    }
  }
    
    private func updateOrientationUI() {
      let isHorizontal = (_orientation == HORIZONTAL_LAYOUT)

    // Show / hide views
    _view.isHidden = isHorizontal
    _collectionView.isHidden = !isHorizontal

    if isHorizontal {
      _automaticHeightConstraint?.isActive = false

      // Ensure scroll direction stays horizontal
      if let layout = _collectionView.collectionViewLayout as? UICollectionViewFlowLayout {
        layout.scrollDirection = .horizontal
        layout.minimumLineSpacing = 8
        layout.minimumInteritemSpacing = 0
      }

      _collectionView.backgroundColor =
        argbToColor(_backgroundColor)

      // ✅ Correct update order:
      _collectionView.reloadData()
      _collectionView.collectionViewLayout.invalidateLayout()

    } else {
      _automaticHeightConstraint?.isActive = true
      _view.reloadData()
    }
    invalidateListViewSize()

  }


  @objc open var Selection: String {
    get {
      return _selection
    }
    set(selection) {
      if let selectedRow = _view.indexPathForSelectedRow {
        _view.deselectRow(at: selectedRow, animated: false)
      }
      if let index = _items.firstIndex(where: { $0["Text1"] as? String == selection }) {
        _selectionIndex = Int32(index) + 1
        _selection = selection
        _selectionDetailText = _items[index]["Text2"] as? String ?? ""
        _view.selectRow(at: IndexPath(item: index, section: 0), animated: true, scrollPosition: .none)
      } else if let index = _elements.firstIndex(of: selection) {
        _selectionIndex = Int32(index) + 1
        _selection = selection
        _selectionDetailText = ""
        _view.selectRow(at: IndexPath(item: index, section: 0), animated: true, scrollPosition: .none)
      } else {
        _selectionIndex = 0
        _selection = ""
        _selectionDetailText = ""
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
      if let index = _items.firstIndex(where: { $0["Text2"] as? String == selectionDetailText }) {
        _selectionIndex = Int32(index) + 1
        _selection = _items[index]["Text1"] as? String ?? ""
        _selectionDetailText = selectionDetailText
        _view.selectRow(at: IndexPath(item: index, section: 0), animated: true, scrollPosition: .none)
      } else {
        _selectionIndex = 0
        _selection = ""
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
      if selectionIndex > 0 && selectionIndex <= Int32(listItemCount),
         let item = listItem(at: Int(selectionIndex) - 1) {
        _selectionIndex = selectionIndex
        _selection = item["Text1"] as? String ?? ""
        _selectionDetailText = item["Text2"] as? String ?? ""
        _view.selectRow(at: IndexPath(row: Int(_selectionIndex) - 1, section: 0), animated: true, scrollPosition: UITableView.ScrollPosition.middle)
      } else {
        _selectionIndex = 0
        _selection = ""
        _selectionDetailText = ""
        if let path = _view.indexPathForSelectedRow {
          _view.deselectRow(at: path, animated: true)
        }
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
        _view.tableHeaderView = filter
        filter.sizeToFit()
        filter.delegate = self
      } else if !_showFilter && _view.tableHeaderView != nil {
        _view.tableHeaderView = nil
      }
      invalidateListViewSize()
    }
  }

  @objc open var HintText: String {
    get {
      return _hint
    }
    set(hint) {
      _hint = hint
      filter.placeholder = _hint
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

  @objc open var FontSize: Int32 {
    get {
      return _fontSize
    }
    set(fontSize) {
      _fontSize = fontSize < 0 ? Int32(7) : fontSize
      elementsCount()
    }
  }

  //FontSizeDetail
  @objc open var FontSizeDetail: Int32 {
    get {
      return _fontSizeDetail
    }
    set(fontSizeDetail) {
      _fontSizeDetail = fontSizeDetail < 0 ? Int32(7) : fontSizeDetail
      elementsCount()
    }
  }

  @objc open var TextAlignmentMain: Int32 {
    get {
      return _textAlignmentMain
    }
    set(alignment) {
      if Alignment(rawValue: alignment) != nil {
        _textAlignmentMain = alignment
        _view.reloadData()
        _collectionView.reloadData()
      }
    }
  }

  @objc open var TextAlignmentDetail: Int32 {
    get {
      return _textAlignmentDetail
    }
    set(alignment) {
      if Alignment(rawValue: alignment) != nil {
        _textAlignmentDetail = alignment
        _view.reloadData()
        _collectionView.reloadData()
      }
    }
  }

  fileprivate func nsTextAlignment(for value: Int32, in view: UIView) -> NSTextAlignment {
    var rtl = false
    if #available(iOS 9.0, *) {
      if UIView.userInterfaceLayoutDirection(for: view.semanticContentAttribute) == .rightToLeft {
        rtl = true
      }
    } else {
      if UIApplication.shared.userInterfaceLayoutDirection == .rightToLeft {
        rtl = true
      }
    }
    guard let align = Alignment(rawValue: value) else {
      return rtl ? .right : .left
    }
    switch align {
      case .normal:
        return rtl ? .right : .left
      case .center:
        return .center
      case .opposite:
        return rtl ? .left : .right
    }
  }

  // MARK: Methods

  @objc open func AddItem(_ mainText: String, _ detailText: String, _ imageName: String) {
    promoteElementsToItemsIfNeeded()
    _items.append(makeListItem(text1: mainText, text2: detailText, image: imageName))
    elementsCount()
  }

  @objc open func AddItemAtIndex(_ addIndex: Int32, _ mainText: String, _ detailText: String, _ imageName: String) {
    guard addIndex > 0 && addIndex <= Int32(listItemCount + 1) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "AddItemAtIndex",
                                                   ErrorMessage.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, addIndex)
      return
    }
    promoteElementsToItemsIfNeeded()
    _items.insert(makeListItem(text1: mainText, text2: detailText, image: imageName), at: Int(addIndex - 1))
    elementsCount()
  }

  @objc open func AddItems(_ items: [AnyObject]) {
    guard !items.isEmpty else {
        return
    }
    addElements(items)
  }

  /* insert element to ListView as Dictionary or as String */
  @objc open func AddItemsAtIndex(_ addIndex: Int32, _ elements: [AnyObject]) {
    let elements = normalizedElements(elements)
    if elements.isEmpty {
      return
    }
    if addIndex < 1 || addIndex > Int32(listItemCount + 1) {
      _container?.form?.dispatchErrorOccurredEvent(self, "AddItemsAtIndex",
                                                   ErrorMessage.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, addIndex)
      return
    }
    
    promoteElementsToItemsIfNeeded()
    let index = Int(addIndex - 1)
    let newItems = elements.map { normalizedListItem(from: $0) }
    _items.insert(contentsOf: newItems, at: index)
    elementsCount()
  }


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
    _collectionView.reloadData()
    _collectionView.collectionViewLayout.invalidateLayout()
    invalidateListViewSize()
  }

  @objc open func RemoveItemAtIndex(_ index: Int32) {
    if index < 1 || index > Int32(listItemCount) {
      _container?.form?.dispatchErrorOccurredEvent(self, "RemoveItemAtIndex",
           ErrorMessage.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, index)
      return
    }
    if !_items.isEmpty {
      _items.remove(at: Int(index - 1))
    } else {
      _elements.remove(at: Int(index - 1))
    }
    elementsCount()
  }

  // MARK: Events

  @objc open func AfterPicking() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterPicking")
  }

  // MARK: UITableViewDataSource

    open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
      let cell = tableView.dequeueReusableCell(withIdentifier: kDefaultTableCell) ??
      UITableViewCell(style: .subtitle, reuseIdentifier: kDefaultTableCell)
      let item = listItem(at: indexPath.row) ?? makeListItem()
      cell.imageView?.image = nil
      tableView.rowHeight = UITableView.automaticDimension

      if _listViewLayoutMode == 0 {
        cell.textLabel?.text = item["Text1"] as? String
        cell.detailTextLabel?.text = ""
        tableView.estimatedRowHeight = 44
      } else {
        if _listViewLayoutMode == 1 {
          tableView.rowHeight = UITableView.automaticDimension
          tableView.estimatedRowHeight = 44
          cell.textLabel?.text = item["Text1"] as? String
          cell.detailTextLabel?.text = item["Text2"] as? String

          // Wrap system labels in a full-width vertical stack so textAlignment
          // is visible for short strings. (UIKit's default subtitle layout
          // sizes labels to content for short text, making centering invisible.)
          cell.layoutMargins = UIEdgeInsets.zero
          cell.separatorInset = UIEdgeInsets.zero
          cell.preservesSuperviewLayoutMargins = true

          let stackView = UIStackView()
          stackView.axis = .vertical
          stackView.alignment = .fill
          stackView.distribution = .fill
          stackView.spacing = 8.0

          // Add the labels to the stack view
          stackView.addArrangedSubview(cell.textLabel!)
          stackView.addArrangedSubview(cell.detailTextLabel!)

          // Add the stack view to the cell's content view
          cell.contentView.addSubview(stackView)

          // Set up constraints
          stackView.translatesAutoresizingMaskIntoConstraints = false
          NSLayoutConstraint.activate([
            stackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
            stackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
            stackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor, constant: 8.0),
            stackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor, constant: -8.0)
          ])
        } else if _listViewLayoutMode == 2 {
          tableView.rowHeight = UITableView.automaticDimension
          tableView.estimatedRowHeight = 60
          cell.textLabel?.text = item["Text1"] as? String
          cell.detailTextLabel?.text = item["Text2"] as? String

          // Create a stack view to hold the labels horizontally. Align by
          // first baseline so the labels' first text lines line up visually
          // regardless of font-size differences (top alignment makes the
          // smaller detail font appear higher than main due to font metrics;
          // .fill stretches labels vertically and centers the text inside).
          let stackView = UIStackView()
          stackView.axis = .horizontal
          stackView.alignment = .firstBaseline
          stackView.distribution = .fillEqually
          stackView.spacing = 8.0

          // Add the labels to the stack view
          stackView.addArrangedSubview(cell.textLabel!)
          stackView.addArrangedSubview(cell.detailTextLabel!)

          // Add the stack view to the cell's content view
          cell.contentView.addSubview(stackView)

          // Set up constraints
          stackView.translatesAutoresizingMaskIntoConstraints = false
          NSLayoutConstraint.activate([
            stackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
            stackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
            stackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor, constant: 8.0),
            stackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor, constant: -8.0)
          ])
        } else if _listViewLayoutMode == 3 {
          tableView.estimatedRowHeight = preferredRowHeight
          cell.textLabel?.text = item["Text1"] as? String ?? ""
          cell.detailTextLabel?.text = ""
          if let imagePath = item["Image"] as? String, !imagePath.isEmpty,
             let image = AssetManager.shared.imageFromPath(path: imagePath) {
            cell.imageView?.image = image
            cell.imageView?.contentMode = .scaleAspectFit
            
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
              cell.imageView!.widthAnchor.constraint(equalToConstant: CGFloat(_imageWidth / 4)),
              cell.imageView!.heightAnchor.constraint(equalToConstant: CGFloat(_imageHeight / 4))
            ])
          }
        } else if _listViewLayoutMode == 4 {
          tableView.estimatedRowHeight = 60
          cell.textLabel?.text = item["Text1"] as? String ?? ""
          cell.detailTextLabel?.text = item["Text2"] as? String ?? ""
          if let imagePath = item["Image"] as? String, !imagePath.isEmpty,
             let image = AssetManager.shared.imageFromPath(path: imagePath) {
            cell.imageView?.image = image
            cell.imageView?.contentMode = .scaleAspectFit
            
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

            // Create a vertical stack view to hold the textLabel and detailTextLabel.
            // Use .fill so labels span the inner stack width and textAlignment is
            // visible regardless of text length.
            let verticalStackView = UIStackView()
            verticalStackView.axis = .vertical
            verticalStackView.alignment = .fill
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
              cell.imageView!.widthAnchor.constraint(equalToConstant: CGFloat(_imageWidth / 4)),
              cell.imageView!.heightAnchor.constraint(equalToConstant: CGFloat(_imageHeight / 4))
            ])
          }
        } else if _listViewLayoutMode == 5 {
          tableView.estimatedRowHeight = 120
          cell.textLabel?.text = item["Text1"] as? String ?? ""
          cell.detailTextLabel?.text = item["Text2"] as? String ?? ""
          if let imagePath = item["Image"] as? String, !imagePath.isEmpty,
             let image = AssetManager.shared.imageFromPath(path: imagePath) {
            cell.imageView?.image = image
            cell.imageView?.contentMode = .scaleAspectFit
            
            // Configure the layout
            cell.layoutMargins = UIEdgeInsets.zero
            cell.separatorInset = UIEdgeInsets.zero
            cell.preservesSuperviewLayoutMargins = true
            
            // Inner stack: labels with .fill so they span the full label-stack
            // width, making textAlignment visible regardless of text length.
            let labelsStackView = UIStackView()
            labelsStackView.axis = .vertical
            labelsStackView.alignment = .fill
            labelsStackView.distribution = .fill
            labelsStackView.spacing = 8.0
            labelsStackView.addArrangedSubview(cell.textLabel!)
            labelsStackView.addArrangedSubview(cell.detailTextLabel!)

            // Outer stack: image (centered) + labels stack
            let verticalStackView = UIStackView()
            verticalStackView.axis = .vertical
            verticalStackView.alignment = .center
            verticalStackView.distribution = .fill
            verticalStackView.spacing = 8.0
            verticalStackView.addArrangedSubview(cell.imageView!)
            verticalStackView.addArrangedSubview(labelsStackView)
            
            // Add the outer stack to the cell's content view
            cell.contentView.addSubview(verticalStackView)
            
            // Set up constraints. The labelsStackView width is pinned to the
            // outer stack so labels span full row width while the image stays
            // centered at its intrinsic / explicit size.
            verticalStackView.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
              verticalStackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
              verticalStackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
              verticalStackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor, constant: 8.0),
              verticalStackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor, constant: -8.0),
              cell.imageView!.widthAnchor.constraint(equalToConstant: CGFloat(_imageWidth / 4)),
              cell.imageView!.heightAnchor.constraint(equalToConstant: CGFloat(_imageHeight / 4)),
              labelsStackView.widthAnchor.constraint(equalTo: verticalStackView.widthAnchor)
            ])
          }
        } else {
          tableView.estimatedRowHeight = 44
          cell.textLabel?.text = item["Text1"] as? String
          cell.detailTextLabel?.text = ""
        }
      }

    // Both labels wrap inside their 50% half (matches the Designer mock).
    cell.textLabel?.numberOfLines = 0
    cell.textLabel?.lineBreakMode = .byWordWrapping
    cell.detailTextLabel?.numberOfLines = 0
    cell.detailTextLabel?.lineBreakMode = .byWordWrapping

    cell.textLabel?.font = cell.textLabel?.font.withSize(CGFloat(_fontSize))
    cell.detailTextLabel?.font = cell.textLabel?.font.withSize(CGFloat(_fontSizeDetail))

    guard let form = _container?.form else {
      return cell
    }

    if _elementCornerRadius > 0 {
      cell.layer.cornerRadius = CGFloat(_elementCornerRadius) / CGFloat(COMPANION_CORRECTION)
      cell.layer.masksToBounds = true
    }
    
    if _dividerThickness > 0 {
      tableView.separatorStyle = .singleLine
    }
    if _dividerColor == Color.default.int32 {
      tableView.separatorColor = preferredTextColor(form)
    } else {
      tableView.separatorColor =  argbToColor(_dividerColor)
    }


    if _backgroundColor == Color.default.int32 {
      cell.backgroundColor = preferredTextColor(form)
    } else {
      cell.backgroundColor = argbToColor(_backgroundColor)
    }

    (cell.backgroundView as? UIView)?.backgroundColor =
          (_elementColor != Color.none.int32)
          ? ((_elementColor == Color.default.int32) ? preferredTextColor(_container?.form) : argbToColor(_elementColor))
          : ((_backgroundColor == Color.default.int32) ? preferredTextColor(_container?.form) : argbToColor(_backgroundColor))

    cell.backgroundColor =
            ((_elementColor != Color.none.int32) && (_elementColor != Color.default.int32))
            ? argbToColor(_elementColor)
            : cell.backgroundColor


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
      cell.textLabel?.font = UIFont(name: "Helvetica", size: CGFloat(_fontSize))
    } else if _fontTypeface == "2" {
      cell.textLabel?.font = UIFont(name: "Times New Roman", size: CGFloat(_fontSize))
    } else if _fontTypeface == "3" {
      cell.textLabel?.font = UIFont(name: "Courier", size: CGFloat(_fontSize))
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

    let selectedBgView = UIView()
    selectedBgView.backgroundColor = argbToColor(_selectionColor)
    cell.selectedBackgroundView = selectedBgView

    cell.textLabel?.textAlignment = nsTextAlignment(for: _textAlignmentMain, in: cell)
    cell.detailTextLabel?.textAlignment = nsTextAlignment(for: _textAlignmentDetail, in: cell)

    return cell
  }

  
    open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
      return listItemCount
    }

    // MARK: UITableViewDelegate

    open func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
      return preferredRowHeight
    }

    open func tableView(_ tableView: UITableView, estimatedHeightForRowAt indexPath: IndexPath) -> CGFloat {
      return preferredRowHeight
    }

    open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
      if let item = listItem(at: indexPath.row) {
        _selectionIndex = Int32(indexPath.row) + 1
        _selection = item["Text1"] as? String ?? ""
        _selectionDetailText = item["Text2"] as? String ?? ""
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

  private var preferredListViewSize: CGSize {
    let width = preferredListViewWidth()
    if _orientation == HORIZONTAL_LAYOUT {
      return CGSize(width: width, height: max(kDefaultItemSize.height, 60.0))
    }

    return CGSize(width: width, height: preferredTableHeight)
  }

  private var preferredTableHeight: CGFloat {
    if _lastSetHeight != kLengthPreferred {
      return max(kDefaultTableCellHeight, _rootView.bounds.height)
    }
    let rows = listItemCount
    let rowHeight = rows == 0 ? kDefaultTableCellHeight : preferredRowHeight * CGFloat(rows)
    let headerHeight = _view.tableHeaderView?.bounds.height ?? 0
    return max(kDefaultTableCellHeight, rowHeight + headerHeight)
  }

  private var preferredRowHeight: CGFloat {
    let mainTextHeight = ceil(listFont(typeface: _fontTypeface, size: _fontSize).lineHeight)
    let detailTextHeight = ceil(listFont(typeface: _fontTypefaceDetail, size: _fontSizeDetail).lineHeight)
    let singleTextHeight = mainTextHeight + kDefaultTableCellVerticalPadding
    let twoTextVerticalHeight = mainTextHeight + detailTextHeight + kDefaultTableCellVerticalPadding
    let twoTextHorizontalHeight = max(mainTextHeight, detailTextHeight) + kDefaultTableCellVerticalPadding

    switch _listViewLayoutMode {
    case 1:
      return max(kDefaultTableCellHeight, twoTextVerticalHeight)
    case 2:
      return max(kDefaultTableCellHeight, twoTextHorizontalHeight)
    case 3:
      return max(60.0, singleTextHeight)
    case 4:
      return max(60.0, twoTextVerticalHeight)
    case 5:
      return max(120.0, twoTextVerticalHeight)
    default:
      return max(kDefaultTableCellHeight, singleTextHeight)
    }
  }

  private func listFont(typeface: String, size: Int32) -> UIFont {
    let pointSize = CGFloat(size)
    if typeface == "1" {
      return UIFont(name: "Helvetica", size: pointSize) ?? UIFont.systemFont(ofSize: pointSize)
    } else if typeface == "2" {
      return UIFont(name: "Times New Roman", size: pointSize) ?? UIFont.systemFont(ofSize: pointSize)
    } else if typeface == "3" {
      return UIFont(name: "Courier", size: pointSize) ?? UIFont.systemFont(ofSize: pointSize)
    }
    return UIFont.systemFont(ofSize: pointSize)
  }

  private func preferredListViewWidth() -> CGFloat {
    if let formWidth = form?.scaleFrameLayout.bounds.width, formWidth > 0 {
      return formWidth
    }
    if let formWidth = form?.view.bounds.width, formWidth > 0 {
      return formWidth
    }
    return 320
  }

  private func invalidateListViewSize() {
    _rootView.invalidateIntrinsicContentSize()
    _rootView.setNeedsLayout()
    _rootView.superview?.setNeedsLayout()
  }

  private final class HListCell: UICollectionViewCell {
  static let reuseId = "HListCell"

  let imageView = UIImageView()
  let titleLabel = UILabel()
  let detailLabel = UILabel()

  override init(frame: CGRect) {
    super.init(frame: frame)

    // The key difference from before:
    backgroundColor = .clear  // don't fight our custom backgrounds
    contentView.backgroundColor = .clear

    // Prepare background + selection layers
    let bg = UIView(frame: .zero)
    bg.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    self.backgroundView = bg

    let selectedBG = UIView(frame: .zero)
    selectedBG.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    self.selectedBackgroundView = selectedBG

    // Layout:
    titleLabel.numberOfLines = 2
    detailLabel.numberOfLines = 2
    detailLabel.font = UIFont.systemFont(ofSize: 12)

    let vstack = UIStackView(arrangedSubviews: [titleLabel, detailLabel])
    vstack.axis = .vertical
    vstack.spacing = 4

    let hstack = UIStackView(arrangedSubviews: [imageView, vstack])
    hstack.axis = .horizontal
    hstack.alignment = .center
    hstack.spacing = 8

    contentView.addSubview(hstack)
    hstack.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
        hstack.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 8),
        hstack.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -8),
        hstack.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 8),
        hstack.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -8),
        imageView.widthAnchor.constraint(equalToConstant: 50)
    ])
  }

  required init?(coder: NSCoder) { fatalError() }
  }

  var elements: [String] {
      return _results ?? _elements
    }

  private var listItemCount: Int {
    if !_items.isEmpty {
      return _items.count
    }
    return elements.count
  }
    
  // UICollectionViewDataSource
  public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return listItemCount
  }

  public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    let cell = collectionView.dequeueReusableCell(withReuseIdentifier: HListCell.reuseId, for: indexPath) as! HListCell

    let item = listItem(at: indexPath.item) ?? makeListItem()
    let mainText = item["Text1"] as? String ?? ""
    let detailText = item["Text2"] as? String ?? ""
    var image: UIImage? = nil

    if let path = item["Image"] as? String,
       !path.isEmpty {
      image = AssetManager.shared.imageFromPath(path: path)
    }

    
    if _elementCornerRadius > 0 {
      cell.layer.cornerRadius = CGFloat(_elementCornerRadius) / CGFloat(COMPANION_CORRECTION)
      cell.layer.masksToBounds = true
    }
    
    (cell.backgroundView as? UIView)?.backgroundColor =
        (_elementColor != Color.none.int32)
        ? argbToColor(_elementColor)
        : argbToColor(_backgroundColor)

    (cell.selectedBackgroundView as? UIView)?.backgroundColor =
        argbToColor(_selectionColor)
    
    _collectionView.backgroundColor =
        argbToColor(_backgroundColor)

    cell.titleLabel.text = mainText
    cell.detailLabel.text = detailText
    cell.titleLabel.font = UIFont.systemFont(ofSize: CGFloat(_fontSize))
    cell.detailLabel.font = UIFont.systemFont(ofSize: CGFloat(_fontSizeDetail))

    // Typeface mapping similar to table
    if _fontTypeface == "1" { cell.titleLabel.font = UIFont(name: "Helvetica", size: CGFloat(_fontSize)) ?? cell.titleLabel.font }
    else if _fontTypeface == "2" { cell.titleLabel.font = UIFont(name: "Times New Roman", size: CGFloat(_fontSize)) ?? cell.titleLabel.font }
    else if _fontTypeface == "3" { cell.titleLabel.font = UIFont(name: "Courier", size: CGFloat(_fontSize)) ?? cell.titleLabel.font }

    if _fontTypefaceDetail == "1" { cell.detailLabel.font = UIFont(name: "Helvetica", size: CGFloat(_fontSizeDetail)) ?? cell.detailLabel.font }
    else if _fontTypefaceDetail == "2" { cell.detailLabel.font = UIFont(name: "Times New Roman", size: CGFloat(_fontSizeDetail)) ?? cell.detailLabel.font }
    else if _fontTypefaceDetail == "3" { cell.detailLabel.font = UIFont(name: "Courier", size: CGFloat(_fontSizeDetail)) ?? cell.detailLabel.font }

    // Text colors
    cell.titleLabel.textColor = argbToColor(_textColor)
    cell.detailLabel.textColor = argbToColor(_textColorDetail)

    // Image
    cell.imageView.image = image
    cell.imageView.isHidden = (image == nil)

    cell.titleLabel.textAlignment = nsTextAlignment(for: _textAlignmentMain, in: cell)
    cell.detailLabel.textAlignment = nsTextAlignment(for: _textAlignmentDetail, in: cell)

    let selectedBgView = UIView()
    selectedBgView.backgroundColor =
      argbToColor(_selectionColor)
    cell.selectedBackgroundView = selectedBgView
    return cell
  }

  // UICollectionViewDelegate (selection → AfterPicking)
  public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    if let item = listItem(at: indexPath.item) {
      _selectionIndex = Int32(indexPath.item) + 1
      _selection = item["Text1"] as? String ?? ""
      _selectionDetailText = item["Text2"] as? String ?? ""
    }
    AfterPicking()
  }

  // UICollectionViewDelegateFlowLayout (optional sizing)
  public func collectionView(_ collectionView: UICollectionView,
                             layout collectionViewLayout: UICollectionViewLayout,
                             sizeForItemAt indexPath: IndexPath) -> CGSize {
    // Make height roughly your rowHeight; width can scale with textSize
    
    //let h = max(CGFloat(_textSize) + 24, kDefaultItemSize.height)
    let h = collectionView.bounds.height
    let w: CGFloat
    switch _listViewLayoutMode {
      case 3, 4: w = 220   // room for image + text
      case 2:   w = 200
      default:  w = 160
    }
    return CGSize(width: w, height: h)
  }

}
