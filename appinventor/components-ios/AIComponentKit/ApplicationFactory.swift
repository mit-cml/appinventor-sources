//
//  ApplicationFactory.swift
//  AIComponentKit
//
//  Created by Evan Patton on 11/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

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
