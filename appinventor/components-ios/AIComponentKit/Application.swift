// mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit

@objc open class Application: UIResponder, UIApplicationDelegate {
  fileprivate var assetManager_: AssetManager?
  @objc let name: String
  static var current: Application? = nil

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

  @objc public init(named name: String) {
    self.name = name
  }

  @objc convenience init(from url: URL, isRepl: Bool) {
    if isRepl {
      self.init()
    } else {
      self.init(from: url)
    }
  }

  @objc open func makeCurrent() {
    Application.current = self
  }

  @objc open var assetManager: AssetManager {
    get {
      if assetManager_ == nil {
        assetManager_ = AssetManager(for: self)
      }
      return assetManager_!
    }
  }

  @objc open var assetPath: String? {
    let resourcePath = Bundle.main.resourcePath ?? ""
    let path = "\(resourcePath)/samples/\(name)/assets"
    if FileManager.default.fileExists(atPath: path) {
      return path
    }
    return nil
  }

  @objc open func pushScreen(named name: String, with startValue: NSObject? = nil) {
    let newForm = ReplForm(nibName: nil, bundle: nil)
    newForm.formName = name
    if let startValue = startValue {
      newForm.startValue = startValue
    }
    SCMInterpreter.shared.setCurrentForm(newForm)
    ReplForm.activeForm?.navigationController?.pushViewController(newForm, animated: true)
    RetValManager.shared().pushScreen(name, withValue: startValue ?? "" as NSObject)
  }

  @objc open func popScreen(with closeValue: String) {
    RetValManager.shared().popScreen(closeValue)
  }
}
