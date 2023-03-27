// -*- mode: swift; swift-mode:basic-offset 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class EventDispatcher: NSObject {
  fileprivate static let mapDispatchDelegateToEventRegistry = NSMutableDictionary()
  fileprivate override init() {}
  
  internal class EventClosure: Hashable {
    internal let componentId: String
    internal let eventName: String
    
    init(componentId: String, eventName: String) {
      self.componentId = componentId
      self.eventName = eventName
    }
  
    open var hashValue: Int {
      get {
        return componentId.hashValue &+ 31 &* eventName.hashValue
      }
    }

    func hash(into hasher: inout Hasher) {
      hasher.combine(componentId)
      hasher.combine(eventName)
    }

    static func == (lhs: EventClosure, rhs: EventClosure) -> Bool {
      return lhs.componentId == rhs.componentId && lhs.eventName == rhs.eventName
    }
  }
  
  fileprivate class EventRegistry {
    internal let dispatchDelegate: HandlesEventDispatching
    internal var eventClosuresMap = [String: Set<EventClosure>]()
    
    internal init(dispatchDelegate: HandlesEventDispatching) {
      self.dispatchDelegate = dispatchDelegate
    }
  }
  
  fileprivate class func getEventRegistry(_ dispatchDelegate: HandlesEventDispatching) -> EventRegistry {
    var er = mapDispatchDelegateToEventRegistry[dispatchDelegate] as! EventRegistry?
    if er == nil {
      er = EventRegistry(dispatchDelegate: dispatchDelegate)
      mapDispatchDelegateToEventRegistry[dispatchDelegate] = er
    }
    return er!
  }
  
  fileprivate class func removeEventRegistry(_ dispatchDelegate: HandlesEventDispatching) -> EventRegistry? {
    let er = mapDispatchDelegateToEventRegistry[dispatchDelegate] as! EventRegistry?
    if er != nil {
      mapDispatchDelegateToEventRegistry.removeObject(forKey: dispatchDelegate)
    }
    return er
  }

  @discardableResult
  open class func dispatchEvent(of component: Component, called eventName: String, arguments: AnyObject...) -> Bool {
    guard let dispatchDelegate = component.dispatchDelegate else {
      return false
    }
    var dispatched = false
    if (dispatchDelegate.canDispatchEvent(of: component, called: eventName)) {
      let er = mapDispatchDelegateToEventRegistry[dispatchDelegate] as! EventRegistry?
      let eventClosures: Set<EventClosure>? = er?.eventClosuresMap[eventName]
      if eventClosures != nil && (eventClosures?.count)! > 0 {
        dispatched = delegateDispatchEvent(to: dispatchDelegate, withClosures: eventClosures!, forComponent: component, arguments: arguments)
      }
      dispatchDelegate.dispatchGenericEvent(of: component, eventName: eventName, unhandled: !dispatched, arguments: arguments)
    }
    return dispatched
  }
  
  fileprivate class func delegateDispatchEvent(to dispatchDelegate: HandlesEventDispatching, withClosures closures: Set<EventClosure>, forComponent component: Component, arguments: [AnyObject]) -> Bool {
    var dispatched = false
    for eventClosure in closures {
      if (dispatchDelegate.dispatchEvent(of: component, called: eventClosure.componentId, with: eventClosure.eventName, having: arguments)) {
        dispatched = true
      }
    }
    return dispatched
  }
  
  @objc open class func registerEventForDelegation(_ dispatchDelegate: HandlesEventDispatching, _ componentName: String, _ eventName: String) {
    let er = getEventRegistry(dispatchDelegate)
    var eventClosures = er.eventClosuresMap[eventName]
    if eventClosures == nil {
      eventClosures = Set<EventClosure>()
      er.eventClosuresMap[eventName] = eventClosures
    }
    _ = eventClosures?.insert(EventClosure(componentId: componentName, eventName: eventName))
    // FIXME: For some reason it appears sets are copy-on-write so it isn't enough to insert the element into the existing set
    er.eventClosuresMap[eventName] = eventClosures
  }
  
  @objc open class func unregisterEventForDelegation(_ dispatchDelegate: HandlesEventDispatching, _ componentName: String, _ eventName: String) {
    let er = getEventRegistry(dispatchDelegate)
    guard let closures = er.eventClosuresMap[eventName], closures.count > 0 else {
      return
    }
    er.eventClosuresMap[eventName] = Set(closures).subtracting(closures.filter({ (closure) -> Bool in
      return closure.componentId == componentName
    }))
  }
  
  @objc open class func unregisterAllEventsForDelegation() {
    for er in mapDispatchDelegateToEventRegistry.allValues {
      if er is EventRegistry {
        (er as! EventRegistry).eventClosuresMap.removeAll()
      }
    }
  }
  
  @objc open class func removeDispatchDelegate(_ dispatchDelegate: HandlesEventDispatching) {
    let er = removeEventRegistry(dispatchDelegate)
    if er != nil {
      er?.eventClosuresMap.removeAll()
    }
  }
}
