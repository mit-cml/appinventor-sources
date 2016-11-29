//
//  Application.swift
//  AIComponentKit
//
//  Created by Evan Patton on 11/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class Application {
  private let assetManager_: AssetManager

  /**
   * Create an Application using the currently running application bundle.
   */
  public init() {
    
  }

  /**
   *
   */
  public init(from url: URL) {
    assetManager_ = AssetManager()
  }

  init(from url: URL, isRepl: Bool) {
    assetManager_ = AssetManager()
  }

  public var assetManager: AssetManager {
    get {
      
    }
  }
}
