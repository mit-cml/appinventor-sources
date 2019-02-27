// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

open class XmlToJson: NSObject, XMLParserDelegate {
  fileprivate var elemStack = [AnyObject]()
  fileprivate var text = ""

  public static let main = XmlToJson()

  public func parserDidStartDocument(_ parser: XMLParser) {
    elemStack.removeAll()
    elemStack.append(NSMutableDictionary())
  }

  open func parseXML(_ xml: String) throws -> String {
    if xml.isEmpty {
      return ""
    }
    var parser: XMLParser
    if let data = xml.data(using: .utf8) {
      parser = XMLParser(data: data)
      parser.delegate = self
      parser.parse()
      if let error = parser.parserError {
        throw error
      } else {
        return try getJsonRepresentation(elemStack[0])
      }
    } else {
      throw YailRuntimeError("Failed to parse xml data", "XML Parsing")
    }
  }

  public func parser(_ parser: XMLParser, didStartElement elementName: String, namespaceURI: String?, qualifiedName qName: String?, attributes attributeDict: [String : String] = [:]) {
    let childDict = NSMutableDictionary()
    for (key, value) in attributeDict {
      childDict.setValue(value, forKey: key)
    }
    let currentValue: AnyObject?
    if let last = elemStack.last, let lastDict = last as? NSMutableDictionary {
      if let value = lastDict.value(forKey: elementName) {
        currentValue = value as AnyObject
      } else {
        currentValue = nil
      }
    } else {
      currentValue = nil
    }
    if let value = currentValue {
      var newArray: [AnyObject] = Array()
      if let currentArray = value as? [AnyObject] {
        newArray.append(contentsOf: currentArray)
      } else {
        newArray.append(value)
      }
      newArray.append(childDict as AnyObject)
      elemStack.last?.setValue(newArray, forKey: elementName)

    } else {
      elemStack.last?.setValue(childDict, forKey: elementName)
    }
    elemStack.append(childDict as AnyObject)
  }

  public func parser(_ parser: XMLParser, foundCharacters string: String) {
    text += string
  }

  public func parser(_ parser: XMLParser, didEndElement elementName: String, namespaceURI: String?, qualifiedName qName: String?) {
    text = text.trimmingCharacters(in: .whitespacesAndNewlines)
    if text.count > 0 {
      if let currentValue = elemStack.last?[elementName] {
        if let array = currentValue as? [AnyObject] {
          array.last?.setValue(text, forKey: "content")
        } else {
          elemStack.last?.setValue(text, forKey: "content")
        }
      }
    }
    text = ""
    elemStack.removeLast()
  }
}
