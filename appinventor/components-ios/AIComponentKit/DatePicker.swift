// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

protocol DatePickerDelegate: UIPopoverPresentationControllerDelegate {
  func datePicked(_ date: Date)
}

protocol DatePickerController {
  func setDate(_ calendar: Calendar)
  func setDate(_ date: Date)
}

/**
 * A DatePickerPhone Controller class.  A viewcontroller that allows the user to select a date
 * (month, day, year) via a popover for the iPad.
 */
fileprivate class DatePickerPadController: UIViewController, DatePickerController {
  fileprivate var _datePickerView: UIDatePicker
  fileprivate var _setDateButton: UIButton
  fileprivate var _delegate: DatePickerDelegate
  
  public init(_ datePickerDelegate: DatePickerDelegate) {
    _delegate = datePickerDelegate
    _datePickerView = UIDatePicker()
    _setDateButton = UIButton()
    super.init(nibName: nil, bundle: nil)
    _datePickerView.datePickerMode = .date
    modalPresentationStyle = .popover
  }
  
  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(code:) has not been implemented")
  }
  
  open override func viewDidLoad() {
    super.viewDidLoad()
    setupViews()
    addLayoutConstraints()
    self.preferredContentSize = CGSize(width: _datePickerView.frame.width, height: 300)
  }
  
  private func setupViews() {
    view.backgroundColor = .clear
    _setDateButton.backgroundColor = .clear
    _setDateButton.setTitle("Set Date", for: .normal)
    _setDateButton.setTitleColor(view.tintColor, for: .normal)
    _setDateButton.addTarget(self, action: #selector(dateSet), for: .touchUpInside)
    _setDateButton.translatesAutoresizingMaskIntoConstraints = false
    _datePickerView.translatesAutoresizingMaskIntoConstraints = false
    
    view.addSubview(_setDateButton)
    view.addSubview(_datePickerView)
  }
  
  private func addLayoutConstraints() {
    _datePickerView.topAnchor.constraint(equalTo: view.topAnchor, constant: 10).isActive = true
    _datePickerView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
    _datePickerView.widthAnchor.constraint(equalTo: view.widthAnchor).isActive = true
    
    _setDateButton.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
    _setDateButton.widthAnchor.constraint(equalToConstant: 100).isActive = true
    _setDateButton.topAnchor.constraint(equalTo: _datePickerView.bottomAnchor, constant: 0).isActive = true
    
    view.bottomAnchor.constraint(equalTo: _setDateButton.bottomAnchor, constant: 20).isActive = true
  }
  
  func dateSet() {
    self.dismiss(animated: true, completion:{
      self._delegate.datePicked(self._datePickerView.date)
    })
  }
  
  func setDate(_ calendar: Calendar) {
    _datePickerView.calendar = calendar
  }
  
  @objc(setDateWithDate:)
  func setDate(_ date: Date) {
    _datePickerView.date = date
  }
}

/**
 * A DatePickerPhone Controller class.  A viewcontroller that allows the user to select a date
 * (month, day, year) via an action sheet for non-iPad devices (iPod, iPhone).
 */
fileprivate class DatePickerPhoneController: UIViewController, DatePickerController {
  fileprivate var _pickerView: UIDatePicker
  fileprivate var _toolBar: UIToolbar
  fileprivate var _delegate: DatePickerDelegate
  
  public init(_ datePickerDelegate: DatePickerDelegate) {
    _delegate = datePickerDelegate
    _pickerView = UIDatePicker()
    _toolBar = UIToolbar()
    
    super.init(nibName: nil, bundle: nil)
    modalPresentationStyle = .custom
    let doneButton = UIBarButtonItem(title: "Done", style: UIBarButtonItemStyle.plain, target: self, action: #selector(self.dismissPicker))
    let cancelButton = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.plain, target: self, action: #selector(self.cancelPicker))
    _toolBar.barStyle = .default
    let flexibleSpace = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.flexibleSpace, target: nil, action: nil)
    _toolBar.setItems([doneButton, flexibleSpace, cancelButton], animated: true)
    _toolBar.isUserInteractionEnabled = true
    _toolBar.sizeToFit()
    _pickerView.datePickerMode = .date
    _pickerView.backgroundColor = .white
  }
  
  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  open override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = .clear
    view.addSubview(_toolBar)
    view.addSubview(_pickerView)
    addLayoutConstraints()
  }
  
  private func addLayoutConstraints() {
    _pickerView.heightAnchor.constraint(equalTo: view.heightAnchor, multiplier: 1.5 / 7).isActive = true
    _toolBar.bottomAnchor.constraint(equalTo: _pickerView.topAnchor).isActive = true
    _toolBar.heightAnchor.constraint(equalToConstant: 50)
    _toolBar.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
    _toolBar.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
    _toolBar.translatesAutoresizingMaskIntoConstraints = false
    _pickerView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
    _pickerView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
    _pickerView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
    _pickerView.translatesAutoresizingMaskIntoConstraints = false
  }
  
  func setDate(_ calendar: Calendar) {
    _pickerView.calendar = calendar
  }
  
  @objc(setDateWithDate:)
  func setDate(_ date: Date) {
    _pickerView.date = date
  }
  
  public func cancelPicker() {
    self.dismiss(animated: true)
  }
  
  public func dismissPicker() {
    self._delegate.datePicked(_pickerView.date)
    self.dismiss(animated: true)
  }
}

/**
 * A DatePicker class.  A component that allows the user to select a date (month, day, year).
 * This component is a button that launches a DatePickerViewController (specific to the device).
 */
open class DatePicker: Picker, DatePickerDelegate {
  fileprivate var _viewController: DatePickerController?
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
    
    _viewController = _isPhone ? DatePickerPhoneController(self) : DatePickerPadController(self)
    _viewController?.setDate(calendar)
    _view.addTarget(self, action: #selector(click), for: UIControlEvents.primaryActionTriggered)
    parent.add(self)
  }
  
  // MARK: DatePicker Properties
  open var Year: Int {
    get {
      return _year
    }
  }
  
  // Month where 1 = January and 12 = December
  open var Month: Int {
    get {
      return _month
    }
  }
  
  open var MonthInText: String {
    get {
      return _localizedMonths[_month - 1]
    }
  }
  
  open var Day: Int {
    get {
      return _day
    }
  }
  
  // the instant of the date that was last picked
  open var Instant: Date {
    get {
      return _instant
    }
  }
  
  // MARK: DatePicker Events
  open func SetDateToDisplay(_ year: Int32, _ month: Int32, _ day: Int32) {
    var dateComponents = DateComponents()
    dateComponents.year = Int(year)
    dateComponents.month = Int(month)
    dateComponents.day = Int(day)
    dateComponents.hour = 0
    dateComponents.minute = 0
    guard let date = Calendar.current.date(from: dateComponents) else {
      _container.form.dispatchErrorOccurredEvent(self, "SetDateToDisplay", ErrorMessage.ERROR_ILLEGAL_DATE.code, ErrorMessage.ERROR_ILLEGAL_DATE.message)
      return
    }
    let computedComponents = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: date)
    if computedComponents != dateComponents {
      _container.form.dispatchErrorOccurredEvent(self, "SetDateToDisplay", ErrorMessage.ERROR_ILLEGAL_DATE.code, ErrorMessage.ERROR_ILLEGAL_DATE.message)
      return
    }
    _instant = date
    _viewController?.setDate(_instant)
    _customDate = true
  }
  
  open func SetDateToDisplayFromInstant(_ instant: Date) {
    _instant = Calendar.current.date(bySettingHour: 0, minute: 0, second: 0, of: instant)!
    self._viewController?.setDate(_instant)
    _customDate = true
  }
  
  open func LaunchPicker() {
    click()
  }
  
  open func datePicked(_ date: Date) {
    let calendar = Calendar.current
    _year = calendar.component(.year, from: date)
    _month = calendar.component(.month, from: date)
    _day = calendar.component(.day, from: date)
    _instant = Calendar.current.date(bySettingHour: 0, minute: 0, second: 0, of: date)!
    
    AfterDateSet()
  }
  
  open func AfterDateSet() {
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
    _container.form.present(_viewController as! UIViewController, animated: true)
  }
}
