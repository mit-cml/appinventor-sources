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

/**
 * Owns the ListView's non-visual list data — the ListView's single source of truth for the row
 * data, mirroring the Android `ListDataModel`. `ListView` reads and mutates the list through this
 * model instead of holding its own fields.
 */
class ListDataModel {
  /// Simple string items (populated for string lists). Mutate through the methods below.
  private(set) var elements = [String]()
  /// Rich rows: Text1 / Text2 / Image (populated for ListData / image layouts).
  /// Mutate through the methods below.
  private(set) var items: [[String: AnyObject]] = []

  // ---- Filtering (makes the search box actually filter the list, for both string and rich rows) ----
  private var query = ""

  /// true when the list holds rich rows (dicts); false for a plain string list.
  var isDataMode: Bool { !items.isEmpty }
  /// Total number of rows before filtering.
  var count: Int { isDataMode ? items.count : elements.count }

  /// Original-index positions of the rows matching the current search (identity when no search).
  /// Recomputed on demand — fine for realistic list sizes; cache if huge lists ever matter.
  var filteredIndices: [Int] {
    guard !query.isEmpty else { return Array(0..<count) }
    let q = query.lowercased()
    return (0..<count).filter { i in
      if isDataMode {
        let row = items[i]
        let t1 = (row["Text1"] as? String ?? "").lowercased()
        let t2 = (row["Text2"] as? String ?? "").lowercased()
        return t1.contains(q) || t2.contains(q)
      } else {
        return elements[i].lowercased().contains(q)
      }
    }
  }

  func setFilter(_ text: String) { query = text }

  // ---- What the table / collection actually draws (filter-aware) ----
  /// Number of rows currently visible (after filtering).
  var displayCount: Int { filteredIndices.count }
  /// Maps a visible row back to its real position in `elements` / `items`.
  func originalIndex(_ displayRow: Int) -> Int { filteredIndices[displayRow] }
  /// Maps a real position to the row it is drawn on, or nil when the filter hides it.
  func toDisplayPosition(_ originalIndex: Int) -> Int? { filteredIndices.firstIndex(of: originalIndex) }

  // ---- Reading a row (callers never have to know whether it came from `elements` or `items`) ----
  static func makeItem(text1: String = "", text2: String = "", image: String = "") -> [String: AnyObject] {
    return [
      "Text1": text1 as AnyObject,
      "Text2": text2 as AnyObject,
      "Image": image as AnyObject
    ]
  }

  /// The row at a real (unfiltered) position, or nil when out of range.
  func item(at index: Int) -> [String: AnyObject]? {
    guard index >= 0 && index < count else {
      return nil
    }
    return isDataMode ? items[index] : ListDataModel.makeItem(text1: elements[index])
  }

  func mainText(at index: Int) -> String {
    return item(at: index)?["Text1"] as? String ?? ""
  }

  func detailText(at index: Int) -> String {
    return item(at: index)?["Text2"] as? String ?? ""
  }

  // ---- Mutations (mirrors the Android ListDataModel's setItems/add/addAt/remove/clear) ----
  func clear() {
    elements.removeAll()
    items.removeAll()
  }

  /// Replaces the list with rich rows, dropping any string rows (used by the ListData setter).
  func setItems(_ newItems: [[String: AnyObject]]) {
    elements.removeAll()
    items = newItems
  }

  func append(_ text: String) {
    elements.append(text)
  }

  func append(_ item: [String: AnyObject]) {
    items.append(item)
  }

  func append(contentsOf newItems: [[String: AnyObject]]) {
    items.append(contentsOf: newItems)
  }

  func insert(_ text: String, at index: Int) {
    elements.insert(text, at: index)
  }

  func insert(_ item: [String: AnyObject], at index: Int) {
    items.insert(item, at: index)
  }

  func insert(contentsOf texts: [String], at index: Int) {
    elements.insert(contentsOf: texts, at: index)
  }

  func insert(contentsOf newItems: [[String: AnyObject]], at index: Int) {
    items.insert(contentsOf: newItems, at: index)
  }

  /// Removes the row at a real (unfiltered) position from whichever array holds it.
  func remove(at index: Int) {
    if items.count > index {
      items.remove(at: index)
    }
    if elements.count > index {
      elements.remove(at: index)
    }
  }
}

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
  fileprivate let _model = ListDataModel()
  fileprivate var _selection = ""
  fileprivate var _selectionDetailText = ""
  fileprivate var _selectionColor = kListViewDefaultSelectionColor
  fileprivate var _selectionIndex = Int32(0)
  fileprivate var _showFilter = false
  fileprivate var _textColor = kListViewDefaultTextColor
  fileprivate var _textColorDetail = kListViewDefaultTextColor
  fileprivate var _fontSize = Int32(22)
  fileprivate var _automaticHeightConstraint: NSLayoutConstraint?
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
    // Rows size themselves from their own constraints, whatever the layout mode.
    _view.rowHeight = UITableView.automaticDimension

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
      if _model.items.count > 0 {
        return _model.items as [AnyObject]
      } else {
        return _model.elements as [AnyObject]
      }
    }
    set(elements) {
      _model.clear()
      // The old selection refers to an item that no longer exists, so drop it (Android clears the
      // selection the same way whenever the data is replaced).
      applySelection(0, scroll: false)
      guard !elements.isEmpty else {
        elementsCount()
        return
      }
      addElements(elements)
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
    return ListDataModel.makeItem(text1: toString(item))
  }

  private func normalizedElements(_ elements: [AnyObject]) -> [AnyObject] {
    if elements.first is SCMSymbol {
      return Array(elements.dropFirst())
    }
    return elements
  }
  
    private func addElements(_ elements: [AnyObject]) {
      if !elements.isEmpty {
        let testItemsForDict = _model.items.first(where: { $0 is NSDictionary })
        let testElementsForDict = elements.first(where: { $0 is NSDictionary })
        let otherElements = elements.filter { !($0 is NSDictionary) }
        let useDictFormat = testItemsForDict?["Text1"] != nil || testElementsForDict?["Text1"] != nil
        if useDictFormat {
          _model.append(contentsOf: elements.compactMap { $0 as? [String: AnyObject] })
          for item in otherElements {
            // Fall back to simple text item
            if let str = item as? String {
              _model.append(ListDataModel.makeItem(text1: str))
            } else if let n = item as? NSNumber {
              _model.append(ListDataModel.makeItem(text1: n.stringValue))
            }
          }
        }
        _model.insert(contentsOf: otherElements.toStringArray(), at: 0)
        elementsCount()
      }
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
      // A row in the image layouts is as tall as its image, so the ListView's own automatic
      // height has to follow the image size the same way it follows the row count.
      _automaticHeightConstraint?.constant = preferredTableHeight
      invalidateListViewSize()
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
        let jsonString = try getJsonRepresentation(_model.items as AnyObject)
        return jsonString
      } catch {
        print("Error serializing JSON: \(error)")
        return ""
      }
    }
    set(jsonString) {
      do {
        if let dictionaries = try getObjectFromJson(jsonString) as? [[String: Any]] {
          _model.setItems(dictionaries.compactMap { dictionary in
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
          })
          // The old selection refers to an item that no longer exists, so drop it.
          applySelection(0, scroll: false)
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


  /// The one place a selection is applied: derives Selection and SelectionDetailText from the model
  /// and moves the highlight to the row that item is drawn on. `index` is 1-based; 0 clears.
  /// Mirrors Android, where SelectionIndex(int) is likewise the single derivation point.
  fileprivate func applySelection(_ index: Int32, scroll: Bool) {
    // Drop the old highlight first: the newly selected item may be hidden by the filter, in which
    // case there is no row to move the highlight to.
    if let selectedRow = _view.indexPathForSelectedRow {
      _view.deselectRow(at: selectedRow, animated: false)
    }
    for path in _collectionView.indexPathsForSelectedItems ?? [] {
      _collectionView.deselectItem(at: path, animated: false)
    }
    guard index > 0, let item = _model.item(at: Int(index) - 1) else {
      _selectionIndex = 0
      _selection = ""
      _selectionDetailText = ""
      return
    }
    _selectionIndex = index
    _selection = item["Text1"] as? String ?? ""
    _selectionDetailText = item["Text2"] as? String ?? ""
    // The highlight belongs to the visible row, which differs from the item's real position while a
    // filter is active. A nil position means the filter currently hides the selected item.
    guard let displayRow = _model.toDisplayPosition(Int(index) - 1) else {
      return
    }
    let path = IndexPath(row: displayRow, section: 0)
    _view.selectRow(at: path, animated: true, scrollPosition: scroll ? .middle : .none)
    _collectionView.selectItem(at: path, animated: true,
                               scrollPosition: scroll ? .centeredHorizontally : [])
  }

  @objc open var Selection: String {
    get {
      return _selection
    }
    set(selection) {
      let index = (0..<_model.count).first { _model.mainText(at: $0) == selection }
      applySelection(index.map { Int32($0) + 1 } ?? 0, scroll: false)
    }
  }

  @objc open var SelectionDetailText: String {
    get {
      return _selectionDetailText
    }
    set(selectionDetailText) {
      // Only rich rows carry detail text, so a plain string list never matches and clears instead.
      let index = _model.isDataMode
        ? (0..<_model.count).first { _model.detailText(at: $0) == selectionDetailText }
        : nil
      applySelection(index.map { Int32($0) + 1 } ?? 0, scroll: false)
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
      applySelection(selectionIndex, scroll: true)
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

  /// True when the list currently holds plain string rows rather than Text1/Text2/Image rows.
  /// For an empty list this falls back to the configured layout, matching the Android behavior.
  private var usesPlainStrings: Bool {
    if !_model.items.isEmpty {
      return false
    }
    if !_model.elements.isEmpty {
      return true
    }
    return _listViewLayoutMode == 0
  }

  @objc open func AddItem(_ mainText: String, _ detailText: String, _ imageName: String) {
    if usesPlainStrings {
      _model.append(mainText)
    } else {
      _model.append(ListDataModel.makeItem(text1: mainText, text2: detailText, image: imageName))
    }
    elementsCount()
  }

  @objc open func AddItemAtIndex(_ addIndex: Int32, _ mainText: String, _ detailText: String, _ imageName: String) {
    guard addIndex > 0 && addIndex <= Int32(_model.count) + 1 else {
      _container?.form?.dispatchErrorOccurredEvent(self, "AddItemAtIndex",
                                                   ErrorMessage.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, addIndex)
      return
    }
    let index = Int(addIndex - 1)
    if usesPlainStrings {
      _model.insert(mainText, at: index)
    } else {
      _model.insert(ListDataModel.makeItem(text1: mainText, text2: detailText, image: imageName),
                    at: index)
    }
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
    
    let index = Int(addIndex - 1)
    let newItems = elements.map { normalizedListItem(from: $0) }
    _model.insert(contentsOf: newItems, at: index)
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
    _model.remove(at: Int(index - 1))
    elementsCount()
  }

  // MARK: Events

  @objc open func AfterPicking() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterPicking")
  }

  // MARK: UITableViewDataSource

    open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
      let layoutReuseIdentifier = "\(kDefaultTableCell)\(_listViewLayoutMode)"
      let cell = tableView.dequeueReusableCell(withIdentifier: layoutReuseIdentifier) ??
        UITableViewCell(style: .subtitle, reuseIdentifier: layoutReuseIdentifier)
      // Map the visible row back to its real position so search filtering works.
      let origRow = _model.originalIndex(indexPath.row)
      let item = _model.item(at: origRow) ?? ListDataModel.makeItem()
      cell.imageView?.image = nil
      // This method rebuilds a cell's subviews every time it binds, so drop the previous build
      // first. A stack left behind by an earlier bind keeps its pins to the contentView's top and
      // bottom, and since rows size themselves those stale pins fight the live stack over the
      // row's height. Only stacks are removed: the layouts that do not build one (mode 0 and the
      // fallback) leave their system labels where UIKit put them, and the reuse identifier is per
      // layout mode, so a cell never crosses over from a layout that does build one.
      cell.contentView.subviews.filter { $0 is UIStackView }.forEach { $0.removeFromSuperview() }

      if _listViewLayoutMode == 0 {
        cell.textLabel?.text = item["Text1"] as? String
        cell.detailTextLabel?.text = ""
      } else {
        if _listViewLayoutMode == 1 {
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
          cell.textLabel?.text = item["Text1"] as? String
          cell.detailTextLabel?.text = item["Text2"] as? String

          // Configure the layout
          cell.layoutMargins = UIEdgeInsets.zero
          cell.separatorInset = UIEdgeInsets.zero
          cell.preservesSuperviewLayoutMargins = true

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
          cell.textLabel?.text = item["Text1"] as? String ?? ""
          cell.detailTextLabel?.text = ""

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

          // The image is optional, the stack is not: it is what pins the row's content to the
          // contentView, and without it a self-sizing row has nothing to take its height from.
          var imageConstraints: [NSLayoutConstraint] = []
          if let imagePath = item["Image"] as? String, !imagePath.isEmpty,
             let image = AssetManager.shared.imageFromPath(path: imagePath) {
            cell.imageView?.image = image
            cell.imageView?.contentMode = .scaleAspectFit
            stackView.addArrangedSubview(cell.imageView!)
            imageConstraints = [
              cell.imageView!.widthAnchor.constraint(equalToConstant: CGFloat(_imageWidth / 4)),
              cell.imageView!.heightAnchor.constraint(equalToConstant: CGFloat(_imageHeight / 4))
            ]
          }

          // Add the labels to the stack view
          stackView.addArrangedSubview(cell.textLabel!)

          // Add the stack view to the cell's content view
          cell.contentView.addSubview(stackView)

          // Set up constraints
          stackView.translatesAutoresizingMaskIntoConstraints = false
          NSLayoutConstraint.activate(imageConstraints + [
            stackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
            stackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
            stackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor, constant: 8.0),
            stackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor, constant: -8.0)
          ])
        } else if _listViewLayoutMode == 4 {
          cell.textLabel?.text = item["Text1"] as? String ?? ""
          cell.detailTextLabel?.text = item["Text2"] as? String ?? ""

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

          // The image is optional, the stack is not: it is what pins the row's content to the
          // contentView, and without it a self-sizing row has nothing to take its height from.
          var imageConstraints: [NSLayoutConstraint] = []
          if let imagePath = item["Image"] as? String, !imagePath.isEmpty,
             let image = AssetManager.shared.imageFromPath(path: imagePath) {
            cell.imageView?.image = image
            cell.imageView?.contentMode = .scaleAspectFit
            horizontalStackView.addArrangedSubview(cell.imageView!)
            imageConstraints = [
              cell.imageView!.widthAnchor.constraint(equalToConstant: CGFloat(_imageWidth / 4)),
              cell.imageView!.heightAnchor.constraint(equalToConstant: CGFloat(_imageHeight / 4))
            ]
          }

          // Add the nested vertical stack view to the horizontal stack view
          horizontalStackView.addArrangedSubview(verticalStackView)

          // Add the textLabel and detailTextLabel to the vertical stack view
          verticalStackView.addArrangedSubview(cell.textLabel!)
          verticalStackView.addArrangedSubview(cell.detailTextLabel!)

          // Add the horizontal stack view to the cell's content view
          cell.contentView.addSubview(horizontalStackView)

          // Set up constraints
          horizontalStackView.translatesAutoresizingMaskIntoConstraints = false
          NSLayoutConstraint.activate(imageConstraints + [
            horizontalStackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
            horizontalStackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
            horizontalStackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor, constant: 8.0),
            horizontalStackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor, constant: -8.0)
          ])
        } else if _listViewLayoutMode == 5 {
          cell.textLabel?.text = item["Text1"] as? String ?? ""
          cell.detailTextLabel?.text = item["Text2"] as? String ?? ""

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

          // The image is optional, the stack is not: it is what pins the row's content to the
          // contentView, and without it a self-sizing row has nothing to take its height from.
          var imageConstraints: [NSLayoutConstraint] = []
          if let imagePath = item["Image"] as? String, !imagePath.isEmpty,
             let image = AssetManager.shared.imageFromPath(path: imagePath) {
            cell.imageView?.image = image
            cell.imageView?.contentMode = .scaleAspectFit
            verticalStackView.addArrangedSubview(cell.imageView!)
            imageConstraints = [
              cell.imageView!.widthAnchor.constraint(equalToConstant: CGFloat(_imageWidth / 4)),
              cell.imageView!.heightAnchor.constraint(equalToConstant: CGFloat(_imageHeight / 4))
            ]
          }
          verticalStackView.addArrangedSubview(labelsStackView)

          // Add the outer stack to the cell's content view
          cell.contentView.addSubview(verticalStackView)

          // Set up constraints. The labelsStackView width is pinned to the
          // outer stack so labels span full row width while the image stays
          // centered at its intrinsic / explicit size.
          verticalStackView.translatesAutoresizingMaskIntoConstraints = false
          NSLayoutConstraint.activate(imageConstraints + [
            verticalStackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
            verticalStackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
            verticalStackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor, constant: 8.0),
            verticalStackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor, constant: -8.0)
          ])
        } else {
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

    let selectedBgView = UIView()
    selectedBgView.backgroundColor = argbToColor(_selectionColor)
    cell.selectedBackgroundView = selectedBgView

    cell.textLabel?.textAlignment = nsTextAlignment(for: _textAlignmentMain, in: cell)
    cell.detailTextLabel?.textAlignment = nsTextAlignment(for: _textAlignmentDetail, in: cell)

    return cell
  }

  
    open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
      return _model.displayCount
    }

    // MARK: UITableViewDelegate

    // Deliberately no heightForRowAt. Implementing it makes UIKit ignore rowHeight,
    // automaticDimension included, so a font-derived number would override the cell's
    // own constraints. Every layout pins its content to the contentView's top and
    // bottom, so Auto Layout already knows the real height, image and wrapped text
    // included. This stays an estimate, and an estimate is allowed to be approximate.
    open func tableView(_ tableView: UITableView, estimatedHeightForRowAt indexPath: IndexPath) -> CGFloat {
      return preferredRowHeight
    }

    open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
      // Map the tapped visible row back to its real position, so selection is correct while filtering.
      // Tapping must not scroll the list — the row is already where the user is looking.
      applySelection(Int32(_model.originalIndex(indexPath.row)) + 1, scroll: false)
      AfterPicking()
    }
    
  // MARK: UISearchBarDelegate

  open func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    _model.setFilter(searchText)
    _view.reloadData()
    _collectionView.reloadData()
    // reloadData drops UIKit's selection state, so the highlight is lost on every filter change.
    // Selection is stored against the original index, so re-highlight the selected item's new
    // visible row while it survives the filter, and clear it only when the filter hides it, so the
    // user never ends up with a selection they cannot see.
    if _selectionIndex > 0 {
      if let displayRow = _model.toDisplayPosition(Int(_selectionIndex) - 1) {
        _view.selectRow(at: IndexPath(row: displayRow, section: 0), animated: false, scrollPosition: .none)
        _collectionView.selectItem(at: IndexPath(item: displayRow, section: 0), animated: false, scrollPosition: [])
      } else {
        applySelection(0, scroll: false)
      }
    }
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

    // The image layouts pin the image to a quarter of its set size — keep this in step with the
    // height anchors in cellForRowAt. The 60 and 120 floors below are what a default 200px image
    // works out to, so without this term a taller image gives correct rows inside a ListView that
    // is still sized as though every image were 200px.
    let imageHeight = CGFloat(_imageHeight / 4)

    switch _listViewLayoutMode {
    case 1:
      return max(kDefaultTableCellHeight, twoTextVerticalHeight)
    case 2:
      return max(kDefaultTableCellHeight, twoTextHorizontalHeight)
    case 3:
      // Image beside the text, so the row is as tall as the taller of the two.
      return max(60.0, singleTextHeight, imageHeight + kDefaultTableCellVerticalPadding)
    case 4:
      // Image beside both labels, so again the taller of the two.
      return max(60.0, twoTextVerticalHeight, imageHeight + kDefaultTableCellVerticalPadding)
    case 5:
      // Image above both labels, so here they add up.
      return max(120.0, imageHeight + twoTextVerticalHeight)
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

  private var listItemCount: Int {
    return _model.count
  }

  // UICollectionViewDataSource
  public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return _model.displayCount
  }

  public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    let cell = collectionView.dequeueReusableCell(withReuseIdentifier: HListCell.reuseId, for: indexPath) as! HListCell

    // Map the visible item back to its real position so search filtering works here too.
    let origRow = _model.originalIndex(indexPath.item)
    let mainText = _model.mainText(at: origRow)
    let detailText = _model.detailText(at: origRow)
    var image: UIImage? = nil
    if let path = _model.item(at: origRow)?["Image"] as? String, !path.isEmpty {
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
    // Map the tapped visible item back to its real position, so selection is correct while filtering.
    applySelection(Int32(_model.originalIndex(indexPath.item)) + 1, scroll: false)
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
