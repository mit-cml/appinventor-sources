// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class ApplicationFactory {
  fileprivate var registry: [String: Application] = [:]
  
  public enum ApplicationFactoryError: Error {
    case applicationNotFound
  }

  open func openApplication(from path: String) throws -> Application {
    throw ApplicationFactoryError.applicationNotFound
  }

  open func downloadApplication(from url: URL, callback: (_ application: Application) -> ()) {
    
  }

  open func deleteApplication(from path: String) -> Bool {
    return false
  }
}
