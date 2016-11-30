//
//  ApplicationFactory.swift
//  AIComponentKit
//
//  Created by Evan Patton on 11/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class ApplicationFactory {
  private var registry: [String: Application] = [:]
  
  public enum ApplicationFactoryError: Error {
    case applicationNotFound
  }

  public func openApplication(from path: String) throws -> Application {
    throw ApplicationFactoryError.applicationNotFound
  }

  public func downloadApplication(from url: URL, callback: (_ application: Application) -> ()) {
    
  }

  public func deleteApplication(from path: String) -> Bool {
    return false
  }
}
