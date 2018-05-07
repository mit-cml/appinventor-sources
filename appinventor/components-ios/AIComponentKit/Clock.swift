// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

open class Clock: NonvisibleComponent {
  fileprivate var _timer: Timer?
  fileprivate var _interval: Int32 = 1000
  fileprivate var _enabled = false
  fileprivate var _alwaysFires = true
  fileprivate var _onScreen = false
  fileprivate var _calendar = Calendar.current

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
      restartTimer()
    }
  }

  open var TimerEnabled: Bool {
    get {
      return _enabled
    }
    set(enabled) {
      if _enabled != enabled {
        _enabled = enabled
        restartTimer()
      }
    }
  }

  open var TimerAlwaysFires: Bool {
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
  
  open func MakeInstant(_ from: String) throws -> Date {
    guard let components = dateParser(from) else {
      throw YailRuntimeError("Argument to MakeInstant should have form MM/DD/YYYY hh:mm:ss, or MM/DD/YYYY or hh:mm", "Sorry to be so picky.")
    }
    
    return _calendar.date(from: components)!
  }
  
  open func MakeInstantFromMillis(_ millis: Int64) -> Date {
    return Date(timeIntervalSince1970: TimeInterval(millis / 1000))
  }
  
  open func GetMillis(_ instant: Date) -> Int64 {
    return Int64(instant.timeIntervalSince1970 * 1000)
  }
  
  open func AddDuration(_ instant: Date, _ quantity: Int64) -> Date {
    let _newInstant = instant
    return _newInstant.addingTimeInterval(TimeInterval(quantity/1000))
  }
  
  open func AddSeconds(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .second, value: Int(quantity), to: instant)!
  }
  
  open func AddMinutes(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .minute, value: Int(quantity), to: instant)!
  }
  
  open func AddHours(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .hour, value: Int(quantity), to: instant)!
  }
  
  open func AddDays(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .day, value: Int(quantity), to: instant)!
  }
  
  open func AddWeeks(_ instant: Date, _ quantity: Int64) -> Date {
    return AddDays(instant, quantity * 7)
  }
  
  open func AddMonths(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .month, value: Int(quantity), to: instant)!
  }
  
  open func AddYears(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .year, value: Int(quantity), to: instant)!
  }
  
  open func Duration(_ start: Date, _ end: Date) -> Int64 {
    return Int64(end.timeIntervalSince(start) * 1000.0)
  }
  
  open func DurationToSeconds(_ duration: Int64) -> Int64 {
    return duration/1000
  }
  
  open func DurationToMinutes(_ duration: Int64) -> Int64 {
    return DurationToSeconds(duration)/60
  }
  
  open func DurationToHours(_ duration: Int64) -> Int64 {
    return DurationToMinutes(duration)/60
  }
  
  open func DurationToDays(_ duration: Int64) -> Int64 {
    return DurationToHours(duration) / 24
  }
  
  open func DurationToWeeks(_ duration: Int64) -> Int64 {
    return DurationToDays(duration) / 7
  }
  
  open func Second(_ instant: Date) -> Int {
    return _calendar.component(.second, from: instant)
  }
  
  open func Minute(_ instant: Date) -> Int {
    return _calendar.component(.minute, from: instant)
  }
  
  open func Hour(_ instant: Date) -> Int {
    return _calendar.component(.hour, from: instant)
  }
  
  open func DayOfMonth(_ instant: Date) -> Int {
    return _calendar.component(.day, from: instant)
  }
  
  open func Weekday(_ instant: Date) -> Int {
    return _calendar.component(.weekday, from: instant)
  }
  
  open func WeekdayName(_ instant: Date) -> String {
    let dayName = DateFormatter()
    dayName.dateFormat = "EEEE"
    return dayName.string(from: instant)
  }
  
  open func Month(_ instant: Date) -> Int {
    return _calendar.component(.month, from: instant)
  }
  
  open func MonthName(_ instant: Date) -> String {
    let dayName = DateFormatter()
    dayName.dateFormat = "LLLL"
    return dayName.string(from: instant)
  }
  
  open func Year(_ instant: Date) -> Int {
    return _calendar.component(.year, from: instant)
  }
  
  open func FormatDateTime(_ instant: Date, _ pattern: String) -> String {
    let dateTimeFormatter = DateFormatter()
    dateTimeFormatter.dateFormat = pattern
    return dateTimeFormatter.string(from: instant)
  }
  
  open func FormatDate(_ instant: Date, _ pattern: String) -> String {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = pattern
    return dateFormatter.string(from: instant)
  }
  
  open func FormatTime(_ instant: Date) -> String {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "h:mm:ss a"
    return dateFormatter.string(from: instant)
  }
  
  private func dateParser(_ from: String) -> DateComponents? {
    let dayTimeFormat = DateFormatter()
    dayTimeFormat.dateFormat = "MM/dd/yyyy hh:mm:ss"
    let dayFormat = DateFormatter()
    dayFormat.dateFormat = "MM/dd/yyyy"
    let timeFormat = DateFormatter()
    timeFormat.dateFormat = "hh:mm"
    
    if let date = dayTimeFormat.date(from: from) {
      return _calendar.dateComponents([.month, .day, .year, .hour, .minute, .second], from: date)
    } else if let date = dayFormat.date(from: from) {
      return _calendar.dateComponents([.month, .day, .year], from: date)
    } else if let date = timeFormat.date(from: from) {
      return _calendar.dateComponents([.hour, .minute], from: date)
    } else {
      return nil
    }
  }
  
  // MARK: Clock Events
  open func Timer() {
    if (_alwaysFires || _onScreen) {
      EventDispatcher.dispatchEvent(of: self, called: "Timer")
    }
  }

  // MARK: Private implementation
  func timerFired(_ timer: Timer) {
    self.performSelector(onMainThread: #selector(self.Timer), with: nil, waitUntilDone: false)
  }

  func restartTimer() {
    _timer?.invalidate()
    if _enabled {
      _timer = Foundation.Timer(timeInterval: TimeInterval(Double(_interval) / 1000.0), target: self, selector: #selector(self.timerFired(_:)), userInfo: nil, repeats: true)
      RunLoop.main.add(_timer!, forMode: .defaultRunLoopMode)
    } else {
      _timer = nil
    }
  }
}
