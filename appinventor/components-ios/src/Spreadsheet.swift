// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2022-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import GTMSessionFetcher
import GoogleAPIClientForREST

@objc class Spreadsheet : NonvisibleComponent {
  
  private var _service = GTLRSheetsService()
  fileprivate var _columns: NSArray = []
  private let _workQueue = DispatchQueue(label: "Spreadsheet", qos: .userInitiated)
  private var _initialized = false
  private var _spreadsheetId = ""
  // This gets changed to the name of the project by MockSpreadsheet by default
  private var _sheetIdDict: [String: Int] = [:]
  private let _observers = NSMutableSet()

  // MARK: Properties
  
  @objc var ApplicationName: String = ""
  @objc var CredentialsJson: String = "" {
    didSet {
      authorize()
    }
  }
  @objc var SpreadsheetID: String {
    get {
      return _spreadsheetId
    }
    set {
      if newValue.hasPrefix("https:") {
        let parts = newValue[newValue.index(newValue.startIndex, offsetBy: 8)...].split(separator: "/")
        _spreadsheetId = String(parts[3])
      } else {
        _spreadsheetId = newValue
      }
    }
  }
  
  // MARK: Methods
  
  @objc func Initialize() {
    _initialized = true
    authorize()
  }
  
  /* Helper Functions for the User */
  
  /**
   * Converts the integer representation of rows and columns to A1-Notation used
   * in Google Sheets for a single cell. For example, row 1 and col 2
   * corresponds to the string \"B1\".
   */
  // Description: Converts the integer representation of row and columns to A1-Notation used in Google Sheets for a single cell
  @objc func GetCellReference(_ row: Int, _ column: Int) -> String {
    let colRange: String = getColString(column)
    return "\(colRange)\(row)"
  }
  
  /**
   * Converts the integer representation of rows and columns for the corners of
   * the range to A1-Notation used in Google Sheets. For example, selecting the
   * range from row 1, col 2 to row 3, col 4 corresponds to the string "B1:D3".
   */
  // Description: Converts the integer representation of row and columns for the corners of the range to A1-Notation used in Google Sheets.
  @objc func GetRangeReference(_ row1: Int, _ column1: Int, _ row2: Int, _ column2: Int) -> String {
    return GetCellReference(row1, column1) + ":" + GetCellReference(row2, column2)
  }
  
  /* Filters and Methods that Use Filters */
  
   // Description: Filters a Google Sheet for rows where the given column number matches the provided value.
   @objc func ReadWithExactFilter(_ sheetName: String, _ colID: Int, _ value: String) {
     print("ReadRowsWithFilter colID \(colID), value \(value)")
     if SpreadsheetID.isEmpty {
       ErrorOccurred("ReadWithExactFilter: Spreadsheet ID is empty.")
       return
     }
     _workQueue.async {
       self.retrieveSheet(sheetName, colID, value, true, true)
     }
   }
   
   // Description: Filters a Google Sheet for rows where the given column number contains the provided value String.
   @objc func ReadWithPartialFilter(_ sheetName: String, _ colID: Int, _ value: String) {
     print("ReadWithPartialFilter colID \(colID), value \(value)")
     if SpreadsheetID.isEmpty {
       ErrorOccurred("ReadWithExactFilter: Spreadsheet ID is empty.")
       return
     }
     _workQueue.async {
       self.retrieveSheet(sheetName, colID, value, false, true)
     }
   }
  
  /* Row Wise Operations */
  
  /*
  Description = On the page with the provided sheetName, this method will read the row at the given rowNumber and triggers the GotRowData callback event.
  */
  @objc func ReadRow(_ sheetName: String, _ rowNumber: Int) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("ReadRow: " + "SpreadsheetID is empty.")
      return
    }
    // If there is no credentials file,
    //   Make a simple HTTP Request
    // Otherwise (there is a credentials file),
    //   Use the Google Sheets API
    
    // properly format the range reference
    print("Read Row number: \(rowNumber)")
    let rangeReference: String = "\(sheetName)!\(rowNumber):\(rowNumber)"
    
    // Asynchronously fetch the data in the row
    _workQueue.async {
      // if noCredentials.json provided, attempt the HTTP request
      if self.CredentialsJson.isEmpty {
        print("Reading Row: No Credentials Located.")
        // cleans the formatted url in case the sheetname needs to be cleaned
        guard let cleanRangeReference: String = (rangeReference).addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
          self.ErrorOccurred("ReadRange: Error occured encoding the query. UTF-8 is unsupported?")
          return
        }
        // Formats the data into the URL to read the range
        let getURL =
        "https://docs.google.com/spreadsheets/d/\(self.SpreadsheetID)/export?format=csv&range=\(cleanRangeReference)"
        print("ReadRow url:", getURL)
        
        // Make the HTTP Request
        let url = URL(string: getURL)!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        // Catch Bad HTTP Request
        NSURLConnection.sendAsynchronousRequest(request, queue: OperationQueue.main) {(response, data, error) in
          if let httpResponse = response as? HTTPURLResponse {
            if httpResponse.statusCode == 400 {
              self.ErrorOccurred("ReadSheet: Bad HTTP Request. Please check the address and try again. " + getURL)
            }
          }
          // Parse the Response
          guard let responseContent = data else {
            self.ErrorOccurred("Error occured, no data retrieved")
            return
          }
          // convert responseContent to String
          guard let responseString = String(data: responseContent, encoding: .utf8) else {
            self.ErrorOccurred("Error occured, data could not be converted to string")
            return
          }

          do {
            let parsedCSV: YailList = try CsvUtil.fromCsvTable(responseString)
            for elem in parsedCSV {
              // if elem is not a YailList, then continue
              if elem is YailList<AnyObject> == false {
                continue
              }
              let row: YailList<AnyObject> = elem as! YailList<AnyObject>
              var rowArray: Array<String> = []
              for x in row {
                rowArray.append(x as! String)
              }
              self.GotRowData(rowArray)
            }
          } catch {
            self.ErrorOccurred("\(error)")
          }
        }
        return
      }
      // run this if there is a credentials json provided
      print("Reading sheet: Credentials Located.")
      // create and execute query to read values
      let query = GTLRSheetsQuery_SpreadsheetsValuesGet.query(withSpreadsheetId: self.SpreadsheetID, range: rangeReference)
      
      self._service.executeQuery(query) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
        if let error = error {
          print("Error", error.localizedDescription)
          self.ErrorOccurred("\(error)")
        } else {
          let readResult = result as? GTLRSheets_ValueRange
          let values: Array<Array<String>> = readResult?.values as? Array<Array<String>> ?? []
          // if we data we got is empty, then return so
          if values.isEmpty {
            self.ErrorOccurred("ReadRow: No data found")
          } else {
            // format the result as a list of Strings and run the callback
            var ret: Array<String> = []
            for obj in values[0] {
              ret.append(obj.isEmpty ? "" : "\(obj)")
              print("obj", "\(obj)")
            }
            self.GotRowData(ret)
          }
        }
      }
    }
  }
  
  // Function description = "Given a list of values as 'data', writes the value to the  " + "row of the sheet with the given row number"
  @objc func WriteRow(_ sheetName: String, _ rowNumber: Int32, _ data: YailList<AnyObject>) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("Write Row: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("Write Row: " + "Credentials JSON file is required.")
      return
    }
    
    // Generates the A1 Reference for the operation
    let rangeRef: String = "\(sheetName)!A\(rowNumber)"
    
    // Generates the 2D list, which are the values to assign to the range
    var values: Array<Array<AnyObject>> = []
    var row: Array<AnyObject> = []
    // copy YailList data to Array
    for x in data {
      row.append(x as AnyObject)
    }
    values.append(row)
    
    // Sets the 2D list above to be the values in the body of the API Call
    let body: GTLRSheets_ValueRange = GTLRSheets_ValueRange()
    body.values = values
    body.majorDimension = "ROWS"
    body.range = rangeRef
    
    // wrap the API call in an Async Utility
    _workQueue.async {
      // create and execute query to write values
      let query = GTLRSheetsQuery_SpreadsheetsValuesUpdate.query(withObject: body, spreadsheetId:  self.SpreadsheetID, range: rangeRef)
      query.valueInputOption = "USER_ENTERED"
      self._service.executeQuery(query) { ticket, result, error in
        if let error = error {
          self.ErrorOccurred("\(error)")
        } else{
          self.FinishedWriteRow()
        }
      }
    }
  }
  
  // Description: Given a list of values as 'data', appends the values to the next empty row of the sheet. Additionally, this returns the row number for the new row
  @objc func AddRow(_ sheetName: String, _ data: YailList<AnyObject>) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("AddRow: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("AddRow: " + "Credentials JSON file is required.")
      return
    }
    
    // Generates the 2D list, which are the values to assign to the range
    var values: Array<Array<AnyObject>> = []
    var row: Array<AnyObject> = []
    // copy YailList data to Array
    for x in data {
      row.append(x as AnyObject)
    }
    values.append(row)
    
    // Sets the 2D list above to be the values in the body of the API Call
    let body: GTLRSheets_ValueRange = GTLRSheets_ValueRange()
    body.values = values
    body.majorDimension = "ROWS"
    
    // need to get actual data from the response
    // create and execute query to read values
    let readQuery = GTLRSheetsQuery_SpreadsheetsValuesGet.query(withSpreadsheetId: SpreadsheetID, range: sheetName)
    
    _service.executeQuery(readQuery) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
      if let error = error {
        print("Error", error.localizedDescription)
        self.ErrorOccurred("\(error)")
      } else {
        let readResult = result as? GTLRSheets_ValueRange
        let values: Array<Array<String>> = (readResult?.values as? Array<Array<String>> ?? [])
        let maxRow = values.isEmpty ? 1 :values.count + 1
        
        // create and execute query to add row
        let query = GTLRSheetsQuery_SpreadsheetsValuesAppend.query(withObject: body, spreadsheetId:  self.SpreadsheetID, range: "\(sheetName)!A\(maxRow)")
        query.valueInputOption = "USER_ENTERED" // USER_ENTERED or RAW
        query.insertDataOption = "INSERT_ROWS" // INSERT_ROWS or OVERRIDE
        // wrap the API call in an Async Utility
        self._workQueue.async {
          // send the append values request
          self._service.executeQuery(query) { ticket, response, error in
            guard let response = response as? GTLRSheets_AppendValuesResponse else {
              return
            }
            // get UpdatedRange returns the range that updates were applied in A1
            var updatedRange: String = response.updates?.updatedRange ?? ""
            // updatedRange is in the form SHEET_NAME!A#:END# => We want #
            var cell: String = updatedRange.components(separatedBy: "!")[1].components(separatedBy: ":")[0]
            let pattern = "(\\d+)"
            // Remove non-numeric characters from the String
            if let regex = try? NSRegularExpression(pattern: pattern) {
              let matches = regex.matches(in: cell, range: NSRange(cell.startIndex..., in: cell))
              let match = matches[0]
              cell = cell[Range(uncheckedBounds: (match.range.lowerBound, match.range.upperBound))]
              print("Match strings: \(matches)")
            }
            let rowNumber: Int = Int(cell) ?? 0
            DispatchQueue.main.async {
              self.FinishedAddRow(rowNumber)
            }
          }
        }
      }
    }
  }
  
  // Description: Deletes the row with the given row number from the table. This does not clear the row, but removes it entirely
  @objc func RemoveRow(_ sheetName: String, _ rowNumber: Int) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("RemoveRow: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("RemoveRow: " + "Credentials JSON file is required.")
      return
    }
    // wrap the API call in an Async Utility
    _workQueue.async {
      let gridId: Int = self.getSheetID(self._service, sheetName)
      if gridId == -1 {
        self.ErrorOccurred("RemoveRow: sheetName not found")
        return
      }
      // create request to delte row
      let deleteRequest = GTLRSheets_DeleteDimensionRequest()
      let dimensionRange: GTLRSheets_DimensionRange = GTLRSheets_DimensionRange()
      dimensionRange.sheetId = (gridId) as NSNumber
      dimensionRange.dimension = "ROWS"
      dimensionRange.startIndex = (rowNumber - 1) as NSNumber
      dimensionRange.endIndex = (rowNumber) as NSNumber
      deleteRequest.range = dimensionRange
      
      var requests: Array<GTLRSheets_Request> = []
      let requestDeleteDimension = GTLRSheets_DeleteDimensionRequest()
      requestDeleteDimension.range = dimensionRange
      let request = GTLRSheets_Request()
      request.deleteDimension = requestDeleteDimension
      requests.append(request)
      
      // add request to batch udpate
      let body = GTLRSheets_BatchUpdateSpreadsheetRequest()
      body.requests = requests
      
      // query as a batch update
      let query = GTLRSheetsQuery_SpreadsheetsBatchUpdate.query(withObject: body, spreadsheetId: self.SpreadsheetID)

      self._service.executeQuery(query) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
        if let error = error {
          self.ErrorOccurred("\(error)")
        } else {
          DispatchQueue.main.async {
            self.FinishedRemoveRow()
          }
        }
      }
    }
  }
  
  /* Column-wise Operations*/
  
  /*
  Description = On the page with the provided sheetName, this method will read the column at the given index and triggers the GotColumnData callback event.
  */
  @objc func ReadColumn(_ sheetName: String, _ column: String) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("ReadColumn: " + "SpreadsheetID is empty.")
      return
    }
    // If there is no credentials file,
    //   Make a simple HTTP Request
    // Otherwise (there is a credentials file),
    //   Use the Google Sheets API
    
    // properly format the range reference
    print("Read column string: \(column)")
    
    var colReference: String = column
    // converts the col number to the corresponding letter
    if column.range(of: "^[0-9]+$", options: .regularExpression, range: nil, locale: nil) != nil {
      colReference = getColString(Int(column) ?? -1)
    }
    
    let rangeReference: String = "\(sheetName)!\(colReference):\(colReference)"
    
    // Asynchronously fetch the data in the row and trigger the callback
    _workQueue.async {
      // if noCredentials.json provided, attempt the HTTP request
      if self.CredentialsJson.isEmpty {
        print("Reading Column: No Credentials Located.")
        // cleans the formatted url in case the sheetname needs to be cleaned
        guard let cleanRangeReference: String = (rangeReference).addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
          self.ErrorOccurred("ReadRange: Error occured encoding the query. UTF-8 is unsupported?")
          return
        }
        // Formats the data into the URL to read the range
        let getURL =
        "https://docs.google.com/spreadsheets/d/\(self.SpreadsheetID)/export?format=csv&range=\(cleanRangeReference)"
        print("ReadColumn url:", getURL)
        
        // Make the HTTP Request
        let url = URL(string: getURL)!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        // Catch Bad HTTP Request
        NSURLConnection.sendAsynchronousRequest(request, queue: OperationQueue.main) {(response, data, error) in
          if let httpResponse = response as? HTTPURLResponse {
            if httpResponse.statusCode == 400 {
              self.ErrorOccurred("ReadColumn: Bad HTTP Request. Please check the address and try again. " + getURL)
            }
          }
          // Parse the Response
          guard let responseContent = data else {
            self.ErrorOccurred("Error occured, no data retrieved")
            return
          }
          // convert responseContent to string
          guard let responseString = String(data: responseContent, encoding: .utf8) else {
            self.ErrorOccurred("Error occured, data could not be converted to string")
            return
          }

          do {
            let parsedCSV: YailList = try CsvUtil.fromCsvTable(responseString)
            var col: Array<String> = []
            for elem in parsedCSV {
              // if elem is not a YailList, then skip
              if elem is YailList<AnyObject> == false {
                continue
              }
              let row: YailList<AnyObject> = elem as! YailList<AnyObject>
              // make sure row[1] is a string
              col.append(row.isEmpty ? "" : "\(row[1])")
              self.GotColumnData(col)
            }
          } catch {
            self.ErrorOccurred("\(error)")
          }
        }
        return
      }
      // run this if there is a credentials json provided
      print("Reading sheet: Credentials Located.")
      let query = GTLRSheetsQuery_SpreadsheetsValuesGet.query(withSpreadsheetId: self.SpreadsheetID, range: rangeReference)
      
      self._service.executeQuery(query) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
        if let error = error {
          print("Error", error.localizedDescription)
          self.ErrorOccurred("\(error)")
        } else {
          let readResult = result as? GTLRSheets_ValueRange
          let values: Array<Array<String>> = readResult?.values as? Array<Array<String>> ?? []
          // if we data we got is empty, then return so
          if values.isEmpty {
            self.ErrorOccurred("ReadColumn: No data found")
          } else {
            // format the result as a list of Strings and run the callback
            var ret: Array<String> = []
            for row in values {
              ret.append(row.isEmpty ? "" : "\(row[0])")
            }
            self.GotColumnData(ret)
          }
        }
      }
    }
  }
  
  // Description = Given a list of values as 'data', writes the values to the column of the sheet and calls the FinishedWriteColumn event
  @objc func WriteColumn(_ sheetName: String, _ column: String, _ data: YailList<AnyObject>) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("Write Column: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("Write Column: " + "Credentials JSON file is required.")
      return
    }
    
    // converts the col number to the corresponding letter
    var colReference: String = column
    // check if column matches the regex
    if column.range(of: "^[0-9]+$", options: .regularExpression, range: nil, locale: nil) != nil {
      colReference = getColString(Int(column)!)
    }
    let rangeRef: String = "\(sheetName)!\(colReference):\(colReference)"
    
    // Generates the body, which are the values to assign to the range
    var values: Array<Array<AnyObject>> = []
    // copy YailList data to Array
    for x in data {
      let r: Array<AnyObject> = [x as AnyObject]
      values.append(r)
    }
    
    // Sets the 2D list above to be the values in the body of the API Call
    let body: GTLRSheets_ValueRange = GTLRSheets_ValueRange()
    body.values = values
    // major dimension controls how input is read, not how data is outputted onto sheet
    body.majorDimension = "ROWS"
    body.range = rangeRef
    
    // wrap the API call in an Async Utility
    _workQueue.async {
      // surround operation with do catch in case it fails
      // create and execute query to write column
      let query = GTLRSheetsQuery_SpreadsheetsValuesUpdate.query(withObject: body, spreadsheetId:  self.SpreadsheetID, range: rangeRef)
      query.valueInputOption = "USER_ENTERED"
      self._service.executeQuery(query) { ticket, result, error in
        if let error = error {
          self.ErrorOccurred("\(error)")
        } else{
          self.FinishedWriteColumn()
        }
      }
    }
  }

  // Description: Given a list of values as 'data', appends the values to the next empty column of the sheet.
  @objc func AddColumn(_ sheetName: String, _ data: YailList<AnyObject>) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("AddColumn: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("AddColumn: " + "Credentials JSON file is required.")
      return
    }

    // Generates the body, which are the values to assign to the range
    var values: Array<Array<AnyObject>> = []
    // copy YailList data to Array
    for x in data {
      let r: Array<AnyObject> = [x as AnyObject]
      values.append(r)
    }

    // Sets the 2D list above to be the values in the body of the API Call
    let body: GTLRSheets_ValueRange = GTLRSheets_ValueRange()
    body.values = values
    body.majorDimension = "ROWS"
    
    // need to get actual data from the response
    // create and execute query to read values
    let readQuery = GTLRSheetsQuery_SpreadsheetsValuesGet.query(withSpreadsheetId: SpreadsheetID, range: sheetName)
    
    _service.executeQuery(readQuery) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
      if let error = error {
        print("Error", error.localizedDescription)
        self.ErrorOccurred("\(error)")
      } else {
        let readResult = result as? GTLRSheets_ValueRange
        let values: Array<Array<String>> = readResult?.values as? Array<Array<String>> ?? [[]]
        print("Reading sheet: values count ", values.count)
        
        if values.isEmpty {
          self.ErrorOccurred("AddColumn: No data found")
          return
        }
        // nextCol gets mutated, keep addedColumn as a constant
        var maxCol: Int = 0
        for list in values {
          print("list count", list.count)
          maxCol = max(maxCol, list.count)
        }
        let nextCol: Int = maxCol + 1
        let addedColumn: Int = nextCol
        
        var _: Array<String> = ["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R",
                                       "S","T","U","V","W","X","Y","Z"]
        let colReference: String = self.getColString(nextCol)
        let rangeRef: String = "\(sheetName)!\(colReference)1"
        
        // create and execute query to add column
        let query = GTLRSheetsQuery_SpreadsheetsValuesUpdate.query(withObject: body, spreadsheetId: self.SpreadsheetID, range: rangeRef)
         query.valueInputOption = "USER_ENTERED" // USER_ENTERED or RAW
        
        // wrap the API call in an Async Utility
        self._workQueue.async {
          // send the append values request
          self._service.executeQuery(query) { ticket, response, error in
            if let error = error {
              self.ErrorOccurred("\(error)")
            } else {
              DispatchQueue.main.async {
                self.FinishedAddColumn(addedColumn)
              }
            }
          }
        }
      }
    }
  }
  
  // Description: Deletes the column with the given column number from the table. This does not clear the column, but removes it entirely
  @objc func RemoveColumn(_ sheetName: String, _ column: String) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("RemoveColumn: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("RemoveColumn: " + "Credentials JSON file is required.")
      return
    }
    var columnNumber: Int = 0
    
    // check if column matches the regex
    if column.range(of: "^[0-9]+$", options: .regularExpression, range: nil, locale: nil) != nil {
      columnNumber = Int(column) ?? -1
    } else {
      columnNumber = getColNum(column)
    }
    
    // wrap the API call in an Async Utility
    _workQueue.async {
      let gridId: Int = self.getSheetID(self._service, sheetName)
      if gridId == -1 {
        self.ErrorOccurred("RemoveColumn: sheetName not found")
        return
      }
      // create request to remove column
      let deleteRequest = GTLRSheets_DeleteDimensionRequest()
      let dimensionRange: GTLRSheets_DimensionRange = GTLRSheets_DimensionRange()
      dimensionRange.sheetId = (gridId) as NSNumber
      dimensionRange.dimension = "COLUMNS"
      dimensionRange.startIndex = (columnNumber - 1) as NSNumber
      dimensionRange.endIndex = (columnNumber) as NSNumber
      deleteRequest.range = dimensionRange
      
      var requests: Array<GTLRSheets_Request> = []
      let requestDeleteDimension = GTLRSheets_DeleteDimensionRequest()
      requestDeleteDimension.range = dimensionRange
      let request = GTLRSheets_Request()
      request.deleteDimension = requestDeleteDimension
      requests.append(request)
      
      // add request to batch update
      let body = GTLRSheets_BatchUpdateSpreadsheetRequest()
      body.requests = requests
      
      // query as a batch update
      let query = GTLRSheetsQuery_SpreadsheetsBatchUpdate.query(withObject: body, spreadsheetId: self.SpreadsheetID)
 
      self._service.executeQuery(query) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
        if let error = error {
          self.ErrorOccurred("\(error)")
        } else {
          DispatchQueue.main.async {
            self.FinishedRemoveColumn()
          }
        }
      }
    }
  }
  
  /* Cell-wise Operations */
  
  /*
  On the page with the provided sheetName, reads the cell at the given
  cellReference and triggers the {@link #GotCellData} callback event. The
  cellReference can be either a text block with A1-Notation, or the result of
  the {@link #GetCellReference} block.
  */
  // Function description = "Given a list of values as 'data', writes the value to the  " + "row of the sheet with the given row number"
  @objc func ReadCell(_ sheetName: String, _ cellReference: String) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("Read cell: " + "SpreadsheetID is empty.")
      return
    }
    // If there is no credentials file,
    //   Make a simple HTTP Request
    // Otherwise (there is a credentials file),
    //   Use the Google Sheets API
    
    // 1. Check that the Cell Reference is actually a single cell
    if cellReference.range(of: "[a-zA-Z]+[0-9]+", options: .regularExpression, range: nil, locale: nil) == nil {
      ErrorOccurred("ReadCell: Invalid Cell Reference")
      return
    }
    
    // 2. Asynchronously fetch the data in the cell
    _workQueue.async {
      print("Reading Cell: ", cellReference)
      // if noCredentials.json provided, attempt the HTTP request
      if self.CredentialsJson.isEmpty {
        print("Reading Cell: No Credentials Located.")
        guard let cleanRangeReference: String = (cellReference).addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
          self.ErrorOccurred("ReadRange: Error occured encoding the query. UTF-8 is unsupported?")
          return
        }
        // Formats the data into the URL to read the range
        let getURL =
        "https://docs.google.com/spreadsheets/d/\(self.SpreadsheetID)/export?format=csv&range=\(cleanRangeReference)"
        print("ReadCell url:", getURL)
        
        // Make the HTTP Request
        let url = URL(string: getURL)!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        // Catch Bad HTTP Request
        NSURLConnection.sendAsynchronousRequest(request, queue: OperationQueue.main) {(response, data, error) in
          if let httpResponse = response as? HTTPURLResponse {
            if httpResponse.statusCode == 400 {
              self.ErrorOccurred("ReadSheet: Bad HTTP Request. Please check the address and try again. " + getURL)
            }
          }
          // Parse the Response
          guard let responseContent = data else {
            self.ErrorOccurred("Error occured, no data retrieved")
            return
          }
          // convert responseContent to string
          guard let responseString = String(data: responseContent, encoding: .utf8) else {
            self.ErrorOccurred("Error occured, data could not be converted to string")
            return
          }

          do {
            let parsedCSV: YailList = try CsvUtil.fromCsvTable(responseString)
            for elem in parsedCSV {
              // if elem is not a YailList, then skip
              if elem is YailList<AnyObject> == false {
                continue
              }
              let row: YailList<AnyObject> = YailList<AnyObject>(object: elem)
              let row1: YailList<AnyObject> = row[1] as! YailList<AnyObject>
              let cellData: String = "\(row.isEmpty ? "" : row1[1])"
              self.GotCellData(cellData)
            }
          } catch {
            self.ErrorOccurred("\(error)")
          }
        }
        return
      }
      // run this if there is a credentials json provided
      print("Reading sheet: Credentials Located.")
      let range = "\(sheetName)!\(cellReference)"
      // create and execute query to read values
      let query = GTLRSheetsQuery_SpreadsheetsValuesGet.query(withSpreadsheetId: self.SpreadsheetID, range: range)
      
      self._service.executeQuery(query) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
        if let error = error {
          print("Error", error.localizedDescription)
          self.ErrorOccurred("\(error)")
        } else {
          let readResult = result as? GTLRSheets_ValueRange
          let values: Array<Array<String>> = readResult?.values as? Array<Array<String>> ?? []
          // if we data we got is iempty, then return so
          if values.isEmpty {
            self.GotCellData("")
            return
          }
          // format the result as a string and run the call back
          var result: String = "\(values[0].isEmpty ? "" : values[0][0])"
          self.GotCellData(result)
        }
      }
    }
  }
  
  // Function description = "Given a list of values as 'data', writes the value to the  " + "cell. Once complte, it triggers the FinishedWriteCell callback event"
  @objc func WriteCell(_ sheetName: String, _ cellReference: String, _ data: AnyObject) {
    print("got into writecell")
    if SpreadsheetID.isEmpty {
      ErrorOccurred("Write Cell: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("Write Cell: " + "Credentials JSON file is required.")
      return
    }
    
    // Generates the A1 Reference for the operation
    let rangeRef: String = "\(sheetName)!\(cellReference)"
    print("Writing Cell:", rangeRef)
    // Form the body as 2D list of Strings, with only one String
    var temp: Array<Array<AnyObject>> = [[]]
    temp[0].append(data)
    // Sets the 2D list above to be the values in the body of the API Call
    let body: GTLRSheets_ValueRange = GTLRSheets_ValueRange()
    body.values = temp
    body.majorDimension = "ROWS"
    body.range = rangeRef
    
    // wrap the API call in an Async Utility
    _workQueue.async {
      // create and execute query to write values
      let query = GTLRSheetsQuery_SpreadsheetsValuesAppend.query(withObject: body, spreadsheetId:  self.SpreadsheetID, range: rangeRef)
      query.valueInputOption = "USER_ENTERED"
      self._service.executeQuery(query) { ticket, result, error in
        if let error = error {
          self.ErrorOccurred("\(error)")
        } else{
          self.FinishedWriteCell()
        }
      }
    }
  }

  /* Range-wise Operations */

    /*
  Description = On the page with the provided sheetName, this method will read the cell at the given range and triggers the GotRangeData callback event.
  */
  @objc func ReadRange(_ sheetName: String, _ rangeReference: String) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("ReadRange: " + "SpreadsheetID is empty.")
      return
    }
    // If there is no credentials file,
    //   Make a simple HTTP Request
    // Otherwise (there is a credentials file),
    //   Use the Google Sheets API
    
    // Asynchronously fetch the data in the row
    _workQueue.async {
      print("Reading Range", rangeReference)
      // if noCredentials.json provided, attempt the HTTP request
      if self.CredentialsJson.isEmpty {
        print("Reading Range: No Credentials Located.")
        // cleans the formatted url in case the sheetname needs to be cleaned
        guard let cleanRangeReference: String = (rangeReference).addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
          self.ErrorOccurred("ReadRange: Error occured encoding the query. UTF-8 is unsupported?")
          return
        }
        // Formats the data into the URL to read the range
        let getURL =
        "https://docs.google.com/spreadsheets/d/\(self.SpreadsheetID)/export?format=csv&range=\(cleanRangeReference)"
        print("ReadRow url:", getURL)
        
        // Make the HTTP Request
        let url = URL(string: getURL)!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        // Catch Bad HTTP Request
        NSURLConnection.sendAsynchronousRequest(request, queue: OperationQueue.main) {(response, data, error) in
          if let httpResponse = response as? HTTPURLResponse {
            if httpResponse.statusCode == 400 {
              self.ErrorOccurred("ReadRange: Bad HTTP Request. Please check the address and try again. " + getURL)
            }
          }
          // Parse the Response
          guard let responseContent = data else {
            self.ErrorOccurred("Error occured, no data retrieved")
            return
          }
          // convert responseContent to string
          guard let responseString = String(data: responseContent, encoding: .utf8) else {
            self.ErrorOccurred("Error occured, data could not be converted to string")
            return
          }

          do {
            let parsedCSV: YailList = try CsvUtil.fromCsvTable(responseString)
            var holder: Array<Array<String>> = []
            for row in parsedCSV {
              var holder2: Array<String> = []
              for cellValue in row as! YailList<AnyObject>{
                holder2.append("\(cellValue)")
              }
              holder.append(holder2)
            }
            self.GotRangeData(holder)
          } catch {
            self.ErrorOccurred("\(error)")
          }
        }
        return
      }
      // run this if there is a credentials json provided
      print("Reading sheet: Credentials Located.")
      // create and execute query to read values
      let query = GTLRSheetsQuery_SpreadsheetsValuesGet.query(withSpreadsheetId: self.SpreadsheetID, range: "\(sheetName)!\(rangeReference)")
      
      self._service.executeQuery(query) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
        if let error = error {
          print("Error", error.localizedDescription)
          self.ErrorOccurred("\(error)")
        } else {
          let readResult = result as? GTLRSheets_ValueRange
          let values: Array<Array<String>> = readResult?.values as? Array<Array<String>> ?? []
          // if we data we got is empty, then return so
          if values.isEmpty {
            self.ErrorOccurred("ReadRange: No data found")
          } else {
            // format the result as a list of Strings and run the callback
            var ret: Array<Array<String>> = []
            for row in values {
              var cellRow: Array<String> = []
              for cellValue in row {
                cellRow.append(cellValue == nil ? "" : "\(cellValue)")
              }
              ret.append(cellRow)
            }
            self.GotRangeData(ret)
          }
        }
      }
    }
  }
  
  // Function description = "Given a list of values as 'data', writes the value to the  " + "range. The number of rows and columns in the range reference must match the dimensions of the data."
  @objc func WriteRange(_ sheetName: String, _ rangeReference: String, _ data: YailList<AnyObject>) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("Write Range: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("Write Range: " + "Credentials JSON file is required.")
      return
    }
    
    // Generates the A1 Reference for the operation
    let rangeRef: String = "\(sheetName)!\(rangeReference)"
    print("Writing Range", rangeRef)
    
    // Generates the 2D list, which are the values to assign to the range
    var values: Array<Array<AnyObject>> = []
    var cols: Int = -1
    
    for elem in data {
      // if elem is not a YailList, then skip
      guard let row = elem as? YailList<AnyObject> else {
        continue
      }
      // construct the row that we will add to the list of rows
      var r: Array<AnyObject> = []
      for o in row {
        if o is SCMSymbol {  // skip *list*
          continue
        }
        r.append(o as AnyObject)
      }
      values.append(r)
      // catch rows of unequal length
      if cols == -1 {
        cols = r.count
      }
      if r.count != cols {
        ErrorOccurred("WriteRange: Rows must have the same length")
        return
      }
    }
    // Check that values has at least 1 row
    if values.count == 0 {
      ErrorOccurred("WriteRange: Data must be a list of lists")
      return
    }
    
    // Sets the 2D list above to be the values in the body of the API Call
    let body: GTLRSheets_ValueRange = GTLRSheets_ValueRange()
    body.values = values
    body.majorDimension = "ROWS"
    body.range = rangeRef
    // wrap the API call in an Async Utility
    _workQueue.async {
      // create and execute query to write values
      let query = GTLRSheetsQuery_SpreadsheetsValuesUpdate.query(withObject: body, spreadsheetId: self.SpreadsheetID, range: rangeRef)
      query.valueInputOption = "USER_ENTERED"
      self._service.executeQuery(query) { ticket, result, error in
        if let error = error {
          self.ErrorOccurred("\(error)")
        } else{
          self.FinishedWriteRange()
        }
      }
    }
  }
  
  // Description: Empties the cells in the given range. Once complete, this block triggers the FinishedClearRange callback event
  @objc func ClearRange(_ sheetName: String, _ rangeReference: String) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("ClearRange: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("ClearRange: " + "Credentials JSON file is required.")
      return
    }
    
    let rangeRef: String = "\(sheetName)!\(rangeReference)"
    print("Clearing Range: \(rangeRef)")
    
    let body: GTLRSheets_ClearValuesRequest = GTLRSheets_ClearValuesRequest()
    // create and execute query to clear values
    let query = GTLRSheetsQuery_SpreadsheetsValuesClear.query(withObject: body, spreadsheetId: self.SpreadsheetID, range: rangeRef)
    self._workQueue.async {
      // send the append values request
      self._service.executeQuery(query) { ticket, response, error in
        if let error = error {
          self.ErrorOccurred("\(error)")
        } else {
          DispatchQueue.main.async {
            self.FinishedClearRange()
          }
        }
      }
    }
  }

  /* Sheet-wise Operations */

  // Description: Adds a new sheet inside the Spreadsheet
  @objc func AddSheet(_ sheetName: String) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("AddSheet: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("AddSheet: " + "Credentials JSON file is required.")
      return
    }
    // wrap the API call in an Async Utility
    _workQueue.async {
      // create add sheet request
      let addSheetRequest = GTLRSheets_AddSheetRequest()
      let addSheetProperties = GTLRSheets_SheetProperties()
      addSheetProperties.title = sheetName
      addSheetRequest.properties = addSheetProperties
      var requests: Array<GTLRSheets_Request> = []
      let request = GTLRSheets_Request()
      request.addSheet = addSheetRequest
      requests.append(request)
      
      // add request to batch update
      let body = GTLRSheets_BatchUpdateSpreadsheetRequest()
      body.requests = requests
      
      // query as a batch update
      let query = GTLRSheetsQuery_SpreadsheetsBatchUpdate.query(withObject: body, spreadsheetId: self.SpreadsheetID)
      
      // execute query and run the callback event block
      self._service.executeQuery(query) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
        if let error = error {
          self.ErrorOccurred("\(error)")
        } else {
          DispatchQueue.main.async {
            self.FinishedAddSheet(sheetName)
          }
        }
      }
    }
  }
  
  // Description: Deletes the specified sheet inside the Spreadsheet
  @objc func DeleteSheet(_ sheetName: String) {
    if SpreadsheetID.isEmpty {
      ErrorOccurred("DeleteSheet: " + "SpreadsheetID is empty.")
      return
    } else if CredentialsJson.isEmpty {
      ErrorOccurred("DeleteSheet: " + "Credentials JSON file is required.")
      return
    }
    
    // wrap the API call in an Async Utility
    _workQueue.async {
      // create delete sheet request
      let deleteSheetRequest = GTLRSheets_DeleteSheetRequest()
      deleteSheetRequest.sheetId = (self.getSheetID(self._service, sheetName)) as NSNumber
      var requests: Array<GTLRSheets_Request> = []
      let request = GTLRSheets_Request()
      request.deleteSheet = deleteSheetRequest
      requests.append(request)
      
      // add request to batch update
      let body = GTLRSheets_BatchUpdateSpreadsheetRequest()
      body.requests = requests
      
      // query as a batch update
      let query = GTLRSheetsQuery_SpreadsheetsBatchUpdate.query(withObject: body, spreadsheetId: self.SpreadsheetID)

      // execute query and run the callback event block
      self._service.executeQuery(query) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
        if let error = error {
          self.ErrorOccurred("\(error)")
        } else {
          DispatchQueue.main.async {
            self.FinishedDeleteSheet(sheetName)
          }
        }
      }
    }
  }
  
  // Description = Reads the *entire* Google Sheet document and triggers the GotSheetData callback event.
  @objc func ReadSheet(_ sheetName: String) {
    guard !SpreadsheetID.isEmpty else {
      ErrorOccurred("ReadSheet: SpreadsheetID is empty.")
      return
    }
    _workQueue.async {
      self.retrieveSheet(sheetName, -1, nil, false, true)
    }
  }
  
  // MARK: Events
  
  // Description: Triggered whenever an API call encounters an error. Details about the error are in `errorMessage`
  @objc func ErrorOccurred(_ errorMessage: String) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "ErrorOccurred", arguments: errorMessage as AnyObject)
    }
  }
  
  // Description: The callback event for the ReadSheet block. The sheetData is a list of rows.
  @objc func GotSheetData(_ sheet: YailList<AnyObject>) {
    EventDispatcher.dispatchEvent(of: self, called: "GotSheetData", arguments: sheet as AnyObject)
  }
  
  // Description: The callback event for the ReadCell block. The cellData is the text value in the cell (and not the underlying formula)
  @objc func GotCellData(_ cellData: String) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "GotCellData", arguments: cellData as AnyObject)
    }
  }
  
  // Description: The callback event for the ReadRow block. The rowDataList is a list of cell values in order of increasing column number
  @objc func GotRowData(_ rowDataList: Array<String>) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "GotRowData", arguments: rowDataList as AnyObject)
    }
  }
  
  // Description: The callback event for the ReadColumn block. The data in the column will be stored as a list of text values in 'columnData'
  @objc func GotColumnData(_ columnData: Array<String>) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "GotColumnData", arguments: columnData as AnyObject)
    }
  }
  
  // Description: The callback event for the ReadRange block. The rangeData is a list of rows with the requested dimensions.
  @objc func GotRangeData(_ rangeData: Array<Array<String>>) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "GotRangeData", arguments: rangeData as AnyObject)
    }
  }
  
  // Description = The callback event for the WriteRow block, called after the values on the table have finished updating
  @objc func FinishedWriteRow() {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedWriteRow")
    }
  }
  
  // Description = The callback event for the WriteColumn block, called after the values on the table have finished updating
  @objc func FinishedWriteColumn() {
        DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedWriteColumn")
    }
  }
  
  // Description = The callback event for the WriteCell block, called after the values on the table have finished updating
  @objc func FinishedWriteCell() {
        DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedWriteCell")
    }
  }
  
  // Description = The callback event for the AddRow block, called once the values on the table have been updated
  @objc func FinishedAddRow(_ rowNumber: Int) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedAddRow", arguments: rowNumber as AnyObject)
    }
  }
  
  // Description = The callback event for the RemoveRow block, called once the values on the table have been updated
  @objc func FinishedRemoveRow() {
    print("Made it inside finished remove row")
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedRemoveRow")
    }
  }
  
  // Description = The callback event for the RemoveColumn block, called once the values on the table have been updated
  @objc func FinishedRemoveColumn() {
    print("Made it inside finished remove column")
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedRemoveColumn")
    }
  }
  
  // Description = The callback event for the AddSheet block, called once the values on the table have been updated
  @objc func FinishedAddSheet(_ sheetName: String) {
    print("Made it inside finished add sheet")
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedAddSheet", arguments: sheetName as AnyObject)
    }
  }
  
  // Description = The callback event for the DeleteSheet block, called once the values on the table have been updated
  @objc func FinishedDeleteSheet(_ sheetName: String) {
    print("Made it inside finished delete sheet")
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedDeleteSheet", arguments: sheetName as AnyObject)
    }
  }
  
  // Description = This event will be triggered once the AddColumn method has finished executing and the values on the spreadsheet have been updated. Additionally, this returns the column number for the new column.
  @objc func FinishedAddColumn(_ columnNumber: Int) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedAddColumn", arguments: columnNumber as AnyObject)
    }
    
  }
  
  // Description: The callback event for the ReadWithExactQuery or ReadWithPartialQuery block. The `response` is a list of rows numbers and a list of rows containing cell data.
  @objc func GotFilterResult(returnRows: Array<Int>, returnData: Array<Array<String>>) {
    print("Made it inside got filter result")
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "GotFilterResult", arguments: returnRows as AnyObject, returnData as AnyObject)
    }
  }
  
  // Description = The callback event for the ClearRange Block
  @objc func FinishedClearRange() {
    print("Made it inside finished clear range")
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedClearRange")
    }
  }
  
  // Description = The callback event for the WriteRange block, called after the values on the table have finished updating
  @objc func FinishedWriteRange() {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FinishedWriteRange")
    }
  }
  
  // MARK: Private implementation
  
  func retrieveSheet(_ sheetName: String, _ colId: Int, _ value: String?, _ exact: Bool, _ fireEvent: Bool) {
    guard !SpreadsheetID.isEmpty else {
      ErrorOccurred("ReadSheet: SpreadsheetID is empty.")
      return
    }
    
    print("Reading Sheet: " + sheetName)
    //If no Credentials.json is provided, attempt the HTTP request
    if CredentialsJson.isEmpty {
      print("Reading sheet: No Credentials Located.")
      // cleans the formatted url in case the sheetname needs to be cleaned
      guard let cleanRangeReference: String = (sheetName + "!").addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
        ErrorOccurred("ReadRange: Error occured encoding the query. UTF-8 is unsupported?")
        return
      }
      // Formats the data into the URL to read the range
      var getURL = "https://docs.google.com/spreadsheets/d/\(SpreadsheetID)/export?format=csv&sheet=\(cleanRangeReference)"
      // Make the HTTP Request
      var url = URL(string: getURL)!
      var request = URLRequest(url: url)
      request.httpMethod = "GET"
      
      // Catch Bad HTTP Request
      NSURLConnection.sendAsynchronousRequest(request, queue: OperationQueue.main) {(response, data, error) in
        if let httpResponse = response as? HTTPURLResponse {
          if httpResponse.statusCode == 400 {
            self.ErrorOccurred("ReadSheet: Bad HTTP Request. Please check the address and try again. " + getURL)
          }
        }
        // Parse the Response
        guard let responseContent = data else {
          self.ErrorOccurred("Error occured, no data retrieved")
          return
        }
        guard let responseString = String(data: responseContent, encoding: .utf8) else {
          self.ErrorOccurred("Error occured, data could not be converted to string")
          return
        }
        do {
          let parsedCsv: YailList = try CsvUtil.fromCsvTable(responseString)
          DispatchQueue.main.async {
            self.updateColumns(parsedCsv as! YailList<YailList<AnyObject>>)
            self.notifyDataObservers(nil, nil)
            if fireEvent {
              if colId >= 0 {
                print("colID>=0")
                do {
                  var return_rows: Array<Int> = []
                  var return_data: Array<Array<String>> = []
                  var rowNum: Int = 0
                  while rowNum < parsedCsv.count {
                    var sheet_row: YailList = try CsvUtil.fromCsvRow(parsedCsv[rowNum] as! String)
                    if sheet_row.count >= colId {
                      //why is sheet row index of a int and not a string?
                      if (exact && sheet_row[colId - 1] as! String == value) || (!exact && (sheet_row[colId - 1] as AnyObject).contains(value!)) {
                        return_rows.append(rowNum)
                        return_data.append(sheet_row as! Array<String>)
                      }
                      rowNum += 1
                    }
                  }
                  self.GotFilterResult(returnRows: return_rows, returnData: return_data)
                } catch {
                  print("ReadWithFilter (no creds) error \(error)")
                  self.ErrorOccurred("\(error)")
                }
              } else {
                self.GotSheetData(YailList(array: parsedCsv))
              }
            }
          }
          
        } catch {
          self.ErrorOccurred("\(error)")
        }
      }
      return
    }
    
    //credentials
    //authenticated version
    print("Reading sheet: Credentials Located.")
    // make range entre spreadsheet
    let range = sheetName
    let query = GTLRSheetsQuery_SpreadsheetsValuesGet.query(withSpreadsheetId: SpreadsheetID, range: range)
    
    _service.executeQuery(query) { (ticket:GTLRServiceTicket, result:Any?, error:Error?) in
      
      if let error = error {
        print("Error", error.localizedDescription)
        self.ErrorOccurred("\(error)")
      } else {
        let readResult = result as? GTLRSheets_ValueRange
        let values: Array<Array<String>> = readResult?.values as! Array<Array<String>>

        let yailValues = YailList<YailList<AnyObject>>(array: values.map { YailList<AnyObject>(array: $0.map { $0 as NSString }) })
        self.updateColumns(yailValues)
        self.notifyDataObservers(nil, nil)

        // no Data found
        if values.isEmpty {
          self.ErrorOccurred("ReadSheet: No data found")
          return
        }
        
        //format the result as a string and run the call back
        var ret: Array<Array<String>> = []
        print("RetrieveSheet data: ", values)
        
        for row in values {
          var cellRow: Array<String> = []
          for cellValue in row {
            cellRow.append(cellValue)
          }
          ret.append(cellRow)
        }
        print("RetrieveSheet return rowcount: ", ret.count)
        
        // We need to re-enter the main thread before we can dispatch the event!
        DispatchQueue.main.async {
          print("RetrieveSheet UiThread")
          if colId >= 0 {
            print("RetrieveWithFilter: colId: \(colId)")
            do {
              var return_rows: Array<Int> = []
              var return_data: Array<Array<String>> = []
              var rowNum: Int = 0
              while rowNum < ret.count {
                print("Reading row: \(rowNum)")
                let sheet_row: Array<String> = ret[rowNum]
                print("Read with Filter row: \(sheet_row)" )
                if sheet_row.count >= colId {
                  print("Checking field: | \(sheet_row[colId-1]) |")
                  if (exact && sheet_row[colId - 1]  == value) || (!exact && (sheet_row[colId - 1] as AnyObject).contains(value!)) {
                    print("Read with Filter check col: \(rowNum)")
                    return_rows.append(rowNum + 1)
                    return_data.append(sheet_row)
                  }
                }
                rowNum += 1
              }
              self.GotFilterResult(returnRows: return_rows, returnData: return_data)
            } catch {
              print("Read with filter error: \(error)")
              self.ErrorOccurred("\(error)")
              return
            }
          } else {
            // read entire spreadsheet
            self.GotSheetData(YailList(array: ret))
          }
        }
      }
    }
  }
  
  // Yields the A1 notation for the column, e.g. col 1 = A, col 2 = B, etc
  func getColString(_ colNumber: Int) -> String{
    if colNumber == 0 {
      return ""
    }
    let alphabet: Array<String> = ["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R",
                                   "S","T","U","V","W","X","Y","Z"]
    let colReference: NSMutableString = ""
    var colRemainder = colNumber
    while colRemainder > 0 {
      let digit: String = alphabet[(colRemainder - 1) % 26]
      colReference.insert(digit, at: 0)
      colRemainder = (colRemainder - 1) / 26
    }
    return colReference as String
  }
  
  // Yields corresponding number to the column inputted
  func getColNum(_ columnRef: String) -> Int{
    if columnRef.isEmpty {
      return -1
    }
    var number: Int = 0
    for c in Array(columnRef) {
      let charDiff: Int = Int(c.asciiValue! - Character("A").asciiValue!)
      number = number * 26 + charDiff + 1
    }
    return number
  }
  
  // process credentials and authorize user
  private func authorize() {
    guard _initialized else {
      return
    }
    guard !CredentialsJson.isEmpty else {
      _service.authorizer = nil
      return
    }
    do {
      let credentialsFile = AssetManager.shared.pathForExistingFileAsset(CredentialsJson)
      let credentials = try Data(contentsOf: URL(fileURLWithPath: credentialsFile))
      _service.authorizer = ServiceAccountAuthorizer(serviceAccountConfig: credentials, scopes: [
        kGTLRAuthScopeSheetsSpreadsheets,
      ])
    } catch {
      self.ErrorOccurred("\(error)")
      return
    }
  }
  
  // manages sheet IDs in the Spreadsheet
  private func getSheetID(_ sheetsSvcParam: GTLRSheetsService, _ sheetName: String)  -> Int {
    if _sheetIdDict.keys.contains(sheetName) {
      // if key already in dict then return value
      return _sheetIdDict[sheetName] ?? 0
    } else {
      // key not in dict
      // create and execute query to read values
      let getSheetRequest = GTLRSheetsQuery_SpreadsheetsGet.query(withSpreadsheetId: self.SpreadsheetID)
      var ranges: Array<String> = []
      ranges.append(sheetName)
      getSheetRequest.ranges = ranges
      getSheetRequest.includeGridData = false
      // create and lock condition so query will be processed before continuing
      var condition: NSCondition = NSCondition()
      condition.lock()
      sheetsSvcParam.executeQuery(getSheetRequest) { (ticket:GTLRServiceTicket, response:Any?, error:Error?) in
        guard let response = response as? GTLRSheets_Spreadsheet else {
          print("Error: Did not get spreadsheet")
          return
        }
        if response.sheets?.count == 0 || response.sheets?.count == nil {
          return
        }
        let sheetID = Int(response.sheets?[0].properties?.sheetId ?? -1)
        self._sheetIdDict[sheetName] = sheetID
        // signal and unlock so code continues after query ends
        condition.lock()
        condition.signal()
        condition.unlock()
      }
      // waits for condition to signal/query to finish executing
      condition.wait()
      condition.unlock()
      if let sheetID = self._sheetIdDict[sheetName] {
        return sheetID
      } else {
        print("Sheet ID was not correctly stored in dictionary.")
        return -1
      }
    }
  }
}

extension Spreadsheet: ObservableDataSource {
  func addDataObserver(_ listener: any DataSourceChangeListener) {
    _observers.add(listener)
  }

  func removeDataObserver(_ listener: any DataSourceChangeListener) {
    _observers.remove(listener)
  }

  func getDataValue(_ key: AnyObject?) -> [Any] {
    return getDataValue(key ?? "" as NSString, false)
  }

  func getDataValue(_ key: AnyObject, _ useHeaders: Bool) -> [Any] {
    return getColumns(key, useHeaders) as! [Any]
  }

  func updateColumns(_ parsedCsv: YailList<YailList<AnyObject>>) {
    _columns = getTranspose(matrix: parsedCsv as [AnyObject])
  }

  func notifyDataObservers(_ key: AnyObject?, _ newValue: AnyObject?) {
    for listener in _observers {
      guard let listener = listener as? DataSourceChangeListener else {
        continue
      }
      listener.onDataSourceValueChange(self, nil, _columns)
    }
  }

  func getColumns(_ keyColumns: AnyObject, _ useHeaders: Bool) -> NSMutableArray {
    guard let keyColumns = (keyColumns as? [AnyObject])?.toStringArray() else {
      return NSMutableArray()
    }
    let resultingColumns = NSMutableArray()
    keyColumns.forEach { columnName in
      let column: NSArray
      if useHeaders {
        column = getColumn(named: columnName)
      } else {
        var colIndex = 0
        for c in columnName.bytes {
          colIndex *= 26
          colIndex += Int(c) - 0x40
        }
        colIndex -= 1
        column = getColumn(at: colIndex)
      }
      resultingColumns.add(column)
    }
    return resultingColumns
  }

  func getColumn(named name: String) -> NSArray {
    for column in _columns {
      guard let column = column as? NSArray else {
        continue
      }
      if !column.isEmpty && (column[0] as? String) == name {
        return column
      }
    }
    return []
  }

  func getColumn(at index: Int) -> NSArray {
    guard index >= 0 && index < _columns.count else {
      return []
    }
    return _columns[index] as? NSArray ?? NSArray()
  }
}

