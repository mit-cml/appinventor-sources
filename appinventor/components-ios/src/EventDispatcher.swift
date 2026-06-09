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
      SCMInterpreter.shared.evalForm("(set-this-form)")
      let er = mapDispatchDelegateToEventRegistry[dispatchDelegate] as! EventRegistry?
      if let eventClosures = er?.eventClosuresMap[eventName], eventClosures.count > 0 {
        dispatched = delegateDispatchEvent(to: dispatchDelegate, withClosures: eventClosures,
                                           forComponent: component, arguments: arguments)
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
    NSLog("EventDispatcher: registerEventForDelegation called for event '\(eventName)' delegate: \(dispatchDelegate) -- count before: \(mapDispatchDelegateToEventRegistry.count)")
    let er = getEventRegistry(dispatchDelegate)
    var eventClosures = er.eventClosuresMap[eventName]
    if eventClosures == nil {
      eventClosures = Set<EventClosure>()
      er.eventClosuresMap[eventName] = eventClosures
    }
    _ = eventClosures?.insert(EventClosure(componentId: componentName, eventName: eventName))
    // FIXME: For some reason it appears sets are copy-on-write so it isn't enough to insert the element into the existing set
    er.eventClosuresMap[eventName] = eventClosures
    NSLog("EventDispatcher: registered event '\(eventName)' for delegate: \(dispatchDelegate) -- count after: \(mapDispatchDelegateToEventRegistry.count)")
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
  
  /// Clears registered event names but does not release dispatch delegates from the registry.
  /// Production code reuses the same form; unit tests must call `removeDispatchDelegate` when
  /// discarding a form so the delegate (and its component tree) can be deallocated.
  @objc open class func unregisterAllEventsForDelegation() {
    for er in mapDispatchDelegateToEventRegistry.allValues {
      if er is EventRegistry {
        (er as! EventRegistry).eventClosuresMap.removeAll()
      }
    }
  }

  /// Number of dispatch delegates currently held in the event registry (for unit tests).
  internal static var registeredDispatchDelegateCount: Int {
    return mapDispatchDelegateToEventRegistry.count
  }
  
  /// Diagnostic helper that logs and returns the current registry count.
  @objc open class func debugRegistryCount() -> Int {
    let c = mapDispatchDelegateToEventRegistry.count
    NSLog("EventDispatcher: debugRegistryCount = %d", c)
    return c
  }
  
  @objc open class func removeDispatchDelegate(_ dispatchDelegate: HandlesEventDispatching) {
    NSLog("EventDispatcher: removeDispatchDelegate called for delegate: \(dispatchDelegate) -- count before: \(mapDispatchDelegateToEventRegistry.count)")
    let er = removeEventRegistry(dispatchDelegate)
    if er != nil {
      er?.eventClosuresMap.removeAll()
    }
    NSLog("EventDispatcher: removeDispatchDelegate completed for delegate: \(dispatchDelegate) -- count after: \(mapDispatchDelegateToEventRegistry.count)")
  }
}
