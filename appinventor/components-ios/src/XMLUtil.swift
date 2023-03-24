// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
      throw YailRuntimeError("Failed to parse XML data", "XML Parsing")
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

/**
 * The `XmlToDictionaries` class is used to parse an XML document into `YailDictionary` objects.
 */
open class XmlToDictionaries: NSObject, XMLParserDelegate {
  private var elemStack = [YailDictionary]()
  private var root: YailDictionary? = nil

  public static let main = XmlToDictionaries()

  /**
   * Parses the XML documented serialized in `xml` into a `YailDictionary` structure with the
   * following keys:
   *
   * - Parameter xml: A `String` containing a XML document.
   *
   * - Returns: A `YailDictionary` containing the XML content if `xml` represents a valid document,
   *     otherwise `nil` is returned.
   *
   * - Throws: `YailRuntimeError` when the XML document does not parse correctly.
   */
  open func parseXML(_ xml: String) throws -> YailDictionary? {
    if xml.isEmpty {
      return nil
    }
    if let data = xml.data(using: .utf8) {
      let parser = XMLParser(data: data)
      parser.delegate = self
      parser.parse()
      if let error = parser.parserError {
        throw error
      } else {
        return root
      }
    } else {
      throw YailRuntimeError("Failed to parse XML data", "XML Parsing")
    }
  }

  /// MARK: XMLParserDelegate

  public func parserDidStartDocument(_ parser: XMLParser) {
    elemStack.removeAll()
  }

  public func parser(_ parser: XMLParser, didStartElement elementName: String, namespaceURI: String?, qualifiedName qName: String?, attributes attributeDict: [String : String] = [:]) {
    let el = YailDictionary()
    el["$tag"] = qName ?? elementName
    el["$namespaceUri"] = namespaceURI ?? ""
    el["$localName"] = elementName
    if let qName = qName, let colon = qName.firstIndex(of: ":") {
      el["$namespace"] = String(qName[..<colon])
    } else {
      el["$namespace"] = ""
    }
    let attrs = YailDictionary()
    attrs.addEntries(from: attributeDict)
    el["$attributes"] = attrs
    el["$content"] = NSMutableArray()
    if let parent = elemStack.last {
      (parent["$content"] as! NSMutableArray).add(el)
      if !parent.containsKey(elementName) {
        parent[elementName] = NSMutableArray()
      }
      (parent[elementName] as! NSMutableArray).add(el)
    }
    if elemStack.isEmpty {
      root = el
    }
    elemStack.append(el)
  }

  public func parser(_ parser: XMLParser, foundCharacters string: String) {
    if let element = elemStack.last {
      (element["$content"] as! NSMutableArray).add(string)
    }
  }

  public func parser(_ parser: XMLParser, didEndElement elementName: String, namespaceURI: String?, qualifiedName qName: String?) {
    _ = elemStack.popLast()
  }
}
