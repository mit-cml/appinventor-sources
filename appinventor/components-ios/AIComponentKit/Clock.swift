//
//  Clock.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class Clock: NonvisibleComponent {
  private var _timer: Timer?
  private var _interval: Int32 = 1000
  private var _enabled = true
  private var _alwaysFires = true
  private var _onScreen = false

  public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  // MARK: Clock Properties
  public var TimerInterval: Int32 {
    get {
      return _interval
    }
    set(interval) {
      _interval = interval
    }
  }

  public var TimerEnabled: Bool {
    get {
      return _enabled
    }
    set(enabled) {
      _enabled = enabled
      if _enabled {
        if let timer = _timer {
          timer.invalidate()
        }
        _timer = Foundation.Timer(timeInterval: TimeInterval(Double(_interval) / 1000.0), target: self, selector: #selector(self.timerFired(_:)), userInfo: nil, repeats: true)
      } else if let timer = _timer {
        timer.invalidate()
        _timer = nil
      }
    }
  }

  public var TimeAlwaysFires: Bool {
    get {
      return _alwaysFires
    }
    set(always) {
      _alwaysFires = always
    }
  }

  // MARK: Clock Methods
  public func SystemTime() -> Int64 {
    return Int64(Date().timeIntervalSince1970 * 1000.0)
  }

  public func Now() -> Date {
    return Date()
  }

  // MARK: Clock Events
  public func Timer() {
    if (_alwaysFires || _onScreen) {
      EventDispatcher.dispatchEvent(of: self, called: "Timer")
    }
  }

  func timerFired(_ timer: Timer) {
    self.performSelector(onMainThread: #selector(self.Timer), with: nil, waitUntilDone: false)
  }
}
