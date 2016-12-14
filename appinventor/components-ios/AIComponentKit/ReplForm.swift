//
//  ReplForm.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/17/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import SchemeKit

open class ReplForm: Form {
  internal static weak var topform: ReplForm?
  fileprivate var _httpdServer: AppInvHTTPD?
  fileprivate var _assetsLoaded = false
  
  public override init(nibName nibNameOrNil: String?, bundle bundleOrNil: Bundle?) {
    super.init(nibName: nibNameOrNil, bundle: bundleOrNil)
    if ReplForm.topform == nil {
      ReplForm.topform = self
    }
    super.application = Application()
    NSLog("nib loader")
  }
  
  public required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    if ReplForm.topform == nil {
      ReplForm.topform = self
    }
    super.application = Application()
    NSLog("coder init")
  }

  open override func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    NSLog("Delegating dispatch to YAIL")
    _componentWithActiveEvent = component
    if let interpreter = _httpdServer?.interpreter {
      let result = interpreter.invokeMethod("dispatchEvent", withArgArray: [component, componentName, eventName, args])
      if (interpreter.exception != nil) {
        NSLog("Exception occurred in YAIL: \((interpreter.exception?.name.rawValue)!) (irritants: \((interpreter.exception)!))");
        return false
      }
      NSLog("dispatchEvent result = \(result)")
      if (result is Bool) {
        return result as! Bool
      } else if (result is NSNumber) {
        return (result as! NSNumber).boolValue
      } else {
        return false
      }
    } else {
      NSLog("No HTTPD server running?")
    }
    return false
  }
  
  open func startHTTPD(_ secure: Bool) {
    if _httpdServer == nil {
      _httpdServer = AppInvHTTPD(port: 8001, rootDirectory: "", secure: secure, for: self)
    }
  }
  
  open var interpreter: SCMInterpreter? {
    get {
      return _httpdServer?.interpreter
    }
  }

  open var activeForm: ReplForm? {
    get {
      if let form = Form.activeForm {
        if form is ReplForm {
          return form as? ReplForm
        }
      }
      return self
    }
  }

  open var assetsLoaded: Bool {
    @objc(isAssetsLoaded)
    get {
      return _assetsLoaded;
    }
  }
  
  open func setAssetsLoaded() {
    _assetsLoaded = true
  }
}
