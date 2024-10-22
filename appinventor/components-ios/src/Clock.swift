// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * Creates a `DateFormatter` object for the given `str`.
 *
 * @param str The date format string
 * @returns A new DateFormatter object configured with the given string
 */
fileprivate func makeFormat(_ str: String) -> DateFormatter {
  let date = DateFormatter()
  date.locale = Locale.preferredLanguage
  date.dateFormat = str
  return date
}

open class Clock: NonvisibleComponent, LifecycleDelegate {
  /**
   * The list of valid date formatters for processing strings into dates.
   */
  private static let DATE_FORMATTERS = [
    makeFormat("MM/dd/yyyy hh:mm:ss a"),
    makeFormat("MM/dd/yyyy HH:mm:ss"),
    makeFormat("MM/dd/yyyy hh:mm a"),
    makeFormat("MM/dd/yyyy HH:mm"),
    makeFormat("MM/dd/yyyy"),
    makeFormat("hh:mm:ss a"),
    makeFormat("HH:mm:ss"),
    makeFormat("hh:mm a"),
    makeFormat("HH:mm")
  ]

  fileprivate var _timer: Timer?
  fileprivate var _interval: Int32 = 1000
  fileprivate var _enabled = false
  fileprivate var _alwaysFires = true
  fileprivate var _onScreen = false
  fileprivate var _calendar = Calendar.current

  public override init(_ container: ComponentContainer) {
    super.init(container)
    TimerEnabled = true
    if container is ReplForm {
      // If we are in the REPL then we may not get an onResume call as the REPL is already active
      _onScreen = true
    }
  }

  // MARK: Clock Properties
  @objc open var TimerInterval: Int32 {
    get {
      return _interval
    }
    set(interval) {
      _interval = interval
      restartTimer()
    }
  }

  @objc open var TimerEnabled: Bool {
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

  @objc open var TimerAlwaysFires: Bool {
    get {
      return _alwaysFires
    }
    set(always) {
      _alwaysFires = always
    }
  }

  // MARK: Clock Methods
  @objc open func SystemTime() -> Int64 {
    return Int64(Date().timeIntervalSince1970 * 1000.0)
  }

  @objc open func Now() -> Date {
    return Date()
  }
  
  @objc open func MakeInstant(_ from: String) throws -> Date {
    guard let date = dateParser(from) else {
      _form?.dispatchErrorOccurredEvent(self, "MakeInstant",
          ErrorMessage.ERROR_ILLEGAL_DATE.code,
          ErrorMessage.ERROR_ILLEGAL_DATE.message)
      throw YailRuntimeError("Argument to MakeInstant should have form MM/DD/YYYY hh:mm:ss, or MM/DD/YYYY or hh:mm", "Sorry to be so picky.")
    }
    
    return date
  }
  
  @objc public func MakeInstantFromMillis(_ millis: Int64) -> Date {
    return Date(timeIntervalSince1970: TimeInterval(Double(millis) / 1000.0))
  }

  @objc open func MakeDate(_ year: Int32, _ month: Int32, _ day: Int32) throws -> Date {
    var dateComponents = DateComponents()
    dateComponents.year = Int(year)
    dateComponents.month = Int(month)
    dateComponents.day = Int(day)

    guard let instant = _calendar.date(from: dateComponents) else {
      _form?.dispatchErrorOccurredEvent(self, "MakeDate",
          ErrorMessage.ERROR_ILLEGAL_DATE.code,
          ErrorMessage.ERROR_ILLEGAL_DATE.message)
      throw YailRuntimeError("Argument to MakeDate should have month from 1-12 and 1-31 for day field.", "Sorry to be so picky.")
    }

    return instant
  }

  @objc open func MakeTime(_ hour: Int32, _ minute: Int32, _ second: Int32) throws -> Date {
    /// NOTE: Android implementation uses today's date for the given date.

    var dateComponents = _calendar.dateComponents([.month, .day, .year], from: Date())
    dateComponents.hour = Int(hour)
    dateComponents.minute = Int(minute)
    dateComponents.second = Int(second)

    guard let instant = _calendar.date(from: dateComponents) else {
      _form?.dispatchErrorOccurredEvent(self, "MakeTime",
          ErrorMessage.ERROR_ILLEGAL_DATE.code,
          ErrorMessage.ERROR_ILLEGAL_DATE.message)
      throw YailRuntimeError("Argument to MakeTime was not valid.", "Sorry to be so picky.")
    }

    return instant
  }

  @objc open func MakeInstantFromParts(_ year: Int32, _ month: Int32, _ day: Int32, _ hour: Int32, _ minute: Int32, _ second: Int32) throws -> Date {
    var dateComponents = DateComponents()
    dateComponents.year = Int(year)
    dateComponents.month = Int(month)
    dateComponents.day = Int(day)
    dateComponents.hour = Int(hour)
    dateComponents.minute = Int(minute)
    dateComponents.second = Int(second)

    guard let instant = _calendar.date(from: dateComponents) else {
      _form?.dispatchErrorOccurredEvent(self, "MakeInstantFromParts",
          ErrorMessage.ERROR_ILLEGAL_DATE.code,
          ErrorMessage.ERROR_ILLEGAL_DATE.message)
      throw YailRuntimeError("Argument to MakeDate should have month from 1-12 and 1-31 for day field.", "Sorry to be so picky.")
    }

    return instant
  }
  
  @objc open func GetMillis(_ instant: Date) -> Int64 {
    return Int64(instant.timeIntervalSince1970 * 1000)
  }
  
  @objc open func AddDuration(_ instant: Date, _ quantity: Int64) -> Date {
    let _newInstant = instant
    return _newInstant.addingTimeInterval(TimeInterval(Double(quantity)/1000.0))
  }
  
  @objc open func AddSeconds(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .second, value: Int(quantity), to: instant)!
  }
  
  @objc open func AddMinutes(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .minute, value: Int(quantity), to: instant)!
  }
  
  @objc open func AddHours(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .hour, value: Int(quantity), to: instant)!
  }
  
  @objc open func AddDays(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .day, value: Int(quantity), to: instant)!
  }
  
  @objc open func AddWeeks(_ instant: Date, _ quantity: Int64) -> Date {
    return AddDays(instant, quantity * 7)
  }
  
  @objc open func AddMonths(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .month, value: Int(quantity), to: instant)!
  }
  
  @objc open func AddYears(_ instant: Date, _ quantity: Int64) -> Date {
    return _calendar.date(byAdding: .year, value: Int(quantity), to: instant)!
  }
  
  @objc open func Duration(_ start: Date, _ end: Date) -> Int64 {
    let diff = end.timeIntervalSince(start)
    let result = Int64(diff * 1000.0)
    return result
  }
  
  @objc open func DurationToSeconds(_ duration: Int64) -> Int64 {
    return duration/1000
  }
  
  @objc open func DurationToMinutes(_ duration: Int64) -> Int64 {
    return DurationToSeconds(duration)/60
  }
  
  @objc open func DurationToHours(_ duration: Int64) -> Int64 {
    return DurationToMinutes(duration)/60
  }
  
  @objc open func DurationToDays(_ duration: Int64) -> Int64 {
    return DurationToHours(duration) / 24
  }
  
  @objc open func DurationToWeeks(_ duration: Int64) -> Int64 {
    return DurationToDays(duration) / 7
  }
  
  @objc open func Second(_ instant: Date) -> Int {
    return _calendar.component(.second, from: instant)
  }
  
  @objc open func Minute(_ instant: Date) -> Int {
    return _calendar.component(.minute, from: instant)
  }
  
  @objc open func Hour(_ instant: Date) -> Int {
    return _calendar.component(.hour, from: instant)
  }
  
  @objc open func DayOfMonth(_ instant: Date) -> Int {
    return _calendar.component(.day, from: instant)
  }
  
  @objc open func Weekday(_ instant: Date) -> Int {
    return _calendar.component(.weekday, from: instant)
  }
  
  @objc open func WeekdayName(_ instant: Date) -> String {
    let dayName = makeFormat("EEEE")
    return dayName.string(from: instant)
  }
  
  @objc open func Month(_ instant: Date) -> Int {
    return _calendar.component(.month, from: instant)
  }
  
  @objc open func MonthName(_ instant: Date) -> String {
    let dayName = makeFormat("LLLL")
    return dayName.string(from: instant)
  }
  
  @objc open func Year(_ instant: Date) -> Int {
    return _calendar.component(.year, from: instant)
  }
  
  @objc open func FormatDateTime(_ instant: Date, _ pattern: String) -> String {
    let dateTimeFormatter = makeFormat(pattern)
    return dateTimeFormatter.string(from: instant)
  }
  
  @objc open func FormatDate(_ instant: Date, _ pattern: String) -> String {
    let dateFormatter = makeFormat(pattern)
    return dateFormatter.string(from: instant)
  }
  
  @objc open func FormatTime(_ instant: Date) -> String {
    let dateFormatter = makeFormat("h:mm:ss a")
    return dateFormatter.string(from: instant)
  }
  
  private func dateParser(_ from: String) -> Date? {
    for format in Clock.DATE_FORMATTERS {
      if let date = format.date(from: from) {
        return date
      }
    }
    return nil
  }
  
  // MARK: Clock Events
  @objc open func Timer() {
    if (_alwaysFires || _onScreen) {
      EventDispatcher.dispatchEvent(of: self, called: "Timer")
    }
  }

  // MARK: LifecycleDelegate implementation
  @objc public func onPause() {
    _onScreen = false
  }

  @objc public func onResume() {
    _onScreen = true
  }

  // MARK: Private implementation
  @objc func timerFired(_ timer: Timer) {
    self.performSelector(onMainThread: #selector(self.Timer), with: nil, waitUntilDone: false)
  }

  @objc func restartTimer() {
    _timer?.invalidate()
    if _enabled {
      _timer = Foundation.Timer(timeInterval: TimeInterval(Double(_interval) / 1000.0), target: self, selector: #selector(self.timerFired(_:)), userInfo: nil, repeats: true)
      RunLoop.main.add(_timer!, forMode: RunLoop.Mode.default)
    } else {
      _timer = nil
    }
  }
}
