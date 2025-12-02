// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2022-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import SchemeKit

@objc public final class YailProcedure: NSObject {

  private let executable: SCMProcedure
  @objc public static let RETURN_VALUE_WHEN_NULL: Any = false

  private init(executable: SCMProcedure) {
    self.executable = executable
    super.init()
  }

  @objc public static func create(_ procedure: SCMProcedure) -> YailProcedure {
    return YailProcedure(executable: procedure)
  }

  @objc public static func createWithName(_ procedureName: String) -> YailProcedure {
    let procedureName = "p$" + procedureName
    guard let procedure = AIComponentKit.Form.getActiveForm()?.environment[procedureName] as? SCMProcedure else {
      fatalError("Cannot read global procedure: \(procedureName)")
    }

    return YailProcedure(executable: procedure)
  }

  @objc public static func callProcedure(_ procedure: YailProcedure, _ args: [Any]) -> Any {
    return procedure.call(args: args)
  }

  private static func escapeUnicode(_ str: String) -> String {
    var b = ""
    for c in str {
      if c.asciiValue != nil {
        b.append(c)
      } else {
        b.append("u")
        b.append(String(format: "%x", c.unicodeScalars.first?.value ?? 0))
      }
    }
    return b
  }

  @objc public static func numArgs(_ procedure: YailProcedure) -> NSInteger {
    return procedure.executable.numberOfArguments
  }

  @objc public func call(args: [Any]) -> Any {
    let correct_args = args[0] is SCMSymbol ? Array(args.dropFirst()) : args
    let returnVal = executable.invoke(withArguments: correct_args)

    return returnVal ?? YailProcedure.RETURN_VALUE_WHEN_NULL
  }
}
