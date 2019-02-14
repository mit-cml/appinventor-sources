//
//  Application.swift
//  AIComponentKit
//
//  Created by Evan Patton on 11/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import UIKit

@objc open class Application: UIResponder, UIApplicationDelegate {
  fileprivate var assetManager_: AssetManager?
  @objc let name: String

  /**
   * Create an Application using the currently running application bundle.
   */
  public override init() {
    if let appName = Bundle.main.infoDictionary?[kCFBundleNameKey as String] as? String {
      name = appName
    } else {
      name = "<unknown>"
    }
    assetManager_ = AssetManager()
    super.init()
  }

  /**
   *
   */
  @objc public init(from url: URL) {
    var temp_name = "UnknownApp"
    do {
      let fileregex = try NSRegularExpression(pattern: "/([^./]+).aia$")
      if let result = fileregex.firstMatch(in: url.absoluteString, range: NSMakeRange(0, url.absoluteString.lengthOfBytes(using: .utf8))) {
        let range = result.range(at: 1)
        temp_name = (url.absoluteString as NSString).substring(with: range)
      }
    } catch {
      // wtf
    }
    name = temp_name
    super.init()
  }

  @objc convenience init(from url: URL, isRepl: Bool) {
    if isRepl {
      self.init()
    } else {
      self.init(from: url)
    }
  }

  @objc open var assetManager: AssetManager {
    get {
      if assetManager_ == nil {
        assetManager_ = AssetManager(for: self)
      }
      return assetManager_!
    }
  }
}
