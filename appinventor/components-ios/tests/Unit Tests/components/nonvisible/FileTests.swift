// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class FileTests: AppInventorTestCase {
  var file: File!
  var createdFile: String? = nil
  let fileManager = FileManager()
  
  override func setUp() {
    super.setUp()
    file = File(form)
    XCTAssertTrue(addComponent(file, named: "File1"))
  }
  
  override func tearDown() {
    if let createdFile = createdFile {
      deleteFilePostTest(fileName: createdFile)
      self.createdFile = nil
    }
    super.tearDown()
  }
  
  // TODO: update to assert that error was dispatched
  func testSaveFileAssetError() {
    let fileName = "//testfile.txt"
    let fileText = "this file should not be saved"
    expectToReceiveEvent(on: form, named: "ErrorOccurred")
    expectNotToReceiveEvent(on: file, named: "AfterFileSaved")

    file.SaveFile(fileText, fileName)

    verify()
  }
  
  func testFileSavePublic() {
    let fileName = "/filePublic.txt"
    let fileText = "this is a public file"
    baseTestFileSave(fileName: fileName, fileText: fileText, isPublic: true)
  }
  
  func testFileSavePrivate() {
    let fileName = "filePrivate.txt"
    let fileText = "this is a private file"
    baseTestFileSave(fileName: fileName, fileText: fileText)
  }
  
  // TODO: update to assert that error was dispatched
  func testFileNewFileAssetError() {
    let fileName = "//testfile.txt"
    let fileText = "this file should not be saved"
    expectToReceiveEvent(on: form, named: "ErrorOccurred")
    expectNotToReceiveEvent(on: file, named: "AfterFileSaved")

    file.AppendToFile(fileText, fileName)
    
    verify()
    XCTAssertFalse(fileExists(fileName), "Asset File should not exist created.")
  }
  
  func testFileAppendNewFilePublic() {
    let fileName = "/filePublicAppendNew.txt"
    let fileText = "this is a public file"
    baseTestFileAppendNew(fileName: fileName, fileText: fileText)
  }
  
  func testFileAppendNewFilePrivate() {
    let fileName = "filePrivateAppendNew.txt"
    let fileText = "this is a private file"
    baseTestFileAppendNew(fileName: fileName, fileText: fileText, isPublic: false)
  }
  
  func testFileAppendFilePublic() {
    let fileName = "/filePublic.txt"
    let fileText = "this is a public file"
    let appendText = " that now has more text."
    baseTestFileAppend(fileName: fileName, originalText: fileText, appendingText: appendText)
  }
  
  func testFileAppendFilePrivate() {
    let fileName = "filePrv.txt"
    let fileText = "this is a private file"
    let appendText = " that now has more text."
    baseTestFileAppend(fileName: fileName, originalText: fileText, appendingText: appendText, isPublic: false)
  }
  
  // TODO: update to assert that error was dispatched
  func testFileDeleteAssetError() {
    let fileName = "//testfile.txt"

    expectToReceiveEvent(on: form, named: "ErrorOccurred")
    file.Delete(fileName)
    
    verify()
    XCTAssertFalse(fileExists(fileName), "Asset File should not be deleted.")
  }
  
  func testFileDeletePublic() {
    let fileName = "/filePublic.txt"
    let fileText = "this is a public file"
    baseTestFileSave(fileName: fileName, fileText: fileText)

    file.Delete(fileName)

    XCTAssertFalse(fileExists(fileName), "Public file should have been deleted.")
  }
  
  func testFileDeletePrivate() {
    let fileName = "/filePriv.txt"
    let fileText = "this is a private file"
    baseTestFileSave(fileName: fileName, fileText: fileText, isPublic: false)

    file.Delete(fileName)
    
    XCTAssertFalse(fileExists(fileName), "Private file should have been deleted.")
  }
  
  // TODO: update to test that GotText was called with proper text
  func testFileReadPublic() {
    let fileName = "/filePublic.txt"
    let fileText = "this is a public file"
    baseTestFileSave(fileName: fileName, fileText: fileText)

    expectToReceiveEvent(on: file, named: "GotText")
    file.ReadFrom(fileName)
    
    verify()
  }
  
  // TODO: update to test that GotText was called with proper text
  func testFileReadPrivate() {
    let fileName = "filePublic.txt"
    let fileText = "this is a private file"
    baseTestFileSave(fileName: fileName, fileText: fileText, isPublic: false)
    expectToReceiveEvent(on: file, named: "GotText")
    
    file.ReadFrom(fileName)
    
    verify()
  }
  
  // MARK: Base Test Functions
  private func baseTestFileSave(fileName: String, fileText: String, isPublic: Bool = true) {
    expectToReceiveEvent(on: file, named: "AfterFileSaved")

    file.SaveFile(fileText, fileName)
    createdFile = fileName

    verify()
    XCTAssertTrue(fileExists(fileName), "\(isPublic ? "Public" : "Private") file should have been created.")
    XCTAssertTrue(fileTextMatches(fileName, fileText), "Text did not match expected text")
  }
  
  private func baseTestFileAppendNew(fileName: String, fileText: String, isPublic: Bool = true) {
    expectToReceiveEvent(on: file, named: "AfterFileSaved")

    file.AppendToFile(fileText, fileName)
    createdFile = fileName

    verify()
    XCTAssertTrue(fileExists(fileName), "\(isPublic ? "Public" : "Private") file should be created.")
    XCTAssertTrue(fileTextMatches(fileName, fileText), "Text did not match expected text")
  }
  
  private func baseTestFileAppend(fileName: String, originalText: String, appendingText: String, isPublic: Bool = true) {
    baseTestFileSave(fileName: fileName, fileText: originalText, isPublic: isPublic)
    expectToReceiveEvent(on: file, named: "AfterFileSaved")

    file.AppendToFile(appendingText, fileName)

    verify()
    XCTAssertTrue(fileExists(fileName), "\(isPublic ? "Public" : "Private") file should exist.")
    XCTAssertTrue(fileTextMatches(fileName, originalText + appendingText), "Text did not match new appended text")
  }
  
  // MARK: Helper Functions
  private func fileExists(_ fileName: String) -> Bool {
    return fileManager.fileExists(atPath: FileUtil.absoluteFileName(fileName, file._form is ReplForm))
  }
  
  private func fileTextMatches(_ fileName: String, _ expectedText: String) -> Bool {
    do {
      let url = URL(fileURLWithPath: FileUtil.absoluteFileName(fileName, file._form is ReplForm))
      let text = try String(contentsOf: url, encoding: .utf8)
      return expectedText == text
    } catch {
      XCTFail("Could not read file")
      return false
    }
  }
  
  func deleteFilePostTest(fileName: String) {
    if fileName.starts(with: "//") {
      return
    }
    let filePath = FileUtil.absoluteFileName(fileName, file._form is ReplForm )
    if filePath.isEmpty || !fileManager.fileExists(atPath: filePath) {
      return
    } else {
      do {
        try fileManager.removeItem(atPath: filePath)
      } catch {
        NSLog("An unexpected error occurred when attempting to delete file: \(fileName)")
      }
    }
  }
}
