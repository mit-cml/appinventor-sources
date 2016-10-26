//
//  EventDispatcher.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/26/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

func ==(lhs: EventDispatcher.EventClosure, rhs: EventDispatcher.EventClosure) -> Bool {
  return lhs.componentId == rhs.componentId && lhs.eventName == rhs.eventName
}

public class EventDispatcher: NSObject {
  private static let mapDispatchDelegateToEventRegistry = NSMutableDictionary()
  private override init() {}
  
  internal class EventClosure: Hashable {
    internal let componentId: String
    internal let eventName: String
    
    init(componentId: String, eventName: String) {
      self.componentId = componentId
      self.eventName = eventName
    }
  
    public var hashValue: Int {
      get {
        return componentId.hashValue &+ 31 &* eventName.hashValue
      }
    }
  }
  
  private class EventRegistry {
    internal let dispatchDelegate: HandlesEventDispatching
    internal var eventClosuresMap = [String: Set<EventClosure>]()
    
    internal init(dispatchDelegate: HandlesEventDispatching) {
      self.dispatchDelegate = dispatchDelegate
    }
  }
  
  private class func getEventRegistry(dispatchDelegate: HandlesEventDispatching) -> EventRegistry {
    var er = mapDispatchDelegateToEventRegistry[dispatchDelegate] as! EventRegistry?
    if er == nil {
      er = EventRegistry(dispatchDelegate: dispatchDelegate)
      mapDispatchDelegateToEventRegistry[dispatchDelegate] = er
    }
    return er!
  }
  
  private class func removeEventRegistry(dispatchDelegate: HandlesEventDispatching) -> EventRegistry? {
    let er = mapDispatchDelegateToEventRegistry[dispatchDelegate] as! EventRegistry?
    if er != nil {
      mapDispatchDelegateToEventRegistry.removeObject(forKey: dispatchDelegate)
    }
    return er
  }

  @discardableResult
  public class func dispatchEvent(of component: Component, called eventName: String, arguments: AnyObject...) -> Bool {
    NSLog("EventDispatcher: Trying to dispatch event \(eventName)")
    let dispatchDelegate = component.dispatchDelegate
    if (dispatchDelegate.canDispatchEvent(of: component, called: eventName)) {
      let er = mapDispatchDelegateToEventRegistry[dispatchDelegate] as! EventRegistry?
      let eventClosures: Set<EventClosure>? = er?.eventClosuresMap[eventName]
      if eventClosures != nil && (eventClosures?.count)! > 0 {
        return delegateDispatchEvent(to: dispatchDelegate, withClosures: eventClosures!, forComponent: component, arguments: arguments)
      }
    }
    return false
  }
  
  private class func delegateDispatchEvent(to dispatchDelegate: HandlesEventDispatching, withClosures closures: Set<EventClosure>, forComponent component: Component, arguments: [AnyObject]) -> Bool {
    var dispatched = false
    for eventClosure in closures {
      if (dispatchDelegate.dispatchEvent(of: component, called: eventClosure.componentId, with: eventClosure.eventName, having: arguments)) {
        dispatched = true
      }
    }
    return dispatched
  }
  
  public class func registerEventForDelegation(_ dispatchDelegate: HandlesEventDispatching, _ componentName: String, _ eventName: String) {
    let er = getEventRegistry(dispatchDelegate: dispatchDelegate)
    var eventClosures = er.eventClosuresMap[eventName]
    if eventClosures == nil {
      eventClosures = Set<EventClosure>()
      er.eventClosuresMap[eventName] = eventClosures
    }
    _ = eventClosures?.insert(EventClosure(componentId: componentName, eventName: eventName))
    // FIXME: For some reason it appears sets are copy-on-write so it isn't enough to insert the element into the existing set
    er.eventClosuresMap[eventName] = eventClosures
  }
  
  public class func unregisterForEventDelegation(_ dispatchDelegate: HandlesEventDispatching, _ componentName: String, _ eventName: String) {
    let er = getEventRegistry(dispatchDelegate: dispatchDelegate)
    let eventClosures = er.eventClosuresMap[eventName]
    if eventClosures == nil || eventClosures?.count == 0 {
      return
    }
    var closures = eventClosures!
    closures.subtract(closures.filter({ (closure) -> Bool in
      return closure.componentId == componentName
    }))
  }
  
  public class func unregisterAllEventsForDelegation() {
    for er in mapDispatchDelegateToEventRegistry.allValues {
      if er is EventRegistry {
        (er as! EventRegistry).eventClosuresMap.removeAll()
      }
    }
  }
  
  public class func removeDispatchDelegate(_ dispatchDelegate: HandlesEventDispatching) {
    let er = removeEventRegistry(dispatchDelegate: dispatchDelegate)
    if er != nil {
      er?.eventClosuresMap.removeAll()
    }
  }
}
