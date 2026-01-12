// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class FontUtilTests: AppInventorTestCase {
  var font: UIFont = UIFont.systemFont(ofSize: 14.0)
  fileprivate var _fontName: String = ""
  fileprivate var _size: CGFloat = 0

  override func setUp() {
    font = UIFont.systemFont(ofSize: 14.0)
    setFont(size: 14.0)
    _fontName = font.familyName
  }
  
  func testBold() {
    setFont(.traitBold, to: true)
    testFont(bold: true)
  }

  func testItalic() {
    setFont(.traitItalic, to: true)
    testFont(italic: true)
  }

  func testBoldThenItalic() {
    setFont(.traitBold, to: true)
    setFont(.traitItalic, to: true)
    testFont(bold: true, italic: true)
  }

  func testItalicThenBold() {
    setFont(.traitItalic, to: true)
    setFont(.traitBold, to: true)
    testFont(bold: true, italic: true)
  }

  func testAndRemoveBold() {
    setFont(.traitBold, to: true)
    setFont(.traitBold, to: false)
    testFont()
  }

  func testAndRemoveItalic() {
    setFont(.traitItalic, to: true)
    setFont(.traitItalic, to: false)
    testFont()
  }

  func testSetAndRemoveBoldWithExistingItalic() {
    setFont(.traitItalic, to: true)
    setFont(.traitBold, to: true)
    setFont(.traitBold, to: false)
    testFont(italic: true)
  }

  func testSetAndRemoveItalicWithExistingBold() {
    setFont(.traitBold, to: true)
    setFont(.traitItalic, to: true)
    setFont(.traitItalic, to: false)
    testFont(bold: true)
  }

  func testSetBoldItalicAndResizeFont() {
    setFont(.traitBold, to: true)
    setFont(.traitItalic, to: true)
    setFont(size: 11.0)
    testFont(bold: true, italic: true)
  }

  func testSetMonospaceFont() {
    font = getFontTypeface(font: font, typeFace: .monospace)!
    _fontName = "Menlo"
    testFont()
    setFont(.traitBold, to: true)
    testFont(bold: true)
    setFont(.traitItalic, to: true)
    testFont(bold: true, italic: true)
    setFont(size: 30.0)
    testFont(bold: true, italic: true)
    setFont(.traitBold, to: false)
    testFont(italic: true)
  }

  func testSetSerifedFont() {
    font = getFontTypeface(font: font, typeFace: .serif)!
    _fontName = "Cochin"
    testFont()
    setFont(.traitBold, to: true)
    testFont(bold: true)
    setFont(.traitItalic, to: true)
    testFont(bold: true, italic: true)
    setFont(size: 30.0)
    testFont(bold: true, italic: true)
    setFont(.traitBold, to: false)
    testFont(italic: true)
  }

  func testChangeBetweenFonts() {
    let initialName = _fontName
    font = getFontTypeface(font: font, typeFace: .monospace)!
    _fontName = "Menlo"
    setFont(.traitBold, to: true)
    setFont(.traitItalic, to: true)
    setFont(size: 11.0)
    testFont(bold: true, italic: true)
    font = getFontTypeface(font: font, typeFace: .serif)!
    _fontName = "Cochin"
    testFont(bold: true, italic: true)
    font = getFontTypeface(font: font, typeFace: .sansSerif)!
    _fontName = initialName
    testFont(bold: true, italic: true)
    font = getFontTypeface(font: font, typeFace: .normal)!
    testFont(bold: true, italic: true)
  }

  fileprivate func setFont(_ trait: UIFontDescriptor.SymbolicTraits, to shouldSet: Bool) {
    if let newFont = getFontTrait(font: font, trait: trait, shouldSet: shouldSet) {
      font = newFont
    } else {
      XCTFail("Could not extract bold/italic font")
    }
  }

  fileprivate func setFont(size: Float32) {
    if let newFont = getFontSize(font: font, size: size) {
      font = newFont
      _size = CGFloat(size)
    } else {
      XCTFail("Could not extract front font")
    }
  }

  fileprivate func testFont(bold: Bool = false, italic: Bool = false) {
    let traits = font.fontDescriptor.symbolicTraits
    XCTAssertTrue(traits.contains([.traitBold]) == bold)
    XCTAssertTrue(traits.contains([.traitItalic]) == italic)
    XCTAssertEqual(font.pointSize, _size)
    XCTAssertEqual(font.familyName, _fontName)
  }
}
