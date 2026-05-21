// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

class CsvParseError: Error {

}

class CsvParser {
  let input: String
  var pos: String.Index
  var line = 0
  var col = 0

  init(input: String) {
    self.input = input
    pos = input.startIndex
  }

  public func hasNext() -> Bool {
    return pos < input.endIndex
  }

  private func unquotify(cell: String) -> String {
    return cell.replacingOccurrences(of: "\"\"", with: "\"")
  }

  public func next() throws -> [String] {
    var result = [String]()
    var start = pos
    var quoted = false
    var inString = false
    var cr = false
    var cellStart = true
    while pos < input.endIndex {
      if input[pos] == "\n" || input[pos] == "\r\n" {
        if !cr {
          if quoted {
            result.append(unquotify(cell: String(input[start..<input.index(before: pos)])))
          } else {
            result.append(unquotify(cell: String(input[start..<pos])))
          }
        }
        pos = input.index(after: pos)
        return result
      } else if cr {
        // CSV possibly has only CR line endings (classic Mac style) assume this is the end of line
        return result
      } else if input[pos] == "\r" {
        if quoted {
          result.append(unquotify(cell: String(input[start..<input.index(before: pos)])))
        } else {
          result.append(unquotify(cell: String(input[start..<pos])))
        }
        cr = true
        cellStart = true
        quoted = false
      } else if input[pos] == "," {
        if !inString {
          if quoted {
            result.append(unquotify(cell: String(input[start..<input.index(before: pos)])))
          } else {
            result.append(unquotify(cell: String(input[start..<pos])))
          }
          cellStart = true
          quoted = false
          start = input.index(after: pos)
        }
      } else if input[pos] == "\"" {
        if cellStart {
          // Start of quoted cell
          quoted = true
          start = input.index(after: pos)
          cellStart = false
          inString = true
        } else if quoted {
          inString = !inString
        }
      } else {
        cellStart = false
      }
      pos = input.index(after: pos)
    }
    if !cr && start != pos {
      if quoted {
        result.append(unquotify(cell: String(input[start..<input.index(before: pos)])))
      } else {
        result.append(unquotify(cell: String(input[start..<pos])))
      }
    }
    return result
  }
}

@objc class CsvUtil: NSObject {
  @objc public class func toCsvRow(_ csvRow: YailList<NSString>) -> String {
    var row = ""
    if csvRow.length == 0 {
      return row
    } else {
      var it = csvRow.makeIterator()
      var sep = "\""
      while let o = it.next() {
        row.append(sep)
        row.append(String(describing: o).replacingOccurrences(of: "\"", with: "\"\""))
        sep = "\",\""
      }
      return row + "\""
    }
  }

  @objc public class func toCsvTable(_ csvList: YailList<YailList<NSString>>) -> String {
    var table = ""
    for row in csvList {
      table.append(toCsvRow(row as! YailList<NSString>))
      table.append("\r\n")
    }
    return table
  }

  @objc public class func fromCsvRow(_ csvString: String) throws -> YailList<NSString> {
    let parser = CsvParser(input: csvString)
    if parser.hasNext() {
      let row = try parser.next()
      return YailList<NSString>(array: row as [NSString], in: SCMInterpreter.shared)
    } else {
      return YailList<NSString>()
    }
  }

  @objc public class func fromCsvTable(_ csvString: String) throws -> YailList<YailList<NSString>> {
    let parser = CsvParser(input: csvString)
    var result = [YailList<NSString>]()
    while parser.hasNext() {
      result.append(YailList<NSString>(array: (try parser.next()) as [NSString], in: SCMInterpreter.shared))
    }
    return YailList<YailList<NSString>>(array: result, in: SCMInterpreter.shared)
  }
}
