// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

class TinyDBTests: XCTestCase {

  var form: Form!
  var db: TinyDB!

  override func setUp() {
    form = ReplForm(nibName: nil, bundle: nil)
    db = TinyDB(form)
  }

  override func tearDown() {
    db.ClearAll()
  }

  func testGetTags() {
    db.StoreValue("tag1", "value1" as AnyObject)
    db.StoreValue("tag2", "value2" as AnyObject)
    XCTAssertEqual(["tag1", "tag2"], db.GetTags())
  }

  func testClearTag() {
    db.StoreValue("tag1", "value1" as AnyObject)
    db.StoreValue("tag2", "value2" as AnyObject)
    XCTAssertEqual("value1", db.GetValue("tag1", "" as AnyObject) as! String)
    XCTAssertEqual("value2", db.GetValue("tag2", "" as AnyObject) as! String)
    db.ClearTag("tag1")
    XCTAssertEqual("", db.GetValue("tag1", "" as AnyObject) as! String)
    XCTAssertEqual("value2", db.GetValue("tag2", "" as AnyObject) as! String)
  }

  func testNamespace() {
    // TinyDB should initialize to TinyDB1
    XCTAssertEqual("TinyDB1", db.Namespace)

    // Store a value in TinyDB1
    db.StoreValue("tag", "value" as AnyObject)

    // Switch to TinyDB2
    db.Namespace = "TinyDB2"
    XCTAssertEqual("TinyDB2", db.Namespace)

    // TinyDB2 shouldn't have "tag" defined yet
    XCTAssertFalse(db.GetValue("tag", false as AnyObject) as! Bool)

    // Set "tag" in TinyDB2
    db.StoreValue("tag", "another value" as AnyObject)
    XCTAssertEqual("another value", db.GetValue("tag", "bad" as AnyObject) as! String)

    // Clear TinyDB2
    db.ClearAll()
    XCTAssertEqual("empty", db.GetValue("tag", "empty" as AnyObject) as! String)

    // TinyDB1 should remain unchanged
    db.Namespace = "TinyDB1"
    XCTAssertEqual("value", db.GetValue("tag", "bad" as AnyObject) as! String)
  }
}
