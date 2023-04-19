// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

class PickerPadController: UIViewController {
  public init() {
    super.init(nibName: nil, bundle: nil)
    modalPresentationStyle = .popover
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    showAlert(message: "init(coder:) has not been implemented")
  }

  open override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = .clear
    setupViews()
    addLayoutConstraints()
  }

  @objc func setupViews() {
    showAlert(message: "setupViews() has not been implemented")
  }

  @objc func addLayoutConstraints() {
    showAlert(message: "addLayoutConstraints() has not been implemented")
  }
}

class PickerPhoneController: UIViewController {
  private var _toolBar = UIToolbar()
  private var _contentView: UIView

  @objc public init(contentView: UIView, screen form: Form) {
    _contentView = contentView
    super.init(nibName: nil, bundle: nil)
    modalPresentationStyle = .custom
    _toolBar.barStyle = .default
    let doneButton = UIBarButtonItem(title: "Done", style: UIBarButtonItem.Style.plain, target: self, action: #selector(self.dismissPicker))
    let flexibleSpace = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
    let cancelButton = UIBarButtonItem(title: "Cancel", style: UIBarButtonItem.Style.plain, target: self, action: #selector(self.cancelPicker))
    _toolBar.setItems([doneButton, flexibleSpace, cancelButton], animated: true)
    _toolBar.isUserInteractionEnabled = true
    _toolBar.sizeToFit()
    _contentView.backgroundColor = preferredBackgroundColor(form)
  }

  public override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = .clear
    view.addSubview(_toolBar)
    view.addSubview(_contentView)
    addLayoutConstraints()
  }

  private func addLayoutConstraints() {
    _contentView.heightAnchor.constraint(equalTo: view.heightAnchor, multiplier: 1.5 / 7).isActive = true
    _toolBar.bottomAnchor.constraint(equalTo: _contentView.topAnchor).isActive = true
    _toolBar.heightAnchor.constraint(equalToConstant: 50).isActive = true
    _toolBar.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
    _toolBar.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
    _toolBar.translatesAutoresizingMaskIntoConstraints = false
    _contentView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
    _contentView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
    _contentView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
    _contentView.translatesAutoresizingMaskIntoConstraints = false
  }

  public required init?(coder aDecoder: NSCoder) {
    showAlert(message: "init(coder:) has not been implemented")
    return nil
  }

  @objc public func dismissPicker() {
    doDismissPicker()
    self.dismiss(animated: true)
  }

  @objc func doDismissPicker() {}

  @objc public func cancelPicker() {
    doCancelPicker()
    self.dismiss(animated: true)
  }

  @objc func doCancelPicker() {}
}
