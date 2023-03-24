//
//  MockComponent.swift
//  SchemeKit
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

@objc class MockContainer: NSObject, NSCopying {
  public func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
}

@objc class MockComponent: NSObject, NSCopying {
  private var _a: Bool = false
  private var _b: Int = 0
  private var _c: Double = 0.0
  private var _d: String = ""
  private var _e: NSObject = NSObject()
  private let _parent: MockContainer

  @objc public init(_ parent: MockContainer) {
    _parent = parent
    super.init()
  }

  @objc public var PropertyA: Bool {
    get {
      return _a
    }
    set(value) {
      _a = value
    }
  }
  
  @objc public var PropertyB: Int {
    get {
      return _b
    }
    set(value) {
      _b = value
    }
  }

  @objc public var PropertyC: Double {
    get {
      return _c
    }
    set(value) {
      _c = value
    }
  }
  
  @objc public var PropertyD: String {
    get {
      return _d
    }
    set(value) {
      _d = value
    }
  }
  
  @objc public var PropertyE: NSObject {
    get {
      return _e
    }
    set(value) {
      _e = value
    }
  }
  
  @objc public func DoStuff(coolBool: Bool) {
    NSLog("Did stuff with bool = \(coolBool)")
  }
  
  @objc public func DoStuff(coolInt: Int) {
    NSLog("Did stuff with int = \(coolInt)")
  }
  
  @objc public func DoStuff(coolDouble: Double) {
    NSLog("Did stuff with int = \(coolDouble)")
  }
  
  @objc public func DoStuff(coolString: String) {
    NSLog("Did stuff with string = \(coolString)")
  }
  
  @objc public func DoStuff(coolObject: String) {
    NSLog("Did stuff with object = \(coolObject)")
  }
  
  @objc public func DoStuff(coolComponent: MockComponent) {
    NSLog("Did stuff with component = \(coolComponent)")
  }
  
  @objc public func DoStuff(_ stuff: MockComponent, times: Int) {
    NSLog("Did stuff \(times) with component = \(stuff)")
  }
  
  @objc public func ConnectWithUUID(uuid: String) {
    NSLog("Did connect with UUID = \(uuid)")
  }
  
  @objc public func ConnectWithUUID(_ uuid: String) {
    NSLog("Did connect with UUID = \(uuid)")
  }
  
  @objc public func Connect(anyObject: AnyObject) {
    NSLog("Did connect with object = \(anyObject)")
  }
  
  @objc public func Connect(any: Any) {
    NSLog("Did connect with any = \(any)")
  }
  
  @objc public func Connect(times: NSNumber) {
    NSLog("Did connect with number = \(times)")
  }

  @objc public static func makeMockComponent(_ parent: MockContainer) -> MockComponent {
    return MockComponent(parent);
  }

  @objc public func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
}
