// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc open class DataFile: NonvisibleComponent, DataSource {

  fileprivate var _rows = YailList<AnyObject>()
  fileprivate var _columns = YailList<AnyObject>()
  fileprivate var _columnNames = YailList<AnyObject>()
  fileprivate var _sourceFile: String = ""

  fileprivate var assetManager: AssetManager?

  @objc public override init(_ parent: ComponentContainer) {
    self.assetManager = parent.form?.application?.assetManager
    super.init(parent)
  }

  // MARK: - DataFile Properties
  @objc open var Rows: YailList<AnyObject> { return self._rows }
  @objc open var Columns: YailList<AnyObject> { return self._columns }
  @objc open var ColumnNames: YailList<AnyObject> { return self._columnNames }

  @objc open var SourceFile: String {
    get {
      return _sourceFile
    }
    set(newSource) {
      _sourceFile = newSource
      // Media file paths in App Inventor are distinguished by double slashes
      ReadFile("//\(newSource)")
    }
  }
  // MARK: - DataFile Methods

  @objc open func ReadFile(_ fileName: String) {
    self.readFromFile(fileName)
  }

  @objc open func getColumn(_ column: String) -> YailList<AnyObject> {
    var stringNames: [String] = []
    for item in self._columnNames {
      stringNames.append("\(item)".trimmingCharacters(in: .whitespacesAndNewlines))
    }

    let cleanTarget = column.trimmingCharacters(in: .whitespacesAndNewlines)
    guard let index = stringNames.firstIndex(of: cleanTarget) else {
      NSLog("DataFile: Could not find column named '\(cleanTarget)'. Available columns are: \(stringNames)")
      return YailList<AnyObject>()
    }

    var columnsArray: [YailList<AnyObject>] = []
    for colObj in self._columns {
      if let colYail = colObj as? YailList<AnyObject> {
        columnsArray.append(colYail)
      }
    }

    if index < columnsArray.count { return columnsArray[index] }
    return YailList<AnyObject>()
  }

  // MARK: - File Parsing

  internal func afterRead(_ result: String?) {
    guard let result = result else { return }

    let trimmedResult = result.trimmingCharacters(in: .whitespacesAndNewlines)

    do {
      if trimmedResult.hasPrefix("{") {
        self._columns = try JsonUtil.getColumnsFromJson(trimmedResult)
        self._rows = try ChartDataSourceUtil.getTranspose(self._columns)
      } else {
        let parsedCSV = try CsvUtil.fromCsvTable(trimmedResult)
        self._rows = parsedCSV as AnyObject as! YailList<AnyObject>
        self._columns = try ChartDataSourceUtil.getTranspose(self._rows)
      }

      var firstRow: YailList<AnyObject>? = nil
      for row in self._rows {
        firstRow = row as? YailList<AnyObject>
        break
      }
      self._columnNames = firstRow ?? YailList<AnyObject>()

    } catch {
      NSLog("DataFile: Unable to parse file.")
    }
  }

  private func readFromFile(_ fileName: String) {
    let cleanName = fileName.replacingOccurrences(of: "//", with: "")
      .replacingOccurrences(of: "/sdcard/", with: "")
      .trimmingCharacters(in: CharacterSet(charactersIn: "/"))

    let docsDir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    var targetURL: URL? = nil

    if let assetPath = self._form?.application?.assetManager.pathForAsset(cleanName, scope: .bundle) {
      if FileManager.default.fileExists(atPath: assetPath) {
        targetURL = URL(fileURLWithPath: assetPath)
      }
    }

    if targetURL == nil {
      let companionURL = docsDir.appendingPathComponent("AppInventor").appendingPathComponent(cleanName)
      if FileManager.default.fileExists(atPath: companionURL.path) {
        targetURL = companionURL
      }
    }

    if targetURL == nil {
      let privateURL = docsDir.appendingPathComponent(cleanName)
      if FileManager.default.fileExists(atPath: privateURL.path) {
        targetURL = privateURL
      }
    }

    guard let finalURL = targetURL else {
      NSLog("DataFile: Unable to locate file path for \(fileName).")
      self.afterRead(nil)
      return
    }

    do {
      NSLog("DataFile: SUCCESS! Found file at: \(finalURL.path)")
      let fileContents = try String(contentsOf: finalURL, encoding: .utf8)
      self.afterRead(fileContents)
    } catch {
      NSLog("DataFile: Error reading file at \(finalURL.path) - \(error.localizedDescription)")
      self.afterRead(nil)
    }
  }

  // MARK: - DataSource Protocol Implementation

  @objc open func getDataValue(_ key: AnyObject?) -> [Any] {
    guard let columnsList = key as? YailList<AnyObject> else {
      return []
    }

    var resultingColumns = [YailList<AnyObject>]()

    for col in columnsList {
      let columnName = "\(col)".trimmingCharacters(in: .whitespacesAndNewlines)

      let columnData = self.getColumn(columnName)
      resultingColumns.append(columnData)
    }

    return resultingColumns
  }
}
