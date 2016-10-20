//
//  MockComponent.swift
//  SchemeKit
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

@objc class MockContainer: NSObject {
}

@objc class MockComponent: NSObject {
  private var _a: Bool = false
  private var _b: Int = 0
  private var _c: Double = 0.0
  private var _d: String = ""
  private var _e: NSObject = NSObject()
  private let _parent: MockContainer

  public init(_ parent: MockContainer) {
    _parent = parent
    super.init()
  }

  public var PropertyA: Bool {
    get {
      return _a
    }
    set(value) {
      _a = value
    }
  }
  
  public var PropertyB: Int {
    get {
      return _b
    }
    set(value) {
      _b = value
    }
  }

  public var PropertyC: Double {
    get {
      return _c
    }
    set(value) {
      _c = value
    }
  }
  
  public var PropertyD: String {
    get {
      return _d
    }
    set(value) {
      _d = value
    }
  }
  
  public var PropertyE: NSObject {
    get {
      return _e
    }
    set(value) {
      _e = value
    }
  }
  
  public func DoStuff(coolBool: Bool) {
    NSLog("Did stuff with bool = \(coolBool)")
  }
  
  public func DoStuff(coolInt: Int) {
    NSLog("Did stuff with int = \(coolInt)")
  }
  
  public func DoStuff(coolDouble: Double) {
    NSLog("Did stuff with int = \(coolDouble)")
  }
  
  public func DoStuff(coolString: String) {
    NSLog("Did stuff with string = \(coolString)")
  }
  
  public func DoStuff(coolObject: String) {
    NSLog("Did stuff with object = \(coolObject)")
  }
  
  public func DoStuff(coolComponent: MockComponent) {
    NSLog("Did stuff with component = \(coolComponent)")
  }
  
  public func DoStuff(_ stuff: MockComponent, times: Int) {
    NSLog("Did stuff \(times) with component = \(stuff)")
  }
  
  public func ConnectWithUUID(uuid: String) {
    NSLog("Did connect with UUID = \(uuid)")
  }
  
  public func ConnectWithUUID(_ uuid: String) {
    NSLog("Did connect with UUID = \(uuid)")
  }
  
  public func Connect(anyObject: AnyObject) {
    NSLog("Did connect with object = \(anyObject)")
  }
  
  public func Connect(any: Any) {
    NSLog("Did connect with any = \(any)")
  }
  
  public func Connect(times: NSNumber) {
    NSLog("Did connect with number = \(times)")
  }

  public static func makeMockComponent(_ parent: MockContainer) -> MockComponent {
    return MockComponent(parent);
  }
}
