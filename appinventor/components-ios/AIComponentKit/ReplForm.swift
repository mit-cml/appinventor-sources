// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

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
      NSLog("dispatchEvent result = \(String(describing: result))")
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

  open override func Initialize() {
    super.Initialize()
    EventDispatcher.dispatchEvent(of: self, called: "Initialize")
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
/*
  open override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    interpreter?.setCurrentForm(self)
  }
*/
  open override func doSwitchForm(to formName: String, startValue: AnyObject?) {
    let newForm = ReplForm(nibName: nil, bundle: nil)
    newForm.formName = formName
    if let startValue = startValue {
      newForm.startValue = startValue
    }
    interpreter?.setCurrentForm(newForm)
    self.navigationController?.pushViewController(newForm, animated: true)
    RetValManager.shared().pushScreen(formName, withValue: newForm.startText as NSString)
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

  @objc open var assetsLoaded: Bool {
    @objc(isAssetsLoaded)
    get {
      return _assetsLoaded;
    }
  }
  
  open func setAssetsLoaded() {
    _assetsLoaded = true
  }

  override func doCloseScreen(withValue value: AnyObject? = nil) {
    super.doCloseScreen(withValue: value)
    do {
      RetValManager.shared().popScreen(try getJsonRepresentation(value))
    } catch {
      RetValManager.shared().popScreen("")
    }
  }

  override func doCloseScreen(withPlainText text: String) {
    super.doCloseScreen(withPlainText: text)
    RetValManager.shared().popScreen(text)
  }

  override open func doCloseApplication() {
    view.makeToast("Closing the application is not allowed in live development mode.")
  }
}
