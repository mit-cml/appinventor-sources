// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * A FileError class.  An error specific to files.
 * @author Nichole Clarke
 */
public struct FileError: Error {
  var type: ErrorMessage { return _errorMessage }
  var message: String { return _errorMessage.message }
  var filePath: String { return _filePath }
  var code: Int32 { return _errorMessage.code }
  
  private var _errorMessage: ErrorMessage
  private var _filePath: String
  
  init(_ errorMessage: ErrorMessage, _ filePath: String) {
    _errorMessage = errorMessage
    _filePath = filePath
  }
}
