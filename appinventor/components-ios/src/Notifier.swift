// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import Toast_Swift

class SyncArray<T> {
  private var array = [T]()
  private let queue: DispatchQueue

  init(on queue: DispatchQueue) {
    self.queue = queue
  }

  open func append(_ item: T) {
    queue.async(flags: .barrier) {
      self.array.append(item)
    }
  }

  open var count: Int {
    get {
      var count = 0
      queue.sync {
        count = self.array.count
      }
      return count
    }
  }

  open func first() -> T? {
    var item: T? = nil
    queue.sync {
      item = array.first
    }
    return item
  }

  open func removeFirst() {
    if count > 0 {
      queue.async(flags: .barrier) {
        self.array.removeFirst()
      }
    }
  }

  open func clear() {
    queue.async(flags: .barrier) {
      self.array.removeAll()
    }
  }
}

class NotifierArray: NSObject {
  fileprivate weak var form: Form? = nil
  private var requests: SyncArray<(String, TimeInterval)>
  private var paused = false
  private let syncQueue = DispatchQueue(label: "SyncPauseNotifierQueue", attributes: .concurrent)

  override init() {
    requests = SyncArray(on: syncQueue)
    super.init()
  }

  open func addAlert(_ message: String, for duration: TimeInterval) {
    let notice = (message, duration)
    requests.append(notice)
    syncQueue.sync {
      if !self.paused, self.requests.count == 1 {
        self.startAlert(notice)
      }
    }
  }

  open func setPausedState(_ shouldPause: Bool) {
    var shouldContinue = true
    syncQueue.sync {
      if paused == shouldPause {
        shouldContinue = false
      }
    }
    if shouldContinue {
      if !shouldPause, let first = self.requests.first() {
        startAlert(first)
      }
      syncQueue.async(flags: .barrier) {
        self.paused = shouldPause
      }
    }
  }

  open func clearNotices() {
    requests.clear()
  }

  private func startAlert(_ notice: (String, TimeInterval)) {
    form?.view.makeToast(notice.0, duration: notice.1, position: ToastPosition.center)
    DispatchQueue.main.asyncAfter(deadline: .now() + notice.1) {
      self.requests.removeFirst()
      self.syncQueue.sync {
        if !self.paused {
          if let first = self.requests.first() {
            self.startAlert(first)
          }
        }
      }
    }
  }
}

private extension String {
  func htmlAttributedString() -> NSAttributedString? {
    guard let data = self.data(using: .utf8, allowLossyConversion: false) else { return nil }
    guard let html = try? NSMutableAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html, .characterEncoding: String.Encoding.utf8.rawValue], documentAttributes: nil) else { return nil }
    return html
  }
}

fileprivate class CustomAlertView: UIView {
  private var background = UIView()
  private var dialog = UIView()
  var stack = UIStackView()
  let kDefaultSpacing: CGFloat = 12
  private var dialogConstraint: NSLayoutConstraint!

  convenience init(title: String, message: String?) {
    self.init(frame: UIScreen.main.bounds)
    self.translatesAutoresizingMaskIntoConstraints = false

    NotificationCenter.default.addObserver(self, selector: #selector(keyboardShown(notification:)), name: UIResponder.keyboardDidShowNotification, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(keyboardShown(notification:)), name: UIResponder.keyboardDidHideNotification, object: nil)

    background.backgroundColor = UIColor.black
    background.alpha = 0.6
    addSubview(background)
    background.translatesAutoresizingMaskIntoConstraints = false
    background.widthAnchor.constraint(equalTo: widthAnchor).isActive = true
    background.heightAnchor.constraint(equalTo: heightAnchor).isActive = true
    background.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
    background.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true


    let titleLabel = makeLabel()
    titleLabel.font = UIFont.boldSystemFont(ofSize: UIFont.labelFontSize)
    titleLabel.text = title
    stack.spacing = kDefaultSpacing
    stack.alignment = .fill
    stack.axis = .vertical

    if let contents = message {
      let messageLabel = makeLabel()
      messageLabel.attributedText = contents.htmlAttributedString()
      messageLabel.font = messageLabel.font.withSize(UIFont.systemFontSize)
    }

    dialog.addSubview(stack)
    addSubview(dialog)

    dialog.heightAnchor.constraint(equalTo: stack.heightAnchor, constant: 20).isActive = true
    dialog.backgroundColor = UIColor(red: 0.95, green: 0.95, blue: 0.95, alpha: 1)
    dialog.layer.cornerRadius = 6
    dialog.translatesAutoresizingMaskIntoConstraints = false
    let centerYConstraint = dialog.centerYAnchor.constraint(equalTo: centerYAnchor)
    centerYConstraint.priority = UILayoutPriority(8)
    centerYConstraint.isActive = true
    dialog.widthAnchor.constraint(equalToConstant: 270).isActive = true
    dialog.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
    dialog.heightAnchor.constraint(lessThanOrEqualTo: heightAnchor).isActive = true
    dialogConstraint = dialog.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor)
    dialogConstraint.isActive = true
    dialogConstraint.priority = UILayoutPriority(9)
    dialog.topAnchor.constraint(greaterThanOrEqualTo: topAnchor).isActive = true

    stack.translatesAutoresizingMaskIntoConstraints = false
    stack.centerXAnchor.constraint(equalTo: dialog.centerXAnchor).isActive = true
    stack.centerYAnchor.constraint(equalTo: dialog.centerYAnchor).isActive = true
    stack.widthAnchor.constraint(equalTo: dialog.widthAnchor, constant: -20).isActive = true
  }

  override init(frame: CGRect) {
    super.init(frame: frame)
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func show(animated: Bool, callback: ((Bool) -> Void)? = nil){
    self.background.alpha = 0
    self.dialog.isHidden = true
    if let parent =  UIApplication.shared.delegate?.window??.rootViewController?.view {
      parent.addSubview(self)
      if let handler = callback {
        handler(true)
      }
      widthAnchor.constraint(equalTo: parent.widthAnchor).isActive = true
      heightAnchor.constraint(equalTo: parent.heightAnchor).isActive = true
      centerXAnchor.constraint(equalTo: parent.centerXAnchor).isActive = true
      centerYAnchor.constraint(equalTo: parent.centerYAnchor).isActive = true
      if animated {
        UIView.animate(withDuration: 0.33, animations: {
          self.background.alpha = 0.66
          self.dialog.isHidden = false
        })
      } else {
        self.background.alpha = 0.66
      }
    }
  }

  func dismiss(animated: Bool, callback: ((Bool) -> Void)? = nil){
    if animated {
      UIView.animate(withDuration: 0.33, animations: {
        self.background.alpha = 0
        self.dialog.isHidden = true
      }, completion: { completed in
        self.removeFromSuperview()
        if let handler = callback {
          handler(completed)
        }
      })
    } else {
      self.removeFromSuperview()
    }
  }

  fileprivate func makeLabel() -> UILabel {
    let label = UILabel(frame: .zero)
    stack.addArrangedSubview(label)
    label.numberOfLines = 0
    label.textAlignment = .center
    return label
  }

  @objc private func keyboardShown(notification: NSNotification) {
    if let info = notification.userInfo {
      let keyboardFrame: CGRect = (info[UIResponder.keyboardFrameEndUserInfoKey] as! NSValue).cgRectValue
      if keyboardFrame.origin.y < UIScreen.main.bounds.size.height {
        dialogConstraint.constant = -keyboardFrame.height - 20
      } else {
        dialogConstraint.constant = 0
      }
    }
  }
}

private class CustomButton: UIButton {
  fileprivate var value: AnyObject!
}

open class Notifier: NonvisibleComponent {
  fileprivate var _notifierLength = ToastLength.long.rawValue
  fileprivate var _backgroundColor = Int32(bitPattern: Color.darkGray.rawValue)
  fileprivate var _textColor = Int32(bitPattern: Color.white.rawValue)
  static var notices = NotifierArray()
  fileprivate var _activeAlert: CustomAlertView? = nil

  public override init(_ container: ComponentContainer) {
    super.init(container)
    if Notifier.notices.form == nil {
      Notifier.notices.form = _form
    }
  }

  // MARK: Notifier Properties
  @objc open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
    }
  }

  @objc open var NotifierLength: Int32 {
    get {
      return _notifierLength
    }
    set(length) {
      _notifierLength = length
    }
  }

  @objc open var TextColor: Int32 {
    get {
      return _textColor
    }
    set(argb) {
      _textColor = argb
    }
  }

  // MARK: Notifier Methods
  @objc open func DismissProgressDialog() {
    _activeAlert?.dismiss(animated: true)
    _activeAlert = nil
  }

  @objc open func LogError(_ message: String) {
    NSLog("Error: \(message)")
  }

  @objc open func LogInfo(_ message: String) {
    NSLog("Info: \(message)")
  }

  @objc open func LogWarning(_ message: String) {
    NSLog("Warning: \(message)")
  }

  @objc open func ShowAlert(_ notice: String) {
    let duration = TimeInterval(_notifierLength == 1 ? 3.5 : 2.0)
    Notifier.notices.addAlert(notice, for: duration)
  }

  @objc open func ShowChooseDialog(_ message: String, _ title: String, _ button1text: String, _ button2text: String, _ cancelable: Bool) {
    if _activeAlert == nil {
      _activeAlert = CustomAlertView(title: title, message: message)
      let button1 = makeButton(button1text, with: button1text as NSString, action: #selector(afterChoosing(sender:)))
      makeBorder(for: button1, vertical: false)
      let button2 = makeButton(button2text, with: button2text as NSString, action: #selector(afterChoosing(sender:)))
      makeBorder(for: button2, vertical: false)
      _activeAlert?.stack.addArrangedSubview(button1)
      _activeAlert?.stack.addArrangedSubview(button2)
      if cancelable {
        let cancel = makeButton("Cancel", with: "Cancel" as NSString, action: #selector(cancelChoosing(sender:)))
        makeBorder(for: cancel, vertical: false)
        cancel.titleLabel?.font = UIFont.boldSystemFont(ofSize: UIFont.buttonFontSize)
        _activeAlert?.stack.addArrangedSubview(cancel)
      }
      _activeAlert?.show(animated: true)
    }
  }

  @objc open func ShowMessageDialog(_ message: String, _ title: String, _ buttonText: String) {
    let alert = CustomAlertView(title: title, message: message)
    let button = makeButton(buttonText, with: alert, action: #selector(dismissAlertView(_:)))
    makeBorder(for: button, vertical: false)
    alert.stack.addArrangedSubview(button)
    alert.show(animated: true)
  }

  @objc fileprivate func dismissAlertView(_ sender: CustomButton) {
    if let alert = sender.value as? CustomAlertView {
      alert.dismiss(animated: true)
    }
  }

  @objc open func ShowPasswordDialog(_ message: String, _ title: String, _ cancelable: Bool) {
    showTextInputDialog(message: message, title: title, cancelable: cancelable, maskInput: true)
  }

  @objc open func ShowProgressDialog(_ message: String, _ title: String) {
    if _activeAlert == nil {
      let alert = CustomAlertView(title: title, message: message)
      let spinner = UIActivityIndicatorView(style: .gray)
      spinner.startAnimating()
      alert.stack.addArrangedSubview(spinner)
      alert.show(animated: true)
      _activeAlert = alert
    }
  }

  @objc open func ShowTextDialog(_ message: String, _ title: String, _ cancelable: Bool) {
    showTextInputDialog(message: message, title: title, cancelable: cancelable)
  }

  private func showTextInputDialog(message: String, title: String, cancelable: Bool, maskInput: Bool = false) {
    guard _activeAlert == nil else { return }

    _activeAlert = CustomAlertView(title: title, message: message)

    let actions = UIStackView()
    actions.axis = .horizontal
    actions.alignment = .center
    actions.distribution = .fillEqually
    actions.spacing = 0

    let text = UITextField(frame: .zero)
    text.borderStyle = .bezel
    text.isSecureTextEntry = maskInput

    _activeAlert?.stack.addArrangedSubview(text)

    let button = makeButton("OK", with: text, action: #selector(afterTextInput(sender:)), shouldSize: false)
    var height = button.intrinsicContentSize.height
    button.titleLabel?.font = UIFont.boldSystemFont(ofSize: UIFont.buttonFontSize)

    if cancelable {
      let cancel = makeButton("Cancel", with: "Cancel" as NSString, action: #selector(afterTextInput(sender:)), shouldSize: false)
      height = max(button.intrinsicContentSize.height, height)
      cancel.frame.size.height = height
      actions.addArrangedSubview(cancel)
      makeBorder(for: actions, vertical: true)
    }

    makeBorder(for: actions, vertical: false)

    button.frame.size.height = height
    actions.addArrangedSubview(button)

    _activeAlert?.stack.addArrangedSubview(actions)
    _activeAlert?.show(animated: true) { completed in
      text.becomeFirstResponder()
    }
  }

  fileprivate func makeBorder(for parent: UIView, vertical: Bool) {
    let border = UIView()
    border.layer.borderWidth = 1
    border.layer.borderColor = UIColor.lightGray.cgColor
    border.translatesAutoresizingMaskIntoConstraints = false
    parent.addSubview(border)
    if vertical {
      border.widthAnchor.constraint(equalToConstant: 1).isActive = true
      border.centerXAnchor.constraint(equalTo: parent.centerXAnchor).isActive = true
      border.topAnchor.constraint(equalTo: parent.topAnchor).isActive = true
      border.bottomAnchor.constraint(equalTo: parent.bottomAnchor, constant: 10).isActive = true
    } else {
      border.leftAnchor.constraint(equalTo: parent.leftAnchor, constant: -10).isActive = true
      border.rightAnchor.constraint(equalTo: parent.rightAnchor, constant: 10).isActive = true
      border.topAnchor.constraint(equalTo: parent.topAnchor, constant: -2).isActive = true
      border.heightAnchor.constraint(equalToConstant: 1).isActive = true
    }
  }

  fileprivate func makeButton(_ title: String, with value: AnyObject, action: Selector, shouldSize: Bool = true) -> UIButton {
    let button = CustomButton(type: .roundedRect)
    button.setTitle(title, for: .normal)
    button.value = value
    button.addTarget(self, action: action, for: .touchUpInside)
    if shouldSize {
      button.frame.size = button.intrinsicContentSize
    }
    return button
  }

  @objc fileprivate func afterTextInput(sender: UIButton) {
    if let button = sender as? CustomButton {
      if let field = button.value as? UITextField {
        AfterTextInput(field.text ?? "")
      } else {
        TextInputCanceled()
        AfterTextInput("Cancel")
      }
    }
    DismissProgressDialog()
  }

  @objc fileprivate func afterChoosing(sender: UIButton) {
    if let button = sender as? CustomButton, let choice = button.value as? String {
      if choice == "Cancel" {
        ChoosingCanceled()
      }
      AfterChoosing(choice)
    }
    DismissProgressDialog()
  }

  @objc fileprivate func cancelChoosing(sender: UIButton) {
    ChoosingCanceled()
    afterChoosing(sender: sender)
  }

  // MARK: Notifier Events
  @objc open func AfterChoosing(_ choice: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterChoosing", arguments: choice as NSString)
  }

  @objc open func AfterTextInput(_ response: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterTextInput", arguments: response as NSString)
  }

  @objc open func ChoosingCanceled() {
    EventDispatcher.dispatchEvent(of: self, called: "ChoosingCanceled")
  }

  @objc open func TextInputCanceled() {
    EventDispatcher.dispatchEvent(of: self, called: "TextInputCanceled")
  }
}
