// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit
import AIComponentKit

public class PlayerForm : Form {

  let interpreter: SCMInterpreter
  let isRootForm: Bool

  /**
   * A Form object that is pending to be pushed onto the view stack.
   */
  private var pendingOpen: Form? = nil

  /**
   * A closure to close the current form.
   */
  private var pendingClose: (() -> ())? = nil

  public init(application: Application) {
    self.interpreter = SCMInterpreter.shared
    self.isRootForm = true
    super.init(application: application, screen: "Screen1")
  }

  public override init(application: Application, screen name: String) {
    self.interpreter = SCMInterpreter.shared
    self.isRootForm = false
    super.init(application: application, screen: name)
  }

  public required init?(coder aCoder: NSCoder) {
    self.interpreter = SCMInterpreter.shared
    self.isRootForm = false
    super.init(coder: aCoder)
    loadScreen()
  }

  open override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(true)
    interpreter.setCurrentForm(self)
    if #unavailable(iOS 16) {
      guard navigationItem.leftBarButtonItem == nil else {
        // We've already set the left bar item
        return
      }
      guard let previousScreen = navigationController?.screenBefore(self) else {
        // No previous screen exists
        return
      }
      let backButton = UIBarButtonItem(title: "〈 \(previousScreen.Title())", style: .plain,
                                       target: self, action: #selector(handleBackButton))
      navigationItem.leftBarButtonItem = backButton
    }
  }

  @objc func handleBackButton() {
    if !EventDispatcher.dispatchEvent(of: self, called: "BackPressed") {
      self.navigationController?.popViewController(animated: true)
    }
  }

  open override func viewDidAppear(_ animated: Bool) {
    if !initialized {
      interpreter.setCurrentForm(self)
      interpreter.evalForm("(ios$start-form)")
      if let exception = interpreter.exception {
        print("Exception in initialization: \(exception)")
        interpreter.clearException()
      }
    }
    super.viewDidAppear(animated)
    updateNavbar()
  }

  private func loadScheme(_ name: String, extension ext: String = "scm") {
    guard let code = (application as? BundledApp)?.loadScheme(for: formName) else {
      fatalError("foo")
    }
    makeActive()
    interpreter.setCurrentForm(self)
    interpreter.evalForm(code)
    if let exception = interpreter.exception {
      print("Error initializing screen \(name): \(exception)")
    }
  }

  func loadScreen() {
    loadScheme(formName)
    viewDidLoad()
  }

  open override func viewDidLoad() {
    super.viewDidLoad()
    guard !isRootForm else {
      // Do not set up a back button for the initial form since there is nothing to go back to
      return
    }
    if #available(iOS 16.0, *) {
      guard navigationItem.backAction == nil else {
        // We've been here already.
        return
      }
      navigationItem.backAction = UIAction() { [self] _ in
        handleBackButton()
      }
    } else {
      // Fallback on earlier versions, see the logic in `viewWillAppear:`
    }
  }

  @objc open override func doSwitchForm(to formName: String, startValue: AnyObject? = nil) {
    let newForm = PlayerForm(application: application!, screen: formName)
    if let startValue = startValue {
      newForm.startValue = startValue
    }
    newForm.loadScreen()
    pendingOpen = newForm
  }

  @objc open override func reportScreenClosed() {
    super.reportScreenClosed()
    DispatchQueue.main.async {
      self.application?.popScreen(with: "")
    }
  }

  @objc open override func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    defer {
      _componentWithActiveEvent = nil
    }
    _componentWithActiveEvent = component
    let result = interpreter.invokeMethod("dispatchEvent", withArgArray: [component, componentName, eventName, args])
    if let exception = interpreter.exception {
      defer {
        interpreter.clearException()
      }
      NSLog("Exception occurred in YAIL: \(exception.name.rawValue) \(exception.reason ?? "")")
      if let exception = interpreter.exception as? PermissionException {
        if self == component as! NSObject && eventName == "PermissionDenied" {
          processException(ex: exception)
        } else {
          PermissionDenied(component, eventName, exception.permissionNeeded)
        }
      } else {
        processException(ex: exception)
      }
      return false
    }
    if (result is Bool) {
      return result as! Bool
    } else if (result is NSNumber) {
      return (result as! NSNumber).boolValue
    } else {
      return false
    }
  }

  open override func dispatchGenericEvent(of component: Component, eventName: String, unhandled: Bool, arguments: [AnyObject]) {
    defer {
      _componentWithActiveEvent = nil
      processScreenSwitching()
    }
    _componentWithActiveEvent = component
    interpreter.invokeMethod("dispatchGenericEvent", withArgArray: [component, eventName, unhandled, arguments])
    if let exception = interpreter.exception {
      defer {
        interpreter.clearException()
      }
      NSLog("Exception occurred in YAIL: \(exception.name.rawValue) \(exception.reason ?? "")")
      if let exception = interpreter.exception as? PermissionException {
        if self == component as! NSObject && eventName == "PermissionDenied" {
          processException(ex: exception)
        } else {
          PermissionDenied(component, eventName, exception.permissionNeeded)
        }
      } else {
        processException(ex: exception)
      }
    }
  }

  open func processException(ex: NSException) {
    if toastAllowed {
      var errorMessage: String! = nil
      if let exception = interpreter.exception as? YailRuntimeError {
        errorMessage = exception.name.rawValue
      } else {
        errorMessage = "\(interpreter.exception!)"
      }
      view.makeToast(errorMessage, duration: 5.0)
    }
  }

  override open func doCloseScreen(withValue value: AnyObject? = nil) {
    // Set up a pending close closure to be executed at the end of the event handler
    pendingClose = {
      super.doCloseScreen(withValue: value)
      do {
        self.application?.popScreen(with: try getJsonRepresentation(value))
      } catch {
        self.application?.popScreen(with: "")
      }
    }
  }

  override open func doCloseScreen(withPlainText text: String) {
    // Set up a pending close closure to be executed at the end of the event handler
    pendingClose = {
      super.doCloseScreen(withPlainText: text)
      self.application?.popScreen(with: text)
    }
  }

  /**
   * Processes any pending screen switches.
   */
  private func processScreenSwitching() {
    if let nextForm = pendingOpen {
      // We are going to open a new screen.
      // Do we also need to close the current one? i.e., the Tribblehunter method
      if pendingClose != nil {
        EventDispatcher.removeDispatchDelegate(self)  // Necessary to clean up event handlers
        pendingClose = nil  // For compatibility with Android framework behavior
        var activityStack = self.navigationController?.viewControllers ?? [self]
        _ = activityStack.popLast()
        activityStack.append(nextForm)
        self.navigationController?.viewControllers = activityStack
      } else {
        // Not closing this form, so just push the new one onto the stack.
        self.navigationController?.pushViewController(nextForm, animated: true)
      }
    } else if let closeFunc = pendingClose {
      // We are only closing this form, so go ahead.
      closeFunc()
    }
    pendingOpen = nil
    pendingClose = nil
  }
}
