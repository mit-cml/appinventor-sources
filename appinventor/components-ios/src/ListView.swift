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
  fileprivate var _fontSizeDetail = Int32(16)
  fileprivate var _listData: [[String: String]] = []   //ListData
  fileprivate var _elementsFromString = [String]()
  fileprivate var _listViewLayoutMode = Int32(0)
  fileprivate var _fontTypeface: String = ""
  fileprivate var _fontTypefaceDetail: String = ""
  fileprivate var _orientation = Int32(1)
  
  // Search Logic Variables
  fileprivate var _searching = false
  fileprivate var _elementsFromStringResults: [String]? = nil
  fileprivate var _mainTextResults: [String]? = nil
  fileprivate var _detailTextResults: [String]? = nil
  fileprivate var _imageNameResults: [String]? = nil
  
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
        _elementsFromString = elements.split(",") as [String]
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
        filter.showsCancelButton =  true

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
    if index < 1 || index > max(_listData.count, _elementsFromString.count) {
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
    
    if !_elementsFromString.isEmpty {
      cell.textLabel?.text = _searching && _elementsFromStringResults != nil ? _elementsFromStringResults![indexPath.row] : _elementsFromString[indexPath.row]
      cell.textLabel?.numberOfLines = 0
      cell.textLabel?.lineBreakMode = .byWordWrapping
    }
    else {
      let listDataIndex = indexPath.row
      
      // MainText Layout
      if _listViewLayoutMode == 0{
        cell.textLabel?.text = _searching && _mainTextResults != nil ? _mainTextResults![indexPath.row] : _listData[listDataIndex]["Text1"]
        cell.textLabel?.numberOfLines = 0
        cell.textLabel?.lineBreakMode = .byWordWrapping
      }
      
      // MainText, DetailText Vertical Layout
      else if _listViewLayoutMode == 1{
        cell.textLabel?.text = _searching && _mainTextResults != nil ? _mainTextResults![indexPath.row] : _listData[listDataIndex]["Text1"]
        cell.detailTextLabel?.text = _searching && _detailTextResults != nil ? _detailTextResults![indexPath.row] : _listData[listDataIndex]["Text2"]
        cell.textLabel?.numberOfLines = 0
        cell.textLabel?.lineBreakMode = .byWordWrapping
      }
      
      // MainText, DetailText Horizontal Layout
      else if _listViewLayoutMode == 2 {
        
        tableView.rowHeight = 60
        
        cell.textLabel?.text = _searching && _mainTextResults != nil ? _mainTextResults![indexPath.row] : _listData[listDataIndex]["Text1"]
        cell.detailTextLabel?.text = _searching && _detailTextResults != nil ? _detailTextResults![indexPath.row] : _listData[listDataIndex]["Text2"]
        cell.textLabel?.numberOfLines = 0
        cell.textLabel?.lineBreakMode = .byWordWrapping
        
        cell.layoutMargins = UIEdgeInsets.zero
        cell.separatorInset = UIEdgeInsets.zero
        cell.preservesSuperviewLayoutMargins = true
        
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.alignment = .leading
        stackView.distribution = .fill
        stackView.addArrangedSubview(cell.textLabel!)
        
        let spacerView = UIView()
        spacerView.widthAnchor.constraint(equalToConstant: 8.0).isActive = true
        stackView.addArrangedSubview(spacerView)
        
        stackView.addArrangedSubview(cell.detailTextLabel!)
        cell.contentView.addSubview(stackView)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        stackView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
          stackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
          stackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
          cell.detailTextLabel!.centerYAnchor.constraint(equalTo: cell.contentView.centerYAnchor),
          cell.textLabel!.centerYAnchor.constraint(equalTo: cell.contentView.centerYAnchor),
        ])
        
      }
      
      // Image, MainText Layout
      else if _listViewLayoutMode == 3 {
        
        tableView.rowHeight = 60
        cell.textLabel?.text = _searching && _mainTextResults != nil ? _mainTextResults![indexPath.row] : _listData[listDataIndex]["Text1"]
        cell.textLabel?.numberOfLines = 0
        cell.textLabel?.lineBreakMode = .byWordWrapping
        
        // Clear the previous image to avoid flickering
        cell.imageView?.image = nil
        
        if let imagePath = _searching && _imageNameResults != nil ? _imageNameResults![indexPath.row] : _listData[listDataIndex]["Image"] {
          // Load the image asynchronously
          DispatchQueue.global().async {
            if let image = AssetManager.shared.imageFromPath(path: imagePath) {
              // Update the UI on the main thread
              DispatchQueue.main.async {
                cell.imageView?.image = image
                cell.setNeedsLayout()
              }
            }
          }
        }
        
        cell.layoutMargins = UIEdgeInsets.zero
        cell.separatorInset = UIEdgeInsets.zero
        cell.preservesSuperviewLayoutMargins = true
        
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.distribution = .fill
        stackView.spacing = 8.0
        
        stackView.addArrangedSubview(cell.imageView!)
        stackView.addArrangedSubview(cell.textLabel!)
        cell.contentView.addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
          stackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
          stackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
          stackView.topAnchor.constraint(equalTo: cell.contentView.topAnchor, constant: 8.0),
          stackView.bottomAnchor.constraint(equalTo: cell.contentView.bottomAnchor, constant: -8.0),
          cell.imageView!.widthAnchor.constraint(equalToConstant: 58.0), // Set the width of the image view
          cell.imageView!.heightAnchor.constraint(equalToConstant: 58.0), // Set the height of the image view
          cell.textLabel!.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 60)
        ])
      }
      
      // Image, MainText, DetailText Vertical Layout
      else if _listViewLayoutMode == 4 {
        tableView.rowHeight = 60
        cell.textLabel?.text = _searching && _mainTextResults != nil ? _mainTextResults![indexPath.row] : _listData[listDataIndex]["Text1"]
        cell.detailTextLabel?.text = _searching && _detailTextResults != nil ? _detailTextResults![indexPath.row] : _listData[listDataIndex]["Text2"]
        cell.textLabel?.numberOfLines = 0
        cell.textLabel?.lineBreakMode = .byWordWrapping
        
        // Clear the previous image to avoid flickering
        cell.imageView?.image = nil
        
        if let imagePath = _searching && _imageNameResults != nil ? _imageNameResults![indexPath.row] : _listData[listDataIndex]["Image"] {
          // Load the image asynchronously
          DispatchQueue.global().async {
            if let image = AssetManager.shared.imageFromPath(path: imagePath) {
              // Update the UI on the main thread
              DispatchQueue.main.async {
                cell.imageView?.image = image
                cell.setNeedsLayout()
              }
            }
          }
        }
        
        cell.layoutMargins = UIEdgeInsets.zero
        cell.separatorInset = UIEdgeInsets.zero
        cell.preservesSuperviewLayoutMargins = true
        
        let HStackView = UIStackView()
        HStackView.axis = .horizontal
        HStackView.distribution = .fill
        HStackView.spacing = 8.0
        
        let VStackView = UIStackView()
        VStackView.axis = .vertical
        VStackView.distribution = .fill
        VStackView.spacing = 8.0
        
        VStackView.addArrangedSubview(cell.textLabel!)
        VStackView.addArrangedSubview(cell.detailTextLabel!)
        
        HStackView.addArrangedSubview(cell.imageView!)
        HStackView.addArrangedSubview(VStackView)
        cell.contentView.addSubview(HStackView)
        HStackView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
          HStackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 8.0),
          HStackView.trailingAnchor.constraint(equalTo: cell.contentView.trailingAnchor, constant: -8.0),
          cell.imageView!.widthAnchor.constraint(equalToConstant: 58.0), // Set the width of the image view
          cell.imageView!.heightAnchor.constraint(equalToConstant: 58.0), // Set the height of the image view
          VStackView.leadingAnchor.constraint(equalTo: cell.contentView.leadingAnchor, constant: 60)
        ])
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
    
    if _selectionColor == Int32(bitPattern: Color.default.rawValue){
      cell.selectionColor = argbToColor(Int32(bitPattern: kListViewDefaultSelectionColor.rawValue))
    } else {
      cell.selectionColor = argbToColor(_selectionColor)
    }
    
    return cell
  }
  
  // Return the number of rows in tableView (indexPath.row value)
  open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
     
     if _searching{
       if _elementsFromStringResults != nil{
         return _elementsFromStringResults!.count
       }
       else if _mainTextResults != nil{
         return _mainTextResults!.count
       }
       else{
         return 0
       }
     }
     else{
       if !_elementsFromString.isEmpty{
         return _elementsFromString.count
       }
       else if !_listData.isEmpty{
         return _listData.count
       }
       else{
         return 0
       }
     }
   }

  // MARK: UITableViewDelegate

  open func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    if indexPath.row < _elementsFromString.count {
      _selectionIndex = Int32(indexPath.row) + 1
      _selection = _elementsFromString[indexPath.row]
      _selectionDetailText = ""
    } else if indexPath.row < _elementsFromString.count + _listData.count {
      let listDataIndex = indexPath.row - _elementsFromString.count
      _selectionIndex = Int32(indexPath.row) + 1
      _selection = _listData[listDataIndex]["Text1"] ?? ""
      _selectionDetailText = _listData[listDataIndex]["Text2"] ?? ""
    }
    AfterPicking()
  }

  // MARK: UISearchBarDelegate
  
  // Search Logic:
  // If there are elements from string, filter them and store results into _elementsFromStringResults Optional String array
  // Else if there are some list data values (MainText, DetailText, ImageName), filter them and store into Optional
  // String arrays with corresponding names _mainTextResults, _detailTextResults, _imageNameResults
  // If at least one of the three list data values that correspond to each other is a search result (for example, MainText),
  // add their corresponding values to results arrays as well (for example, DetailText and ImageName) if they are
  // visible in the current ListView layout mode
  open func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    
    if !searchText.isEmpty  {
      _searching = true
      _elementsFromStringResults = [String]()
      _mainTextResults = [String]()
      _detailTextResults = [String]()
      _imageNameResults = [String]()
      
      if !_elementsFromString.isEmpty{
        _mainTextResults = nil
        _elementsFromStringResults = _elementsFromString.filter { $0.localizedCaseInsensitiveContains(searchText) }
      }
      else if !_listData.isEmpty {
        
        _elementsFromStringResults = nil
        var mainTextArray: [String] = []
        var detailTextArray: [String] = []
        var imageNameArray: [String] = []
        
        for dict in _listData {
            let text1 = dict["Text1"] ?? ""
            let text2 = dict["Text2"] ?? ""
            let image = dict["Image"] ?? ""
            
            mainTextArray.append(text1)
            detailTextArray.append(text2)
            imageNameArray.append(image)
        }
        
        for (index, mainText) in mainTextArray.enumerated() {
          switch _listViewLayoutMode {
          case 0:
            if mainText.lowercased().contains(searchText.lowercased()){
              _mainTextResults?.append(mainText)
            }
          case 1:
            if mainText.lowercased().contains(searchText.lowercased()) || detailTextArray[index].lowercased().contains(searchText.lowercased()){
              _mainTextResults?.append(mainText)
              _detailTextResults?.append(detailTextArray[index])
            }
          case 2:
            if mainText.lowercased().contains(searchText.lowercased()) || detailTextArray[index].lowercased().contains(searchText.lowercased()){
              _mainTextResults?.append(mainText)
              _detailTextResults?.append(detailTextArray[index])
            }
          case 3:
            if mainText.lowercased().contains(searchText.lowercased()) || imageNameArray[index].lowercased().contains(searchText.lowercased()){
              _mainTextResults?.append(mainText)
              _imageNameResults?.append(imageNameArray[index])
            }
          case 4:
            if mainText.lowercased().contains(searchText.lowercased()) || detailTextArray[index].lowercased().contains(searchText.lowercased()) || imageNameArray[index].lowercased().contains(searchText.lowercased()){
              _mainTextResults?.append(mainText)
              _detailTextResults?.append(detailTextArray[index])
              _imageNameResults?.append(imageNameArray[index])
            }
          default:
            print("Error: ListView Layout Mode Not Identified")
          }
        }
      }else{
        _elementsFromStringResults = nil
        _mainTextResults = nil
        _detailTextResults = nil
        _imageNameResults = nil
      }
    }else{
      _searching = false
      _elementsFromStringResults = nil
      _mainTextResults = nil
      _detailTextResults = nil
      _imageNameResults = nil
    }
    _view.reloadData()
  }
  
  open func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
    searchBar.endEditing(true)
  }
  
  open func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
    searchBar.text = ""
    searchBar.resignFirstResponder()
    _view.resignFirstResponder()
    _searching = false
    _elementsFromStringResults = nil
    _mainTextResults = nil
    _detailTextResults = nil
    _imageNameResults = nil
    _view.reloadData()
  }
}

  // extension of the UITableViewCell class defines a computed property selectionColor
  // for setting and getting the background color of a cell when it is selected
extension UITableViewCell {
    var selectionColor: UIColor {
        set {
            let view = UIView()
            view.backgroundColor = newValue
            self.selectedBackgroundView = view
        }
        get {
            return self.selectedBackgroundView?.backgroundColor ?? UIColor.clear
        }
    }
}
