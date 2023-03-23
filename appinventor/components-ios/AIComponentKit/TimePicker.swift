// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * A TimePicker class.  A component that allows the user to select a time (hour, minute).
 * This component is a button that launches a TimePickerViewController (specific to the device).
 */
open class TimePicker: Picker, DateTimePickerDelegate {
  fileprivate var _viewController: DateTimePickerController?
  fileprivate var _hour, _minute: Int
  fileprivate var _instant: Date
  fileprivate var _customTime = false
  fileprivate let _isPhone = UIDevice.current.userInterfaceIdiom == .phone
  
  public override init(_ parent: ComponentContainer) {
    let calendar = Calendar.current
    let now = Date()
    let dateComponents = calendar.dateComponents([.day, .month, .year, .hour, .minute, .timeZone], from: now)
    _hour = dateComponents.hour!
    _minute = dateComponents.minute!
    _instant = calendar.date(from: dateComponents)!
    
    super.init(parent)
    super.setDelegate(self)
    
    _viewController = getDateTimePickerController(self, screen: parent.form!, isDatePicker: false, isPhone: _isPhone)
    _viewController?.pickerView.setValue(preferredTextColor(parent.form), forKeyPath: "textColor")
    _viewController?.setDateTime(calendar)
    _view.addTarget(self, action: #selector(click), for: UIControl.Event.primaryActionTriggered)
  }
  
  // MARK: DatePicker Properties
  // hour is in 24 hour format -- 11pm returns 23
  @objc open var Hour: Int {
    get {
      return _hour
    }
  }
  
  @objc open var Minute: Int {
    get {
      return _minute
    }
  }
  
  // the instant of the time that was last picked
  @objc open var Instant: Date {
    get {
      return _instant
    }
  }
  
  // MARK: TimePicker Events
  @objc open func SetTimeToDisplay(_ hour: Int32, _ minute: Int32) {
    if !(0...23 ~= hour) {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTimeToDisplay",
          ErrorMessage.ERROR_ILLEGAL_HOUR.code, ErrorMessage.ERROR_ILLEGAL_HOUR.message)
    } else if !(0...59 ~= minute) {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetTimeToDisplay",
          ErrorMessage.ERROR_ILLEGAL_MINUTE.code, ErrorMessage.ERROR_ILLEGAL_MINUTE.message)
    } else {
      var timeComponents = DateComponents()
      timeComponents.hour = Int(hour)
      timeComponents.minute = Int(minute)
      let time = Calendar.current.date(from: timeComponents)!
      _instant = time
      _viewController?.setTime(_instant)
      _customTime = true
    }
  }
  
  @objc open func SetTimeToDisplayFromInstant(_ instant: Date) {
    let timeComponents = Calendar.current.dateComponents([.day, .month, .year, .hour, .minute, .timeZone], from: instant)
    _instant = Calendar.current.date(from: timeComponents)!
    self._viewController?.setTime(_instant)
    _customTime = true
  }
  
  @objc open func LaunchPicker() {
    click()
  }
  
  @objc open func dateTimePicked(_ date: Date) {
    let calendar = Calendar.current
    let timeComponents = calendar.dateComponents([.day, .month, .year, .hour, .minute, .timeZone], from: date)
    
    _hour = timeComponents.hour!
    _minute = timeComponents.minute!
    _instant = calendar.date(from: timeComponents)!
    
    AfterTimeSet()
  }
  
  @objc open func AfterTimeSet() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterTimeSet")
  }
  
  open override func click() {
    if !_customTime {
      _instant = Date()
      _viewController?.setTime(_instant)
    } else {
      _customTime = false
    }
    if !_isPhone {
      if let popover = (_viewController as! UIViewController).popoverPresentationController {
        popover.delegate = self
        popover.sourceView = _view
        popover.sourceRect = _view.frame
      }
    }
    _container?.form?.present(_viewController as! UIViewController, animated: true)
  }
}

