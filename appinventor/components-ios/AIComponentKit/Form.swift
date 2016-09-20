//
//  Form.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import UIKit

public class Form: UIKit.UIViewController, Component, ComponentContainer, HandlesEventDispatching {
  private let TAG = "Form"
  private let RESULT_NAME = "APP_INVENTOR_RESULT"
  private let ARGUMENT_NAME = "APP_INVENTOR_START"
  public let APPINVENTOR_URL_SCHEME = "appinventor"
  var activeForm: Form?
  private var deviceDensity: Float?
  private var compatScalingFactor: Float?
  private var applicationIsBeingClosed = false
  var formName: String?
  private var screenInitialized = false
  
  public func canDispatchEvent(of component: Component, called eventName: String) -> Bool {
    // TODO(ewpatton): Implementation
    return false
  }
  
  public func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    // TODO(ewpatton): Implementation
    return false
  }

  public var dispatchDelegate: HandlesEventDispatching {
    get {
      return self
    }
  }
  
  public var form: Form {
    get {
      return self
    }
  }

  public func add(component: ViewComponent) {
    // TODO(ewpatton): Implementation
  }
  
  public func setChildWidth(of component: ViewComponent, width: Int32) {
    // TODO(ewpatton): Implementation
  }
  
  public func setChildHeight(of component: ViewComponent, height: Int32) {
    // TODO(ewpatton): Implementation
  }
  
  public var Width: Int32 {
    get {
      // TODO(ewpatton): Implementation
      return 0
    }
    set {
      // TODO(ewpatton): Implementation
    }
  }
  
  public var Height: Int32 {
    get {
      // TODO(ewpatton): Implementation
      return 0
    }
    set {
      // TODO(ewpatton): Implementation
    }
  }
}
