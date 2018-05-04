// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

protocol TimePickerDelegate: UIPopoverPresentationControllerDelegate {
  func timePicked(_ date: Date)
}

protocol TimePickerController {
  func setTime(_ calendar: Calendar)
  func setTime(_ date: Date)
}

/**
 * A TimePickerPhone Controller class.  A viewcontroller that allows the user to select a time
 * (hour, minute) via a popover for the iPad.
 */
fileprivate class TimePickerPadController: UIViewController, TimePickerController {
  fileprivate var _timePickerView: UIDatePicker
  fileprivate var _setTimeButton: UIButton
  fileprivate var _delegate: TimePickerDelegate
  
  public init(_ timePickerDelegate: TimePickerDelegate) {
    _delegate = timePickerDelegate
    _timePickerView = UIDatePicker()
    _setTimeButton = UIButton()
    super.init(nibName: nil, bundle: nil)
    _timePickerView.datePickerMode = .time
    modalPresentationStyle = .popover
  }
  
  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(code:) has not been implemented")
  }
  
  open override func viewDidLoad() {
    super.viewDidLoad()
    setupViews()
    addLayoutConstraints()
    self.preferredContentSize = CGSize(width: _timePickerView.frame.width, height: 300)
  }
  
  private func setupViews() {
    view.backgroundColor = .clear
    _setTimeButton.backgroundColor = .clear
    _setTimeButton.setTitle("Set Time", for: .normal)
    _setTimeButton.setTitleColor(view.tintColor, for: .normal)
    _setTimeButton.addTarget(self, action: #selector(timeSet), for: .touchUpInside)
    _setTimeButton.translatesAutoresizingMaskIntoConstraints = false
    _timePickerView.translatesAutoresizingMaskIntoConstraints = false
    
    view.addSubview(_setTimeButton)
    view.addSubview(_timePickerView)
  }
  
  private func addLayoutConstraints() {
    _timePickerView.topAnchor.constraint(equalTo: view.topAnchor, constant: 10).isActive = true
    _timePickerView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
    _timePickerView.widthAnchor.constraint(equalTo: view.widthAnchor).isActive = true
    
    _setTimeButton.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
    _setTimeButton.widthAnchor.constraint(equalToConstant: 100).isActive = true
    _setTimeButton.topAnchor.constraint(equalTo: _timePickerView.bottomAnchor, constant: 0).isActive = true
    
    view.bottomAnchor.constraint(equalTo: _setTimeButton.bottomAnchor, constant: 20).isActive = true
  }
  
  func timeSet() {
    self.dismiss(animated: true, completion:{
      self._delegate.timePicked(self._timePickerView.date)
    })
  }
  
  func setTime(_ calendar: Calendar) {
    _timePickerView.calendar = calendar
  }
  
  @objc(setTimeWithDate:)
  func setTime(_ date: Date) {
    _timePickerView.date = date
  }
}

/**
 * A TimePickerPhoneController class.  A viewcontroller that allows the user to select a time
 * (hour, minute) via an action sheet for non-iPad devices (iPod, iPhone).
 */
fileprivate class TimePickerPhoneController: UIViewController, TimePickerController {
  fileprivate var _pickerView: UIDatePicker
  fileprivate var _toolBar: UIToolbar
  fileprivate var _delegate: TimePickerDelegate
  
  public init(_ timePickerDelegate: TimePickerDelegate) {
    _delegate = timePickerDelegate
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
    _pickerView.datePickerMode = .time
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
  
  func setTime(_ calendar: Calendar) {
    _pickerView.calendar = calendar
  }
  
  @objc(setTimeWithDate:)
  func setTime(_ date: Date) {
    _pickerView.date = date
  }
  
  public func cancelPicker() {
    self.dismiss(animated: true)
  }
  
  public func dismissPicker() {
    self._delegate.timePicked(_pickerView.date)
    self.dismiss(animated: true)
  }
}

/**
 * A TimePicker class.  A component that allows the user to select a time (hour, minute).
 * This component is a button that launches a TimePickerViewController (specific to the device).
 */
open class TimePicker: Picker, TimePickerDelegate {
  fileprivate var _viewController: TimePickerController?
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
    
    _viewController = _isPhone ? TimePickerPhoneController(self) : TimePickerPadController(self)
    _viewController?.setTime(calendar)
    _view.addTarget(self, action: #selector(click), for: UIControlEvents.primaryActionTriggered)
    parent.add(self)
  }
  
  // MARK: DatePicker Properties
  // hour is in 24 hour format -- 11pm returns 23
  open var Hour: Int {
    get {
      return _hour
    }
  }
  
  open var Minute: Int {
    get {
      return _minute
    }
  }
  
  // the instant of the time that was last picked
  open var Instant: Date {
    get {
      return _instant
    }
  }
  
  // MARK: TimePicker Events
  open func SetTimeToDisplay(_ hour: Int32, _ minute: Int32) {
    if !(0...23 ~= hour) {
      _container.form.dispatchErrorOccurredEvent(self, "SetTimeToDisplay", ErrorMessage.ERROR_ILLEGAL_HOUR.code, ErrorMessage.ERROR_ILLEGAL_HOUR.message)
    } else if !(0...59 ~= minute) {
      _container.form.dispatchErrorOccurredEvent(self, "SetTimeToDisplay", ErrorMessage.ERROR_ILLEGAL_MINUTE.code, ErrorMessage.ERROR_ILLEGAL_MINUTE.message)
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
  
  open func SetTimeToDisplayFromInstant(_ instant: Date) {
    let timeComponents = Calendar.current.dateComponents([.day, .month, .year, .hour, .minute, .timeZone], from: instant)
    _instant = Calendar.current.date(from: timeComponents)!
    self._viewController?.setTime(_instant)
    _customTime = true
  }
  
  open func LaunchPicker() {
    click()
  }
  
  open func timePicked(_ date: Date) {
    let calendar = Calendar.current
    let timeComponents = calendar.dateComponents([.day, .month, .year, .hour, .minute, .timeZone], from: date)
    
    _hour = timeComponents.hour!
    _minute = timeComponents.minute!
    _instant = calendar.date(from: timeComponents)!
    
    AfterTimeSet()
  }
  
  open func AfterTimeSet() {
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
    _container.form.present(_viewController as! UIViewController, animated: true)
  }
}

