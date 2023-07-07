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
  fileprivate var _backgroundColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _elements = [String]()
  fileprivate var _selection = ""
  fileprivate var _selectionColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _selectionIndex = Int32(0)
  fileprivate var _showFilter = false
  fileprivate var _textColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _textSize = Int32(22)
  fileprivate var _automaticHeightConstraint: NSLayoutConstraint!
  fileprivate var _results: [String]? = nil
  fileprivate var _fontSizeDetail = Int32(16)
  
  fileprivate var _listData: [[String: String]] = []   //ListData
  fileprivate var _listViewLayoutMode = Int32()

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
      return _elements as [AnyObject]
    }
    set(elements) {
      _elements = elements.toStringArray()
      _automaticHeightConstraint.constant = _elements.isEmpty ? kDefaultTableCellHeight : kDefaultTableCellHeight * CGFloat(_elements.count)
      if let searchBar = _view.tableHeaderView as? UISearchBar {
        self.searchBar(searchBar, textDidChange: searchBar.text ?? "")
      } else {
        _view.reloadData()
      }
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
        
        let jsonObject = try getObjectFromJson(jsonString)
           print("JSON object: \(jsonObject)")
        
              if let dictionaries = try getObjectFromJson(jsonString) as? [[String: Any]] {

                _listData = dictionaries.compactMap { dictionary in
                                if let text1 = dictionary["Text1"] as? String,
                                   let text2 = dictionary["Text2"] as? String  {
                                  return ["Text1": text1, "Text2": text2]
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
          print(ListViewLayout)
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
      return _selectionIndex
    }
    set(selectionIndex) {
      if selectionIndex > 0 && selectionIndex <= Int32(_elements.count) {
        _selectionIndex = selectionIndex
        _selection = _elements[Int(selectionIndex) - 1]
      } else {
        _selectionIndex = 0
        _selection = ""
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
               cell.textLabel?.numberOfLines = 1
               cell.textLabel?.lineBreakMode = .byTruncatingTail
           }
    
    else {
             let listDataIndex = indexPath.row - _elements.count
              
              cell.textLabel?.text = _listData[listDataIndex]["Text1"]
              
        
      
             if _listViewLayoutMode == 1{
               cell.textLabel?.text = _listData[listDataIndex]["Text1"]
               cell.detailTextLabel?.text = _listData[listDataIndex]["Text2"]
             }
             
             else if _listViewLayoutMode == 2 {
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
              
               
             }
   
      
               
               
             if ((_listData[listDataIndex]["Text1"]?.contains("\n")) != nil) {
                   cell.textLabel?.numberOfLines = 0
                   cell.textLabel?.lineBreakMode = .byWordWrapping
               } else {
                   cell.textLabel?.numberOfLines = 1
                   cell.textLabel?.lineBreakMode = .byTruncatingTail
               }
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
    if _textColor == Color.default.int32 {
      cell.textLabel?.textColor = preferredBackgroundColor(form)
    } else {
      cell.textLabel?.textColor = argbToColor(_textColor)
    }
    if cell.selectedBackgroundView == nil {
      cell.selectedBackgroundView = UIView()
    }
//    if elements[indexPath.row].contains("\n") {
//      cell.textLabel?.numberOfLines = 0
//      cell.textLabel?.lineBreakMode = .byWordWrapping
//    }
//    else {
//      cell.textLabel?.numberOfLines = 1
//      cell.textLabel?.lineBreakMode = .byTruncatingTail
//    }
    cell.selectedBackgroundView?.backgroundColor = argbToColor(_selectionColor == Int32(bitPattern: Color.default.rawValue) ? Int32(bitPattern: kListViewDefaultSelectionColor.rawValue) : _selectionColor)
  
    
    return cell
  }

  open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return _listData.count
  }

  // MARK: UITableViewDelegate

  open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    _selectionIndex = Int32(indexPath.row) + 1
    _selection = elements[indexPath.row]
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
