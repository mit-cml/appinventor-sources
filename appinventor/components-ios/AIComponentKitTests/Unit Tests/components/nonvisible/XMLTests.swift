// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class XMLTests: XCTestCase {
  func testCorrectXml() {
    do {
      // XML adapted from w3schools sample XML
      let xml = try getObjectFromJson(try XmlToJson.main.parseXML("""
      <mailbox>
        <note>
          <to>Tove</to>
          <from>Jani</from>
          <heading>Reminder</heading>
          <body>Don't forget me this weekend!</body>
        </note>
        <note>
          <to>Jani</to>
          <from>Tove</from>
          <heading>Re: Reminder</heading>
          <body>Oops.</body>
        </note>
      </mailbox>
      """))
      if let xmlObject = xml as? NSDictionary,
        let notes = xmlObject.value(forKeyPath: "mailbox.note") as? [NSDictionary] {
        XCTAssertEqual(notes[0].value(forKeyPath: "to.content") as? String, "Tove")
        XCTAssertEqual(notes[0].value(forKeyPath: "from.content") as? String, "Jani")
        XCTAssertEqual(notes[0].value(forKeyPath: "heading.content") as? String, "Reminder")
        XCTAssertEqual(notes[0].value(forKeyPath: "body.content") as? String, "Don't forget me this weekend!")
        XCTAssertEqual(notes[1].value(forKeyPath: "to.content") as? String, "Jani")
        XCTAssertEqual(notes[1].value(forKeyPath: "from.content") as? String, "Tove")
        XCTAssertEqual(notes[1].value(forKeyPath: "heading.content") as? String, "Re: Reminder")
        XCTAssertEqual(notes[1].value(forKeyPath: "body.content") as? String, "Oops.")
      } else {
        throw YailRuntimeError("XML failed to initialize", "Xml Test")
      }
    } catch let error {
      XCTFail(error.localizedDescription)
    }
  }

  func testEmptyString() {
    do {
      let xml = try XmlToJson.main.parseXML("")
      XCTAssertEqual(xml, "")
    } catch let err {
      XCTFail(err.localizedDescription)
    }
  }

  func testMismatchedTag() {
    XCTAssertThrowsError(try XmlToJson.main.parseXML("""
      <note>
        <to>Tove</to>
        <from>Jani</from>
        <heading>Reminder</body>
        <body>Don't forget me this weekend!</heading>
      </note>
      """), "Failed to throw error")
  }

  func testMalformedTag() {
    XCTAssertThrowsError(try XmlToJson.main.parseXML("""
      <note>
        <to>Tove</to>
        <from>Jani</from>
        <heading>Reminder<heading>
        <body>Don't forget me this weekend!</body>
      </note>
      """), "Failed to throw error")
  }

  func testMissingTag() {
    XCTAssertThrowsError(try XmlToJson.main.parseXML("""
      <note>
        <to>Tove</to>
        <from>Jani</from>
        <heading>Reminder
        <body>Don't forget me this weekend!</body>
      </note>
      """), "Failed to throw error")
  }
}
