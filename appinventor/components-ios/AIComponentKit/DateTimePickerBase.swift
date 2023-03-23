// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

protocol DateTimePickerController {
  func setDateTime(_ calendar: Calendar)
  func setDate(_ date: Date)
  func setTime(_ date: Date)
  var pickerView: UIDatePicker { get }
}

protocol DateTimePickerDelegate: UIPopoverPresentationControllerDelegate {
  func dateTimePicked(_ date: Date)
}

class DateTimePickerPadController: PickerPadController, DateTimePickerController {
  private var _pickerView = UIDatePicker()
  private var _setDateTimeButton = UIButton()
  private var _delegate: DateTimePickerDelegate
  private var _isDatePicker: Bool

  public init(_ delegate: DateTimePickerDelegate, isDatePicker: Bool) {
    _delegate = delegate
    _isDatePicker = isDatePicker
    super.init()
    _pickerView.datePickerMode = isDatePicker ? .date: .time
  }

  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func setupViews() {
    _setDateTimeButton.backgroundColor = .clear
    _setDateTimeButton.setTitle("Set \(_isDatePicker ? "Date": "Time")", for: .normal)
    _setDateTimeButton.setTitleColor(view.tintColor, for: .normal)
    _setDateTimeButton.addTarget(self, action: #selector(dateTimeSet), for: .touchUpInside)
    _setDateTimeButton.translatesAutoresizingMaskIntoConstraints = false
    _pickerView.translatesAutoresizingMaskIntoConstraints = false

    view.addSubview(_setDateTimeButton)
    view.addSubview(_pickerView)
  }

  override func addLayoutConstraints() {
    _pickerView.topAnchor.constraint(equalTo: view.topAnchor, constant: 10).isActive = true
    _pickerView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
    _pickerView.widthAnchor.constraint(equalTo: view.widthAnchor).isActive = true

    _setDateTimeButton.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
    _setDateTimeButton.widthAnchor.constraint(equalToConstant: 100).isActive = true
    _setDateTimeButton.topAnchor.constraint(equalTo: _pickerView.bottomAnchor, constant: 0).isActive = true

    view.bottomAnchor.constraint(equalTo: _setDateTimeButton.bottomAnchor, constant: 20).isActive = true
    self.preferredContentSize = CGSize(width: _pickerView.frame.width, height: 300)
  }

  @objc func dateTimeSet() {
    dismiss(animated: true) {
      self._delegate.dateTimePicked(self._pickerView.date)
    }
  }

  @objc func setDateTime(_ calendar: Calendar) {
    _pickerView.calendar = calendar
  }

  @objc(setTimeWithDate:)
  func setTime(_ date: Date) {
    _pickerView.date = date
  }

  @objc(setDateWithDate:)
  func setDate(_ date: Date) {
    _pickerView.date = date
  }
  
  open var pickerView: UIDatePicker {
    get {
      return _pickerView
    }
  }
}

class DateTimePickerPhoneController: PickerPhoneController, DateTimePickerController {
  private var _pickerView = UIDatePicker()
  private var _delegate: DateTimePickerDelegate

  public init(_ delegate: DateTimePickerDelegate, screen form: Form, isDatePicker: Bool) {
    _delegate = delegate
    super.init(contentView: _pickerView, screen: form)
    _pickerView.datePickerMode = isDatePicker ? .date: .time
  }

  public required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  @objc func setDateTime(_ calendar: Calendar) {
    _pickerView.calendar = calendar
  }

  @objc(setTimeWithDate:)
  func setTime(_ date: Date) {
    _pickerView.date = date
  }

  @objc(setDateWithDate:)
  func setDate(_ date: Date) {
    _pickerView.date = date
  }

  override func doDismissPicker() {
    _delegate.dateTimePicked(_pickerView.date)
  }
  
  open var pickerView: UIDatePicker {
    get {
      return _pickerView
    }
  }
}

func getDateTimePickerController(_ delegate: DateTimePickerDelegate, screen form: Form, isDatePicker: Bool, isPhone: Bool) -> DateTimePickerController {
  if isPhone {
    return DateTimePickerPhoneController(delegate, screen: form, isDatePicker: isDatePicker)
  } else {
    return DateTimePickerPadController(delegate, isDatePicker: isDatePicker)
  }
}
