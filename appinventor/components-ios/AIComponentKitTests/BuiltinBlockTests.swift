// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import XCTest
@testable import AIComponentKit

/**
 * Reads the contents of the file at the given pathname into the given interpreter. The list of
 * defined test procedure names is returned to facilitate testing.
 *
 * @param path The path to the test YAIL file
 * @param interpreter The interpreter that will receive and evaluate the YAIL
 * @returns A list of the procedure names for evaluation
 */
public func loadTestYail(path: String, into interpreter: SCMInterpreter) throws -> [String] {
  let subpath = path.substring(to: path.index(path.endIndex, offsetBy: -5))
  if let url = Bundle(for: BuiltinBlockTests.self).url(forResource: subpath, withExtension: "yail") {
    do {
      let yail = try String(contentsOf: url, encoding: String.Encoding.utf8)
      interpreter.evalForm(yail)
      if let exception = interpreter.exception {
        XCTFail("Exception: \(exception.name.rawValue) (\(exception))")
        throw TestFailure()
      }
      let regex = try! NSRegularExpression(pattern: "p\\$test_[^\\s)]+", options: [])
      var tests = [String]()
      let matches = regex.matches(in: yail, options: [], range: NSRange(location: 0, length: yail.count))
      for match in matches {
        tests.append(yail[Range(match.range, in: yail)!])
      }
      return tests
    } catch {
      XCTFail("Unable to load \(path)")
      throw TestFailure()
    }
  } else {
    throw TestFailure()
  }
}

func optionallyPrint(_ exception: NSException?) -> String {
  if let exception = exception {
    return " Exception was thrown: \(exception.name.rawValue) (\(exception))"
  } else {
    return ""
  }
}

var coverage = false
var coverageData = [String: [String: Bool]]()

class BuiltinBlockTests: XCTestCase {

  func runTestsFromYail(path: String) throws {
    var passing: Int32 = 0, failing: Int32 = 0
    let interpreter = try getInterpreterForTesting()
    let testNames = try loadTestYail(path: path, into: interpreter)
    for test in testNames {
      let result = interpreter.evalForm("((get-var \(test)))")
      if result == "#t" && interpreter.exception == nil {
        passing += 1
      } else {
        failing += 1
      }
      if coverage {
        let block = test.suffix(from: test.index(test.startIndex, offsetBy: 7))
        var parts = block.split(separator: "_", maxSplits: 1, omittingEmptySubsequences: false)
        if parts.count == 1 {
          parts.append("text")
        } else if parts[0] == "obfuscated" {
          parts[1] = "obfuscated"
          parts[0] = "text"
        }
        let key = String(parts[0])
        if var data = coverageData[key] {
          data[String(block)] = result == "#t" && interpreter.exception == nil
        } else {
          coverageData[key] = [String(block): result == "#t" && interpreter.exception == nil]
        }
      } else {
        XCTAssertTrue(result == "#t" && interpreter.exception == nil,
                      "\(test) failed." + optionallyPrint(interpreter.exception))
      }
      interpreter.clearException()
    }
    print("Passed: \(passing)")
    print("Failed: \(failing)")
  }

  func testBitwiseAnd() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("255", interpreter.evalForm("(bitwise-and 255 255)"))
    XCTAssertEqual("0", interpreter.evalForm("(bitwise-and 0 255)"))
    XCTAssertEqual("255", interpreter.evalForm("(bitwise-and (bitwise-arithmetic-shift-right 65280 8) 255)"))
  }

  func testBitwiseArithmetic() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("255", interpreter.evalForm("(bitwise-arithmetic-shift-right 65280 8)"))
    XCTAssertEqual("65280", interpreter.evalForm("(bitwise-arithmetic-shift-left 255 8)"))
  }

  func testKawaListToYailList() throws {
    let interpreter = try getInterpreterForTesting()
    XCTAssertEqual("(*list* 255 255 0 0)", interpreter.evalForm("(kawa-list->yail-list (list 255 255 0 0))"))
  }

  func testSplitColor() throws {
    let interpreter = try getInterpreterForTesting()
    let result = interpreter.evalForm("(call-yail-primitive split-color (*list-for-runtime* -65536) '(number) \"split-color\")")
    NSLog("result = \(result)")
  }

  func testControlBlocks() throws {
    try runTestsFromYail(path: "control.yail")
  }

  func testLogicBlocks() throws {
    try runTestsFromYail(path: "logic.yail")
  }

  func testMathBlocks() throws {
//    try runTestsFromYail(path: "math.yail")
  }

  func testTextBlocks() throws {
    try runTestsFromYail(path: "text.yail")
  }

  func testListBlocks() throws {
    try runTestsFromYail(path: "lists.yail")
  }

  func testColorBlocks() throws {
    try runTestsFromYail(path: "colors.yail")
  }
}
