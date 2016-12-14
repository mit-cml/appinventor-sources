//
//  Clock.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

open class Clock: NonvisibleComponent {
  fileprivate var _timer: Timer?
  fileprivate var _interval: Int32 = 1000
  fileprivate var _enabled = true
  fileprivate var _alwaysFires = true
  fileprivate var _onScreen = false

  public override init(_ container: ComponentContainer) {
    super.init(container)
    TimerEnabled = true
  }

  // MARK: Clock Properties
  open var TimerInterval: Int32 {
    get {
      return _interval
    }
    set(interval) {
      _interval = interval
    }
  }

  open var TimerEnabled: Bool {
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
        RunLoop.main.add(_timer!, forMode: .defaultRunLoopMode)
      } else if let timer = _timer {
        timer.invalidate()
        _timer = nil
      }
    }
  }

  open var TimeAlwaysFires: Bool {
    get {
      return _alwaysFires
    }
    set(always) {
      _alwaysFires = always
    }
  }

  // MARK: Clock Methods
  open func SystemTime() -> Int64 {
    return Int64(Date().timeIntervalSince1970 * 1000.0)
  }

  open func Now() -> Date {
    return Date()
  }

  // MARK: Clock Events
  open func Timer() {
    if (_alwaysFires || _onScreen) {
      EventDispatcher.dispatchEvent(of: self, called: "Timer")
    }
  }

  func timerFired(_ timer: Timer) {
    self.performSelector(onMainThread: #selector(self.Timer), with: nil, waitUntilDone: false)
  }

  // MARK: Clock class functions
  open class func FormatDate(_ date: Date, _ format: String) -> String {
    let formatter = DateFormatter()
    formatter.dateFormat = format
    return formatter.string(from: date)
  }
}
