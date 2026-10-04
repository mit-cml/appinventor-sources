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

  // ---- Selection (stored by ORIGINAL item index, so it survives filtering) ----
  /// The selected original indexes, in the order the items were picked. An array rather than a
  /// single value so MultiSelect can hold several; UIKit's own selection state cannot be the
  /// source of truth because `reloadData()` wipes it on every filter change.
  private(set) var selectedIndices: [Int] = []

  /// The first selected original index, or nil when nothing is selected.
  var firstSelection: Int? { selectedIndices.first }

  /// The most recently selected original index, or nil when nothing is selected.
  var lastSelection: Int? { selectedIndices.last }

  func isSelected(_ originalIndex: Int) -> Bool { selectedIndices.contains(originalIndex) }

  /// Selects only the given original index, replacing any previous selection.
  func selectOnly(_ originalIndex: Int) {
    selectedIndices = [originalIndex]
  }

  /// Adds or removes the given original index from the selection (used by MultiSelect).
  func toggleSelection(_ originalIndex: Int) {
    if let position = selectedIndices.firstIndex(of: originalIndex) {
      selectedIndices.remove(at: position)
    } else {
      selectedIndices.append(originalIndex)
    }
  }

  func clearSelection() {
    selectedIndices.removeAll()
  }

  /// Moves selected indexes up by `count` so they still point at the same items after rows are
  /// inserted at `index`. Selections before the insert point are unaffected.
  private func shiftSelectionForInsert(at index: Int, count: Int) {
    selectedIndices = selectedIndices.map { $0 >= index ? $0 + count : $0 }
  }

  /// Drops the selection of the item removed at `index` and moves the ones after it down one, so
  /// the remaining selections still point at the items the user picked.
  private func dropAndShiftSelectionForRemove(at index: Int) {
    selectedIndices = selectedIndices.compactMap { selected in
      if selected == index {
        return nil
      }
      return selected > index ? selected - 1 : selected
    }
  }

  // ---- Mutations (mirrors the Android ListDataModel's setItems/add/addAt/remove/clear) ----
  func clear() {
    elements.removeAll()
    items.removeAll()
    // The old items are gone, so the indexes that referred to them mean nothing now.
    selectedIndices.removeAll()
  }

  /// Replaces the list with rich rows, dropping any string rows (used by the ListData setter).
  func setItems(_ newItems: [[String: AnyObject]]) {
    elements.removeAll()
    items = newItems
    selectedIndices.removeAll()
  }

  // The append methods leave every existing index where it was, so the selection needs no
  // adjustment; only the insert and remove methods move rows around.

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
    shiftSelectionForInsert(at: index, count: 1)
  }

  func insert(_ item: [String: AnyObject], at index: Int) {
    items.insert(item, at: index)
    shiftSelectionForInsert(at: index, count: 1)
  }

  func insert(contentsOf texts: [String], at index: Int) {
    elements.insert(contentsOf: texts, at: index)
    shiftSelectionForInsert(at: index, count: texts.count)
  }

  func insert(contentsOf newItems: [[String: AnyObject]], at index: Int) {
    items.insert(contentsOf: newItems, at: index)
    shiftSelectionForInsert(at: index, count: newItems.count)
  }

  // Replacing a row moves nothing, so the indexes around it are untouched. The replaced row does
  // lose its selection: a different item occupies that position now, so a selection pointing there
  // no longer refers to what the user picked.

  func replace(at index: Int, with text: String) {
    elements[index] = text
    dropSelection(at: index)
  }

  func replace(at index: Int, with item: [String: AnyObject]) {
    items[index] = item
    dropSelection(at: index)
  }

  private func dropSelection(at index: Int) {
    selectedIndices.removeAll { $0 == index }
  }

  /// Removes the row at a real (unfiltered) position from whichever array holds it.
  func remove(at index: Int) {
    if items.count > index {
      items.remove(at: index)
    }
    if elements.count > index {
      elements.remove(at: index)
    }
    dropAndShiftSelectionForRemove(at: index)
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
  fileprivate var _multiSelect = false
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
      // Re-running the filter also re-applies the selection to the reloaded rows.
      self.searchBar(searchBar, textDidChange: searchBar.text ?? "")
      _collectionView.reloadData()
    } else {
      _view.reloadData()
      _collectionView.reloadData()
      // reloadData drops UIKit's selection state, so without a filter bar to do it for us the
      // highlight has to be put back or it vanishes every time the data changes.
      restoreSelectedRows()
    }
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
    guard index > 0, _model.item(at: Int(index) - 1) != nil else {
      // An index outside the list selects nothing, so report nothing rather than keeping a number
      // that points at no row.
      _model.clearSelection()
      clearSelectionInfo()
      restoreSelectedRows()
      return
    }
    let originalIndex = Int(index) - 1
    if _multiSelect {
      // Setting the index picks that row. Unlike a tap it never un-picks one, so that setting the
      // same index twice does not quietly undo itself.
      if !_model.isSelected(originalIndex) {
        _model.toggleSelection(originalIndex)
      }
    } else {
      _model.selectOnly(originalIndex)
    }
    readSelectionInfo(from: originalIndex)
    restoreSelectedRows(scrollTo: scroll ? originalIndex : nil)
  }

  /// Points what the Selection blocks report at the item at the given original index.
  /// Does not change which rows are selected.
  fileprivate func readSelectionInfo(from originalIndex: Int) {
    _selectionIndex = Int32(originalIndex) + 1
    _selection = _model.mainText(at: originalIndex)
    _selectionDetailText = _model.detailText(at: originalIndex)
  }

  /// Clears what the Selection blocks report, without touching the selection the model holds, so
  /// that MultiSelect keeps the rest of the set the user built up.
  fileprivate func clearSelectionInfo() {
    _selectionIndex = 0
    _selection = ""
    _selectionDetailText = ""
  }

  /// Re-applies the model's selection to UIKit, which is the one place the highlight can live but
  /// cannot be trusted to remember it: `reloadData()` wipes UIKit's selection, and it only ever
  /// tracks rows that are currently on screen. Rows the filter hides are skipped — they stay
  /// selected in the model and light up again when the query clears. Pass an original index to
  /// scroll that row into view.
  fileprivate func restoreSelectedRows(scrollTo originalIndex: Int? = nil) {
    for path in _view.indexPathsForSelectedRows ?? [] {
      _view.deselectRow(at: path, animated: false)
    }
    for path in _collectionView.indexPathsForSelectedItems ?? [] {
      _collectionView.deselectItem(at: path, animated: false)
    }
    for selected in _model.selectedIndices {
      guard let displayRow = _model.toDisplayPosition(selected) else {
        continue
      }
      let path = IndexPath(row: displayRow, section: 0)
      let scrollToThisRow = selected == originalIndex
      _view.selectRow(at: path, animated: scrollToThisRow,
                      scrollPosition: scrollToThisRow ? .middle : .none)
      _collectionView.selectItem(at: path, animated: scrollToThisRow,
                                 scrollPosition: scrollToThisRow ? .centeredHorizontally : [])
    }
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

  /// Whether the user can select more than one element at a time. While this is on, a tap adds a
  /// row to or removes it from `SelectedItems`, and `Selection` / `SelectionIndex` report the row
  /// touched last.
  @objc open var MultiSelect: Bool {
    get {
      return _multiSelect
    }
    set(multiSelect) {
      _multiSelect = multiSelect
      _view.allowsMultipleSelection = multiSelect
      _collectionView.allowsMultipleSelection = multiSelect
      // Switching modes starts from a clean slate: a set built up in one mode has no meaning in
      // the other, and a leftover highlight would outlive what the Selection blocks report.
      _model.clearSelection()
      clearSelectionInfo()
      _view.reloadData()
      _collectionView.reloadData()
    }
  }

  /// Every element the user has selected, in the order they were picked. Unless `MultiSelect` is
  /// enabled this holds at most one element, since selecting a row replaces the previous selection.
  @objc open var SelectedItems: [AnyObject] {
    return _model.selectedIndices.compactMap { _model.item(at: $0) }
      .map { _model.isDataMode ? $0 as AnyObject : ($0["Text1"] as? String ?? "") as AnyObject }
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
    if _selectionIndex >= addIndex {
      // The new row pushed the selected item down one.
      _selectionIndex += 1
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
    if _selectionIndex >= addIndex {
      // The new rows pushed the selected item down by however many went in.
      _selectionIndex += Int32(newItems.count)
    }
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

  /// Replaces the item at the given index. The row stops being selected, because a different item
  /// occupies that position afterwards. When MultiSelect left other items selected, Selection and
  /// SelectionIndex move to the most recent of those, so they report nothing selected only when
  /// SelectedItems really is empty.
  @objc open func UpdateItemAtIndex(_ updateIndex: Int32, _ mainText: String, _ detailText: String,
                                    _ imageName: String) {
    guard updateIndex > 0 && updateIndex <= Int32(listItemCount) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "UpdateItemAtIndex",
                                                   ErrorMessage.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, updateIndex)
      return
    }
    let index = Int(updateIndex - 1)
    if usesPlainStrings {
      _model.replace(at: index, with: mainText)
    } else {
      _model.replace(at: index,
                     with: ListDataModel.makeItem(text1: mainText, text2: detailText, image: imageName))
    }
    if _selectionIndex == updateIndex {
      // The replaced row has stopped being one of the user's picks, so move what the singular
      // properties report onto the most recent pick that is left. Reporting nothing while
      // MultiSelect still holds a set would read as "nothing is selected", which is not true.
      if let remaining = _model.lastSelection {
        readSelectionInfo(from: remaining)
      } else {
        clearSelectionInfo()
      }
    }
    // Replacing a row moves nothing, so no other selected index needs adjusting.
    elementsCount()
  }

  @objc open func RemoveItemAtIndex(_ index: Int32) {
    if index < 1 || index > Int32(listItemCount) {
      _container?.form?.dispatchErrorOccurredEvent(self, "RemoveItemAtIndex",
           ErrorMessage.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, index)
      return
    }
    _model.remove(at: Int(index - 1))
    if _selectionIndex == index {
      // The item the blocks are reporting is the one that just went away.
      clearSelectionInfo()
    } else if _selectionIndex > index {
      // Everything after the hole moved down one, so follow the item that is still selected.
      _selectionIndex -= 1
    }
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
              cell.imageView!.heightAnchor.constraint(equalToConstant: CGFloat(_imageHeight / 4))
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

    open func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
      return preferredRowHeight
    }

    open func tableView(_ tableView: UITableView, estimatedHeightForRowAt indexPath: IndexPath) -> CGFloat {
      return preferredRowHeight
    }

    open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
      // Map the tapped visible row back to its real position, so selection is correct while filtering.
      // Tapping must not scroll the list — the row is already where the user is looking.
      handleTap(onOriginalIndex: _model.originalIndex(indexPath.row), selected: true)
    }

    /// With `allowsMultipleSelection` on, UIKit reports a tap on an already selected row here
    /// rather than through `didSelectRowAt`, so the deselecting half of a MultiSelect tap only
    /// arrives via this method. Programmatic `deselectRow` calls do not reach it, and in
    /// single-select mode UIKit uses it to retire the previous row, which is bookkeeping the model
    /// has already done — hence the guard.
    open func tableView(_ tableView: UITableView, didDeselectRowAt indexPath: IndexPath) {
      guard _multiSelect else {
        return
      }
      handleTap(onOriginalIndex: _model.originalIndex(indexPath.row), selected: false)
    }

    /// The one place a tap is turned into a selection change, for both the table and the
    /// collection view. MultiSelect toggles the tapped row and leaves the rest of the set alone;
    /// otherwise the tap replaces the selection. Either way the row just touched is what the
    /// Selection blocks go on to report, including when the touch was the one that un-picked it.
    fileprivate func handleTap(onOriginalIndex originalIndex: Int, selected: Bool) {
      if _multiSelect {
        // UIKit has already moved its own highlight, so only the model needs syncing.
        if _model.isSelected(originalIndex) != selected {
          _model.toggleSelection(originalIndex)
        }
        readSelectionInfo(from: originalIndex)
      } else {
        applySelection(Int32(originalIndex) + 1, scroll: false)
      }
      AfterPicking()
    }
    
  // MARK: UISearchBarDelegate

  open func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    _model.setFilter(searchText)
    _view.reloadData()
    _collectionView.reloadData()
    // reloadData drops UIKit's selection state, so every highlight is lost on each filter change.
    // Selection is stored against original indexes, so re-highlight whichever selected rows the
    // filter still shows.
    restoreSelectedRows()
    // A single selection is the one thing Selection reports, so a hidden one would be a selection
    // the user cannot see: drop it once the filter hides it. MultiSelect is a set the user is
    // building up instead, so searching again must not throw away what they already picked.
    if !_multiSelect && _selectionIndex > 0
        && _model.toDisplayPosition(Int(_selectionIndex) - 1) == nil {
      applySelection(0, scroll: false)
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
    handleTap(onOriginalIndex: _model.originalIndex(indexPath.item), selected: true)
  }

  /// The collection view's half of the MultiSelect tap, for the same reason as the table's
  /// `didDeselectRowAt`: un-picking an already selected item is only reported here.
  public func collectionView(_ collectionView: UICollectionView, didDeselectItemAt indexPath: IndexPath) {
    guard _multiSelect else {
      return
    }
    handleTap(onOriginalIndex: _model.originalIndex(indexPath.item), selected: false)
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
