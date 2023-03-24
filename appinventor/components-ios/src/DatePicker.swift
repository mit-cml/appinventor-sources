// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * A DatePicker class.  A component that allows the user to select a date (month, day, year).
 * This component is a button that launches a DatePickerViewController (specific to the device).
 */
open class DatePicker: Picker, DateTimePickerDelegate {
  fileprivate var _viewController: DateTimePickerController?
  fileprivate var _year, _month, _day: Int
  fileprivate var _instant: Date
  fileprivate var _localizedMonths: [String]
  fileprivate var _customDate = false
  fileprivate let _isPhone = UIDevice.current.userInterfaceIdiom == .phone
  
  public override init(_ parent: ComponentContainer) {
    let calendar = Calendar.current
    let now = Date()
    _localizedMonths = calendar.monthSymbols
    _year = calendar.component(.year, from: now)
    _month = calendar.component(.month, from: now)
    _day = calendar.component(.day, from: now)
    _instant = calendar.date(bySettingHour: 0, minute: 0, second: 0, of: now)!
    
    super.init(parent)
    super.setDelegate(self)

    guard let form = parent.form else {
      return
    }
    
    _viewController = getDateTimePickerController(self, screen: form, isDatePicker: true, isPhone: _isPhone)
    _viewController?.pickerView.setValue(preferredTextColor(form), forKeyPath: "textColor")
    _viewController?.setDateTime(calendar)
    _view.addTarget(self, action: #selector(click), for: UIControl.Event.primaryActionTriggered)
  }
  
  // MARK: DatePicker Properties
  @objc open var Year: Int {
    get {
      return _year
    }
  }
  
  // Month where 1 = January and 12 = December
  @objc open var Month: Int {
    get {
      return _month
    }
  }
  
  @objc open var MonthInText: String {
    get {
      return _localizedMonths[_month - 1]
    }
  }
  
  @objc open var Day: Int {
    get {
      return _day
    }
  }
  
  // the instant of the date that was last picked
  @objc open var Instant: Date {
    get {
      return _instant
    }
  }
  
  // MARK: DatePicker Events
  @objc open func SetDateToDisplay(_ year: Int32, _ month: Int32, _ day: Int32) {
    var dateComponents = DateComponents()
    dateComponents.year = Int(year)
    dateComponents.month = Int(month)
    dateComponents.day = Int(day)
    dateComponents.hour = 0
    dateComponents.minute = 0
    guard let date = Calendar.current.date(from: dateComponents) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetDateToDisplay", ErrorMessage.ERROR_ILLEGAL_DATE.code, ErrorMessage.ERROR_ILLEGAL_DATE.message)
      return
    }
    let computedComponents = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: date)
    if computedComponents != dateComponents {
      _container?.form?.dispatchErrorOccurredEvent(self, "SetDateToDisplay", ErrorMessage.ERROR_ILLEGAL_DATE.code, ErrorMessage.ERROR_ILLEGAL_DATE.message)
      return
    }
    _instant = date
    _viewController?.setDate(_instant)
    _customDate = true
  }
  
  @objc open func SetDateToDisplayFromInstant(_ instant: Date) {
    _instant = Calendar.current.date(bySettingHour: 0, minute: 0, second: 0, of: instant)!
    self._viewController?.setDate(_instant)
    _customDate = true
  }
  
  @objc open func LaunchPicker() {
    click()
  }
  
  @objc open func dateTimePicked(_ date: Date) {
    let calendar = Calendar.current
    _year = calendar.component(.year, from: date)
    _month = calendar.component(.month, from: date)
    _day = calendar.component(.day, from: date)
    _instant = Calendar.current.date(bySettingHour: 0, minute: 0, second: 0, of: date)!
    
    AfterDateSet()
  }
  
  @objc open func AfterDateSet() {
    EventDispatcher.dispatchEvent(of: self, called: "AfterDateSet")
  }
  
  open override func click() {
    if !_customDate {
      let now = Date()
      _instant = Calendar.current.date(bySettingHour: 0, minute: 0, second: 0, of: now)!
      _viewController?.setDate(_instant)
    } else {
      _customDate = false
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
