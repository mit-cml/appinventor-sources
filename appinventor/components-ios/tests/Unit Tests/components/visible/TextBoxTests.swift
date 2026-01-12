// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import XCTest
@testable import AIComponentKit

class TextBoxTests: XCTestCase {

  func testNumberOnlyText() {
    let textField = UITextField()
    let textView = UITextView()
    let adapter = TextBoxAdapter()
    adapter.numbersOnly = true

    // Test textField
    textField.text = ""
    XCTAssertTrue(adapter.textField(textField,
        shouldChangeCharactersIn: NSRange(location: 0, length: 0),
        replacementString: "1"))
    XCTAssertFalse(adapter.textField(textField,
        shouldChangeCharactersIn: NSRange(location: 0, length: 0),
        replacementString: "a"))
    XCTAssertTrue(adapter.textField(textField,
        shouldChangeCharactersIn: NSRange(location: 0, length: 0),
        replacementString: "."))
    XCTAssertTrue(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 0, length: 0), replacementString: "-"))
    XCTAssertTrue(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 0, length: 0), replacementString: "+"))

    textField.text = "-"
    XCTAssertTrue(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 1, length: 0), replacementString: "1"))
    XCTAssertFalse(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 1, length: 0), replacementString: "a"))
    XCTAssertTrue(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 1, length: 0), replacementString: "."))
    XCTAssertFalse(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 1, length: 0), replacementString: "-"))
    XCTAssertFalse(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 1, length: 0), replacementString: "+"))

    textField.text = "-1."
    XCTAssertTrue(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 3, length: 0), replacementString: "1"))
    XCTAssertFalse(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 3, length: 0), replacementString: "a"))
    XCTAssertFalse(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 3, length: 0), replacementString: "."))
    XCTAssertFalse(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 3, length: 0), replacementString: "-"))
    XCTAssertFalse(adapter.textField(textField, shouldChangeCharactersIn: NSRange(location: 3, length: 0), replacementString: "+"))

    // Test textView
    textView.text = ""
    XCTAssertTrue(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 0, length: 0), replacementText: "1"))
    XCTAssertFalse(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 0, length: 0), replacementText: "a"))
    XCTAssertTrue(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 0, length: 0), replacementText: "."))
    XCTAssertTrue(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 0, length: 0), replacementText: "-"))
    XCTAssertTrue(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 0, length: 0), replacementText: "+"))

    textView.text = "-"
    XCTAssertTrue(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 1, length: 0), replacementText: "1"))
    XCTAssertFalse(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 1, length: 0), replacementText: "a"))
    XCTAssertTrue(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 1, length: 0), replacementText: "."))
    XCTAssertFalse(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 1, length: 0), replacementText: "-"))
    XCTAssertFalse(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 1, length: 0), replacementText: "+"))

    textView.text = "-1."
    XCTAssertTrue(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 3, length: 0), replacementText: "1"))
    XCTAssertFalse(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 3, length: 0), replacementText: "a"))
    XCTAssertFalse(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 3, length: 0), replacementText: "."))
    XCTAssertFalse(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 3, length: 0), replacementText: "-"))
    XCTAssertFalse(adapter.textView(textView, shouldChangeTextIn: NSRange(location: 3, length: 0), replacementText: "+"))
  }
}
