// MARK: - App Inventor Data Utilities

struct ChartDataSourceUtil {
  static func getTranspose(_ matrix: YailList<AnyObject>) throws -> YailList<AnyObject> {
    var rows: [[AnyObject]] = []
    
    for rowObj in matrix {
      if let rowYail = rowObj as? YailList<AnyObject> {
        var rowArray: [AnyObject] = []
        for item in rowYail {
          rowArray.append(item as AnyObject)
        }
        rows.append(rowArray)
      }
    }
    
    guard !rows.isEmpty else { return YailList<AnyObject>() }
    
    var transposed: [[AnyObject]] = []
    let maxCols = rows.map { $0.count }.max() ?? 0
    
    for colIndex in 0..<maxCols {
      var newRow: [AnyObject] = []
      for rowArray in rows {
        if colIndex < rowArray.count {
          newRow.append(rowArray[colIndex])
        } else {
          newRow.append("" as AnyObject)
        }
      }
      transposed.append(newRow)
    }

    let result = transposed.map { YailList<AnyObject>(array: $0 as [AnyObject], in: SCMInterpreter.shared) }
    return YailList<AnyObject>(array: result as [AnyObject], in: SCMInterpreter.shared)
  }
}

struct JsonUtil {
  /// Parses a JSON string and extracts columns.
  static func getColumnsFromJson(_ jsonString: String) throws -> YailList<AnyObject> {
    guard let data = jsonString.data(using: .utf8) else {
      throw NSError(domain: "JsonUtilError", code: 1, userInfo: [NSLocalizedDescriptionKey: "Invalid UTF8 string"])
    }
    
    let jsonObject = try JSONSerialization.jsonObject(with: data, options: .allowFragments)
    var columns: [YailList<AnyObject>] = []
    
    // Handle case where JSON is an Array of Dictionaries
    if let jsonArray = jsonObject as? [[String: Any]] {
      guard let firstItem = jsonArray.first else { return YailList<AnyObject>() }
      let keys = Array(firstItem.keys).sorted()
      
      for key in keys {
        var columnData: [AnyObject] = [key as AnyObject]
        for row in jsonArray {
          let value = row[key] ?? ""
          columnData.append(value as AnyObject)
        }
        columns.append(YailList<AnyObject>(array: columnData))
      }
    }
    // Handle case where JSON is a Dictionary of Arrays
    else if let jsonDict = jsonObject as? [String: [Any]] {
      let keys = Array(jsonDict.keys).sorted()
      
      for key in keys {
        var columnData: [AnyObject] = [key as AnyObject] 
        let arrayValues = jsonDict[key] ?? []
        for value in arrayValues {
          columnData.append(value as AnyObject)
        }
        columns.append(YailList<AnyObject>(array: columnData))
      }
    } else {
      throw NSError(domain: "JsonUtilError", code: 2, userInfo: [NSLocalizedDescriptionKey: "Unsupported JSON format"])
    }
    
    return YailList<AnyObject>(array: columns)
  }
}
