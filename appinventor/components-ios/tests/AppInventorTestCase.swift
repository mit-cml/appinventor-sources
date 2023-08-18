// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

public typealias CheckFunc = (_ arguments: [AnyObject]) -> Swift.Void

struct EventDescription: Hashable {
  let component: Component
  let eventName: String

  public init(_ component: Component, _ name: String) {
    self.component = component
    self.eventName = name
  }

  var hashValue: Int {
    return ObjectIdentifier(component).hashValue &* 37 &+ eventName.hashValue
  }

  static func == (lhs: EventDescription, rhs: EventDescription) -> Bool {
    return ObjectIdentifier(lhs.component) == ObjectIdentifier(rhs.component) && lhs.eventName == rhs.eventName
  }
}

/**
 * A concrete implementation ReplForm that intercepts events, for use with AppInventorComponentBase.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
class TestForm : ReplForm {

  let _interpreter: SCMInterpreter
  var checkerMap = [EventDescription: [CheckFunc]]()
  var testComponents = [String: Component]()

  public init(_ interpreter: SCMInterpreter) {
    _interpreter = interpreter
    super.init(nibName: nil, bundle: nil)
    _interpreter.setCurrentForm(self)
  }

  required init?(coder: NSCoder) {
    _interpreter = SCMInterpreter()
    super.init(coder: coder)
    _interpreter.setCurrentForm(self)
  }

  open override var interpreter: SCMInterpreter {
    get {
      return _interpreter
    }
  }

  open override func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    if let callbacks = checkerMap[EventDescription(component, eventName)] {
      for callback in callbacks {
        callback(args)
      }
    }
    return true
  }
}

/**
 * AppInventorTestCase provides a base class for other tests in App Inventor. It provides mechanisms
 * to test for events occurring in the system to support testing asynchronous behaviors in
 * components.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
class AppInventorTestCase: XCTestCase {
  var form: TestForm!
  var interpreter: SCMInterpreter!
  var expectations = [XCTestExpectation]()

  open override func setUp() {
    do {
      interpreter = try getInterpreterForTesting()
      form = TestForm(interpreter)
      form.Initialize()
    } catch {
      XCTFail()
    }
  }

  /**
   * Asserts that the given component receives the event named. An optional check function can be
   * given to make further assertions when the event is run.
   *
   * @param component The component of interest
   * @param eventName The event that is expected to be fired on {@code component}
   * @param checker An optional callback that will be run when the event fires that can be used to
   *                perform additional assertions.
   */
  open func expectToReceiveEvent(on component: Component, named eventName: String, checker: CheckFunc? = nil) {
    EventDispatcher.registerEventForDelegation(component.dispatchDelegate!, String(describing: component), eventName)
    let desc = EventDescription(component, eventName)
    var callbacks = form.checkerMap[desc] ?? [CheckFunc]()
    let expect = expectation(description: "\(component)$\(eventName)")
    callbacks.append({(_ arguments: [AnyObject]) in
      if let checker = checker {
        checker(arguments)
      }
      expect.fulfill()
    })
    expectations.append(expect)
    form.checkerMap[desc] = callbacks
  }

  /**
   * Asserts that the given component should not receive the event named.
   *
   * Example:
   * <pre>
   * expectNotToRecieve(form, "ErrorOccurred")
   * form.dispatchErrorEventOccurred(form, "Test",
   *   Int32(ErrorMessage.ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED.rawValue))
   * verify() // test will fail because form received the "ErrorOccurred" event
   * </pre>
   *
   * @param component The component of interest
   * @param eventName The name of the event that {@code component} should not receive.
   */
  open func expectNotToReceiveEvent(on component: Component, named eventName: String) {
    EventDispatcher.registerEventForDelegation(component.dispatchDelegate!, String(describing: component), eventName)
    let desc = EventDescription(component, eventName);
    var callbacks = form.checkerMap[desc] ?? [CheckFunc]()
    callbacks.append({(_ arguments: [AnyObject]) -> Void in
      XCTFail("Received \(eventName) on component \(component) but this event was not expected.");
      })
    form.checkerMap[desc] = callbacks
  }

  /**
   * Verifies that the event expectations were fulfilled.
   *
   * @param timeout An optional timeout value to use, in seconds [default = 0.5]
   */
  open func verify(timeout: TimeInterval = 0.5) {
    wait(for: expectations, timeout: timeout)

    // Clean up expectations otherwise the test framework will throw an exception due to repeat use
    expectations.removeAll()
    form.checkerMap.removeAll()
  }

  /**
   * Adds the given `component` to the `form` used for testing.
   *
   * - Parameter component: The component instance to register. It should have `form` in its
   *                        ancestor hierarchy.
   * - Parameter name: The name of the component.
   */
  open func addComponent(_ component: Component, named name: String) -> Bool {
    interpreter.setValue(component, forSymbol: name)
    interpreter.evalForm("(add-to-current-form-environment \"\(name)\" \(name))")
    if interpreter.exception == nil {
      form.testComponents[name] = component
      return true
    }
    return false
  }

  open override func tearDown() {
    EventDispatcher.unregisterAllEventsForDelegation()
  }
}
